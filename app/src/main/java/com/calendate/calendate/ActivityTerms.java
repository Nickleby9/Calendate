package com.calendate.calendate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.calendate.calendate.utils.MyUtils;

public class ActivityTerms extends AppCompatActivity implements View.OnClickListener {

    BootstrapButton btnAccept, btnDecline;
    String method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        btnAccept = (BootstrapButton) findViewById(R.id.btnAccept);
        btnDecline = (BootstrapButton) findViewById(R.id.btnDecline);

        if (getIntent().getExtras() != null)
            method = getIntent().getExtras().getString("method");
        else {
            Intent intent1 = new Intent(this, LoginActivity.class);
            startActivity(intent1);
        }

        MyUtils.fixBootstrapButton(this, btnAccept);

        btnDecline.setOnClickListener(this);
        btnAccept.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (method.equals("register")){
            Intent intent = new Intent(this, RegistrationActivity.class);
            intent.putExtra("method", method);
            intent.putExtra("username", getIntent().getExtras().getString("username"))
                    .putExtra("email", getIntent().getExtras().getString("email"))
                    .putExtra("password", getIntent().getExtras().getString("password"))
                    .putExtra("password2", getIntent().getExtras().getString("password2"));
            switch (id){
                case R.id.btnAccept:
                    intent.putExtra("accepted", true);
                    break;
                case R.id.btnDecline:
                    intent.putExtra("accepted", false);
                    break;
            }
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("method", method);
            switch (id){
                case R.id.btnAccept:
                    intent.putExtra("accepted", true);
                    break;
                case R.id.btnDecline:
                    intent.putExtra("accepted", false);
                    break;
            }
            startActivity(intent);
        }
    }
}
