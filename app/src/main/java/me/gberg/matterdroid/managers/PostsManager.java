package me.gberg.matterdroid.managers;

import org.tautua.markdownpapers.Markdown;
import org.tautua.markdownpapers.parser.ParseException;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import me.gberg.matterdroid.api.TeamAPI;
import me.gberg.matterdroid.events.AddPostsEvent;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channel;
import me.gberg.matterdroid.model.Post;
import me.gberg.matterdroid.model.PostDeletedMessage;
import me.gberg.matterdroid.model.PostEditedMessage;
import me.gberg.matterdroid.model.PostedMessage;
import me.gberg.matterdroid.model.Posts;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.model.WebSocketMessage;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
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

        bus.toWebSocketBusObservable()
                .observeOn(Schedulers.computation())
                .subscribe(new  Action1<WebSocketMessage>() {
                    @Override
                    public void call(WebSocketMessage message) {
                        if (message instanceof PostedMessage) {
                            handlePostedMessage((PostedMessage) message);
                        } else if (message instanceof PostEditedMessage) {
                            handlePostEditedMessage((PostEditedMessage) message);
                        } else if (message instanceof PostDeletedMessage) {
                            handlePostDeletedMessage((PostDeletedMessage) message);
                        }
                    }
                });
    }

    public void setChannel(final Channel channel) {
        this.channel = channel;
        if (this.posts != null) {
            this.posts.clear();
        }

        // Load the initial set of message for this channel.
        Observable<Response<Posts>> initialLoadObservable = teamApi.posts(team.id, channel.id, 0, 60);
        initialLoadObservable.subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.computation())
                .subscribe(new Subscriber<Response<Posts>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        // TODO: Handle error here.
                    }

                    @Override
                    public void onNext(final Response<Posts> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            // TODO: Handle API Error here.
                        }

                        // Request is successful.
                        // Clear posts list and then populate in order.
                        posts = new ArrayList<Post>();
                        for (String id: response.body().order) {
                            Post post = response.body().posts.get(id);
                            parseMarkdown(post);
                            posts.add(post);
                        }
                        bus.send(new AddPostsEvent(posts, 0));
                    }
                });
    }

    public void loadMorePosts() {
        Observable<Response<Posts>> morePostsObservable = teamApi.postsBefore(team.id, channel.id, posts.get(posts.size() - 1).id, 0, 60);
        morePostsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.computation())
                .subscribe(new Subscriber<Response<Posts>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        // TODO: Handle error here.
                    }

                    @Override
                    public void onNext(final Response<Posts> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            // TODO: Handle API Error here.
                        }

                        // Request is successful.
                        List<Post> newPosts = new ArrayList<Post>();
                        for (String id: response.body().order) {
                            Post post = response.body().posts.get(id);
                            parseMarkdown(post);
                            newPosts.add(post);
                        }
                        bus.send(new AddPostsEvent(newPosts, posts.size(), true));
                        posts.addAll(newPosts);
                    }
                });
    }

    public void emitMessages() {
        bus.send(new AddPostsEvent(posts, 0));
    }

    public void handlePostedMessage(final PostedMessage message) {
        if (!message.channelId.equals(channel.id)) {
            // Message does not belong to the current channel.
            return;
        }

        Post post = message.parsedProps.post;
        parseMarkdown(post);

        posts.add(0, post);

        List<Post> newPosts = new ArrayList<>();
        newPosts.add(post);
        bus.send(new AddPostsEvent(newPosts, 0));
    }

    public void handlePostEditedMessage(final PostEditedMessage message) {
        // TODO
    }

    public void handlePostDeletedMessage(final PostDeletedMessage message) {
        // TODO
    }

    private void parseMarkdown(final Post post) {
        Reader in = new StringReader(post.message);
        Writer out = new StringWriter();

        Markdown md = new Markdown();
        try {
            md.transform(in, out);
            post.markdown = out.toString();
        } catch(ParseException e) {
            Timber.w(e);
            post.markdown = post.message;
        }
    }
}
