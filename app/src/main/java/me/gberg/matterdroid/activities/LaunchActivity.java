package me.gberg.matterdroid.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;

import javax.inject.Inject;

import me.gberg.matterdroid.App;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.api.UserAPI;
import me.gberg.matterdroid.di.modules.APIModule;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.ServerConnectionParameters;
import me.gberg.matterdroid.model.User;
import me.gberg.matterdroid.settings.LoginSettings;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import retrofit2.Response;
import retrofit2.Retrofit;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class LaunchActivity extends AppCompatActivity {

    @Inject
    LoginSettings loginSettings;

    @Inject
    App app;

    @Inject
    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate() called.");
        setContentView(R.layout.ac_launch);

        ((App) getApplication()).getAppComponent().inject(this);

        final ServerConnectionParameters serverConnectionParameters = loginSettings.getServerConnectionParameters();

        // Check if we have a token.
        if (serverConnectionParameters.token == null) {
            // No token. Go straight to LoginActivity.
            Timber.v("Token is null.");
            LoginActivity.launch(this);
            finish();
            return;
        }

        Timber.v("Token is not null");

        // Test if the token is valid.
        final APIModule apiModule = new APIModule();
        final Retrofit retrofit = apiModule.providesRetrofit(gson, serverConnectionParameters);
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
                        // Unhandled error. Log it.
                        Timber.e(e, e.getMessage());
                        LoginActivity.launch(LaunchActivity.this);
                        finish();
                    }

                    @Override
                    public void onNext(final Response<User> response) {

                        // Handle HTTP error response codes that we recognise.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode + " with error id " + apiError.id);
                            return;
                        }

                        // We have retrieved ME successfully.
                        Timber.i("Successfully retrieved the ME object. Token is still live.");

                        User user = response.body();

                        Timber.i("User ID: " + user.id);

                        // Token is valid. Fast Forward to the Choose Team activity.
                        // TODO: Add a check if the team is already chosen, then skip straight to main activity once it exists.
                        app.createUserComponent(user, serverConnectionParameters);
                        ChooseTeamActivity.launch(LaunchActivity.this);
                        finish();
                    }
                });
    }
}
