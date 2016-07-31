package me.gberg.matterdroid.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.fastadapter.adapters.FastItemAdapter;
import com.mikepenz.iconics.context.IconicsContextWrapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.gberg.matterdroid.App;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.adapters.items.ChooseTeamTeamItem;
import me.gberg.matterdroid.api.UserAPI;
import me.gberg.matterdroid.di.components.UserComponent;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.InitialLoad;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ChooseTeamActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.co_choose_team_list)
    RecyclerView chooseTeamList;

    @Inject
    UserAPI userAPI;

    @Inject
    ErrorParser errorParser;

    private FastItemAdapter teamsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate() called");

        // Redirect to the login activity if we can't inject the UserComponent.
        UserComponent userComponent = ((App) getApplication()).getUserComponent();
        if (userComponent == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        userComponent.inject(this);

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

        Observable<Response<InitialLoad>> initialLoadObservable = userAPI.initialLoad();
        initialLoadObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response<InitialLoad>>() {
                    @Override
                    public void onCompleted() {
                        Timber.v("Completed.");
                    }

                    @Override
                    public void onError(final Throwable e) {
                        // Unhandled error. Log it.
                        Timber.e(e, e.getMessage());
                    }

                    @Override
                    public void onNext(final Response<InitialLoad> response) {

                        // Handle HTTP Response errors.
                        if (!response.isSuccessful()) {
                            APIError apiError = errorParser.parseError(response);
                            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode + " with error id " + apiError.id);
                            return;
                        }

                        // Request is successful.
                        onInitialLoadCompletedSuccessfully(response.body());
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    void onInitialLoadCompletedSuccessfully(InitialLoad initialLoad) {
        Timber.v("onInitialLoadCompletedSuccessfully() called.");

        // Build a list of Team items for the adapter.
        List<ChooseTeamTeamItem> teamItems = new ArrayList<ChooseTeamTeamItem>();
        for (Team team: initialLoad.teams) {
            teamItems.add(new ChooseTeamTeamItem(team));
        }

        // Update what's shown in the adapter.
        teamsAdapter.set(teamItems);
    }

}
