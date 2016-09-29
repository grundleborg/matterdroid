package me.gberg.matterdroid.managers;

import java.util.Map;

import me.gberg.matterdroid.api.UserAPI;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.model.User;
import me.gberg.matterdroid.model.Users;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class UsersManager {

    private final Team team;
    private final UserAPI userAPI;
    private final Bus bus;
    private final ErrorParser errorParser;

    private Users users;

    public UsersManager(final Team team, final UserAPI userAPI, final Bus bus, final ErrorParser errorParser) {
        this.team = team;
        this.userAPI = userAPI;
        this.bus = bus;
        this.errorParser = errorParser;

        // Observe the bus for connection resets.
        bus.getConnectionStateSubject()
                .subscribe(new Action1<WebSocketManager.ConnectionState>() {
                    @Override
                    public void call(final WebSocketManager.ConnectionState connectionState) {
                        if (connectionState == WebSocketManager.ConnectionState.Connected) {
                            loadUsers();
                        }
                    }
                });
    }

    private void loadUsers() {
        Observable<Response<Map<String, User>>> initialLoadObservable = userAPI.users(team.id());
        initialLoadObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<Map<String, User>>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Timber.e(e, "users API call returned an error.");
                    }

                    @Override
                    public void onNext(final Response<Map<String, User>> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            Timber.e("Users Error: " + apiError.statusCode() + apiError.detailedError());
                        }

                        // Request is successful.
                        Users users = Users.builder()
                                .setUsers(response.body())
                                .build();
                        bus.getUsersSubject().onNext(users);
                    }
                });
    }
}
