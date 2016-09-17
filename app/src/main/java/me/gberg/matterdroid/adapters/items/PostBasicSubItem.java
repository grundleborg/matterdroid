package me.gberg.matterdroid.adapters.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.model.Post;

public class PostBasicSubItem extends AbstractItem<PostBasicSubItem, PostBasicSubItem.ViewHolder> {
    private final Post post;

    public PostBasicSubItem(final Post post) {
        this.post = post;
    }

    @Override
    public int getType() {
        return R.id.id_it_post_basic_sub;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.it_post_basic_sub;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.body.setText(post.message);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.it_message_text)
        TextView body;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private static final ViewHolderFactory<? extends ViewHolder> FACTORY = new ItemFactory();

    protected static class ItemFactory implements ViewHolderFactory<ViewHolder> {
        public ViewHolder create(View v) {
            return new ViewHolder(v);
        }
    }

    @Override
    public ViewHolderFactory<? extends ViewHolder> getFactory() {
        return FACTORY;
    }
}
