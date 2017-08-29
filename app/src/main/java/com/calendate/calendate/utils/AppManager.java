package com.calendate.calendate.utils;

import android.app.Application;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.google.firebase.database.FirebaseDatabase;

public class AppManager extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
