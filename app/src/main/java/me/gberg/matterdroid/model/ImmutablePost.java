package me.gberg.matterdroid.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.immutables.gson.Gson;
import org.immutables.value.Value;
import org.tautua.markdownpapers.Markdown;
import org.tautua.markdownpapers.parser.ParseException;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import timber.log.Timber;

@Value.Immutable
public abstract class ImmutablePost {

    @Nullable
    public abstract String id();

    @SerializedName("create_at")
    public abstract long createAt();

    @SerializedName("update_at")
    @Value.Default
    public long updateAt() {
        return 0;
    };

    @SerializedName("delete_at")
    @Value.Default
    public long deleteAt() {
        return 0;
    };

    @SerializedName("user_id")
    public abstract String userId();

    @SerializedName("channel_id")
    public abstract String channelId();

    @Nullable
    @SerializedName("root_id")
    public abstract String rootId();

    @Nullable
    @SerializedName("parent_id")
    public abstract String parentId();

    @Nullable
    @SerializedName("original_id")
    public abstract String originalId();

    public abstract String message();

    @Nullable
    public abstract String type();

    // public Properties props;

    @Nullable
    public abstract String hashtags();

    public abstract List<String> filenames();

    @Nullable
    @SerializedName("pending_post_id")
    public abstract String pendingPostId();

    @Gson.Ignore
    @Value.Derived
    public String markdown() {
        Reader in = new StringReader(message());
        Writer out = new StringWriter();

        Markdown md = new Markdown();
        try {
            md.transform(in, out);
            return out.toString();
        } catch(ParseException e) {
            return message();
        }
    }

    @Gson.Ignore
    @Value.Default
    public boolean pending() {
        return false;
    };

    public boolean shouldStartNewPostBlock(final Post previousPost) {
        // No previous post.
        if (previousPost == null) {
            return true;
        }

        // Previous post is not an "ordinary" message.
        if (!previousPost.hasType(TYPE_ORDINARY)) {
            return true;
        }

        // This post is not an "ordinary" message.
        if (!hasType(TYPE_ORDINARY)) {
            return true;
        }

        // Different user on the previous post.
        if (!previousPost.userId().equals(this.userId())) {
            return true;
        }

        // Too much time past since last post.
        if (previousPost.createAt() + 300000 < this.createAt()) {
            return true;
        }

        return false;
    }

    public boolean hasType(final String type) {
        if (this.type() == null) {
            return type.equals(TYPE_ORDINARY);
        }

        return this.type().equals(type);
    }

    public final static String TYPE_ORDINARY = "";
    public final static String TYPE_JOIN_LEAVE = "system_join_leave";
    public final static String TYPE_HEADER_CHANGE = "system_header_change";
}
