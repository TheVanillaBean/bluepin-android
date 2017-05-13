package com.bluepinapp.bluepin.model;

/**
 * Created by Alex on 1/21/2017.
 */

import com.bluepinapp.bluepin.DataService.FBDataService;
import com.bluepinapp.bluepin.POJO.UserCastEvent;
import com.bluepinapp.bluepin.util.Constants;
import com.bluepinapp.bluepin.util.Util;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;

@Parcel /* Variables are not private because of the Parcel Dependency - Reflection */
@IgnoreExtraProperties
public class User {

    String uuid;
    String email;
    String userType;
    String fullName;
    String phoneNumber;
    String phoneNumberVerified;
    String password;

    String businessName;
    String businessType;
    String businessDesc;
    String businessWebsite;
    String businessHours;
    String userProfilePicLocation;

    String deviceToken;

    public String getDeviceToken() {
        return (deviceToken == null) ? "" : deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getUUID() {
        return (uuid == null) ? "" : uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEmail() {
        return (email == null) ? "" : email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return (userType == null) ? "" : userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFullName() {
        return (fullName == null) ? "" : fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return (phoneNumber == null) ? "" : phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumberVerified() {
        return (phoneNumberVerified == null) ? "" : phoneNumberVerified;
    }

    public void setPhoneNumberVerified(String phoneNumberVerified) {
        this.phoneNumberVerified = phoneNumberVerified;
    }

    public String getPassword() {
        return (password == null) ? "" : password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBusinessName() {
        return (businessName == null) ? "" : businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessType() {
        return (businessType == null) ? "" : businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessDesc() {
        return (businessDesc == null) ? "" : businessDesc;
    }

    public void setBusinessDesc(String businessDesc) {
        this.businessDesc = businessDesc;
    }

    public String getBusinessWebsite() {
        return (businessWebsite == null) ? "" : businessWebsite;
    }

    public void setBusinessWebsite(String businessWebsite) {
        this.businessWebsite = businessWebsite;
    }

    public String getBusinessHours() {
        return (businessHours == null) ? "" : businessHours;
    }

    public void setBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public String getUserProfilePicLocation() {
        return (userProfilePicLocation == null) ? "" : userProfilePicLocation;
    }

    public void setUserProfilePicLocation(String userProfilePicLocation) {
        this.userProfilePicLocation = userProfilePicLocation;
    }

    public User() {
    }

    public User(String email, String password, String userType) {
        this.email = email;
        this.password = password;
        this.userType = userType;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put(Constants.UUID, uuid);
        result.put(Constants.EMAIL, email);
        result.put(Constants.USER_TYPE, userType);
        result.put(Constants.FULL_NAME, fullName);
        result.put(Constants.PHONE_NUMBER, phoneNumber);
        result.put(Constants.PHONE_NUMBER_VERIFIED, phoneNumberVerified);
        result.put(Constants.PASSWORD, password);
        result.put(Constants.BUSINESS_NAME, businessName);
        result.put(Constants.BUSINESS_TYPE, businessType);
        result.put(Constants.BUSINESS_DESC, businessDesc);
        result.put(Constants.BUSINESS_WEBSITE, businessWebsite);
        result.put(Constants.BUSINESS_HOURS, businessHours);
        result.put(Constants.USER_PROFILE_PIC_LOC, userProfilePicLocation);
        result.put(Constants.DEVICE_TOKEN, deviceToken);
        return result;
    }

    public static void castUser(String uuid){

        FBDataService.getInstance().usersRef().child(uuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) return;

                User user = dataSnapshot.getValue(User.class);

                EventBus.getDefault().post(new UserCastEvent(null, user));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                EventBus.getDefault().post(new UserCastEvent(databaseError.getMessage(), null));
            }
        });

    }

    public static void castUserWithCompletion(String uuid, final Util.completionInterfaceUser completionInterface){

        FBDataService.getInstance().usersRef().child(uuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) return;

                User user = dataSnapshot.getValue(User.class);

                completionInterface.onComplete(null, user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                completionInterface.onComplete("Error " + databaseError.getMessage(), null);
            }
        });

    }

}
