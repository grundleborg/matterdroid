package me.gberg.matterdroid.model;

import java.util.List;

public class Post {
    public String id;
    public long createAt;
    public long updateAt;
    public long deleteAt;
    public String userId;
    public String channelId;
    public String rootId;
    public String parentId;
    public String originalId;
    public String message;
    public String type;
    // public Properties props;
    public String hashtags;
    public List<String> filenames;
    public String pendingPostId;
}
