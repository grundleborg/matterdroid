package me.gberg.matterdroid.di.components;

import javax.inject.Singleton;

import dagger.Component;
import me.gberg.matterdroid.activities.LaunchActivity;
import me.gberg.matterdroid.activities.LoginActivity;
import me.gberg.matterdroid.di.modules.APIModule;
import me.gberg.matterdroid.di.modules.AppModule;
import me.gberg.matterdroid.di.modules.GsonModule;
import me.gberg.matterdroid.di.modules.UserModule;

@Singleton
@Component(modules = {AppModule.class, GsonModule.class})
public interface AppComponent {

    // Injectors
    void inject(LaunchActivity activity);
    void inject(LoginActivity activity);

    // Sub Components
    UserComponent newUserComponent(UserModule userModule, APIModule apiModule);
}
