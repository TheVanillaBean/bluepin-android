package com.bluepinapp.bluepin.POJO;

import android.support.annotation.NonNull;

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
