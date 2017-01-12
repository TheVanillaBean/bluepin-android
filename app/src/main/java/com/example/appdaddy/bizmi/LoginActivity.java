package com.example.appdaddy.bizmi;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by AppDaddy on 12/28/16.
 */

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initBackgroundImage();
    }

    private void initBackgroundImage() {
        ImageView background = (ImageView) findViewById(R.id.logo_imageview);
        Glide.with(this)
                .load(R.drawable.bizmi_logo)
                .fitCenter()
                .into(background);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
