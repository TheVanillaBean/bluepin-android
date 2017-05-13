package com.bluepinapp.bluepin.model;

import com.stfalcon.chatkit.commons.models.IDialog;

import java.util.ArrayList;

/*
 * Created by troy379 on 04.04.17.
 */
public class Dialog implements IDialog<MessageDialog> {

    private String id;
    private String dialogPhoto;
    private String dialogName;
    private ArrayList<DialogUser> users;
    private MessageDialog lastMessage;

    private int unreadCount;

    public Dialog(String id, String name, String photo,
                  ArrayList<DialogUser> users, MessageDialog lastMessage, int unreadCount) {

        this.id = id;
        this.dialogName = name;
        this.dialogPhoto = photo;
        this.users = users;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public ArrayList<DialogUser> getUsers() {
        return users;
    }

    @Override
    public MessageDialog getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(MessageDialog lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
    }
}
