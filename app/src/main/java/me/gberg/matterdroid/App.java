package me.gberg.matterdroid;

import android.app.Application;

import me.gberg.matterdroid.di.components.AppComponent;
import me.gberg.matterdroid.di.components.DaggerAppComponent;
import me.gberg.matterdroid.di.components.TeamComponent;
import me.gberg.matterdroid.di.components.UserComponent;
import me.gberg.matterdroid.di.modules.APIModule;
import me.gberg.matterdroid.di.modules.AppModule;
import me.gberg.matterdroid.di.modules.GsonModule;
import me.gberg.matterdroid.di.modules.TeamModule;
import me.gberg.matterdroid.di.modules.UserModule;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.model.User;
import timber.log.Timber;

public class App extends Application {

    private AppComponent appComponent;
    private UserComponent userComponent;
    private TeamComponent teamComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // In DEBUG builds, print all timber logging calls to the standard Android logging output.
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Timber.v("onCreate() called.");

        // Initialise Dagger Components
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .gsonModule(new GsonModule())
                .build();

    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

    public UserComponent createUserComponent(final User user) {
        userComponent = appComponent.newUserComponent(
                new UserModule(user),
                new APIModule());
        return userComponent;
    }

    public void releaseUserComponent() {
        this.userComponent = null;
    }

    public UserComponent getUserComponent() {
        return this.userComponent;
    }

    public TeamComponent createTeamComponent(final Team team) {
        teamComponent = userComponent.newTeamComponent(
                new TeamModule(team)
        );
        return teamComponent;
    }

    public void releaseTeamComponent() {
        this.teamComponent = null;
    }

    public TeamComponent getTeamComponent() {
        return this.teamComponent;
    }
}
