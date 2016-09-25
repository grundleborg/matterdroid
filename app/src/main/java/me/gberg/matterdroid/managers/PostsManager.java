package me.gberg.matterdroid.managers;

import org.joda.time.DateTime;
import org.tautua.markdownpapers.Markdown;
import org.tautua.markdownpapers.parser.ParseException;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.gberg.matterdroid.api.TeamAPI;
import me.gberg.matterdroid.events.AddPostsEvent;
import me.gberg.matterdroid.events.RemovePostEvent;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channel;
import me.gberg.matterdroid.model.IWebSocketMessage;
import me.gberg.matterdroid.model.Post;
import me.gberg.matterdroid.model.PostDeletedMessage;
import me.gberg.matterdroid.model.PostEditedMessage;
import me.gberg.matterdroid.model.PostedMessage;
import me.gberg.matterdroid.model.Posts;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;
import retrofit2.Response;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class PostsManager {

    private final Bus bus;
    private final Team team;
    private final TeamAPI teamApi;
    private final SessionManager sessionManager;
    private final ErrorParser errorParser;

    private Channel channel;
    private List<Post> posts;
    private Map<String, Post> postsMap;

    public PostsManager(final Bus bus, final Team team, final TeamAPI teamApi,
                        final SessionManager sessionManager, ErrorParser errorParser) {
        this.bus = bus;
        this.team = team;
        this.teamApi = teamApi;
        this.sessionManager = sessionManager;
        this.errorParser = errorParser;

        bus.toWebSocketBusObservable()
                .observeOn(Schedulers.computation())
                .subscribe(new  Action1<IWebSocketMessage>() {
                    @Override
                    public void call(IWebSocketMessage message) {
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
            this.postsMap.clear();
        }

        // Load the initial set of message for this channel.
        Observable<Response<Posts>> initialLoadObservable = teamApi.posts(team.id(), channel.id(), 0, 60);
        initialLoadObservable.subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.computation())
                .subscribe(new Subscriber<Response<Posts>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Timber.e(e, "initialLoadObservable threw error.");
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
                        postsMap = new HashMap<String, Post>();
                        for (String id: response.body().order()) {
                            Post post = response.body().posts().get(id);
                            post = parseMarkdown(post);
                            posts.add(post);
                            postsMap.put(post.id(), post);
                        }
                        bus.send(new AddPostsEvent(new ArrayList<Post>(posts), 0));
                    }
                });
    }

    public boolean loadMorePosts() {
        if (posts.size() < 2) {
            return false;
        }
        Observable<Response<Posts>> morePostsObservable = teamApi.postsBefore(team.id(), channel.id(), posts.get(posts.size() - 1).id(), 0, 60);
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
                        for (String id: response.body().order()) {
                            Post post = response.body().posts().get(id);
                            post = parseMarkdown(post);
                            newPosts.add(post);
                        }
                        bus.send(new AddPostsEvent(new ArrayList<Post>(newPosts), posts.size(), true));
                        posts.addAll(newPosts);
                        for (final Post post: posts) {
                            postsMap.put(post.id(), post);
                        }
                    }
                });
        return true;
    }

    public void emitMessages() {
        Single.create(new Single.OnSubscribe<Void>() {
            @Override
            public void call(final SingleSubscriber<? super Void> singleSubscriber) {
                if (posts != null) {
                    bus.send(new AddPostsEvent(new ArrayList<Post>(posts), 0));
                }
                singleSubscriber.onSuccess(null);
            }
        }).subscribeOn(Schedulers.computation()).subscribe();
    }

    public void handlePostedMessage(final PostedMessage message) {
        if (channel == null) {
            // We aren't listening to any channel at the momnet.
            return;
        }

        if (!message.webSocketMessage().channelId().equals(channel.id())) {
            // Message does not belong to the current channel.
            return;
        }

        Post post = message.parsedData().post();
        post = parseMarkdown(post);

        applyNewPost(post);
    }

    public void applyNewPost(final Post post) {
        // TODO: What is needed here to enforce correct ordering of posts?

        // Check if this is replacing a pending post.
        if (post.pendingPostId() != null && post.pendingPostId().length() > 0) {
            Timber.d("Replacing a pending post.");
            final Post replacedPost = postsMap.get(post.pendingPostId());
            if (replacedPost != null) {
                // There is a pending post to replace.
                postsMap.remove(post.pendingPostId());
                int removedPosition = posts.indexOf(replacedPost);
                posts.remove(removedPosition);
                bus.send(new RemovePostEvent(removedPosition));
            }
        }

        if (!postsMap.containsKey(post.id())) {
            posts.add(0, post);
            postsMap.put(post.id(), post);

            List<Post> newPosts = new ArrayList<>();
            newPosts.add(post);
            bus.send(new AddPostsEvent(new ArrayList<Post>(newPosts), 0));
        } else {
            // TODO: Post is updated (depending on timestamp), not new.
            Timber.w("Duplicate receipt of post not handled yet.");
        }
    }

    public void handlePostEditedMessage(final PostEditedMessage message) {
        // TODO
    }

    public void handlePostDeletedMessage(final PostDeletedMessage message) {
        // TODO
    }

    public void createNewPost(final String message) {
        final Post prePost = Post.builder()
                .setMessage(message)
                .setChannelId(channel.id())
                .setPendingPostId(sessionManager.getUser().id() + ":" + DateTime.now().getMillis())
                .setUserId(sessionManager.getUser().id())
                .setCreateAt(DateTime.now().getMillis())
                .setPending(true)
                .build();
        final Post post = parseMarkdown(prePost);

        Single.create(new Single.OnSubscribe<Post>() {
            @Override
            public void call(final SingleSubscriber<? super Post> singleSubscriber) {
                posts.add(0, post);
                postsMap.put(post.pendingPostId(), post);

                ArrayList<Post> posts = new ArrayList<Post>();
                posts.add(post);
                bus.send(new AddPostsEvent(new ArrayList<Post>(posts), 0, true));

                singleSubscriber.onSuccess(post);
            }
        }).subscribeOn(Schedulers.computation()).subscribe();

        Observable<Response<Post>> createPostObservable = teamApi.createPost(team.id(), channel.id(), post);
        createPostObservable.subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.computation())
                .subscribe(new Subscriber<Response<Post>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("completed()");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        // TODO: Handle error here.
                    }

                    @Override
                    public void onNext(final Response<Post> response) {
                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            // TODO: Handle API Error here.
                        }

                        if (!post.channelId().equals(channel.id())) {
                            // Message does not belong to the current channel.
                            return;
                        }

                        Post post = response.body();
                        post = parseMarkdown(post);
                        applyNewPost(post);
                    }
                });
    }

    // FIXME: Replace this with the decoding being part of the GSON decode process, or the Post object
    // initialisation process.
    private Post parseMarkdown(final Post post) {
        Reader in = new StringReader(post.message());
        Writer out = new StringWriter();

        Markdown md = new Markdown();
        try {
            md.transform(in, out);
            return Post.builder()
                    .from(post)
                    .setMarkdown(out.toString())
                    .build();
        } catch(ParseException e) {
            return Post.builder()
                    .from(post)
                    .setMarkdown(post.message())
                    .build();
        }
    }
}
