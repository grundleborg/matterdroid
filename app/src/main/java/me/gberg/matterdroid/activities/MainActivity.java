package me.gberg.matterdroid.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

public class MainActivity extends AppCompatActivity {

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

    Drawer drawer;

    private IItemAdapter<IDrawerItem> drawerAdapter;
    private Channels channels;

    private Channel channel;
    private FastItemAdapter<IItem> postsAdapter;

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
        postsView.setLayoutManager(new LinearLayoutManager(this));
        postsView.setItemAnimator(new DefaultItemAnimator());
        postsView.setAdapter(postsAdapter);

        channelsManager.loadChannels();

        // Load saved instance state.
        if (savedInstanceState != null) {
            Timber.v("SavedInstanceState found.");
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
        if (channel != null) {
            savedInstanceState.putString(STATE_CURRENT_CHANNEL, channel.id);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    private void onChannelSelected(long id) {
        channel = channels.channels.get((int) id);

        // Clear the message adapter.
        postsAdapter.clear();

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
        String lastUser = null;
        List<Post> posts = event.getPosts();
        ListIterator<Post> postsIterator = posts.listIterator(posts.size());
        while(postsIterator.hasPrevious()) {
            final Post post = postsIterator.previous();
            if (post.userId.equals(lastUser)) {
                postItems.add(new PostBasicSubItem(post));
            } else {
                postItems.add(new PostBasicTopItem(post, profileImagePicasso));
                lastUser = post.userId;
            }
        }
        postsAdapter.add(0, postItems);
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
