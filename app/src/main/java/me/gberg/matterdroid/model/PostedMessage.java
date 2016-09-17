package me.gberg.matterdroid.model;

import com.google.gson.Gson;

public class PostedMessage extends  WebSocketMessage {
    public static PostedMessage create(Gson gson, String payload) {
        PostedMessage message = gson.fromJson(payload, PostedMessage.class);
        message.parsedProps = new Props();
        message.parsedProps.channelDisplayName = message.props.get("channel_display_name");
        message.parsedProps.channelType = message.props.get("channel_type");
        message.parsedProps.post = gson.fromJson(message.props.get("post"), Post.class);
        message.parsedProps.senderName = message.props.get("sender_name");
        message.parsedProps.teamId = message.props.get("team_id");
        return message;
    }

    public static class Props {
        public String channelDisplayName;
        public String channelType;
        public Post post;
        public String senderName;
        public String teamId;
    }

    public Props parsedProps;
}

