package me.gberg.matterdroid.model;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public abstract class ImmutableUsers {

    public abstract Map<String, User> users();
}
