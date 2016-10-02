package me.gberg.matterdroid.model;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public abstract class ImmutableWebSocketMessage {

    public abstract String event();

    public abstract Broadcast broadcast();

    public abstract Map<String, String> data();
}
