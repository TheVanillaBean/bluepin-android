package com.example.appdaddy.bizmi.util;

/**
 * Created by Alex on 1/25/2017.
 */

public class Constants {

    public static final String SINCH_API_KEY = "fb9c06d5-53e7-4d60-83d0-cb853587884a";
    public static final String SINCH_API_SECRET = "8QqCD+re2UKeC2MscmZujw==";

    //Maps Error Code
    public static final int ERROR_DIALOG_REQUEST = 9001;

    //User Types
    public static final String USER_BUSINESS_TYPE = "Business";
    public static final String USER_CUSTOMER_TYPE = "Customer";

    //putExtra
    public static final String EXTRA_PHONE_NUMBER = "phoneNumberExtra";
    public static final String EXTRA_USER_ID = "userIDExtra";
    public static final String EXTRA_USER_PARCEL = "userParcel";
    public static final String EXTRA_RESERVATION_PARCEL = "reservationParcel";
    public static final String EXTRA_LOCATION_LAT = "locationLatExtra";
    public static final String EXTRA_LOCATION_LONG = "locationLongExtra";
    public static final String EXTRA_WEBSITE = "websiteExtra";
    public static final String EXTRA_DURATION = "durationExtra";

    //String Signiture Keys
    public static final String SIG_NOT_UPDATED = "No";
    public static final String SIG_YES_UPDATED = "Yes";

    //Model - User
    public static final String UUID = "uuid";
    public static final String EMAIL = "email";
    public static final String USER_TYPE = "userType";
    public static final String FULL_NAME = "fullName";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String PHONE_NUMBER_VERIFIED = "phoneNumberVerified";
    public static final String PASSWORD = "password";
    public static final String BUSINESS_NAME = "businessName";
    public static final String BUSINESS_TYPE = "businessType";
    public static final String BUSINESS_DESC = "businessDesc";
    public static final String BUSINESS_WEBSITE = "businessWebsite";
    public static final String BUSINESS_HOURS = "businessHours";
    public static final String USER_PROFILE_PIC_LOC = "userProfilePicLocation";
    public static final String BUSINESS_LOC = "businessLocation";
    public static final String DEVICE_TOKEN = "deviceToken";

    //Firebase Messages

    public static final String MESSAGE_UID = "messageID";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String MESSAGE_LOCATION = "messageLocation";
    public static final String MESSAGE_DATA = "messageData";
    public static final String MESSAGE_SENDERID = "senderUID";
    public static final String MESSAGE_RECIPIENTID = "recipientUID";
    public static final String MESSAGE_TIMESTAMP = "timeStamp";
    public static final String MESSAGE_CHANNEL_NAME = "channelName";
    public static final String MESSAGE_RECIPIENT_NAME = "recipientName";
    public static final String MESSAGE_SENDER_NAME = "senderName";

    public static final String MESSAGE_TEXT_TYPE = "type/text";

    //Firebase Notification Requests
    public static final String REQUEST_ID = "requestID";
    public static final String REQUEST_SENDER_ID = "senderID";
    public static final String REQUEST_RECIPIENT_ID = "recipientID";
    public static final String REQUEST_SENDER_NAME = "senderName";
    public static final String REQUEST_MESSAGE = "message";

    public static final String MESSAGE_NOTIF = "Message";
    public static final String NEW_RESERVATION_NOTIF = "Reservation";
    public static final String EXISTING_RESERVATION_NOTIF = "ReservationChange";
    public static final String DELETED_RESERVATION_NOTIF = "ReservationDeleted";
    public static final String ACCEPTED_RESERVATION_NOTIF = "AcceptedRes";
    public static final String DECLINED_RESERVATION_NOTIF = "DeclinedRes";

    //Firebase Reservations
    public static final String RESERVATION_UID = "uuid";
    public static final String RESERVATION_STATUS = "status";
    public static final String RESERVATION_TIMESTAMP = "timestamp";
    public static final String RESERVATION_SCHEDULED_TIME = "scheduledTime";
    public static final String RESERVATION_PARTY_LEADER_ID = "leaderID";
    public static final String RESERVATION_BUSINESS_ID = "businessID";
    public static final String RESERVATION_APPOINTMENT_TIME_INTERVAL = "appointmentTimeInterval";

    public static final String PENDING_STATUS = "pending";
    public static final String ACTIVE_STATUS = "active";
    public static final String INACTIVE_STATUS = "inactive";
    public static final String DECLINED_STATUS = "declined";

    //CHILD NODES
    public static final String FIR_CHILD_USERS = "Users";
    public static final String FIR_CHILD_USERS_BUSINESS = "Business-Users";
    public static final String FIR_CHILD_USERS_CUSTOMER = "Customer-Users";
    public static final String FIR_CHILD_USER_CHANNELS = "User-Channels";
    public static final String FIR_CHILD_CHANNELS = "Channels";
    public static final String FIR_CHILD_MESSAGES = "Messages";
    public static final String FIR_CHILD_RESERVATIONS = "Reservations";
    public static final String FIR_CHILD_NOTIFICATIONS = "notificationRequests";
    public static final String FIR_CHILD_CUSTOMER_FOLLOWERS = "Customer-Followers";
    public static final String FIR_CHILD_BUSINESS_FOLLOWERS = "Business-Followers";
    public static final String FIR_CHILD_USER_RESERVATIONS = "User-Reservations";

    //STORAGE NODES
    public static final String FIR_STORAGE_CHILD_USER_PROFILE_PICS = "Profile-Pictures";
    public static final String FIR_STORAGE_CHILD_MESSAGES = "Messages";

}
