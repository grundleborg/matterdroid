package me.gberg.matterdroid.presenters;

import android.os.Bundle;

import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import me.gberg.matterdroid.R;
import me.gberg.matterdroid.activities.ChannelDrawerItem;
import me.gberg.matterdroid.activities.MainActivity;
import me.gberg.matterdroid.adapters.items.PostBasicSubItem;
import me.gberg.matterdroid.adapters.items.PostBasicTopItem;
import me.gberg.matterdroid.adapters.items.PostItem;
import me.gberg.matterdroid.di.scopes.TeamScope;
import me.gberg.matterdroid.events.AddPostsEvent;
import me.gberg.matterdroid.events.ChannelsEvent;
import me.gberg.matterdroid.events.MembersEvent;
import me.gberg.matterdroid.events.RemovePostEvent;
import me.gberg.matterdroid.events.UsersEvent;
import me.gberg.matterdroid.managers.ChannelsManager;
import me.gberg.matterdroid.managers.MembersManager;
import me.gberg.matterdroid.managers.PostsManager;
import me.gberg.matterdroid.managers.SessionManager;
import me.gberg.matterdroid.managers.UsersManager;
import me.gberg.matterdroid.managers.WebSocketManager;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channel;
import me.gberg.matterdroid.model.Channels;
import me.gberg.matterdroid.model.Post;
import me.gberg.matterdroid.model.Users;
import me.gberg.matterdroid.utils.picasso.ProfileImagePicasso;
import me.gberg.matterdroid.utils.rx.Bus;
import okhttp3.OkHttpClient;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

@TeamScope
public class MainActivityPresenter extends AbstractActivityPresenter<MainActivity> {

    // Injected Misc.
    private final Bus bus;
    private final OkHttpClient httpClient;

    // Injected State Managers.
    private final ChannelsManager channelsManager;
    private final MembersManager membersManager;
    private final PostsManager postsManager;
    private final SessionManager sessionManager;
    private final UsersManager usersManager;
    private final WebSocketManager webSocketManager;

    // Misc Local State.
    private IItemAdapter<IDrawerItem> drawerAdapter;
    private Channels channels;
    private boolean horribleHackShouldTriggerEmitPosts = false;
    private Users users;

    private Channel channel;
    private FastItemAdapter<IItem> postsAdapter;
    private FooterAdapter<ProgressItem> footerAdapter;
    private boolean noMoreScrollBack = false;

    private ProfileImagePicasso profileImagePicasso;

    private final Subscription busSubscription;
    private Subscription webSocketTimeoutSubscription;

