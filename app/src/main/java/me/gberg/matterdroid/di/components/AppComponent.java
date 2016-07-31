package me.gberg.matterdroid.di.components;

import javax.inject.Singleton;

import dagger.Component;
import me.gberg.matterdroid.di.modules.APIModule;
import me.gberg.matterdroid.di.modules.AppModule;
import me.gberg.matterdroid.di.modules.UserModule;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    // Sub Components
    UserComponent newUserComponent(UserModule userModule, APIModule apiModule);
}
