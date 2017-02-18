package com.example.appdaddy.bizmi;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.appdaddy.bizmi.DataService.AuthService;
import com.example.appdaddy.bizmi.DataService.FBDataService;
import com.example.appdaddy.bizmi.POJO.SinchVerifyEvent;
import com.example.appdaddy.bizmi.POJO.UserCastEvent;
import com.example.appdaddy.bizmi.POJO.UserUpdateEvent;
import com.example.appdaddy.bizmi.controller.BusinessMainActivity;
import com.example.appdaddy.bizmi.controller.CustomerMainActivity;
import com.example.appdaddy.bizmi.controller.CustomerSignUpActivity;
import com.example.appdaddy.bizmi.controller.LoginActivity;
import com.example.appdaddy.bizmi.controller.MainActivity;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.Dialog;
import com.example.appdaddy.bizmi.util.SMSVerificationListener;
import com.sinch.verification.PhoneNumberUtils;
import com.sinch.verification.SinchVerification;
import com.sinch.verification.Verification;
import com.sinch.verification.VerificationListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PhoneVerificationActivity extends AppCompatActivity {

    @BindView(R.id.verification_input) EditText verificationInput;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.verify_btn) Button mVerifyBtn;

    private VerificationListener mListener;
    private Verification mVerification;
    private String mPhoneNumberInput;
    private String mVerificationCodeInput;

    private MaterialDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Verify Number");
        }
        progressDialog = Dialog.showProgressIndeterminateDialog(PhoneVerificationActivity.this, "Loading...", "Verifying code...", false);

        mListener = new SMSVerificationListener();

        if (getIntent().getStringExtra(Constants.EXTRA_PHONE_NUMBER) != null){
            mPhoneNumberInput = getIntent().getStringExtra(Constants.EXTRA_PHONE_NUMBER);
            initiateVerificationProcess();
        }

    }

    private void initiateVerificationProcess(){
        String defaultRegion = PhoneNumberUtils.getDefaultCountryIso(PhoneVerificationActivity.this);
        String phoneNumberInE164 = PhoneNumberUtils.formatNumberToE164(mPhoneNumberInput, defaultRegion);
        mVerification = SinchVerification.createSmsVerification(BizmiApplication.getSinchConfig(), phoneNumberInE164, mListener);
        mVerification.initiate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhoneVerificationCallback(SinchVerifyEvent event) {
        if (event.getError() == null){
            Toast.makeText(PhoneVerificationActivity.this, "Verification Successful", Toast.LENGTH_LONG).show();
            User.castUser(getIntent().getStringExtra(Constants.EXTRA_USER_ID));
        }else{
            mVerifyBtn.setEnabled(true);
            progressDialog.dismiss();
            Dialog.showDialog(PhoneVerificationActivity.this, "Verification Error", event.getError(), "Okay");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserCastCallBack(UserCastEvent event) {
        if (event.getError() == null){
            User newUser = event.getUser();
            newUser.setPhoneNumberVerified("true");
            FBDataService.getInstance().updateUser(newUser);
        }else{
            mVerifyBtn.setEnabled(true);
            progressDialog.dismiss();
            Dialog.showDialog(PhoneVerificationActivity.this, "Authentication Error", event.getError(), "Okay");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateUserCallback(UserUpdateEvent event) {
        mVerifyBtn.setEnabled(true);
        progressDialog.dismiss();
        if (event.getError() == null){
            navigateToTabBarActivity();
        }else{
            Dialog.showDialog(PhoneVerificationActivity.this, "Authentication Error", event.getError(), "Okay");
        }
    }

    @OnClick(R.id.verify_btn)
    public void onVerifyBtn() {
        mVerifyBtn.setEnabled(false);
        progressDialog.show();
        mVerificationCodeInput = verificationInput.getText().toString();
        verificationInput.setText("");
        mVerification.verify(mVerificationCodeInput);
    }

    private void navigateToTabBarActivity(){
        Intent intent = new Intent(PhoneVerificationActivity.this, CustomerMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
