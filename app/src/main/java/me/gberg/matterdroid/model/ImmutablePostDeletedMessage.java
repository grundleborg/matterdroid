package me.gberg.matterdroid.model;

import com.google.gson.Gson;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ImmutablePostDeletedMessage implements IWebSocketMessage {
    public static PostDeletedMessage create(Gson gson, WebSocketMessage message) {
        Post post = gson.fromJson(message.data().get("post"), Post.class);
        return PostDeletedMessage.builder()
                .setParsedData(PostDeletedMessageData.builder()
                        .setPost(post)
                        .build())
                .setBroadcast(message.broadcast())
                .build();
    }

    @Value.Immutable
    public abstract static class ImmutablePostDeletedMessageData {
        public abstract Post post();
    }

    public abstract Broadcast broadcast();

    public abstract PostDeletedMessageData parsedData();
}
