package com.iyr.ian.utils.bluetooth.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public abstract class IntPreference {
    private final SharedPreferences preferences;
    private final String key;
    private final int defaultValue;

    protected IntPreference(Context context,
                            String key,
                            int defaultValue) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public int get() {
        return preferences.getInt(key, defaultValue);
    }

    public void set(int value) {
        preferences.edit().putInt(key, value).apply();
    }

}
