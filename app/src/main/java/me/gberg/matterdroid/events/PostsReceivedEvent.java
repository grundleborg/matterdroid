package me.gberg.matterdroid.events;

import java.util.List;

import me.gberg.matterdroid.model.APIError;
import me.gberg.matterdroid.model.Post;

public class PostsReceivedEvent extends APIEvent {
    private final List<Post> posts;

    public PostsReceivedEvent(Throwable throwable) {
        super(throwable);
        posts = null;
    }

    public PostsReceivedEvent(APIError apiError) {
        super(apiError);
        posts = null;
    }

    public PostsReceivedEvent(final List<Post> posts) {
        super();
        this.posts = posts;
    }

    public List<Post> getPosts() {
        return this.posts;
    }
}
