package com.example.appdaddy.bizmi.POJO;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;

/**
 * Created by Alex on 1/21/2017.
 */

public class UploadProgressEvent {

    private final double progress;

    public UploadProgressEvent(@NonNull Double progress){
        this.progress = progress;
    }

    public Double getProgress() {
        return progress;
    }

}
