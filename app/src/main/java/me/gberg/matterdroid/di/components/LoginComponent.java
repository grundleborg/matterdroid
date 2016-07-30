package me.gberg.matterdroid.di.components;

import javax.inject.Singleton;

import dagger.Component;
import me.gberg.matterdroid.activities.LoginActivity;
import me.gberg.matterdroid.di.modules.AppModule;
import me.gberg.matterdroid.di.modules.LoginModule;

@Singleton
@Component(modules={AppModule.class, LoginModule.class})
public interface LoginComponent {
    void inject(LoginActivity activity);
}
