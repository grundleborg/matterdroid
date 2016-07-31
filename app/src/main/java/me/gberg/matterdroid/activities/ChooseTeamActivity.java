package me.gberg.matterdroid.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import javax.inject.Inject;

import me.gberg.matterdroid.App;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.di.components.UserComponent;
import me.gberg.matterdroid.model.ServerConnectionParameters;
import me.gberg.matterdroid.model.User;
import timber.log.Timber;

public class ChooseTeamActivity extends AppCompatActivity {

    @Inject
    User user;

    @Inject
    ServerConnectionParameters serverConnectionParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate() called");

        UserComponent userComponent = ((App) getApplication()).getUserComponent();
        if (userComponent == null) {
            // TODO: Launch the login activity, and end this activity.
            return;
        }
        userComponent.inject(this);

        setContentView(R.layout.ac_choose_team);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
