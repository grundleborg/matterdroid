package me.gberg.matterdroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.adapters.FooterAdapter;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.trello.navi.component.support.NaviAppCompatActivity;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.navi.NaviLifecycle;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.gberg.matterdroid.App;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.adapters.items.PostBasicSubItem;
import me.gberg.matterdroid.adapters.items.PostBasicTopItem;
import me.gberg.matterdroid.di.components.TeamComponent;
import me.gberg.matterdroid.events.ChannelsEvent;
import me.gberg.matterdroid.events.MembersEvent;
import me.gberg.matterdroid.events.PostsReceivedEvent;
import me.gberg.matterdroid.managers.ChannelsManager;
import me.gberg.matterdroid.managers.MembersManager;
import me.gberg.matterdroid.managers.PostsManager;
import me.gberg.matterdroid.managers.SessionManager;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Channel;
import me.gberg.matterdroid.model.Channels;
import me.gberg.matterdroid.model.Post;
import me.gberg.matterdroid.utils.picasso.ProfileImagePicasso;
import me.gberg.matterdroid.utils.rx.Bus;
import okhttp3.OkHttpClient;
import rx.functions.Action1;
import timber.log.Timber;

public class MainActivity extends NaviAppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.co_main_messages_list)
    RecyclerView postsView;

    @Inject
    Bus bus;

    @Inject
    SessionManager sessionManager;

    @Inject
    ChannelsManager channelsManager;

    @Inject
    PostsManager postsManager;

    @Inject
    MembersManager membersManager;

    @Inject
    OkHttpClient httpClient;

    private final LifecycleProvider<ActivityEvent> provider
            = NaviLifecycle.createActivityLifecycleProvider(this);

    Drawer drawer;

    private IItemAdapter<IDrawerItem> drawerAdapter;
    private Channels channels;

    private Channel channel;
    private FastItemAdapter<IItem> postsAdapter;
    private FooterAdapter<ProgressItem> footerAdapter;
    private boolean noMoreScrollBack = false;
    EndlessRecyclerOnScrollListener infiniteScrollListener;

    private ProfileImagePicasso profileImagePicasso;

    private final static String STATE_CURRENT_CHANNEL = "me.gberg.matterdroid.activities.MainActivity.state.current_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate() Called");

        // Redirect to Choose Team activity if we can't inject the Team Component.
        TeamComponent teamComponent = ((App) getApplication()).getTeamComponent();
        if (teamComponent == null) {
            ChooseTeamActivity.launch(this);
            finish();
            return;
        }
        teamComponent.inject(this);

        // Subscribe to the event bus.
        // TODO: Unsubscribe at the correct lifecycle events.
        bus.toObserverable()
                .compose(provider.bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof ChannelsEvent) {
                            handleChannelsEvent((ChannelsEvent) event);
                        } else if (event instanceof PostsReceivedEvent) {
                            handlePostsReceivedEvent((PostsReceivedEvent) event);
                        } else if (event instanceof MembersEvent) {
                            handleMembersEvent((MembersEvent) event);
                        }
                    }
                });

        setContentView(R.layout.ac_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        profileImagePicasso = new ProfileImagePicasso(sessionManager.getServer(), this, httpClient);

        // Set up the drawer.
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(final View view, final int position, final IDrawerItem drawerItem) {
                        onChannelSelected(drawerItem.getIdentifier());
                        drawer.closeDrawer();
                        return true;
                    }
                })
                .build();

        drawerAdapter = drawer.getItemAdapter();

        postsAdapter = new FastItemAdapter<>();
        footerAdapter = new FooterAdapter<>();

        final LinearLayoutManager postsViewLayoutManager = new LinearLayoutManager(this);
        postsViewLayoutManager.setReverseLayout(true);
        postsView.setLayoutManager(postsViewLayoutManager);
        postsView.setItemAnimator(new DefaultItemAnimator());
        postsView.setAdapter(footerAdapter.wrap(postsAdapter));
        recreateOnScrollListener();

        channelsManager.loadChannels();

        // Load saved instance state.
        if (savedInstanceState != null) {
            Timber.v("SavedInstanceState found.");
            postsAdapter.withSavedInstanceState(savedInstanceState);
            final String channelId = savedInstanceState.getString(STATE_CURRENT_CHANNEL);
            if (channelId != null) {
                // Note: This will only restore the selected channel if the app hasn't been killed.
                channel = channelsManager.getChannelForId(channelId);
            }
        }

        if (channel != null) {
            postsManager.emitMessages();
        } else {
            drawer.openDrawer();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Timber.v("onSaveInstanceState()");
        savedInstanceState = postsAdapter.saveInstanceState(savedInstanceState);
        if (channel != null) {
            savedInstanceState.putString(STATE_CURRENT_CHANNEL, channel.id);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Whenever the contents of the view changes, we need to recreate the endless scroll listener
     * as resetting it messes up how we use it. At some point we should probably investiage a proper
     * solution to this issue.
     */
    private void recreateOnScrollListener() {
        postsView.clearOnScrollListeners();
        infiniteScrollListener = new EndlessRecyclerOnScrollListener(footerAdapter) {
            @Override
            public void onLoadMore(final int currentPage) {
                Timber.v("onLoadMore() noMoreScrollback: " + noMoreScrollBack);
                if (!noMoreScrollBack) {
                    footerAdapter.clear();
                    footerAdapter.add(new ProgressItem().withEnabled(false));
                    postsManager.loadMorePosts();
                }
            }
        };
        postsView.addOnScrollListener(infiniteScrollListener);
    }

    private void onChannelSelected(long id) {
        Timber.v("onChannelSelected(): " + id);
        channel = channels.channels.get((int) id);

        // Clear the message adapter.
        footerAdapter.clear();
        postsAdapter.clear();

        recreateOnScrollListener();

        noMoreScrollBack = false;

        postsManager.setChannel(channel);
        membersManager.setChannel(channel);
    }

    private void handleChannelsEvent(final ChannelsEvent event) {
        Timber.v("handleChannelsEvent()");
        if (event.isApiError()) {
            APIError apiError = event.getApiError();
            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode + " with error id " + apiError.id);
            return;
        } else if (event.isError()) {
            // Unhandled error. Log it.
            Throwable e = event.getThrowable();
            Timber.e(e, e.getMessage());
            return;
        }

        // Success.
        this.channels = event.getChannels();

        for (Channel channel: channels.channels) {
            drawerAdapter.add(new PrimaryDrawerItem()
                    .withIdentifier(channels.channels.indexOf(channel))
                    .withName(channel.displayName));
        }
    }

    private void handlePostsReceivedEvent(final PostsReceivedEvent event) {
        Timber.v("handlePostsReceivedEvent()");
        if (event.isApiError()) {
            APIError apiError = event.getApiError();
            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode + " with error id " + apiError.id);
            return;
        } else if (event.isError()) {
            // Unhandled error. Log it.
            Throwable e = event.getThrowable();
            Timber.e(e, e.getMessage());
            return;
        }

        // Success.
        List<IItem> postItems = new ArrayList<>();
        Post previousPost = null;
        for (final Post post: event.getPosts()) {
            if (previousPost != null) {
                if (post.userId.equals(previousPost.userId)) {
                    postItems.add(new PostBasicSubItem(previousPost));
                } else {
                    postItems.add(new PostBasicTopItem(previousPost, profileImagePicasso));
                }
            }
            previousPost = post;
        }
        if (previousPost != null) {
            postItems.add(new PostBasicTopItem(previousPost, profileImagePicasso));
        }
        if (event.getPosts().size() == 0) {
            noMoreScrollBack = true;
        }
        postsAdapter.add(postItems);
        footerAdapter.clear();
    }

    private void handleMembersEvent(final MembersEvent event) {
        Timber.v("handleMembersEvent()");
        if (event.isApiError()) {
            APIError apiError = event.getApiError();
            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode + " with error id " + apiError.id);
            return;
        } else if (event.isError()) {
            // Unhandled error. Log it.
            Throwable e = event.getThrowable();
            Timber.e(e, e.getMessage());
            return;
        }

        // Success
        Timber.i("Members for channel retrieved: "+event.getMembersCount());
    }

    public static void launch(final Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }
}
