package me.gberg.matterdroid.managers;

import android.os.HandlerThread;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import me.gberg.matterdroid.App;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.api.TeamAPI;
import me.gberg.matterdroid.events.PostsEvent;
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
import me.gberg.matterdroid.utils.rx.TeamBus;
import retrofit2.Response;
import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class PostsManager {

    private final TeamBus bus;
    private final Team team;
    private final TeamAPI teamApi;
    private final SessionManager sessionManager;
    private final ErrorParser errorParser;
    private final App app;

    private Channel channel;
    private List<Post> posts;
    private Map<String, Post> postsMap;

    private HandlerThread thread;

    public PostsManager(final App app, final TeamBus bus, final Team team, final TeamAPI teamApi,
                        final SessionManager sessionManager, ErrorParser errorParser) {
        this.app = app;
        this.bus = bus;
        this.team = team;
        this.teamApi = teamApi;
        this.sessionManager = sessionManager;
        this.errorParser = errorParser;

        Timber.v("PostsManager constructed.");

        thread = new HandlerThread("PostsManagerWorker");
        thread.start();

        bus.toWebSocketBusObservable()
                .observeOn(AndroidSchedulers.from(thread.getLooper()))
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

        // Observe the bus for connection resets.
        bus.getConnectionStateSubject()
                .observeOn(AndroidSchedulers.from(thread.getLooper()))
                .subscribe(new Action1<WebSocketManager.ConnectionState>() {
                    @Override
                    public void call(final WebSocketManager.ConnectionState connectionState) {
                        if (connectionState == WebSocketManager.ConnectionState.Connected) {
                            reloadPosts();
                        }
                    }
                });
    }

    public void reloadPosts() {
        // If the channel is currently set, and there's more than 1 post, then do a full reload.
        if (channel == null || posts == null || posts.size() == 0) {
            return;
        }

        Timber.v("reloadPosts()");

        Observable<Response<Posts>> loadSinceObservabe = teamApi.postsSince(team.id(), channel.id(), posts.get(0).createAt());
        loadSinceObservabe
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.from(thread.getLooper()))
                .subscribe(new Subscriber<Response<Posts>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        Timber.e(e, "loadSinceObservable threw error.");
                    }

                    @Override
                    public void onNext(final Response<Posts> response) {
                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            Timber.e("Posts Since Error: " + apiError.statusCode() + apiError.detailedError());
                        }

                        ListIterator<String> li = response.body().order().listIterator(response.body().order().size());
                        while (li.hasPrevious()) {
                            final Post post = response.body().posts().get(li.previous());

                            // Post already deleted. Don't add it.
                            if (post.deleteAt() > 0) {
                                continue;
                            }

                            // Check if the post is already present.
                            if (postsMap.containsKey(post.id())) {
                                // Post is already present. Replace it.
                                final Post oldPost = postsMap.get(post.id());
                                int position = posts.indexOf(oldPost);
                                posts.remove(position);
                                posts.add(position, post);
                                postsMap.put(post.id(), post);
                                continue;
                            }

                            // Post is not already present.
                            // TODO: Add the post in the correct place in the sequence of messages.
                            posts.add(0, post);
                            postsMap.put(post.id(), post);
                        }
                        bus.getPostsSubject().onNext(new PostsEvent(new ArrayList<Post>(posts)));
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
        initialLoadObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.from(thread.getLooper()))
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
                            final Post post = response.body().posts().get(id);
                            posts.add(post);
                            postsMap.put(post.id(), post);
                        }
                        bus.getPostsSubject().onNext(new PostsEvent(new ArrayList<Post>(posts)));
                    }
                });
    }

    public boolean loadMorePosts() {
        if (posts.size() < 2) {
            return false;
        }
        Observable<Response<Posts>> morePostsObservable = teamApi.postsBefore(team.id(), channel.id(), posts.get(posts.size() - 1).id(), 0, 60);
        morePostsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.from(thread.getLooper()))
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
                            newPosts.add(response.body().posts().get(id));
                        }
                        posts.addAll(newPosts);
                        for (final Post post: posts) {
                            postsMap.put(post.id(), post);
                        }
                        bus.getPostsSubject().onNext(new PostsEvent(new ArrayList<Post>(posts), true));
                    }
                });
        return true;
    }

    public void handlePostedMessage(final PostedMessage message) {
        if (channel == null) {
            // We aren't listening to any channel at the momnet.
            return;
        }

        if (!message.broadcast().channelId().equals(channel.id())) {
            // Message does not belong to the current channel.
            return;
        }

        applyNewPost(message.parsedData().post());
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
            }
        }

        if (!postsMap.containsKey(post.id())) {
            posts.add(0, post);
            postsMap.put(post.id(), post);

            List<Post> newPosts = new ArrayList<>();
            newPosts.add(post);
            bus.getPostsSubject().onNext(new PostsEvent(new ArrayList<Post>(posts)));
        } else {
            // TODO: Post is updated (depending on timestamp), not new.
            Timber.w("Duplicate receipt of post not handled yet.");
        }
    }

    public void handlePostEditedMessage(final PostEditedMessage message) {
        final Post newPost = message.parsedData().post();

        if (channel == null) {
            // We aren't listening to any channel at the moment.
            return;
        }

        if (!message.broadcast().channelId().equals(channel.id())) {
            // Message does not belong to the current channel.
            return;
        }

        // If the post isn't in the map, we aren't showing it.
        if (!postsMap.containsKey(newPost.id())) {
            return;
        }

        // Remove the old post, and replace it with the new one.
        final Post oldPost = postsMap.get(newPost.id());
        int position = posts.indexOf(oldPost);
        posts.remove(position);
        posts.add(position, newPost);
        postsMap.put(newPost.id(), newPost);

        // Emit the posts.
        bus.getPostsSubject().onNext(new PostsEvent(new ArrayList<Post>(posts)));
    }

    public void handlePostDeletedMessage(final PostDeletedMessage message) {
        final Post newPost = message.parsedData().post();

        if (channel == null) {
            // We aren't listening to any channel at the moment.
            return;
        }

        if (!message.broadcast().channelId().equals(channel.id())) {
            // Message does not belong to the current channel.
            return;
        }

        // If the post isn't in the map, we aren't showing it.
        if (!postsMap.containsKey(newPost.id())) {
            return;
        }

        // Create a post with the "Message Deleted" text.
        final Post replacePost = Post.copyOf(newPost).withMessage(app.getResources().getString(R.string.general_post_deleted_text));

        // Remove the old post, and replace it with the new one.
        final Post oldPost = postsMap.get(newPost.id());
        int position = posts.indexOf(oldPost);
        posts.remove(position);
        posts.add(position, replacePost);
        postsMap.put(newPost.id(), replacePost);

        // Emit the posts.
        bus.getPostsSubject().onNext(new PostsEvent(new ArrayList<Post>(posts)));
    }

    public void createNewPost(final String message) {
        Single.create(new Single.OnSubscribe<Post>() {
            @Override
            public void call(final SingleSubscriber<? super Post> singleSubscriber) {
                final Post post = Post.builder()
                        .setMessage(message)
                        .setChannelId(channel.id())
                        .setPendingPostId(sessionManager.getUser().id() + ":" + DateTime.now().getMillis())
                        .setUserId(sessionManager.getUser().id())
                        .setCreateAt(DateTime.now().getMillis())
                        .setPending(true)
                        .build();

                if (post.channelId().equals(channel.id())) {
                    posts.add(0, post);
                    postsMap.put(post.pendingPostId(), post);
                    bus.getPostsSubject().onNext(new PostsEvent(new ArrayList<Post>(posts)));
                }

                Observable<Response<Post>> createPostObservable = teamApi.createPost(team.id(), channel.id(), post);
                createPostObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.from(thread.getLooper()))
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

                                applyNewPost(response.body());
                            }
                        });

                singleSubscriber.onSuccess(post);
            }
        })
                .subscribeOn(AndroidSchedulers.from(thread.getLooper()))
                .subscribe();
    }
}
