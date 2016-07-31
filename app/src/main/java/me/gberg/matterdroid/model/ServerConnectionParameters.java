package me.gberg.matterdroid.model;

/**
 * Created by gberg on 31/07/16.
 */
public class ServerConnectionParameters {
    public String server;
    public String token;

    public ServerConnectionParameters(final String server, final String token) {
        this.server = server;
        this.token = token;
    }
}
