package me.gberg.matterdroid.di.components;

import dagger.Subcomponent;
import me.gberg.matterdroid.activities.ChooseTeamActivity;
import me.gberg.matterdroid.di.modules.APIModule;
import me.gberg.matterdroid.di.modules.UserModule;
import me.gberg.matterdroid.di.scopes.UserScope;

@UserScope
@Subcomponent(modules = {UserModule.class, APIModule.class})
public interface UserComponent {
    void inject(ChooseTeamActivity activity);
}
