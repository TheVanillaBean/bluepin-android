package com.example.appdaddy.bizmi.util;

import android.util.Log;

import com.example.appdaddy.bizmi.POJO.SinchVerifyEvent;
import com.sinch.verification.CodeInterceptionException;
import com.sinch.verification.IncorrectCodeException;
import com.sinch.verification.InitiationResult;
import com.sinch.verification.InvalidInputException;
import com.sinch.verification.ServiceErrorException;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Alex on 2/1/2017.
 */


public class SMSVerificationListener implements com.sinch.verification.VerificationListener{

    public SMSVerificationListener(){

    }

    @Override
    public void onInitiated(InitiationResult initiationResult) {

    }

    @Override
    public void onInitiationFailed(Exception e) {
        if (e instanceof InvalidInputException) {
            EventBus.getDefault().post(new SinchVerifyEvent("Incorrect Number Provided..."));
        } else if (e instanceof ServiceErrorException) {
            EventBus.getDefault().post(new SinchVerifyEvent("Cloud Service Error..."));
        } else {
            EventBus.getDefault().post(new SinchVerifyEvent("Phone Verification Error..."));
        }
    }

    @Override
    public void onVerified() {
        EventBus.getDefault().post(new SinchVerifyEvent(null));
    }

    @Override
    public void onVerificationFailed(Exception e) {
        if (e instanceof InvalidInputException) {
            EventBus.getDefault().post(new SinchVerifyEvent("Incorrect Number or Code Provided..."));
        } else if (e instanceof CodeInterceptionException) {
            EventBus.getDefault().post(new SinchVerifyEvent("Verification Failed. Try manually submitting the SMS code..."));
        } else if (e instanceof IncorrectCodeException) {
            EventBus.getDefault().post(new SinchVerifyEvent("Incorrect Code Provided..."));
        } else if (e instanceof ServiceErrorException) {
            EventBus.getDefault().post(new SinchVerifyEvent("Cloud Service Error..."));
        } else {
            EventBus.getDefault().post(new SinchVerifyEvent("Phone Verification Error..."));
        }
    }
}
