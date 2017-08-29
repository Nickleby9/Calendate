package com.calendate.calendate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.calendate.calendate.models.User;
import com.calendate.calendate.utils.CustomBootstrapStyle;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    BootstrapButton btnRegister;
    EditText etUsername, etPassword, etPassword2, etEmail;
    private FirebaseAuth mAuth;
    TextView tvError;
    FirebaseDatabase mDatabase;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mDatabase = FirebaseDatabase.getInstance();
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPassword2 = (EditText) findViewById(R.id.etPassword2);
        etEmail = (EditText) findViewById(R.id.etEmail);
        tvError = (TextView) findViewById(R.id.tvError);
        btnRegister = (BootstrapButton) findViewById(R.id.btnRegister);
        btnRegister.setBootstrapBrand(new CustomBootstrapStyle(this));

        btnRegister.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

    }

    public boolean isEmptyFields() {
        String username = etUsername.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String password2 = etPassword2.getText().toString();
        if (username.equals("") || email.equals("") || password.equals("") || password2.equals(""))
            return true;
        return false;
    }

    @Override
    public void onClick(View v) {
        tvError.setVisibility(View.INVISIBLE);
        final String username = etUsername.getText().toString();
        final String email = etEmail.getText().toString();
        final String password = etPassword.getText().toString();
        String password2 = etPassword2.getText().toString();
        if (!isEmptyFields() && password.equals(password2)) {
            showProgress(true);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(Task<AuthResult> task) {
                                            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                            UserProfileChangeRequest change = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                                            if (user != null) {
                                                user.updateProfile(change).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        user.reload().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {

                                                                User newUser = new User(user);
                                                                mDatabase.getReference("users").child(user.getUid()).setValue(newUser);

                                                                Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                            if (!task.isSuccessful()) {
                                showProgress(false);
                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    etPassword.setError(getString(R.string.error_weak_password));
                                    etPassword.requestFocus();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    etEmail.setError(getString(R.string.error_invalid_email));
                                    etEmail.requestFocus();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    etEmail.setError(getString(R.string.error_user_exists));
                                    etEmail.requestFocus();
                                } catch (FirebaseNetworkException e){
                                    tvError.setText(R.string.network_error);
                                    tvError.setVisibility(View.VISIBLE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        } else {
            if (isEmptyFields()) {
                tvError.setText(R.string.error_empty_fields);
                tvError.setVisibility(View.VISIBLE);
                return;
            }
            etPassword2.setError(getString(R.string.error_mismatch_passwords));
            etPassword2.requestFocus();
        }
    }

    private ProgressDialog dialog;

    private void showProgress(boolean show) {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setTitle(getString(R.string.registering));
            dialog.setMessage(etEmail.getText());
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }
        if (show)
            dialog.show();
        else
            dialog.dismiss();
    }

}