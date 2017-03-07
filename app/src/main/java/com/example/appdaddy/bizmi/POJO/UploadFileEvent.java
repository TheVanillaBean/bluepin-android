package com.example.appdaddy.bizmi.POJO;

import android.support.annotation.Nullable;

import com.example.appdaddy.bizmi.util.L;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.UploadTask;

import java.io.File;

/**
 * Created by Alex on 1/21/2017.
 */

public class UploadFileEvent {

    private final String error;

    private final File file;

    private final UploadTask.TaskSnapshot snapshot;

    public UploadFileEvent(@Nullable String error, @Nullable UploadTask.TaskSnapshot snapshot, File file){
        this.error = error;
        this.snapshot = snapshot;
        this.file = file;
    }

    public String getError() {
        return error;
    }

    public File getFile() {
        return file;
    }

    public UploadTask.TaskSnapshot getUploadTask() {
        return snapshot;
    }
}
