package com.calendate.calendate;

import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordDialog extends DialogFragment {

    Button btnSend;
    EditText etEmail;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnSend = (Button) view.findViewById(R.id.btnLogin);
        etEmail = (EditText) view.findViewById(R.id.etEmail);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                if (!email.equals("")){
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(view.getContext(), R.string.email_sent, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                dismiss();
            } else {
                    etEmail.setError(getString(R.string.empty_mail));
                    etEmail.requestFocus();
                }
            }
        });

    }


}
