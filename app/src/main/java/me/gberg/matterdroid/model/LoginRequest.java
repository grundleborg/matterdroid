package me.gberg.matterdroid.model;

/**
 * Created by gberg on 29/07/16.
 */
public class LoginRequest {

    public String loginId;
    public String password;
    public String token;

    public LoginRequest(final String loginId, final String password, final String token) {
        this.loginId = loginId;
        this.password = password;
        this.token = token;
    }
}
