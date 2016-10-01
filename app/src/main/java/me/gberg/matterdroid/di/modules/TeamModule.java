package me.gberg.matterdroid.di.modules;

import com.google.gson.Gson;

import dagger.Module;
import dagger.Provides;
import me.gberg.matterdroid.api.TeamAPI;
import me.gberg.matterdroid.api.UserAPI;
import me.gberg.matterdroid.di.scopes.TeamScope;
import me.gberg.matterdroid.managers.ChannelsManager;
import me.gberg.matterdroid.managers.MembersManager;
import me.gberg.matterdroid.managers.PostsManager;
import me.gberg.matterdroid.managers.SessionManager;
import me.gberg.matterdroid.managers.UsersManager;
import me.gberg.matterdroid.managers.WebSocketManager;
import me.gberg.matterdroid.model.Team;
import me.gberg.matterdroid.utils.retrofit.ErrorParser;
import me.gberg.matterdroid.utils.rx.TeamBus;
import retrofit2.Retrofit;

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

    @Provides
    @TeamScope
    TeamAPI providesTeamApi(Retrofit retrofit) {
        return retrofit.create(TeamAPI.class);
    }

    @Provides
    @TeamScope
    TeamBus teamBus() {
        return new TeamBus();
    }

    @Provides
    @TeamScope
    ChannelsManager providesChannelsManager(TeamBus bus, Team team, TeamAPI teamApi, ErrorParser errorParser) {
        return new ChannelsManager(bus, team, teamApi, errorParser);
    }

    @Provides
    @TeamScope
    PostsManager providesPostsManager(TeamBus bus, Team team, TeamAPI teamApi, SessionManager sessionManager, ErrorParser errorParser) {
        return new PostsManager(bus, team, teamApi, sessionManager, errorParser);
    }

    @Provides
    @TeamScope
    MembersManager providesMembersManager(TeamBus bus, Team team, TeamAPI teamAPI, ErrorParser errorParser) {
        return new MembersManager(errorParser, bus, team, teamAPI);
    }

    @Provides
    @TeamScope
    WebSocketManager providesWebSocketManager(TeamBus bus, Team team, Gson gson,
                                              SessionManager sessionManager, ErrorParser errorParser) {
        return new WebSocketManager(bus, team, gson, sessionManager, errorParser);
    }

    @Provides
    @TeamScope
    UsersManager providesUsersManager(Team team, UserAPI userAPI, TeamBus bus, ErrorParser errorParser) {
        return new UsersManager(team, userAPI, bus, errorParser);
    }
}
