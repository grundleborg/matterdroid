package me.gberg.matterdroid.managers;

import me.gberg.matterdroid.api.UserAPI;
import me.gberg.matterdroid.events.TeamsListEvent;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.InitialLoad;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class TeamsManager {

    private final ErrorParser errorParser;
    private final Bus bus;
    private final UserAPI userAPI;

    public TeamsManager(final ErrorParser errorParser, final Bus bus, final UserAPI userAPI) {
        this.errorParser = errorParser;
        this.bus = bus;
        this.userAPI = userAPI;
    }

    public void loadAvailableTeams() {
        Observable<Response<InitialLoad>> initialLoadObservable = userAPI.initialLoad();
        initialLoadObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<InitialLoad>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        bus.send(new TeamsListEvent(e));
                    }

                    @Override
                    public void onNext(final Response<InitialLoad> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            bus.send(new TeamsListEvent(apiError));
                        }

                        // Request is successful.
                        bus.send(new TeamsListEvent(response.body().teams));
                    }
                });
    }
}
