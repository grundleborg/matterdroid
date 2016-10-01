package me.gberg.matterdroid.activities;

import android.os.Bundle;

import com.google.gson.Gson;
import com.trello.navi.component.support.NaviAppCompatActivity;
import com.trello.rxlifecycle.LifecycleProvider;
import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.navi.NaviLifecycle;

import javax.inject.Inject;

import me.gberg.matterdroid.App;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.events.TokenCheckEvent;
import me.gberg.matterdroid.managers.SessionManager;
import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.utils.rx.Bus;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import timber.log.Timber;

public class LaunchActivity extends NaviAppCompatActivity {

    private final LifecycleProvider<ActivityEvent> lifecycleProvider
            = NaviLifecycle.createActivityLifecycleProvider(this);

    @Inject
    App app;

    @Inject
    Bus bus;

    @Inject
    Gson gson;

    @Inject
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.v("onCreate() called.");
        setContentView(R.layout.ac_launch);

        ((App) getApplication()).getAppComponent().inject(this);

        // Subscribe to the event bus.
        bus.toObserverable()
                .compose(lifecycleProvider.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object event) {

                        if (event instanceof TokenCheckEvent) {
                            handleTokenCheckEvent((TokenCheckEvent) event);
                        }
                    }
                });

        // Determine from the session state what to do.
        if (sessionManager.isLoggedIn()) {
            if (sessionManager.isTeamSelected()) {
                // Logged in and team selected. Go straight to the main activity.
                MainActivity.launch(this);
                finish();
                return;
            } else {
                // Logged in but no team selected. Go to the Select Team activity.
                ChooseTeamActivity.launch(this);
                finish();
                return;
            }
        } else if (sessionManager.hasToken()) {
            // We have credentials to login, but we aren't logged in. Attempt the login.
            sessionManager.attemptToken();
        } else {
            // We don't have the all necessary credentials to login, so go to the Login activity.
            LoginActivity.launch(this);
            finish();
            return;
        }
    }

    final void handleTokenCheckEvent(final TokenCheckEvent event) {

        if (event.isApiError()) {
            // TODO: Display a visual indication of the API error.
            APIError apiError = event.getApiError();
            Timber.e("Unrecognised HTTP response code: " + apiError.statusCode() + " with error id " + apiError.id());
            return;

        } else if (event.isError()) {
            // Unhandled error. Log it.
            Throwable e = event.getThrowable();
            Timber.e(e, e.getMessage());
            LoginActivity.launch(this);
            finish();
            return;
        }

        // Success.
        // TODO: If team has already been chosen, go straight to the main activity.
        sessionManager.setUser(event.getUser());
        ChooseTeamActivity.launch(this);
        finish();
    }
}
