package com.bluepinapp.bluepin.model;

/**
 * Created by Alex on 1/21/2017.
 */

import android.support.annotation.NonNull;

import com.bluepinapp.bluepin.DataService.FBDataService;
import com.bluepinapp.bluepin.util.Constants;
import com.bluepinapp.bluepin.util.L;
import com.bluepinapp.bluepin.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import org.parceler.Parcel;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Parcel /* Variables are not private because of the Parcel Dependency - Reflection */
@IgnoreExtraProperties
public class Message {

    String messageID;
    String messageType;
    String messageData;
    String senderUID;
    String recipientUID;
    String channelName;
    long timeStamp;

    String messageLocation;
    String timestampFormatted;
    User senderUserObj;
    User recipientUserObj;


    public String getMessageID() {
        return (messageID == null) ? "" : messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getMessageType() {
        return (messageType == null) ? "" : messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageData() {
        return (messageData == null) ? "" : messageData;
    }

    public void setMessageData(String messageData) {
        this.messageData = messageData;
    }

    public String getSenderUID() {
        return (senderUID == null) ? "" : senderUID;
    }

    public void setSenderUID(String senderUID) {
        this.senderUID = senderUID;
    }

    public String getRecipientUID() {
        return (recipientUID == null) ? "" : recipientUID;
    }

    public void setRecipientUID(String recipientUID) {
        this.recipientUID = recipientUID;
    }

    public String getChannelName() {
        return (channelName == null) ? "" : channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessageLocation() {
        return (messageLocation == null) ? "" : messageLocation;
    }

    public void setMessageLocation(String messageLocation) {
        this.messageLocation = messageLocation;
    }

    public String getTimestampFormatted() {
        return (timestampFormatted == null) ? "" : timestampFormatted;
    }

    public void setTimestampFormatted(String timestamp) {
        this.timestampFormatted = timestamp;
    }

    @Exclude
    public User getSenderUserObj() {
        return senderUserObj;
    }

    @Exclude
    public void setSenderUserObj(User senderUserObj) {
        this.senderUserObj = senderUserObj;
    }

    @Exclude
    public User getRecipientUserObj() {
        return recipientUserObj;
    }

    @Exclude
    public void setRecipientUserObj(User recipientUserObj) {
        this.recipientUserObj = recipientUserObj;
    }

    public Message() {
    }

    public Message(String messageType, String messageData, String senderUID, String recipientUID, String channelName) {
        this.messageType = messageType;
        this.messageData = messageData;
        this.senderUID = senderUID;
        this.recipientUID = recipientUID;
        this.channelName = channelName;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put(Constants.MESSAGE_UID, messageID);
        result.put(Constants.MESSAGE_TYPE, messageType);
        result.put(Constants.MESSAGE_DATA, messageData);
        result.put(Constants.MESSAGE_SENDERID, senderUID);
        result.put(Constants.MESSAGE_RECIPIENTID, recipientUID);
        result.put(Constants.MESSAGE_CHANNEL_NAME, channelName);
        result.put(Constants.MESSAGE_TIMESTAMP, timeStamp);
        result.put(Constants.MESSAGE_LOCATION, messageLocation);
        result.put(Constants.MESSAGE_TIMESTAMP_FORMATTED, timestampFormatted);

        return result;
    }

    public static void castMessage(String uuid, final Util.completionInterfaceMessage completionInterface){

        FBDataService.getInstance().messagesRef().child(uuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) return;


                final Message message = dataSnapshot.getValue(Message.class);

                L.m("converted message almost --");
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd", Locale.US);
                message.timestampFormatted = sdf.format(message.timeStamp);


                final long ONE_MEGABYTE = 1024 * 1024;
                if(message.messageLocation != null){
                    FBDataService.getInstance().messagesStorageRef().child(message.messageLocation).getBytes(10 * ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {

                            if(message.messageType.equals(Constants.MESSAGE_TEXT_TYPE)){
                                message.messageData = new String(bytes);
                            }

                            L.m("onMessageDataGathered -- " + message.messageData);

                            User.castUserWithCompletion(message.senderUID, new Util.completionInterfaceUser() {
                                @Override
                                public void onComplete(String error, User user) {
                                    if (error == null){
                                        message.senderUserObj = user;
                                        User.castUserWithCompletion(message.recipientUID, new Util.completionInterfaceUser() {
                                            @Override
                                            public void onComplete(String error, User user) {
                                                message.recipientUserObj = user;
                                                completionInterface.onComplete(null, message);
                                            }
                                        });
                                    }
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            completionInterface.onComplete("Error " + e.getMessage(), null);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                completionInterface.onComplete("Error " + databaseError.getMessage(), null);
            }
        });

    }

}
