package me.gberg.matterdroid.model;

/**
 * Created by gberg on 30/07/16.
 */
public class APIError {
    public String id;
    public String message;
    public String detailedError;
    public String requestId;
    public int statusCode;
    public boolean isOauth;

    public boolean is(final String id) {
        if (this.id.equals(id)) {
            return true;
        }
        return false;
    }

    public final static String LOGIN_UNRECOGNISED_EMAIL = "store.sql_user.get_for_login.app_error";
    public final static String LOGIN_WRONG_PASSWORD = "api.user.check_user_password.invalid.app_error";
}
