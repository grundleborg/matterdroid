package me.gberg.matterdroid;

import android.app.Application;

import me.gberg.matterdroid.di.components.DaggerLoginComponent;
import me.gberg.matterdroid.di.components.LoginComponent;
import me.gberg.matterdroid.di.modules.AppModule;
import me.gberg.matterdroid.di.modules.LoginModule;
import timber.log.Timber;

public class App extends Application {

    private LoginComponent loginComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // In DEBUG builds, print all timber logging calls to the standard Android logging output.
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        // Initialise Dagger Components
        loginComponent = DaggerLoginComponent.builder()
                .appModule(new AppModule(this))
                .loginModule(new LoginModule())
                .build();
    }

    public LoginComponent getLoginComponent() {
        return loginComponent;
    }
}
