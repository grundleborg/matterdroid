package me.gberg.matterdroid.utils.api;

public class HttpHeaders {
    public final static String AUTHORIZATION = "Authorization";
    public final static String TOKEN = "Token";
    public final static String PARAM_BEARER = "Bearer";

    public static String buildAuthorizationHeader(final String token) {
        return new StringBuilder()
                .append(PARAM_BEARER)
                .append(" ")
                .append(token)
                .toString();
    }
}
