package me.gberg.matterdroid.events;

import java.util.List;

import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Team;

public class TeamsListEvent extends APIEvent {
    private final List<Team> teams;

    public TeamsListEvent(Throwable throwable) {
        super(throwable);
        teams = null;
    }

    public TeamsListEvent(APIError apiError) {
        super(apiError);
        teams = null;
    }

    public TeamsListEvent(final List<Team> teams) {
        super();
        this.teams = teams;
    }

    public List<Team> getTeams() {
        return this.teams;
    }
}
