package me.gberg.matterdroid.di.components;

import dagger.Subcomponent;
import me.gberg.matterdroid.activities.MainActivity;
import me.gberg.matterdroid.di.modules.TeamModule;
import me.gberg.matterdroid.di.scopes.TeamScope;

@TeamScope
@Subcomponent(modules = {TeamModule.class})
public interface TeamComponent {
    void inject(MainActivity activity);
}
