package com.example.appdaddy.bizmi.model;

/**
 * Created by Alex on 1/21/2017.
 */

import com.example.appdaddy.bizmi.DataService.FBDataService;
import com.example.appdaddy.bizmi.POJO.ReservationEvent;
import com.example.appdaddy.bizmi.POJO.UserCastEvent;
import com.example.appdaddy.bizmi.util.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcel;

import java.util.HashMap;
import java.util.Map;

@Parcel /* Variables are not private because of the Parcel Dependency - Reflection */
@IgnoreExtraProperties
public class Reservation {

    String uuid;
    String status;
    String scheduledTime;
    String leaderID;
    String businessID;
    String leaderName;

    Double appointmentTimeInterval;

    public String getUUID() {
        return (uuid == null) ? "" : uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStatus() {
        return (status == null) ? "" : status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getScheduledTime() {
        return (scheduledTime == null) ? "" : scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getLeaderID() {
        return (leaderID == null) ? "" : leaderID;
    }

    public void setLeaderID(String leaderID) {
        this.leaderID = leaderID;
    }

    public String getBusinessID() {
        return (businessID == null) ? "" : businessID;
    }

    public void setBusinessID(String businessID) {
        this.businessID = businessID;
    }

    public String getLeaderName() { return (leaderName == null) ? "" : leaderName;  }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public Double getAppointmentTimeInterval() {
        return (appointmentTimeInterval == null) ? 0.0 : appointmentTimeInterval;
    }

    public void setAppointmentTimeInterval(Double appointmentTimeInterval) {
        this.appointmentTimeInterval = appointmentTimeInterval;
    }

    public Reservation() {
    }

    public Reservation(String uuid, String status, String scheduledTime, String leaderID, String businessID, Double appointmentTimeInterval, String leaderName) {
        this.uuid = uuid;
        this.status = status;
        this.scheduledTime = scheduledTime;
        this.leaderID = leaderID;
        this.businessID = businessID;
        this.appointmentTimeInterval = appointmentTimeInterval;
        this.leaderName = leaderName;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put(Constants.RESERVATION_UID, uuid);
        result.put(Constants.RESERVATION_STATUS, status);
        result.put(Constants.RESERVATION_SCHEDULED_TIME, scheduledTime);
        result.put(Constants.RESERVATION_PARTY_LEADER_ID, leaderID);
        result.put(Constants.RESERVATION_BUSINESS_ID, businessID);
        result.put(Constants.RESERVATION_APPOINTMENT_TIME_INTERVAL, appointmentTimeInterval);
        result.put(Constants.RESERVATION_PARTY_LEADER_NAME, leaderName);
        return result;

    }

    public static void castReservation(String uuid){

        FBDataService.getInstance().reservationsRef().child(uuid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) return;

                Reservation reservation = dataSnapshot.getValue(Reservation.class);

                EventBus.getDefault().post(new ReservationEvent(null, reservation));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                EventBus.getDefault().post(new UserCastEvent(databaseError.getMessage(), null));
            }
        });

    }

}
