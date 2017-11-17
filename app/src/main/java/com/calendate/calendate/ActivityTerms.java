package com.calendate.calendate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.calendate.calendate.utils.MyUtils;

public class ActivityTerms extends AppCompatActivity implements View.OnClickListener {

    BootstrapButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        btnBack = (BootstrapButton) findViewById(R.id.btnBack);

        MyUtils.fixBootstrapButton(this, btnBack);

        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onBackPressed();
    }
}
