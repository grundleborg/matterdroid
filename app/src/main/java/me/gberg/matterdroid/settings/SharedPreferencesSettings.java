package me.gberg.matterdroid.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesSettings {

    private final SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    protected SharedPreferencesSettings(final Context context, final String fileName, final int mode) {
        prefs = context.getSharedPreferences(fileName, mode);
    }

    protected void startEditing() {
        editor = prefs.edit();
    }

    protected void finishEditing() {
        editor.commit();
        editor = null;
    }

    protected void putString(final String key, final String value) {
        editor.putString(key, value);
    }

    protected String getString(final String key, final String defaultValue) {
        return prefs.getString(key, defaultValue);
    }
}
