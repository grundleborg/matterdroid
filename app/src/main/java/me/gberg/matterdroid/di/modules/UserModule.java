package me.gberg.matterdroid.di.modules;

import dagger.Module;
import dagger.Provides;
import me.gberg.matterdroid.di.scopes.UserScope;
import me.gberg.matterdroid.model.ServerConnectionParameters;
import me.gberg.matterdroid.model.User;

@Module
public class UserModule {

    private final User user;
    private final ServerConnectionParameters serverConnectionParameters;

    public UserModule(User user, ServerConnectionParameters serverConnectionParameters) {
        this.user = user;
        this.serverConnectionParameters = serverConnectionParameters;
    }

    @Provides
    @UserScope
    User providesUser() {
        return user;
    }

    @Provides
    @UserScope
    ServerConnectionParameters providesServerConnectionParameters() {
        return serverConnectionParameters;
    }
}
