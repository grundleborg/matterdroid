package me.gberg.matterdroid.di.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.gberg.matterdroid.App;
import me.gberg.matterdroid.settings.LoginSettings;

@Module
public class AppModule {

    App app;

    public AppModule(final App app) {
        this.app = app;
    }

    @Provides
    @Singleton
    App providesApp() {
        return app;
    }

    @Provides
    @Singleton
    LoginSettings providesLoginSettings(final App app) {
        return new LoginSettings(app);
    }
}