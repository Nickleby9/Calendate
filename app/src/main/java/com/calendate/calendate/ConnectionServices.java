package com.calendate.calendate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

class ConnectionServices {

    static void signInWithGoogle(){

    }

    static void sinInWithFacebook(){

    }

    static GoogleSignInOptions initGoogleSignInOptions(Context context) {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    }

    static GoogleApiClient initGoogleApiClient(AppCompatActivity activity, GoogleSignInOptions gso) {
        return new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    static GoogleApiClient initGoogleApiClient(FragmentActivity activity, Context context, int i, GoogleSignInOptions gso) {
        return new GoogleApiClient.Builder(context)
                .enableAutoManage(activity /* FragmentActivity */,i, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    static CallbackManager initCallbackManager(){
        return CallbackManager.Factory.create();
    }
}
