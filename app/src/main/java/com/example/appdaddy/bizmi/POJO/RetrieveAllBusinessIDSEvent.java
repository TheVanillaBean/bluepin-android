package com.example.appdaddy.bizmi.POJO;

import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Alex on 1/21/2017.
 */

public class RetrieveAllBusinessIDSEvent {

    private final String error;

    //TODO: Convert Business IDS to User Object inside this constructor
    public RetrieveAllBusinessIDSEvent(@Nullable String error){
        this.error = error;
    }

    public String getError() {
        return error;
    }

}
