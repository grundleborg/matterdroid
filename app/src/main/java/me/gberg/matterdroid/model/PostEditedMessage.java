package me.gberg.matterdroid.model;

import com.google.gson.Gson;

public class PostEditedMessage extends WebSocketMessage {
    public static PostEditedMessage create(Gson gson, String payload) {
        PostEditedMessage message = gson.fromJson(payload, PostEditedMessage.class);
        message.parsedProps = new Props();
        message.parsedProps.post = gson.fromJson(message.props.get("post"), Post.class);
        return message;
    }

    public static class Props {
        public Post post;
    }

    public Props parsedProps;
}

