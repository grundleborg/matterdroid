package me.gberg.matterdroid.model;

import com.google.gson.annotations.SerializedName;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Value.Immutable
public abstract class ImmutableChannel {

    public abstract String id();

    @SerializedName("team_id")
    public abstract String teamId();

    public abstract String type();

    @SerializedName("display_name")
    public abstract String displayName();

    public abstract String name();

    public boolean hasType(final String typeString) {
        return type().equals(typeString);
    }

    @Gson.Ignore
    @Value.Derived
    public List<String> directParticipants() {
        List<String> list = new ArrayList<>();
        if (hasType(TYPE_DIRECT)) {
            // Split the channel ID string on __
            list.addAll(Arrays.asList(name().split("__")));
        }
        return list;
    }

    public final static String TYPE_OPEN = "O";
    public final static String TYPE_PRIVATE = "P";
    public final static String TYPE_DIRECT = "D";
}
