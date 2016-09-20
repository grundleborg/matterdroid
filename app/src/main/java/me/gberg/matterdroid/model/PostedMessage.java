package me.gberg.matterdroid.model;

import com.google.gson.Gson;

public class PostedMessage extends  WebSocketMessage {
    public static PostedMessage create(Gson gson, String payload) {
        PostedMessage message = gson.fromJson(payload, PostedMessage.class);
        message.parsedData = new Data();
        message.parsedData.channelDisplayName = message.data.get("channel_display_name");
        message.parsedData.channelType = message.data.get("channel_type");
        message.parsedData.post = gson.fromJson(message.data.get("post"), Post.class);
        message.parsedData.senderName = message.data.get("sender_name");
        message.parsedData.teamId = message.data.get("team_id");
        return message;
    }

    public static class Data {
        public String channelDisplayName;
        public String channelType;
        public Post post;
        public String senderName;
        public String teamId;
    }

    public Data parsedData;
}

