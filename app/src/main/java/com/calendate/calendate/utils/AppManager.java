package com.calendate.calendate.utils;

import android.support.multidex.MultiDexApplication;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.firebase.database.FirebaseDatabase;

public class AppManager extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
