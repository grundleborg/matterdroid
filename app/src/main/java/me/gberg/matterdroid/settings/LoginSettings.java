package me.gberg.matterdroid.settings;

import android.content.Context;

import me.gberg.matterdroid.App;
import me.gberg.matterdroid.BuildConfig;
import me.gberg.matterdroid.model.ServerConnectionParameters;
import timber.log.Timber;

public class LoginSettings extends SharedPreferencesSettings {

    public LoginSettings(App app) {
        super(app, FILE, Context.MODE_PRIVATE);
        Timber.v("Shared preferences file: " + FILE);
    }

    public void setServer(final String server) {
        startEditing();
        putString(KEY_SERVER, server);
        finishEditing();
    }

    public void setEmail(final String email) {
        startEditing();
        putString(KEY_EMAIL, email);
        finishEditing();
    }

    public void setToken(final String token) {
        startEditing();
        putString(KEY_TOKEN, token);
        finishEditing();
    }

    public String getServer() {
        return getString(KEY_SERVER, null);
    }

    public String getEmail() {
        return getString(KEY_EMAIL, null);
    }

    public String getToken() {
        return getString(KEY_TOKEN, null);
    }

    public ServerConnectionParameters getServerConnectionParameters() {
        return new ServerConnectionParameters(getServer(), getToken());

    }

    private final static String FILE = BuildConfig.APPLICATION_ID + ".settings.LoginSettings";
    private final static String KEY_SERVER = "server";
    private final static String KEY_EMAIL = "email";
    private final static String KEY_TOKEN = "token";
}
