package me.gberg.matterdroid.di.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import me.gberg.matterdroid.App;
import me.gberg.matterdroid.managers.SessionManager;
import me.gberg.matterdroid.settings.LoginSettings;
import me.gberg.matterdroid.utils.rx.Bus;

@Module
public class AppModule {

    private final App app;

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
    Bus providesBus() {
        return new Bus();
    }

    @Provides
    @Singleton
    LoginSettings providesLoginSettings(final App app) {
        return new LoginSettings(app);
    }

    @Provides
    @Singleton
    SessionManager providesSessioNManager(App app, LoginSettings loginSettings, Bus bus) {
        return new SessionManager(app, loginSettings, bus);
    }
}