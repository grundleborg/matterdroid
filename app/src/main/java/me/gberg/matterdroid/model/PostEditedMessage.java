package me.gberg.matterdroid.model;

import com.google.gson.Gson;

public class PostEditedMessage extends WebSocketMessage {
    public static PostEditedMessage create(Gson gson, String payload) {
        PostEditedMessage message = gson.fromJson(payload, PostEditedMessage.class);
        message.parsedData = new Data();
        message.parsedData.post = gson.fromJson(message.data.get("post"), Post.class);
        return message;
    }

    public static class Data {
        public Post post;
    }

    public Data parsedData;
}

