package me.gberg.matterdroid;

import android.app.Application;

import timber.log.Timber;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // In DEBUG builds, print all timber logging calls to the standard Android logging output.
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
