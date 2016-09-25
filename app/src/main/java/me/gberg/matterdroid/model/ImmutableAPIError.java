package me.gberg.matterdroid.model;

import com.google.gson.annotations.SerializedName;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ImmutableAPIError {

    public abstract String id();

    public abstract String message();

    @SerializedName("detailed_error")
    public abstract String detailedError();

    @SerializedName("request_id")
    public abstract String requestId();

    @SerializedName("status_code")
    public abstract int statusCode();

    @SerializedName("is_oauth")
    public abstract boolean isOauth();

    public boolean is(final String id) {
        if (this.id().equals(id)) {
            return true;
        }
        return false;
    }

    public final static String LOGIN_UNRECOGNISED_EMAIL = "store.sql_user.get_for_login.app_error";
    public final static String LOGIN_WRONG_PASSWORD = "api.user.check_user_password.invalid.app_error";
}
