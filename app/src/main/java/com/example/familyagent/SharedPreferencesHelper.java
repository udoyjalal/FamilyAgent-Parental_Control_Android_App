package com.example.familyagent;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferencesHelper {
    private final String EMAIL_ADDRESS = "SharedPreferencesHelper.EMAIL_ADDRESS";
    private final String INTERVAL_TIME = "SharedPreferencesHelper.INTERVAL_TIME";
    private SharedPreferences mPreferences;

    public SharedPreferencesHelper(Context mContext) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public void saveEmailAddress(String email) {
        final SharedPreferences.Editor mEditor = mPreferences.edit();
        mEditor.putString(EMAIL_ADDRESS, email);
        mEditor.apply();
    }

    public String getEmailAddress() {
        return mPreferences.getString(EMAIL_ADDRESS, "");
    }

    public void saveIntervalTime(int time) {
        final SharedPreferences.Editor mEditor = mPreferences.edit();
        mEditor.putInt(INTERVAL_TIME, time);
        mEditor.apply();
    }

    public int getIntervalTime() {
        return mPreferences.getInt(INTERVAL_TIME, -1);
    }

}

