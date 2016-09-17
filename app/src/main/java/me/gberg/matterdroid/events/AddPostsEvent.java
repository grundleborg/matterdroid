package me.gberg.matterdroid.events;

import java.util.List;

import me.gberg.matterdroid.model.Post;

public class AddPostsEvent {
    private final List<Post> posts;
    private final int position;
    private final boolean scrollback;

    public AddPostsEvent(final List<Post> posts, final int position) {
        this.posts = posts;
        this.position = position;
        this.scrollback = false;
    }

    public AddPostsEvent(final List<Post> posts, final int position, final boolean scrollback) {
        this.posts = posts;
        this.position = position;
        this.scrollback = scrollback;
    }

    public final List<Post> getPosts() {
        return posts;
    }

    public final int getPosition() {
        return position;
    }

    public final boolean isScrollback() {
        return scrollback;
    }
}
