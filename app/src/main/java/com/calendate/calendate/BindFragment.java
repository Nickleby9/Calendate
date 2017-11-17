package com.calendate.calendate;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.calendate.calendate.models.User;
import com.calendate.calendate.utils.CustomBootstrapStyle;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class BindFragment extends Fragment implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_GOOGLE_LOGIN = 1;
    BootstrapButton btnBind;
    EditText etUsername, etEmail, etPassword, etPassword2;
    TextView tvError, tvTerms;
    FirebaseUser user;
    FirebaseDatabase mDatabase;
    OnNotAnonymousListener mListener;
    FirebaseAuth mAuth;
    GoogleApiClient mApiClient;
    CallbackManager mCallbackManager;
    Button btnGoogle, btnFacebook;
    boolean acceptedTerms = false;
    CheckBox cbTerms;
    OnBindFinishedListener mFinishedListener;


    public BindFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bind, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnBind = (BootstrapButton) view.findViewById(R.id.btnBind);
        etUsername = (EditText) view.findViewById(R.id.etUsername);
        etEmail = (EditText) view.findViewById(R.id.etEmail);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        etPassword2 = (EditText) view.findViewById(R.id.etPassword2);
        tvTerms = (TextView) view.findViewById(R.id.tvTerms);
        tvError = (TextView) view.findViewById(R.id.tvError);
        btnGoogle = (Button) view.findViewById(R.id.btnGoogle);
        btnFacebook = (Button) view.findViewById(R.id.btnFacebook);
        cbTerms = (CheckBox) view.findViewById(R.id.cbTerms);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = ConnectionServices.initGoogleSignInOptions(getContext());

        mApiClient = ConnectionServices.initGoogleApiClient(getActivity(), getContext(), 1, gso);


        mCallbackManager = ConnectionServices.initCallbackManager();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });


        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && !user.isAnonymous()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.oops)
                    .setMessage(R.string.anonymous_register_error)
                    .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            mListener.onNotAnonymous();
                        }
                    })
                    .setCancelable(false)
                    .show();

        }

        btnBind.setBootstrapBrand(new CustomBootstrapStyle(getContext()));
        btnBind.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
        btnFacebook.setOnClickListener(this);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        linkAndSignIn(credential);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        AuthCredential credential = null;
        switch (id) {
            case R.id.btnGoogle:
                if (cbTerms.isChecked()) {
                    showProgress(true, getString(R.string.google_login_msg));
                    Intent googleIntent = Auth.GoogleSignInApi
                            .getSignInIntent(mApiClient);
                    startActivityForResult(googleIntent, RC_GOOGLE_LOGIN);
                } else {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
                    builder.setTitle("")
                            .setMessage(R.string.accept_terms_request)
                            .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                }
                break;
            case R.id.btnFacebook:
                if (cbTerms.isChecked()) {
                    showProgress(true, getString(R.string.facebook_login_msg));
                    LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
                } else {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
                    builder.setTitle("")
                            .setMessage(R.string.accept_terms_request)
                            .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                }
                break;
            case R.id.btnBind:
                if (cbTerms.isChecked()) {
                    credential = EmailAuthProvider.getCredential(
                            etEmail.getText().toString(), etPassword.getText().toString());
                    if (mAuth.getCurrentUser() != null) {
                        mAuth.getCurrentUser().linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                UserProfileChangeRequest change = new UserProfileChangeRequest.Builder().setDisplayName(etUsername.getText().toString()).build();
                                User user = new User(authResult.getUser());
                                mDatabase.getReference("users").child(user.getUid()).removeValue();
                                mDatabase.getReference("users").child(user.getUid()).setValue(user);
                            }
                        });
                    }
                } else {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
                    builder.setTitle("")
                            .setMessage(R.string.accept_terms_request)
                            .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                }
                break;
            case R.id.tvTerms:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi
                    .getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                showProgress(false, "");
                GoogleSignInAccount account = result.getSignInAccount();
                if (account != null) {
                    AuthCredential credential = GoogleAuthProvider
                            .getCredential(account.getIdToken(), null);
                    linkAndSignIn(credential);
                }
            } else {
                Toast.makeText(getContext(), R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
                showProgress(false, "");
            }
        } else {
            showProgress(false, "");
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void linkAndSignIn(final AuthCredential credential) {
        if (credential != null && user != null)
            user.linkWithCredential(credential).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseUser newUser = authResult.getUser();
                    User mUser = new User(newUser);
                    if (!etUsername.getText().toString().equals("") || !etUsername.getText().toString().isEmpty())
                        mUser.setUsername(etUsername.getText().toString());
                    mUser.setUsername(newUser.getDisplayName());
                    mDatabase.getReference("users").child(newUser.getUid()).removeValue();
                    mDatabase.getReference("users").child(newUser.getUid()).setValue(mUser);
                    Toast.makeText(getContext(), R.string.registered, Toast.LENGTH_SHORT).show();
                    mFinishedListener.onBindFinished(newUser);
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    startActivity(intent);
                }
            });


        /*
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        showProgress(false, "");
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        User newUser = new User(user);
                        mDatabase.getReference("users").child(user.getUid()).setValue(newUser);
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showProgress(false, "");
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
                builder.setTitle("Login error")
                        .setMessage("You have already registered using different credentials. \nPlease login with a different provider.")
                        .setNeutralButton("Continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            }
        });
        */
    }

    private ProgressDialog dialog;

    private void showProgress(boolean show, String msg) {
        if (dialog == null) {
            dialog = new ProgressDialog(getContext());
            dialog.setTitle(getString(R.string.logging));
            dialog.setMessage(msg);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }
        if (show)
            dialog.show();
        else
            dialog.dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNotAnonymousListener) {
            mListener = (OnNotAnonymousListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNotAnonymousListener");
        }
        if (context instanceof OnBindFinishedListener) {
            mFinishedListener = (OnBindFinishedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBindFinishedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mListener != null)
            mListener = null;
//        if (mFinishedListener != null)
//            mFinishedListener = null;
        mApiClient.stopAutoManage(getActivity());
        mApiClient.disconnect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    interface OnNotAnonymousListener {
        void onNotAnonymous();
    }

    interface OnBindFinishedListener {
        void onBindFinished(FirebaseUser user);
    }
}
