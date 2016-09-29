package me.gberg.matterdroid.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.gberg.matterdroid.api.TeamAPI;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channel;
import me.gberg.matterdroid.model.Channels;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
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

        // Observe the bus for connection resets.
        bus.getConnectionStateSubject()
                .subscribe(new Action1<WebSocketManager.ConnectionState>() {
                    @Override
                    public void call(final WebSocketManager.ConnectionState connectionState) {
                        if (connectionState == WebSocketManager.ConnectionState.Connected) {
                            loadChannels();
                        }
                    }
                });
    }

    private void loadChannels() {
        Observable<Response<Channels>> observable = teamApi.channels(team.id());
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<Channels>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Timber.e(e, "channels API call returned an error.");
                    }

                    @Override
                    public void onNext(final Response<Channels> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            Timber.e("Channels Error: " + apiError.statusCode() + apiError.detailedError());
                        }

                        // Request is successful.
                        Channels channels = response.body();
                        List<Channel> channelsMutable = new ArrayList<Channel>();
                        channelsMutable.addAll(channels.channels());
                        Collections.sort(channelsMutable, new Comparator<Channel>() {
                            public int compare(Channel c1, Channel c2){
                                if (c1.type().equals(c2.type())) {
                                    // Same type. Compare based on name.
                                    return c1.displayName().compareTo(c2.displayName());
                                }

                                // Different types. Sort based on type.
                                if (c1.type().equals("O")) {
                                    return -1;
                                } else if (c2.type().equals("O")) {
                                    return 1;
                                } else if (c1.type().equals("P")) {
                                    return -1;
                                } else {
                                    return 1;
                                }
                            }
                        });
                        Channels newChannels = Channels.builder()
                                .from(channels)
                                .setChannels(channelsMutable)
                                .build();
                        ChannelsManager.this.channels = newChannels;
                        bus.getChannelsSubject().onNext(newChannels);
                    }
                });
    }
}
