package com.tokinonagare.fakewechatlocation;

import android.content.Context;
import android.content.SharedPreferences;

import de.robv.android.xposed.XSharedPreferences;

/**
 * Created by tokinonagare on 1/25/17.
 */

public class SharedLocationData {
    private static final String TAG = "Debug";
    private SharedPreferences sharedPreferences = null;
    private XSharedPreferences xSharedPreferences = null;

    public SharedLocationData() {
        xSharedPreferences = new XSharedPreferences("com.tokinonagare.fakewechatlocation");
        xSharedPreferences.makeWorldReadable();
        this.reload();
    }

    public SharedLocationData(Context context) {
        this.sharedPreferences = context.getSharedPreferences("com.tokinonagare.fakewechatlocation_preferences", 1);
    }

    public String getString(String key, String defaultValue) {
        if (xSharedPreferences != null) {
            return xSharedPreferences.getString(key, defaultValue);
        }

        return defaultValue;
    }

    public String getString(String key) {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(key, "");
        }

        return "";
    }

    public void setString(String key, String value) {
        if (sharedPreferences != null) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public void reload() {
        if (xSharedPreferences != null) {
            xSharedPreferences.reload();
        }
    }
}
