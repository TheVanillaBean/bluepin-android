package com.example.appdaddy.bizmi.controller;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.appdaddy.bizmi.BizmiApplication;
import com.example.appdaddy.bizmi.DataService.AuthService;
import com.example.appdaddy.bizmi.DataService.FBDataService;
import com.example.appdaddy.bizmi.POJO.SinchVerifyEvent;
import com.example.appdaddy.bizmi.POJO.UserCastEvent;
import com.example.appdaddy.bizmi.PhoneVerificationActivity;
import com.example.appdaddy.bizmi.R;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.Dialog;
import com.example.appdaddy.bizmi.util.SMSVerificationListener;
import com.example.appdaddy.bizmi.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sinch.verification.PhoneNumberUtils;
import com.sinch.verification.SinchVerification;
import com.sinch.verification.Verification;
import com.sinch.verification.VerificationListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.logo_imageview) ImageView background;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initBackgroundImage();
        if (Util.isGooglePlayServicesAvailable(MainActivity.this)){
            validateUserToken();
        }

    }

    private void initBackgroundImage() {
        Glide.with(this)
                .load(R.drawable.bizmi_logo)
                .fitCenter()
                .into(background);
    }

    private void validateUserToken() {
        Log.d("TAG", "AuthListener");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    User.castUser(user.getUid());
                } else {
                    Log.d("TAG", "Login");
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }

            }
        };
    }

    private void navigateToTabBarActivity(User user){

        Intent intent = null;
        if(user.getUserType().equals(Constants.USER_BUSINESS_TYPE)){
            intent = new Intent(MainActivity.this, BusinessMainActivity.class);
        }else{
            if(user.getPhoneNumberVerified().equals("true")){
                intent = new Intent(MainActivity.this, CustomerMainActivity.class);
            }else{
                intent = new Intent(MainActivity.this, PhoneVerificationActivity.class);
                intent.putExtra(Constants.EXTRA_PHONE_NUMBER, user.getPhoneNumber());
            }
        }

        intent.putExtra(Constants.EXTRA_USER_ID, user.getUUID());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserCastCallBack(UserCastEvent event) {
        if (event.getError() == null){
            this.navigateToTabBarActivity(event.getUser());
        }else{
            Dialog.showDialog(MainActivity.this, "Authentication Error", event.getError(), "Okay");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        AuthService.getInstance().getAuthInstance().addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        if (mAuthListener != null) {
            AuthService.getInstance().getAuthInstance().removeAuthStateListener(mAuthListener);
        }
    }

}
