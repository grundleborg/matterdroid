package me.gberg.matterdroid.managers;

import java.util.Map;

import me.gberg.matterdroid.api.UserAPI;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.User;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.TeamBus;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class DirectProfilesManager {

    private final TeamBus teamBus;
    private final UserAPI userApi;
    private final ErrorParser errorParser;

    public DirectProfilesManager(final TeamBus teamBus, final UserAPI userApi, ErrorParser errorParser) {
        this.teamBus = teamBus;
        this.userApi = userApi;
        this.errorParser = errorParser;

        // Observe the bus for connection resets.
        teamBus.getConnectionStateSubject()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<WebSocketManager.ConnectionState>() {
                    @Override
                    public void call(final WebSocketManager.ConnectionState connectionState) {
                        if (connectionState == WebSocketManager.ConnectionState.Connected) {
                            loadDirectProfiles();
                        }
                    }
                });
    }

    private void loadDirectProfiles() {
        Observable<Response<Map<String, User>>> initialLoadObservable = userApi.directProfiles();
        initialLoadObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<Map<String, User>>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Timber.e(e, "Direct Profiles API returned an error.");
                    }

                    @Override
                    public void onNext(final Response<Map<String, User>> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            Timber.e("Direct Profiles Error: " + apiError.statusCode() + apiError.detailedError());
                        }

                        teamBus.getDirectProfilesSubject().onNext(response.body());
                    }
                });
    }
}
