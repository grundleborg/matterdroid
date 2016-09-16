package me.gberg.matterdroid.managers;

import java.util.ArrayList;
import java.util.List;

import me.gberg.matterdroid.api.TeamAPI;
import me.gberg.matterdroid.events.PostsReceivedEvent;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channel;
import me.gberg.matterdroid.model.Post;
import me.gberg.matterdroid.model.Posts;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class PostsManager {

    private final Bus bus;
    private final Team team;
    private final TeamAPI teamApi;
    private final ErrorParser errorParser;

    private Channel channel;
    private List<Post> posts;

    public PostsManager(final Bus bus, final Team team, final TeamAPI teamApi,
                        ErrorParser errorParser) {
        this.bus = bus;
        this.team = team;
        this.teamApi = teamApi;
        this.errorParser = errorParser;
    }

    public void setChannel(final Channel channel) {
        this.channel = channel;
        if (this.posts != null) {
            this.posts.clear();
        }

        // Load the initial set of message for this channel.
        Observable<Response<Posts>> initialLoadObservable = teamApi.posts(team.id, channel.id, 0, 60);
        initialLoadObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<Posts>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        bus.send(new PostsReceivedEvent(e));
                    }

                    @Override
                    public void onNext(final Response<Posts> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            bus.send(new PostsReceivedEvent(apiError));
                        }

                        // Request is successful.
                        posts = new ArrayList<Post>();
                        for (String id: response.body().order) {
                            posts.add(response.body().posts.get(id));
                        }
                        bus.send(new PostsReceivedEvent(posts));
                    }
                });
    }

    public void emitMessages() {
        bus.send(new PostsReceivedEvent(posts));
    }
}
