package com.example.appdaddy.bizmi.POJO;

import android.support.annotation.Nullable;

/**
 * Created by Alex on 1/21/2017.
 */

public class RetrieveSubscriptionStatusEvent {

    private final String error;
    private final boolean status;

    public RetrieveSubscriptionStatusEvent(boolean status, @Nullable String error){
        this.status = status;
        this.error = error;
    }

    public String getError() {
        return error;
    }
    public boolean getStatus() {return status;}

}
