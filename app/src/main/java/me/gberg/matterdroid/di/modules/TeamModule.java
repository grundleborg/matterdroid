package me.gberg.matterdroid.di.modules;

import dagger.Module;
import dagger.Provides;
import me.gberg.matterdroid.di.scopes.TeamScope;
import me.gberg.matterdroid.model.Team;

@Module
public class TeamModule {

    private final Team team;

    public TeamModule(Team team) {
        this.team = team;
    }

    @Provides
    @TeamScope
    Team providesTeam() {
        return team;
    }
}
