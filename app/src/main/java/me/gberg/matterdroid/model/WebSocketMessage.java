package me.gberg.matterdroid.model;

import java.util.Map;

public class WebSocketMessage {
    public String teamId;
    public String channelId;
    public String userId;
    public String action;
    public Map<String, String> props;
}
