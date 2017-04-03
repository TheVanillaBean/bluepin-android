package com.example.appdaddy.bizmi.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.example.appdaddy.bizmi.DataService.AuthService;
import com.example.appdaddy.bizmi.DataService.FBDataService;
import com.example.appdaddy.bizmi.POJO.AuthEvent;
import com.example.appdaddy.bizmi.POJO.UserCastEvent;
import com.example.appdaddy.bizmi.PhoneVerificationActivity;
import com.example.appdaddy.bizmi.R;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.Dialog;
import com.example.appdaddy.bizmi.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by AppDaddy on 12/28/16.
 */

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.logo_imageview) ImageView background;
    @BindView(R.id.email_input) EditText emailField;
    @BindView(R.id.password_input) EditText passwordField;
    @BindView(R.id.login_btn) Button loginBtn;
    @BindView(R.id.sign_up_btn) TextView signUpBtn;
    @BindView(R.id.forgot_password_btn) TextView forgotPasswordBtn;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initBackgroundImage();
        progressDialog = Dialog.showProgressIndeterminateDialog(LoginActivity.this, "Loading...", "Signing in...", false);
    }

    private void initBackgroundImage() {
        Glide.with(this)
                .load(R.drawable.bizmi_logo)
                .fitCenter()
                .into(background);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void showSignUpAlertDialog(){
        new MaterialDialog.Builder(this)
                .title("New User")
                .content("Are you a customer or a business?")
                .positiveText("Business")
                .negativeText("Customer")
                .autoDismiss(true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(LoginActivity.this, BusinessSignUpActivity.class);
                        startActivity(intent);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(LoginActivity.this, CustomerSignUpActivity.class);
                        startActivity(intent);
                    }
                })
                .typeface("Roboto-Regular.ttf", "Roboto-Light.ttf")
                .show();
    }

    private boolean validateInputFields(){
        return !(emailField.getText().toString().equals("") || passwordField.getText().toString().equals(""));
    }

    private void navigateToTabBarActivity(User user){
        Intent intent = null;
        if(user.getUserType().equals(Constants.USER_BUSINESS_TYPE)){
            intent = new Intent(LoginActivity.this, BusinessMainActivity.class);
        }else{
            if(user.getPhoneNumberVerified().equals("true")){
                intent = new Intent(LoginActivity.this, CustomerMainActivity.class);
            }else{
                intent = new Intent(LoginActivity.this, PhoneVerificationActivity.class);
                intent.putExtra(Constants.EXTRA_PHONE_NUMBER, user.getPhoneNumber());
            }
        }

        intent.putExtra(Constants.EXTRA_USER_ID, user.getUUID());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSignInCallBack(AuthEvent event) {
        progressDialog.dismiss();
        if (event.getError() == null){
            User.castUser(event.getUser().getUid());
        }else{
            Dialog.showDialog(LoginActivity.this, "Authentication Error", event.getError(), "Okay");
            loginBtn.setEnabled(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserCastCallBack(UserCastEvent event) {
        loginBtn.setEnabled(true);
        if (event.getError() == null){
            this.navigateToTabBarActivity(event.getUser());
        }else{
            Dialog.showDialog(LoginActivity.this, "Authentication Error", event.getError(), "Okay");
        }
    }

    @OnClick(R.id.forgot_password_btn)
    public void onForgotPasswordBtnPressed() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.sign_up_btn)
    public void onSignUpBtnPressed() {
        showSignUpAlertDialog();
    }

    @OnClick(R.id.login_btn)
    public void onLoginBtnPressed() {
        if (validateInputFields()){

            loginBtn.setEnabled(false);

            String email = emailField.getText().toString();
            String password = passwordField.getText().toString();

            if (Util.isGooglePlayServicesAvailable(LoginActivity.this)){
                progressDialog.show();
                AuthService.getInstance().login(email, password);
            }

        }else{
            Dialog.showDialog(LoginActivity.this, "Invalid", "Please Enter an Email and Password", "Okay");
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
