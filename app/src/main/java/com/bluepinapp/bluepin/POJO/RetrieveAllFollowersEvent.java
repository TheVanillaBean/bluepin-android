package com.bluepinapp.bluepin.POJO;

import android.support.annotation.Nullable;

/**
 * Created by Alex on 1/21/2017.
 */

public class RetrieveAllFollowersEvent {

    private final String error;

    //TODO: Convert Business IDS to User Object inside this constructor
    public RetrieveAllFollowersEvent(@Nullable String error){
        this.error = error;
    }

    public String getError() {
        return error;
    }

}
