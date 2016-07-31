package me.gberg.matterdroid;

import android.app.Application;

import me.gberg.matterdroid.di.components.AppComponent;
import me.gberg.matterdroid.di.components.DaggerAppComponent;
import me.gberg.matterdroid.di.components.DaggerLoginComponent;
import me.gberg.matterdroid.di.components.LoginComponent;
import me.gberg.matterdroid.di.components.UserComponent;
import me.gberg.matterdroid.di.modules.APIModule;
import me.gberg.matterdroid.di.modules.AppModule;
import me.gberg.matterdroid.di.modules.LoginModule;
import me.gberg.matterdroid.di.modules.UserModule;
import me.gberg.matterdroid.model.ServerConnectionParameters;
import me.gberg.matterdroid.model.User;
import timber.log.Timber;

public class App extends Application {

    private AppComponent appComponent;
    private LoginComponent loginComponent;
    private UserComponent userComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // In DEBUG builds, print all timber logging calls to the standard Android logging output.
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // Initialise Dagger Components
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        loginComponent = DaggerLoginComponent.builder()
                .appModule(new AppModule(this))
                .loginModule(new LoginModule())
                .build();
    }

    public LoginComponent getLoginComponent() {
        return loginComponent;
    }

    public UserComponent createUserComponent(final User user, final ServerConnectionParameters serverConnectionParameters) {
        userComponent = appComponent.newUserComponent(
                new UserModule(user, serverConnectionParameters),
                new APIModule());
        return userComponent;
    }

    public void releaseUserComponent() {
        this.userComponent = null;
    }

    public UserComponent getUserComponent() {
        return this.userComponent;
    }
}
