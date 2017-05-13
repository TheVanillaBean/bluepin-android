package com.bluepinapp.bluepin.model;

/**
 * Created by Alex on 4/15/2017.
 */


import com.stfalcon.chatkit.commons.models.IUser;

public class DialogUser implements IUser {

    private String id;
    private String name;
    private String avatar;
    private boolean online;

    public DialogUser(String id, String name, String avatar, boolean online) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.online = online;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public boolean isOnline() {
        return online;
    }
}