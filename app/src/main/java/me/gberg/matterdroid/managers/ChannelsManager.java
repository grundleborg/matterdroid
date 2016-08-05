package me.gberg.matterdroid.managers;

import me.gberg.matterdroid.api.TeamAPI;
import me.gberg.matterdroid.events.ChannelsEvent;
import me.gberg.matterdroid.events.TeamsListEvent;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channels;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ChannelsManager {

    private Bus bus;
    private final Team team;
    private final TeamAPI teamApi;
    private final ErrorParser errorParser;

    private Channels channels;

    public ChannelsManager(final Bus bus, final Team team, final TeamAPI teamApi,
                           ErrorParser errorParser) {
        this.bus = bus;
        this.team = team;
        this.teamApi = teamApi;
        this.errorParser = errorParser;
    }

    public void loadChannels() {
        Observable<Response<Channels>> observable = teamApi.channels(team.id);
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<Channels>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        bus.send(new ChannelsEvent(e));
                    }

                    @Override
                    public void onNext(final Response<Channels> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            bus.send(new TeamsListEvent(apiError));
                        }

                        // Request is successful.
                        bus.send(new ChannelsEvent(response.body()));
                    }
                });
    }
}