    @Inject
    public MainActivityPresenter(final Bus bus, final ChannelsManager channelsManager,
                                 final MembersManager membersManager, final PostsManager postsManager,
                                 final SessionManager sessionManager, final UsersManager usersManager,
                                 final WebSocketManager webSocketManager, final OkHttpClient httpClient) {
        this.bus = bus;
        this.channelsManager = channelsManager;
        this.membersManager = membersManager;
        this.postsManager = postsManager;
        this.sessionManager = sessionManager;
        this.usersManager = usersManager;
        this.webSocketManager = webSocketManager;
        this.httpClient = httpClient;

        Timber.v("Constructing MainActivityPresenter.");

        // Subscribe to the event bus.
        busSubscription = this.bus.toObserverable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof ChannelsEvent) {
                            handleChannelsEvent((ChannelsEvent) event);
                            updateDrawer();
                        } else if (event instanceof AddPostsEvent) {
                            handleAddPostsEvent((AddPostsEvent) event);
                        } else if (event instanceof RemovePostEvent) {
                            handleRemovePostEvent((RemovePostEvent) event);
                        } else if (event instanceof MembersEvent) {
                            handleMembersEvent((MembersEvent) event);
                        } else if (event instanceof UsersEvent) {
                            handleUsersEvent((UsersEvent) event);
                        }
                    }
                });

        // Adapters that belong to the Presenter.
        postsAdapter = new FastItemAdapter<>();
        footerAdapter = new FooterAdapter<>();
    }

    @Override
    protected void onCreated(final Bundle savedInstanceState) {
        Timber.v("onTakeView()");

        // Drawer
        drawerAdapter = getView().drawer.getItemAdapter();

        getView().setupPostsView(footerAdapter.wrap(postsAdapter));

        webSocketManager.connect();
        channelsManager.loadChannels();
        usersManager.loadUsers();

        // Load saved instance state.
        if (savedInstanceState != null) {
            Timber.v("SavedInstanceState found.");
            postsAdapter.withSavedInstanceState(savedInstanceState);
            final String channelId = savedInstanceState.getString(STATE_CURRENT_CHANNEL);
            if (channelId != null) {
                // Note: This will only restore the selected channel if the app hasn't been killed.
                if (channels != null) {
                    channel = channels.getChannelForId(channelId);
                }
            }
        }

        if (channel != null) {
            postsManager.emitMessages();
        } else {
            getView().openDrawer();
        }

        profileImagePicasso = new ProfileImagePicasso(sessionManager.getServer(), getView(), httpClient);
    }

    @Override
    protected void onStarted() {
        Timber.v("onStarted()");
    }

    @Override
    protected void onResumed() {
        Timber.v("onResumed()");

        if (webSocketTimeoutSubscription != null) {
            webSocketTimeoutSubscription.unsubscribe();
            webSocketTimeoutSubscription = null;
        }

        webSocketManager.connect();
    }

    @Override
    protected void onPaused() {
        Timber.v("onPaused()");

        if (webSocketTimeoutSubscription == null) {
            webSocketTimeoutSubscription = Single.create(new Single.OnSubscribe<Void>() {
                @Override
                public void call(final SingleSubscriber<? super Void> singleSubscriber) {
                    singleSubscriber.onSuccess(null);
                }
            })
            .delay(15, TimeUnit.SECONDS)
            .subscribe(new Action1<Void>() {
                @Override
                public void call(final Void aVoid) {
                    Timber.v("Unsubscribe Web Socket Single Fired.");
                    webSocketManager.disconnect();
                    webSocketTimeoutSubscription = null;
                }
            });
        }
    }

    @Override
    protected void onStopped() {
        Timber.v("onStopped()");
    }

    @Override
    protected void onDestroyed() {
        Timber.v("onDestroyed()");
    }

    @Override
    protected void onSaveInstanceState(final Bundle bundle) {
        Timber.v("onSaveInstanceState()");
        if (channel != null) {
            bundle.putString(STATE_CURRENT_CHANNEL, channel.id());
        }
    }

    @Override
    public void leaveScope() {
        // This method exists because I am struggling to see how to get the presenter to drop its
        // references to other things that are keeping it hanging around after it goes out of scope
        // and thus stopping it being garbage collected.
        this.busSubscription.unsubscribe();
    }

    private void handleChannelsEvent(final ChannelsEvent event) {
        Timber.v("handleChannelsEvent()");
        if (event.isApiError()) {
            APIError apiError = event.getApiError();
            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode() + " with error id " + apiError.id());
            return;
        } else if (event.isError()) {
            // Unhandled error. Log it.
            Throwable e = event.getThrowable();
            Timber.e(e, e.getMessage());
            return;
        }

        // Success.
        this.channels = event.getChannels();
    }

    private void updateDrawer() {
        Timber.v("updateDrawer()");

        // Check all the state we need for this has been received.
        if (this.channels == null) {
            Timber.v("Incomplete state so not updating drawer.");
        }

        drawerAdapter.clear();

        List<Channel> publicChannels = new ArrayList<>();
        List<Channel> privateChannels = new ArrayList<>();
        List<Channel> dmChannels = new ArrayList<>();

        for (final Channel channel : channels.channels()) {
            if (channel.type().equals("O")) {
                publicChannels.add(channel);
            } else if (channel.type().equals("P")) {
                privateChannels.add(channel);
            } else if (channel.type().equals("D")) {
                dmChannels.add(channel);
            } else {
                publicChannels.add(channel);
            }
        }

        drawerAdapter.add(new PrimaryDrawerItem().withName(R.string.it_channels_header_public));
        for (final Channel channel : publicChannels) {
            drawerAdapter.add(new ChannelDrawerItem()
                    .withChannel(channel)
                    .withName(channel.displayName())
            );
        }

        drawerAdapter.add(new PrimaryDrawerItem().withName(R.string.it_channels_header_private));
        for (final Channel channel : privateChannels) {
            drawerAdapter.add(new ChannelDrawerItem()
                    .withChannel(channel)
                    .withName(channel.displayName())
            );
        }

        drawerAdapter.add(new PrimaryDrawerItem().withName(R.string.it_channels_header_dm));
        for (final Channel channel : dmChannels) {
            drawerAdapter.add(new ChannelDrawerItem()
                    .withChannel(channel)
                    .withName("<Unknown User>")
            );
        }
    }
    private void handleAddPostsEvent(final AddPostsEvent event) {
        Timber.v("handleAddPostsEvent()");

        if (users == null) {
            Timber.v("not bothering as members manager is not yet populated.");
            horribleHackShouldTriggerEmitPosts = true;
            return;
        }

        Post previousPost = null;

        // If we are inserting at the end of the adapter, there is no previous post. However, if not
        // then we should set the previous post to the one "before" where we are inserting.
        if (event.getPosition() < postsAdapter.getItemAdapter().getItemCount()) {
            try {
                PostItem postItem = (PostItem) postsAdapter.getItem(event.getPosition());
                // Check it is not null in case there is some other item here due to wrapped adapters.
                if (postItem != null) {
                    previousPost = postItem.getPost();
                }
            } catch (ClassCastException e) {
                // Not a PostItem, so ignore it.
            }

        }

        // Iterate through the posts to be added in *reverse order*, but once we have decided which
        // type of PostItem to use, reverse the order again when adding them to the new items list
        // so that we end up with them in the right order. This is necessary because they are
        // ordered programatically in descending time, which is the opposite of how the user
        // actually perceives things when interacting with the posts list.
        List<IItem> newPostItems = new ArrayList<>();
        List<Post> newPosts = event.getPosts();

        ListIterator<Post> newPostsIterator = newPosts.listIterator(newPosts.size());
        while (newPostsIterator.hasPrevious()) {
            final Post post = newPostsIterator.previous();
            if (shouldStartNewPostBlock(previousPost, post)) {
                newPostItems.add(0, new PostBasicTopItem(post, profileImagePicasso, users.users().get(post.userId())));
            } else {
                newPostItems.add(0, new PostBasicSubItem(post));
            }
            previousPost = post;
        }

        // Check our scroll position before the update to decide whether to scroll automatically to
        // the top (visually, bottom) of the view once the new items have been added.
        if (event.getPosition() == 0) {
            getView().checkShouldAutoScrollPostsView();
        }

        // Add the new items to the adapter.
        postsAdapter.add(event.getPosition(), newPostItems);
        if (event.isScrollback()) {
            footerAdapter.clear();
        }

        // If there is an item directly *before* (ie. below) where we insert these items, then we
        // should check whether to convert it's PostItem type too.
        // TODO: Implement me!

        // Now the editing of the adapter contents is complete, do the autoscroll if appropriate.
        getView().autoScrollPostsView();
    }

    private boolean shouldStartNewPostBlock(final Post previousPost, final Post thisPost) {
        // No previous post.
        if (previousPost == null) {
            return true;
        }

        // Different user on the previous post.
        if (!previousPost.userId().equals(thisPost.userId())) {
            return true;
        }

        // Too much time past since last post.
        if (previousPost.createAt() + 900000 < thisPost.createAt()) {
            return true;
        }

        return false;
    }

    private void handleRemovePostEvent(final RemovePostEvent event) {
        postsAdapter.remove(event.getPosition());
        // TODO: Make sure this doesn't break the top/sub division of posts.
    }

    private void handleMembersEvent(final MembersEvent event) {
        Timber.v("handleMembersEvent()");
        if (event.isApiError()) {
            APIError apiError = event.getApiError();
            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode() + " with error id " + apiError.id());
            return;
        } else if (event.isError()) {
            // Unhandled error. Log it.
            Throwable e = event.getThrowable();
            Timber.e(e, e.getMessage());
            return;
        }

        // Success
        Timber.i("Members for channel retrieved: "+event.getMembersCount());

        if (postsAdapter.getAdapterItemCount() == 0 && horribleHackShouldTriggerEmitPosts) {
            postsManager.emitMessages();
            horribleHackShouldTriggerEmitPosts = false;
        }
    }

    private void handleUsersEvent(UsersEvent usersEvent) {
        Timber.v("handleUsersEvent()");
        users = usersEvent.getUsers();

        if (horribleHackShouldTriggerEmitPosts) {
            postsManager.emitMessages();
            horribleHackShouldTriggerEmitPosts = false;
        }
    }

    public void sendMessage(final CharSequence text) {
        if (text == null || text.length() <= 0) {
            return;
        }

        postsManager.createNewPost(text.toString());
    }

    public boolean drawerItemClicked(final Drawer drawer, long id, final IDrawerItem drawerItem) {
        if (drawerItem instanceof ChannelDrawerItem) {
            channel = ((ChannelDrawerItem) drawerItem).getChannel();

            // Set activity title.
            getView().setTitle(channel.displayName());

            // Clear the message adapter.
            footerAdapter.clear();
            postsAdapter.clear();

            getView().recreateOnScrollListener();

            noMoreScrollBack = false;

            membersManager.setChannel(channel);
            postsManager.setChannel(channel);
            drawer.closeDrawer();
        }
        return true;
    }

    public void loadMorePosts() {
        Timber.v("onLoadMore() noMoreScrollback: " + noMoreScrollBack);
        boolean canLoadMore = postsManager.loadMorePosts();
        if (!noMoreScrollBack && canLoadMore) {
            footerAdapter.clear();
            footerAdapter.add(new ProgressItem().withEnabled(false));
        }
    }

    public FooterAdapter getFooterAdapter() {
        return footerAdapter;
    }

    private final static String STATE_CURRENT_CHANNEL = "me.gberg.matterdroid.activities.MainActivity.state.current_channel";
}
