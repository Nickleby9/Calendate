package com.calendate.calendate;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();

        //can't save to db... no user yet.
        SharedPreferences prefs = getSharedPreferences("id", MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("token", token);
        //
        editor.commit();
    }
}