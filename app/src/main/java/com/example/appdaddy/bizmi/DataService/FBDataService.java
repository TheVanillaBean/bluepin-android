package com.example.appdaddy.bizmi.DataService;

import android.app.Activity;

import com.example.appdaddy.bizmi.POJO.RetrieveAllBusinessIDSEvent;
import com.example.appdaddy.bizmi.POJO.UserUpdateEvent;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 1/25/2017.
 */

public class FBDataService {


    private static final FBDataService _instance = new FBDataService();
    private static final FirebaseDatabase  mDatabase = FirebaseDatabase.getInstance();
    private static final StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();

    public static FBDataService getInstance() {
        return _instance;
    }

    public static FirebaseDatabase getDataInstance() {
        return mDatabase;
    }

    //-----------------Database References------------------//

    public DatabaseReference mainRef() {
        return mDatabase.getReference();
    }

    public DatabaseReference usersRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_USERS);
    }

    public DatabaseReference businessUserRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_USERS_BUSINESS);
    }

    public DatabaseReference customerUserRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_USERS_CUSTOMER);
    }

    public DatabaseReference businessFollowersRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_BUSINESS_FOLLOWERS);
    }

    public DatabaseReference customerFollowersRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_CUSTOMER_FOLLOWERS);
    }

    public DatabaseReference userChannelsRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_USER_CHANNELS);
    }

    public DatabaseReference channelsRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_CHANNELS);
    }

    public DatabaseReference messagesRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_MESSAGES);
    }

    public DatabaseReference userReservationsRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_USER_RESERVATIONS);
    }

    public DatabaseReference reservationsRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_RESERVATIONS);
    }

    public DatabaseReference notificationsRef() {
        return mDatabase.getReference(Constants.FIR_CHILD_NOTIFICATIONS);
    }

    //-----------------End Database References------------------//

    //-----------------Storage References--------------------//

    public StorageReference mainStorageRef() {
        return mStorageReference;
    }

    public StorageReference profilePicsStorageRef() {
        return mStorageReference.child(Constants.FIR_STORAGE_CHILD_USER_PROFILE_PICS);
    }

    public StorageReference messagesStorageRef() {
        return mStorageReference.child(Constants.FIR_STORAGE_CHILD_MESSAGES);
    }

    //-----------------End Storage References--------------------//

    private ArrayList<String> allBusinessIDS = new ArrayList<>();

    public ArrayList<String> getAllBusinessIDS(){
        return allBusinessIDS;
    }

    public void saveUser(final User user){
        Map<String, Object> properties = user.toMap();
        usersRef().child(user.getUUID()).setValue(properties, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null){
                    if (user.getUserType().equals(Constants.USER_CUSTOMER_TYPE)){
                        customerUserRef().child(user.getUUID()).setValue(true);
                    }else{
                        businessUserRef().child(user.getUUID()).setValue(true);
                    }
                    EventBus.getDefault().post(new UserUpdateEvent(null));
                }else{
                    EventBus.getDefault().post(new UserUpdateEvent(databaseError.getMessage()));
                }
            }
        });
    }

    public void updateUser(final User user){
        Map<String, Object> properties = user.toMap();
        usersRef().child(user.getUUID()).updateChildren(properties, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null){
                    EventBus.getDefault().post(new UserUpdateEvent(null));
                }else{
                    EventBus.getDefault().post(new UserUpdateEvent(databaseError.getMessage()));
                }
            }
        });
    }

    public void retrieveAllBusinesses(){
        businessUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() == null){
                    EventBus.getDefault().post(new RetrieveAllBusinessIDSEvent("No businesses were retrieved..."));
                }else{
                    allBusinessIDS.clear();
                    String businessKey;

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        businessKey = postSnapshot.getKey();
                        allBusinessIDS.add(businessKey);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                EventBus.getDefault().post(new RetrieveAllBusinessIDSEvent("Error retrieving businesses " + databaseError.getMessage()));
            }
        });
    }


    public FBDataService(){
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

}
