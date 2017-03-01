package com.example.appdaddy.bizmi.DataService;

import com.example.appdaddy.bizmi.POJO.RetrieveAllBusinessIDSEvent;
import com.example.appdaddy.bizmi.POJO.RetrieveSubscriptionStatusEvent;
import com.example.appdaddy.bizmi.POJO.UserUpdateEvent;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.L;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
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

    private ArrayList<String> customerFollowingList = new ArrayList<>();

    public ArrayList<String> getCustomerFollowingList(){
        return customerFollowingList;
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

                    EventBus.getDefault().post(new RetrieveAllBusinessIDSEvent(null));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                EventBus.getDefault().post(new RetrieveAllBusinessIDSEvent("Error retrieving businesses " + databaseError.getMessage()));
            }
        });
    }

    //Begin Subscription Status

    public void subscribeToBusiness(String customerID, String businessID){

        if(customerFollowingList.size() > 0){
            if(isSubscribed(businessID)){
                businessFollowersRef().child(businessID).child(customerID).removeValue();
                customerFollowersRef().child(customerID).child(businessID).removeValue();
                customerFollowingList.remove(businessID);
                EventBus.getDefault().post(new RetrieveSubscriptionStatusEvent(false, null));
            }else{
                businessFollowersRef().child(businessID).child(customerID).setValue(ServerValue.TIMESTAMP);
                customerFollowersRef().child(customerID).child(businessID).setValue(ServerValue.TIMESTAMP);
                customerFollowingList.add(businessID);
                EventBus.getDefault().post(new RetrieveSubscriptionStatusEvent(true, null));
            }
        }else{
            businessFollowersRef().child(businessID).child(customerID).setValue(ServerValue.TIMESTAMP);
            customerFollowersRef().child(customerID).child(businessID).setValue(ServerValue.TIMESTAMP);
            customerFollowingList.add(businessID);
            EventBus.getDefault().post(new RetrieveSubscriptionStatusEvent(true, null));
        }

    }

    public void retrieveSubscriptionStatus(String customerID, String businessID){

       if(customerFollowingList.size() > 0){
            if(isSubscribed(businessID)){
                EventBus.getDefault().post(new RetrieveSubscriptionStatusEvent(true, null));
            }else{
                EventBus.getDefault().post(new RetrieveSubscriptionStatusEvent(false, null));
            }
       }else{
           retrieveAllBusinessesFollowedByUser(customerID, businessID);
       }

    }

    private void retrieveAllBusinessesFollowedByUser(final String customerID, final String businessID){

        customerFollowersRef().child(customerID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                customerFollowingList.clear();
                String businessKey;

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    businessKey = postSnapshot.getKey();
                    customerFollowingList.add(businessKey);
                }

                if(customerFollowingList.size() == 0){
                    EventBus.getDefault().post(new RetrieveSubscriptionStatusEvent(false, null));
                }else {
                    retrieveSubscriptionStatus(customerID, businessID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                L.m("cancell followed user");
                EventBus.getDefault().post(new RetrieveSubscriptionStatusEvent(false, "Error retrieving businesses... " + databaseError.getMessage()));
            }
        });

    }

    private boolean isSubscribed(String businessID){

        for (String businessKey : getCustomerFollowingList()) {
            if(businessKey.equals(businessID)){
                return true;
            }
        }

        return false;

    }

    //End Subscription Status


    public FBDataService(){
    }

}
