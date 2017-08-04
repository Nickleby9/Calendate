package com.calendate.calendate;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapText;
import com.calendate.calendate.models.User;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

import static com.beardedhen.androidbootstrap.font.FontAwesome.FA_SIGN_IN;
import static com.beardedhen.androidbootstrap.font.FontAwesome.FA_USER_PLUS;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_GOOGLE_LOGIN = 1;
    BootstrapButton btnLogin, btnRegister;
    Button btnGoogle, btnFacebook;
    EditText etUsername, etPassword;
    FirebaseAuth mAuth;
    Boolean exit = false;
    GoogleApiClient mApiClient;
    FirebaseDatabase mDatabase;
    CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FacebookSdk.sdkInitialize(getApplicationContext()); //auto start.
        setContentView(R.layout.activity_login);

        mDatabase = FirebaseDatabase.getInstance();

        btnLogin = (BootstrapButton) findViewById(R.id.btnLogin);
        btnRegister = (BootstrapButton) findViewById(R.id.btnRegister);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnGoogle = (Button) findViewById(R.id.btnGoogle);
        btnFacebook = (Button) findViewById(R.id.btnFacebook);

        btnLogin.setBootstrapBrand(new CustomBootstrapStyle(this));
        btnRegister.setBootstrapBrand(new CustomBootstrapStyle(this));

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
        btnFacebook.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setBootstrapText(new BootstrapText.Builder(this)
                .addText(getString(R.string.btn_login) + " ")
                .addFontAwesomeIcon(FA_SIGN_IN)
                .build()
        );

        btnRegister.setBootstrapText(new BootstrapText.Builder(this)
                .addText(getString(R.string.btn_register) + " ")
                .addFontAwesomeIcon(FA_USER_PLUS)
                .build()
        );

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        mCallbackManager = CallbackManager.Factory.create();
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btnLogin:
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                if (!username.equals("") && !password.equals("")) {
                    if (username.contains("@")) {
                        showProgress(true, etUsername.getText().toString());
                        AuthCredential credential = EmailAuthProvider.getCredential(username, password);
                        linkAndSignIn(credential);
                    } else {
//                    mDatabase.getReference("users").
                    }
                } else {
                    detailsIncorrect();
                }
                break;
            case R.id.btnRegister:
                Intent intent1 = new Intent(this, RegistrationActivity.class);
                startActivity(intent1);
                break;
            case R.id.btnGoogle:
                showProgress(true, getString(R.string.google_login_msg));
                Intent googleIntent = Auth.GoogleSignInApi
                        .getSignInIntent(mApiClient);
                startActivityForResult(googleIntent, RC_GOOGLE_LOGIN);
                break;
            case R.id.btnFacebook:
                showProgress(true, getString(R.string.facebook_login_msg));
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi
                    .getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                showProgress(false, "");
                GoogleSignInAccount account = result.getSignInAccount();
                AuthCredential credential = GoogleAuthProvider
                        .getCredential(account.getIdToken(), null);
                linkAndSignIn(credential);
            } else {
                Toast.makeText(this, R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
                showProgress(false, "");
            }
        } else {
            showProgress(false, "");
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void detailsIncorrect() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.login_error_title)
                .setMessage(R.string.login_error_description)
                .setPositiveButton(R.string.login_error_try, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        showProgress(false, "");
                    }
                })
                .setNegativeButton(R.string.login_error_forgot, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ForgotPassword f = new ForgotPassword();
                        f.show(getSupportFragmentManager(), "forgotPassword");
                    }
                });
        AlertDialog dialog = builder.show();
    }

    @Override
    public void onBackPressed() {
        if (!exit) {
            Toast.makeText(this, R.string.back_twice, Toast.LENGTH_SHORT).show();
            exit = true;
        } else {
            ActivityCompat.finishAffinity(this);
        }
    }

    private ProgressDialog dialog;

    private void showProgress(boolean show, String msg) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed to connect to Google API services", Toast.LENGTH_SHORT).show();
        showProgress(false, "");
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        linkAndSignIn(credential);
    }

    public void linkAndSignIn(final AuthCredential credential) {
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mAuth.signInWithCredential(credential)
                                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        User newUser = new User(user);
                                        mDatabase.getReference("users").child(user.getUid()).setValue(newUser);
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                        showProgress(false, "");
                    }
                }
            });
        }
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        showProgress(false, "");
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        User newUser = new User(user);
                        mDatabase.getReference("users").child(user.getUid()).setValue(newUser);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showProgress(false, "");
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
    }
}
