package me.gberg.matterdroid.model;

import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@Value.Immutable
public abstract class ImmutablePosts {

    public abstract List<String> order();

    public abstract Map<String, Post> posts();
}
