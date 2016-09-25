package me.gberg.matterdroid.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

import java.util.List;

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

    @Nullable
    @Gson.Ignore
    public abstract String markdown();

    @Gson.Ignore
    @Value.Default
    public boolean pending() {
        return false;
    };
}
