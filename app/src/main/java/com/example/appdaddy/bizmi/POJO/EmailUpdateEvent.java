package com.example.appdaddy.bizmi.POJO;

import android.support.annotation.Nullable;

/**
 * Created by Alex on 1/21/2017.
 */

public class EmailUpdateEvent {

    private final String error;

    public EmailUpdateEvent(@Nullable String error){
        this.error = error;
    }

    public String getError() {
        return error;
    }

}
