package com.bluepinapp.bluepin.DataService;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.bluepinapp.bluepin.POJO.RetrieveAllBusinessIDSEvent;
import com.bluepinapp.bluepin.POJO.RetrieveAllFollowersEvent;
import com.bluepinapp.bluepin.POJO.RetrieveSubscriptionStatusEvent;
import com.bluepinapp.bluepin.POJO.UploadFileEvent;
import com.bluepinapp.bluepin.POJO.UploadProgressEvent;
import com.bluepinapp.bluepin.POJO.UserUpdateEvent;
import com.bluepinapp.bluepin.model.User;
import com.bluepinapp.bluepin.util.Constants;
import com.bluepinapp.bluepin.util.L;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
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

    private ArrayList<String> customerFollowingList = new ArrayList<>();

    public ArrayList<String> getCustomerFollowingList(){
        return customerFollowingList;
    }

    private HashMap<String, Long> allFollowersTime = new HashMap<String, Long> ();

    public HashMap<String, Long>  getAllFollowersTime(){
        return allFollowersTime;
    }

    private ArrayList<String> allFollowers = new ArrayList<>();

    public ArrayList<String> getAllFollowers(){
        return allFollowers;
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

    public void uploadFile(StorageReference filePath, final File file, StorageMetadata metadata){

        Uri fileURI = Uri.fromFile(file);

        UploadTask uploadTask = filePath.putFile(fileURI, metadata);

        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                EventBus.getDefault().post(new UploadProgressEvent(progress));
            }
        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                EventBus.getDefault().post(new UploadFileEvent("Image Upload Paused. Please Check Network State" , null, null));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                EventBus.getDefault().post(new UploadFileEvent("Failed to Upload Image" + exception.getMessage(), null, null));
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                EventBus.getDefault().post(new UploadFileEvent(null, taskSnapshot, file));
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

    public void retrieveAllFollowers(String businessID){
        businessFollowersRef().child(businessID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() == null){
                    EventBus.getDefault().post(new RetrieveAllFollowersEvent("No businesses were retrieved..."));
                }else{
                    allFollowersTime.clear();
                    allFollowers.clear();
                    String customerKey;
                    Long timeKey;

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        customerKey = postSnapshot.getKey();
                        timeKey = (Long) postSnapshot.getValue();
                        allFollowers.add(customerKey);
                        allFollowersTime.put(customerKey, timeKey);
                        L.m(customerKey + " " + timeKey);

                    }

                    EventBus.getDefault().post(new RetrieveAllFollowersEvent(null));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                EventBus.getDefault().post(new RetrieveAllFollowersEvent("Error retrieving businesses " + databaseError.getMessage()));
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

    public void removeReservationForUser(String reservationID, String businessID, String customerID){

        reservationsRef().child(reservationID).removeValue();
        userReservationsRef().child(businessID).child(reservationID).removeValue();
        userReservationsRef().child(customerID).child(reservationID).removeValue();

    }

    //End Subscription Status

    public FBDataService(){
    }

}