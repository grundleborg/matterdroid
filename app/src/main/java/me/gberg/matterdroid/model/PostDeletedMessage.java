package me.gberg.matterdroid.model;

import com.google.gson.Gson;

public class PostDeletedMessage extends WebSocketMessage {
    public static PostDeletedMessage create(Gson gson, String payload) {
        PostDeletedMessage message = gson.fromJson(payload, PostDeletedMessage.class);
        message.parsedData = new Data();
        message.parsedData.post = gson.fromJson(message.data.get("post"), Post.class);
        return message;
    }

    public static class Data {
        public Post post;
    }

    public Data parsedData;
}
