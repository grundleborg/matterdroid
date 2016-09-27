package me.gberg.matterdroid.model;

import com.google.gson.annotations.SerializedName;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ImmutableChannel {

    public abstract String id();

    @SerializedName("team_id")
    public abstract String teamId();

    public abstract String type();

    @SerializedName("display_name")
    public abstract String displayName();

    public boolean hasType(final String typeString) {
        return type().equals(typeString);
    }

    public final static String TYPE_OPEN = "O";
    public final static String TYPE_PRIVATE = "P";
    public final static String TYPE_DIRECT = "D";
}
