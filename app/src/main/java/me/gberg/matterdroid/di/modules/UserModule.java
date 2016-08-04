package me.gberg.matterdroid.di.modules;

import dagger.Module;
import dagger.Provides;
import me.gberg.matterdroid.api.UserAPI;
import me.gberg.matterdroid.di.scopes.UserScope;
import me.gberg.matterdroid.managers.TeamsManager;
import me.gberg.matterdroid.model.User;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.Bus;

@Module
public class UserModule {

    private final User user;

    public UserModule(User user) {
        this.user = user;
    }

    @Provides
    @UserScope
    User providesUser() {
        return user;
    }

    @Provides
    @UserScope
    TeamsManager providesTeamsManager(ErrorParser errorParser, Bus bus, UserAPI userAPI) {
        return new TeamsManager(errorParser, bus, userAPI);
    }
}
