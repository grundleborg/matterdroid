package me.gberg.matterdroid.model;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class ImmutableChannels {

    public abstract List<Channel> channels();
}
