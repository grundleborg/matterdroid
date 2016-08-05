package me.gberg.matterdroid.adapters.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mikepenz.fastadapter.items.AbstractItem;
import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.model.Post;

public class PostItem extends AbstractItem<PostItem, PostItem.ViewHolder> {
    private final Post post;

    public PostItem(final Post post) {
        this.post = post;
    }

    @Override
    public int getType() {
        return R.id.id_it_post;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.it_post;
    }

    @Override
    public void bindView(ViewHolder viewHolder) {
        super.bindView(viewHolder);

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
