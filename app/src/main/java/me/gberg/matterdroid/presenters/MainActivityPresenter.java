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
import me.gberg.matterdroid.adapters.items.ChannelDrawerItem;
import me.gberg.matterdroid.activities.MainActivity;
import me.gberg.matterdroid.adapters.items.PostBasicSubItem;
import me.gberg.matterdroid.adapters.items.PostBasicTopItem;
import me.gberg.matterdroid.adapters.items.PostItem;
import me.gberg.matterdroid.adapters.items.PostSystemTopItem;
import me.gberg.matterdroid.di.scopes.TeamScope;
import me.gberg.matterdroid.events.PostsEvent;
import me.gberg.matterdroid.managers.ChannelsManager;
import me.gberg.matterdroid.managers.MembersManager;
import me.gberg.matterdroid.managers.PostsManager;
import me.gberg.matterdroid.managers.SessionManager;
import me.gberg.matterdroid.managers.UsersManager;
import me.gberg.matterdroid.managers.WebSocketManager;
import me.gberg.matterdroid.model.Channel;
import me.gberg.matterdroid.model.Channels;
import me.gberg.matterdroid.model.Post;
import me.gberg.matterdroid.model.Users;
import me.gberg.matterdroid.utils.picasso.ProfileImagePicasso;
import me.gberg.matterdroid.utils.rx.TeamBus;
import okhttp3.OkHttpClient;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

@TeamScope
public class MainActivityPresenter extends AbstractActivityPresenter<MainActivity> {

    // Injected dependencies.
    private final TeamBus bus;
    private final OkHttpClient httpClient;
    private final ChannelsManager channelsManager;
    private final PostsManager postsManager;
    private final SessionManager sessionManager;
    private final UsersManager usersManager;
    private final WebSocketManager webSocketManager;

    // ViewModel.
    private Channels channels;
    private Users users;
    private FastItemAdapter<IItem> postsAdapter;
    private WebSocketManager.ConnectionState connectionState = WebSocketManager.ConnectionState.Disconnected;
    private List<Post> posts;
    private List<Post> newPosts;

    // Presenter State.
    private IItemAdapter<IDrawerItem> drawerAdapter;
    private Channel channel;
    private FooterAdapter<ProgressItem> footerAdapter;
    private boolean noMoreScrollBack = false;
    private String activityTitle;
    private boolean isScrollback = false;
    private boolean isPostsListReset = false;

    // Utils
    private ProfileImagePicasso profileImagePicasso;

    // Subscriptions
    private Subscription webSocketTimeoutSubscription;

    @Inject
    public MainActivityPresenter(final TeamBus bus, final ChannelsManager channelsManager,
                                 final MembersManager membersManager, final PostsManager postsManager,
                                 final SessionManager sessionManager, final UsersManager usersManager,
                                 final WebSocketManager webSocketManager, final OkHttpClient httpClient) {
        // Injected dependencies.
        this.bus = bus;
        this.channelsManager = channelsManager;
        this.postsManager = postsManager;
        this.sessionManager = sessionManager;
        this.usersManager = usersManager;
        this.webSocketManager = webSocketManager;
        this.httpClient = httpClient;

        Timber.v("Constructing MainActivityPresenter.");

        // Subscribe to connection state changes.
        addSubscription(
                bus.getConnectionStateSubject()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<WebSocketManager.ConnectionState>() {
                            @Override
                            public void call(final WebSocketManager.ConnectionState connectionState) {
                                handleConnectionStateChanged(connectionState);
                            }
                        })
        );

