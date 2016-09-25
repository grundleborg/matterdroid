package me.gberg.matterdroid.model;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class ImmutableInitialLoad {

    public abstract User user();

    public abstract List<Team> teams();
}
