package me.gberg.matterdroid.model;

import com.google.gson.annotations.SerializedName;

import org.immutables.value.Value;

import java.util.Map;

@Value.Immutable
public abstract class ImmutableWebSocketMessage {

    @SerializedName("team_id")
    public abstract String teamId();

    @SerializedName("channel_id")
    public abstract String channelId();

    @SerializedName("user_id")
    public abstract String userId();

    public abstract String event();

    public abstract Map<String, String> data();
}
