package me.gberg.matterdroid.managers;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import me.gberg.matterdroid.App;
import me.gberg.matterdroid.api.LoginAPI;
import me.gberg.matterdroid.api.UserAPI;
import me.gberg.matterdroid.di.modules.APIModule;
import me.gberg.matterdroid.events.LoginEvent;
import me.gberg.matterdroid.events.TokenCheckEvent;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.LoginRequest;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.model.User;
import me.gberg.matterdroid.settings.LoginSettings;
import me.gberg.matterdroid.utils.api.HttpHeaders;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SessionManager {

    private final LoginSettings loginSettings;
    private final Bus bus;
    private final App app;

    private Gson gson;

    private String server;
    private String email;
    private String token;

    private User user;
    private Team team;

    public SessionManager(final App app, final LoginSettings loginSettings, Bus bus) {
        this.app = app;
        this.loginSettings = loginSettings;
        this.bus = bus;

        gson = null;

        server = loginSettings.getServer();
        email = loginSettings.getEmail();
        token = loginSettings.getToken();
    }

    protected Gson getGson() {
        if (gson == null) {
            // Initialise stuff the Session Manager needs.
            gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();
        }

        return gson;
    }

    public boolean hasToken() {
        return token != null;
    }

    public boolean isLoggedIn() {
        return user != null;
    }

    public boolean isTeamSelected() {
        return team != null;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(final String server) {
        this.server = server;
        loginSettings.setServer(server);
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
        loginSettings.setEmail(email);
    }

    public String getToken() {
        return this.token;
    }

    protected void setToken(final String token) {
        this.token = token;
        loginSettings.setToken(token);
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setTeam(Team team) {
        app.createTeamComponent(team);
        this.team = team;
    }

    public void attemptToken() {
        final APIModule apiModule = new APIModule();
        final Retrofit retrofit = apiModule.providesRetrofit(apiModule.providesOkHttpClient(this), getGson(), this);
        final UserAPI userAPI = apiModule.providesUserAPI(retrofit);
        final ErrorParser errorParser = apiModule.providesErrorParser(retrofit);

        Observable<Response<User>> meObservable = userAPI.me();
        meObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<User>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        bus.send(new TokenCheckEvent(e));
                    }

                    @Override
                    public void onNext(final Response<User> response) {

                        // Handle HTTP error response codes that we recognise.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            bus.send(new TokenCheckEvent(apiError));
                            return;
                        }

                        // We have retrieved ME successfully.
                        Timber.i("Successfully retrieved the ME object. Token is still live.");
                        User user = response.body();

                        Timber.i("User ID: " + user.id);
                        app.createUserComponent(user);

                        bus.send(new TokenCheckEvent(response.body()));
                    }
                });
    }

    public void attemptLogin(final String password) {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(server)
                .addConverterFactory(GsonConverterFactory.create(getGson()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        final ErrorParser errorParser = new ErrorParser(retrofit);

        LoginAPI loginService = retrofit.create(LoginAPI.class);
        LoginRequest loginRequest = new LoginRequest(email, password, null);
        Observable<Response<User>> loginObservable = loginService.login(loginRequest);
        loginObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<User>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(Throwable e) {
                        bus.send(new LoginEvent(e));
                    }

                    @Override
                    public void onNext(Response<User> response) {

                        // Handle HTTP error response codes that we recognise.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            bus.send(new LoginEvent(apiError));

                        }

                        // We have logged in successfully.
                        user = response.body();
                        Timber.i("Logged in successfully with User ID: " + user.id);

                        // Create the UserComponent.
                        String token = response.headers().get(HttpHeaders.TOKEN);
                        app.createUserComponent(user);

                        // Save the fact we've logged in.
                        setToken(token);

                        bus.send(new LoginEvent(response.body()));
                    }
                });
    }
}
