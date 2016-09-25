package me.gberg.matterdroid.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ImmutableLoginRequest {

    @SerializedName("login_id")
    public abstract String loginId();

    public abstract String password();

    @Nullable
    public abstract String token();
}
