package me.gberg.matterdroid.model;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ImmutableUser {

    public abstract String id();

    public abstract String username();
}
