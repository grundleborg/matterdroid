package me.gberg.matterdroid.model;

import com.google.gson.annotations.SerializedName;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ImmutableBroadcast {

    @SerializedName("team_id")
    public abstract String teamId();

    @SerializedName("channel_id")
    public abstract String channelId();

    @SerializedName("user_id")
    public abstract String userId();
}
