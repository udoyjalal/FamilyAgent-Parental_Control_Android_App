package com.example.familyagent;

import android.app.Application;

public class ParentalMonitorApplication extends Application {

    private SharedPreferencesHelper mHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mHelper = new SharedPreferencesHelper(this);
    }

    public SharedPreferencesHelper getSharedPreferencesHelper() {
        return mHelper;
    }

}

