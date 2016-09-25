package me.gberg.matterdroid.model;

import com.google.gson.annotations.SerializedName;

import org.immutables.value.Value;

import java.util.List;

@Value.Immutable
public abstract class ImmutableExtraInfo {

    public abstract String id();

    public abstract List<Member> members();

    @SerializedName("member_count")
    public abstract int memberCount();
}
