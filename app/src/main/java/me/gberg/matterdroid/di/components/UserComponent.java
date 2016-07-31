package me.gberg.matterdroid.di.components;

import android.app.Activity;

import dagger.Subcomponent;
import me.gberg.matterdroid.di.modules.UserModule;
import me.gberg.matterdroid.di.scopes.UserScope;

@UserScope
@Subcomponent(modules = {UserModule.class})
public interface UserComponent {
    void inject(Activity activity);
}
