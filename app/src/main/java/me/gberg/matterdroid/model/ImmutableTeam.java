package me.gberg.matterdroid.model;

import com.google.gson.annotations.SerializedName;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ImmutableTeam {

    public abstract String id();

    public abstract String name();

    @SerializedName("display_name")
    public abstract String displayName();
}
