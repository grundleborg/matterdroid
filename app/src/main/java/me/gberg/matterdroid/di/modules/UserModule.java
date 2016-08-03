package me.gberg.matterdroid.di.modules;

import dagger.Module;
import dagger.Provides;
import me.gberg.matterdroid.di.scopes.UserScope;
import me.gberg.matterdroid.model.User;

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
}
