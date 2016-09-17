package me.gberg.matterdroid.model;

import com.google.gson.Gson;

public class PostDeletedMessage extends WebSocketMessage {
    public static PostDeletedMessage create(Gson gson, String payload) {
        PostDeletedMessage message = gson.fromJson(payload, PostDeletedMessage.class);
        message.parsedProps = new Props();
        message.parsedProps.post = gson.fromJson(message.props.get("post"), Post.class);
        return message;
    }

    public static class Props {
        public Post post;
    }

    public Props parsedProps;
}
