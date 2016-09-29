package me.gberg.matterdroid.adapters.items;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fastadapter.utils.ViewHolderFactory;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.gberg.matterdroid.R;
import me.gberg.matterdroid.model.Post;
import me.gberg.matterdroid.utils.ui.HtmlTextView;
import me.gberg.matterdroid.utils.ui.HtmlTextViewLinkMovementMethod;

public class PostSystemTopItem extends PostItem<PostSystemTopItem, PostSystemTopItem.ViewHolder> {

    public PostSystemTopItem(final Post post) {
        super(post);
    }

    @Override
    public int getType() {
        return R.id.id_it_post_system_top;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.it_post_basic_top;
    }

    @Override
    public void bindView(ViewHolder viewHolder, List payloads) {
        super.bindView(viewHolder, payloads);

        viewHolder.username.setText(R.string.general_system_user_name);

        DateTime time = new DateTime(post.createAt());
        viewHolder.time.setText(time.toString(DateTimeFormat.forPattern("HH:mm")));

        viewHolder.body.setHtml(post.markdown(), viewHolder.imageGetter);

        viewHolder.userIcon.setImageResource(R.drawable.ic_mm_mono);
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.it_message_text)
        HtmlTextView body;

        @BindView(R.id.it_message_user_icon)
        ImageView userIcon;

        @BindView(R.id.it_post_basic_top_username)
        TextView username;

        @BindView(R.id.it_post_basic_top_time)
        TextView time;

        HtmlHttpImageGetter imageGetter;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            imageGetter = new HtmlHttpImageGetter(body);
            body.setMovementMethod(new HtmlTextViewLinkMovementMethod());
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
