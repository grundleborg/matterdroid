package me.gberg.matterdroid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;
import com.mikepenz.iconics.context.IconicsContextWrapper;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.gberg.matterdroid.App;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.di.components.TeamComponent;
import me.gberg.matterdroid.managers.SessionManager;
import me.gberg.matterdroid.presenters.MainActivityPresenter;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class MainActivity extends PresentedActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.co_main_messages_list)
    RecyclerView postsView;

    @BindView(R.id.co_main_new_message)
    EditText newMessageView;

    @BindView(R.id.co_main_send)
    ImageView sendView;

    @Inject
    MainActivityPresenter presenter;

    @Inject
    OkHttpClient httpClient;

    @Inject
    SessionManager sessionManager;

    public Drawer drawer;

    EndlessRecyclerOnScrollListener infiniteScrollListener;

    private boolean shouldAutoScroll = false;

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

        setContentView(R.layout.ac_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Set up the drawer.
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(final View view, final int position, final IDrawerItem drawerItem) {
                        return presenter.drawerItemClicked(drawer, position, drawerItem);
                    }
                })
                .build();

        // Connect to the presenter.
        presenter.takeView(this, savedInstanceState);
    }

    public void setupPostsView(final RecyclerView.Adapter adapter) {
        final LinearLayoutManager postsViewLayoutManager = new LinearLayoutManager(this);
        postsViewLayoutManager.setReverseLayout(true);
        postsView.setLayoutManager(postsViewLayoutManager);
        postsView.setItemAnimator(new DefaultItemAnimator());
        postsView.setAdapter(adapter);
        recreateOnScrollListener();
    }

    public void openDrawer() {
        drawer.openDrawer();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.me_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.me_main_about:
                new LibsBuilder()
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .start(this);
                return true;
            case R.id.me_main_change_team:
                presenter.leaveScope();
                sessionManager.changeTeam();
                ChooseTeamActivity.launch(this);
                finish();
                return true;
            case R.id.me_main_log_out:
                presenter.leaveScope();
                sessionManager.logOut();
                LoginActivity.launch(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @OnClick(R.id.co_main_send)
    void onSendClicked() {
        Timber.v("onSendClicked()");
        final CharSequence text = newMessageView.getText();
        presenter.sendMessage(text);
        newMessageView.setText(null);
    }

    /**
     * Whenever the contents of the view changes, we need to recreate the endless scroll listener
     * as resetting it messes up how we use it. At some point we should probably investiage a proper
     * solution to this issue.
     */
    public void recreateOnScrollListener() {
        postsView.clearOnScrollListeners();
        infiniteScrollListener = new EndlessRecyclerOnScrollListener(presenter.getFooterAdapter()) {
            @Override
            public void onLoadMore(final int currentPage) {
                presenter.loadMorePosts();
            }
        };
        postsView.addOnScrollListener(infiniteScrollListener);
    }

    private void onDrawerItemClicked(long id) {
        Timber.v("onChannelSelected(): " + id);

    }

    public void setTitle(final String title) {
        toolbar.setTitle(title);
    }

    public void checkShouldAutoScrollPostsView() {
        shouldAutoScroll = true;
        if (postsView.getAdapter().getItemCount() != 0) {
            int firstCompletelyVisibleItemPosition = ((LinearLayoutManager) postsView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            shouldAutoScroll = (firstCompletelyVisibleItemPosition == 0);
        }
    }

    public void autoScrollPostsView() {
        if (shouldAutoScroll) {
            postsView.scrollToPosition(0);
        }
        shouldAutoScroll = false;
    }

    public int getPostsListPosition() {
        return ((LinearLayoutManager) postsView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
    }

    public void setPostsListPosition(int position) {
        postsView.scrollToPosition(position);
    }

    public void scrollPostsListToPosition(int position) {
        postsView.smoothScrollToPosition(position);
    }

    public static void launch(final Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }
}
