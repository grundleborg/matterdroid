package me.gberg.matterdroid.adapters.items;

import android.support.v7.widget.RecyclerView;

import com.mikepenz.fastadapter.items.AbstractItem;

import me.gberg.matterdroid.model.Post;

public abstract class PostItem<Item extends AbstractItem<?, ?>, VH extends RecyclerView.ViewHolder> extends AbstractItem<Item, VH> {
    protected Post post;
    public PostItem(final Post post) {
        this.post = post;
    }

    public final Post getPost() {
        return post;
    }
}
