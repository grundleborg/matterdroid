package me.gberg.matterdroid.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.iconics.context.IconicsContextWrapper;
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
import me.gberg.matterdroid.adapters.items.ChooseTeamTeamItem;
import me.gberg.matterdroid.di.components.UserComponent;
import me.gberg.matterdroid.events.TeamsListEvent;
import me.gberg.matterdroid.managers.SessionManager;
import me.gberg.matterdroid.managers.TeamsManager;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.utils.rx.Bus;
import rx.functions.Action1;
import timber.log.Timber;

public class ChooseTeamActivity extends NaviAppCompatActivity {

    private final LifecycleProvider<ActivityEvent> lifecycleProvider
            = NaviLifecycle.createActivityLifecycleProvider(this);

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.co_choose_team_list)
    RecyclerView chooseTeamList;

    @Inject
    Bus bus;

    @Inject
    SessionManager sessionManager;

    @Inject
    TeamsManager teamsManager;

    private FastItemAdapter teamsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate() called");

        // Redirect to the login activity if we can't inject the UserComponent.
        UserComponent userComponent = ((App) getApplication()).getUserComponent();
        if (userComponent == null) {
            LoginActivity.launch(this);
            finish();
            return;
        }
        userComponent.inject(this);

        // Subscribe to the event bus.
        bus.toObserverable()
                .compose(lifecycleProvider.bindToLifecycle())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {
                        if (event instanceof TeamsListEvent) {
                            handleTeamsListEvent((TeamsListEvent) event);
                        }
                    }
                });

        setContentView(R.layout.ac_choose_team);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Implement me!
            }
        });

        chooseTeamList.setItemAnimator(new DefaultItemAnimator());
        chooseTeamList.setLayoutManager(new LinearLayoutManager(this));

        teamsAdapter = new FastItemAdapter();
        chooseTeamList.setAdapter(teamsAdapter);

        teamsAdapter.withOnClickListener(new FastAdapter.OnClickListener() {
            @Override
            public boolean onClick(final View v, final IAdapter adapter, final IItem item, final int position) {
                ChooseTeamTeamItem teamItem = (ChooseTeamTeamItem) item;

                Team team = teamItem.getTeam();

                sessionManager.setTeam(team);
                MainActivity.launch(ChooseTeamActivity.this);
                finish();

                return true;
            }
        });

        // Get the teams.
        teamsManager.loadAvailableTeams();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    private void handleTeamsListEvent(final TeamsListEvent event) {
        if (event.isApiError()) {
            APIError apiError = event.getApiError();
            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode() + " with error id " + apiError.id());
            return;
        }

        if (event.isError()) {
            // Unhandled error. Log it.
            Throwable e = event.getThrowable();
            Timber.e(e, e.getMessage());
            return;
        }

        List<Team> teams = event.getTeams();

        // Build a list of Team items for the adapter.
        List<ChooseTeamTeamItem> teamItems = new ArrayList<ChooseTeamTeamItem>();
        for (Team team: teams) {
            teamItems.add(new ChooseTeamTeamItem(team));
        }

        // Update what's shown in the adapter.
        teamsAdapter.set(teamItems);
    }

    public static void launch(final Activity activity) {
        Intent intent = new Intent(activity, ChooseTeamActivity.class);
        activity.startActivity(intent);
    }
}