        // Subscribe to the channel list.
        addSubscription(
                bus.getChannelsSubject()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Channels>() {
                            @Override
                            public void call(final Channels channels) {
                                handleChannelsChanged(channels);
                            }
                        })
        );

        // Subscribe to the posts list.
        addSubscription(
                bus.getPostsSubject()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<PostsEvent>() {
                            @Override
                            public void call(final PostsEvent postsEvent) {
                                handlePostsChanged(postsEvent);
                            }
                        })
        );

        // Subscribe to the channels list.
        addSubscription(
                bus.getUsersSubject()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Users>() {
                            @Override
                            public void call(final Users users) {
                                handleUsersChanged(users);
                            }
                        })
        );

        // Adapters that belong to the Presenter.
        postsAdapter = new FastItemAdapter<>();
        footerAdapter = new FooterAdapter<>();
    }

    @Override
    protected void onCreated(final Bundle savedInstanceState) {
        Timber.v("onTakeView()");

        // Get the drawer adapter from the view.
        drawerAdapter = getView().drawer.getItemAdapter();

        // Initialise the Posts view.
        getView().setupPostsView(footerAdapter.wrap(postsAdapter));

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

        // If the channel is saved, but there are no posts shown, ask the posts adapter to emit it's
        // current state of posts (typically triggered on restoring a saved activity state, but when
        // the app itself hasn't been killed). Otherwise, open the drawer to offer a selection of channels.
        if (channel != null) {
            updatePosts();
        } else {
            getView().openDrawer();
        }

        updateDrawer();

        // Instantiate the Profile Image Picasso (depends on context).
        profileImagePicasso = new ProfileImagePicasso(sessionManager.getServer(), getView(), httpClient);

        setActivityTitle(activityTitle);
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

        if (connectionState == connectionState.Disconnected) {
            webSocketManager.connect();
            getView().setPostsRefreshing(true);
        }
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
        super.leaveScope();

        Timber.v("leaveScope()");

        // Make sure there's no references to this presenter hanging around.
        if (webSocketTimeoutSubscription != null) {
            webSocketTimeoutSubscription.unsubscribe();
            webSocketTimeoutSubscription = null;
        }

        // Disconnect from the web socket.
        webSocketManager.disconnect();
    }

    private void handleConnectionStateChanged(final WebSocketManager.ConnectionState connectionState) {
        Timber.v("handleConnectionStateChanged(): " + connectionState);

        this.connectionState = connectionState;
    }

    private void handleChannelsChanged(final Channels channels) {
        Timber.v("handleChannelsChanged()");

        this.channels = channels;

        // React to the channels list changing.
        updateDrawer();
    }

    private void handlePostsChanged(final PostsEvent postsEvent) {
        Timber.v("handlePostsChanged()");

        this.newPosts = postsEvent.getPosts();

        if (postsEvent.isScrollback()) {
            isScrollback = true;
        }

        if (postsEvent.isReset()) {
            isPostsListReset = true;
        }

        // React to the posts list changing.
        updatePosts();
    }

    private void handleUsersChanged(final Users users) {
        Timber.v("handleUsersChanged()");

        this.users = users;

        updatePosts();
    }

    private void updateDrawer() {
        Timber.v("updateDrawer()");

        // Check all the state we need for this has been received.
        if (this.channels == null || this.users == null) {
            Timber.v("Incomplete state so not updating drawer.");
            return;
        }

        drawerAdapter.clear();

        List<Channel> publicChannels = new ArrayList<>();
        List<Channel> privateChannels = new ArrayList<>();
        List<Channel> dmChannels = new ArrayList<>();

        for (final Channel channel : channels.channels()) {
            if (channel.hasType(Channel.TYPE_OPEN)) {
                publicChannels.add(channel);
            } else if (channel.hasType(Channel.TYPE_PRIVATE)) {
                privateChannels.add(channel);
            } else if (channel.hasType(Channel.TYPE_DIRECT)) {
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

    private void updatePosts() {
        Timber.v("updatePosts()");

        // Check state conditions before carrying out the update.
        if (users == null) {
            Timber.v("Not updating posts as we haven't got all the needed state yet.");
            return;
        }

        getView().setPostsRefreshing(false);

        // If posts is null, we can short-circuit the delta-application process.
        if (posts == null || posts.isEmpty()) {
            Timber.v("posts == null or isEmpty() -> skip diffing and just apply posts list direct.");
            posts = newPosts;
            addPostsToAdapter(newPosts, 0);
            return;
        }

        // Clear the footer adapter if scrollback is done. Do this before merging to avoid wrapped
        // items screwing things up.
        if (isScrollback) {
            footerAdapter.clear();
            isScrollback = false;
        }

        // Short-circuit if the posts list has been reset.
        if (isPostsListReset) {
            Timber.v("posts is reset -> skip diffing and just apply new list direct.");
            int position = getView().getPostsListPosition();
            posts = newPosts;
            postsAdapter.clear();
            addPostsToAdapter(newPosts, 0);
            isPostsListReset = false;
            getView().setPostsListPosition(position);
            return;
        }

        Timber.d("About to start removing posts. Posts Size: " + posts.size()
                + " New Posts Size: " + newPosts.size()
                + " Adapter Size: " + postsAdapter.getAdapterItemCount());

        // Remove any old posts that are not *in newPosts in the same order*.
        int lastPostIndex = 0;
        int i = 0;
        while (i < posts.size()) {
            int postIndex = postsIndexOf(lastPostIndex, newPosts, posts.get(i));
            if (postIndex == -1) {
                // Didn't find the post ahead of here in the newPosts. Remove it.
                removePostFromAdapter(posts.get(i), i);
                posts.remove(i);
            } else {
                // Did find it. Nothing to remove, but increment the iterator variables.
                i++;
                lastPostIndex = postIndex;
            }
        }

        Timber.d("About to start adding new posts. Posts Size: " + posts.size()
                + " New Posts Size: " + newPosts.size()
                + " Adapter Size: " + postsAdapter.getAdapterItemCount());

        // Special case for if the posts list is empty.
        if (posts.size() == 0) {
            Timber.d("Posts is empty after deleting. Add all the new ones automatically.");
            addPostsToAdapter(newPosts, 0);
            return;
        }

        // Now add the new posts.
        int sliceStart = 0;
        for (int j = 0; j < posts.size(); j++) {
            for (int k = sliceStart; k < newPosts.size(); k++) {
                if (posts.get(j) == newPosts.get(k)) {
                    if (j == posts.size() -1 && posts.size() < newPosts.size()) {
                        // Reached the end of the existing posts. Add everything new past here.
                        Timber.d("Adding all remaining posts at the end of newPosts.");
                        List<Post> slice = newPosts.subList(sliceStart + 1, newPosts.size());
                        addPostsToAdapter(slice, sliceStart + 1);
                        posts.addAll(sliceStart + 1, slice);
                        j = posts.size(); // Bring the outer loop to an end.
                    } else if (k == sliceStart) {
                        // Nothing new found. Increase the slicer.
                        sliceStart = j + 1;
                    } else {
                        // We found some stuff to slice. Create the slice and add the posts.
                        Timber.d("Creating slice to add. Slice Start:" + sliceStart + " J:" + j + " K:" + k);
                        List<Post> slice = newPosts.subList(sliceStart, k);
                        addPostsToAdapter(slice, sliceStart);
                        posts.addAll(sliceStart, slice);
                        // Then increment the slicer *and j*.
                        sliceStart = j + slice.size() + 1;
                        j = j + slice.size();
                    }
                    break;
                }
            }
        }

        Timber.d("Done updating Posts. Posts Size: " + posts.size()
                + " New Posts Size: " + newPosts.size()
                + " Adapter Size: " + postsAdapter.getAdapterItemCount());
    }

    private int postsIndexOf(int startIndex, List<Post> posts, Post post) {
        for (int i = startIndex; i < posts.size(); i++) {
            if (posts.get(i) == post) {
                return i;
            }
        }
        return -1;
    }

    private void addPostsToAdapter(final List<Post> posts, int position) {
        Timber.v("addPostsToAdapter(): Posts: " + posts.size() + " Position: " + position);

        Post previousPost = null;

        // If we are inserting at the end of the adapter, there is no previous post. However, if not
        // then we should set the previous post to the one "before" where we are inserting.
        if (position < postsAdapter.getItemAdapter().getItemCount()) {
            try {
                PostItem postItem = (PostItem) postsAdapter.getItem(position);
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

        ListIterator<Post> newPostsIterator = posts.listIterator(posts.size());
        while (newPostsIterator.hasPrevious()) {
            final Post post = newPostsIterator.previous();
            if (post.shouldStartNewPostBlock(previousPost)) {
                if (post.hasType(Post.TYPE_JOIN_LEAVE) || post.hasType(Post.TYPE_HEADER_CHANGE)) {
                    newPostItems.add(0, new PostSystemTopItem(post));
                } else {
                    newPostItems.add(0, new PostBasicTopItem(post, profileImagePicasso, users.users().get(post.userId())));
                }
            } else {
                newPostItems.add(0, new PostBasicSubItem(post));
            }
            previousPost = post;
        }

        // Check our scroll position before the update to decide whether to scroll automatically to
        // the top (visually, bottom) of the view once the new items have been added.
        if (position == 0) {
            getView().checkShouldAutoScrollPostsView();
        }

        // Add the new items to the adapter.
        postsAdapter.add(position, newPostItems);

        // If there is an item directly *before* (ie. below) where we insert these items, then we
        // should check whether to convert it's PostItem type too.
        // TODO: Implement me!

        // Now the editing of the adapter contents is complete, do the autoscroll if appropriate.
        getView().autoScrollPostsView();
    }

    private void removePostFromAdapter(final Post post, int position) {
        Timber.v("RemovePostFromAdapter() " + position + " size: " + postsAdapter.getAdapterItemCount());

        postsAdapter.remove(position);

        // TODO: Make sure this doesn't break the top/sub division of posts.
    }

    public void sendMessage(final CharSequence text) {
        if (text == null || text.length() <= 0) {
            return;
        }

        postsManager.createNewPost(text.toString());

        getView().scrollPostsListToPosition(0);
    }

    public boolean drawerItemClicked(final Drawer drawer, long id, final IDrawerItem drawerItem) {
        if (drawerItem instanceof ChannelDrawerItem) {

            getView().setPostsRefreshing(true);

            // Reset the ViewModel state.
            channel = ((ChannelDrawerItem) drawerItem).getChannel();
            posts.clear();
            newPosts.clear();

            // Set activity title.
            setActivityTitle(channel.displayName());

            // Clear the message adapter.
            footerAdapter.clear();
            postsAdapter.clear();

            getView().recreateOnScrollListener();

            noMoreScrollBack = false;

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

    private void setActivityTitle(final String title) {
        activityTitle = title;
        if (getView() != null) {
            getView().setTitle(title);
        }
    }

    private final static String STATE_CURRENT_CHANNEL = "me.gberg.matterdroid.activities.MainActivity.state.current_channel";
}
