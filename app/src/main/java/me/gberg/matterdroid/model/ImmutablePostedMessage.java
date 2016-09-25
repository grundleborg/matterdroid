package me.gberg.matterdroid.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ImmutablePostedMessage implements IWebSocketMessage {
    public static PostedMessage create(Gson gson, WebSocketMessage message) {
        Post post = gson.fromJson(message.data().get("post"), Post.class);
        return PostedMessage.builder()
                .setParsedData(PostedMessageData.builder()
                        .setChannelDisplayName(message.data().get("channel_display_name"))
                        .setChannelType(message.data().get("channel_type"))
                        .setPost(post)
                        .setSenderName(message.data().get("sender_name"))
                        .setTeamId(message.data().get("team_id"))
                        .build())
                .setWebSocketMessage(message)
                .build();
    }

    @Value.Immutable
    public abstract static class ImmutablePostedMessageData {

        @SerializedName("channel_display_name")
        public abstract String channelDisplayName();

        @SerializedName("channel_type")
        public abstract String channelType();

        public abstract Post post();

        @SerializedName("sender_name")
        public abstract String senderName();

        @SerializedName("team_id")
        public abstract String teamId();
    }

    public abstract WebSocketMessage webSocketMessage();

    public abstract PostedMessageData parsedData();
}
