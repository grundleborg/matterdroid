package me.gberg.matterdroid.model;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ImmutableMember {

    public abstract String id();

    public abstract String nickname();

    public abstract String email();

    public abstract String roles();

    public abstract String username();
}
