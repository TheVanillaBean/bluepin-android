package com.bluepinapp.bluepin.util;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.bluepinapp.bluepin.model.*;
import com.bluepinapp.bluepin.model.Dialog;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alex on 2/19/2017.
 */

public class Util {

    public static Address reverseGeoCodeAddress(Context context, GeoLocation location) throws IOException {

        Geocoder gc = new Geocoder(context);

        List<Address> list = gc.getFromLocation(location.latitude, location.longitude, 1);

        if (list != null && list.size() > 0) {

            if (list.get(0) != null){
                return list.get(0);
            }

            return null;
        }

        return null;


    }

    @NonNull
    public static String getImagePathPNG(String UUID) {
        return UUID + ".png";
    }

    public static String getUSStateCode(Address USAddress){
        String fullAddress = "";
        for(int j = 0; j <= USAddress.getMaxAddressLineIndex(); j++)
            if (USAddress.getAddressLine(j) != null)
                fullAddress = fullAddress + " " + USAddress.getAddressLine(j);

        String stateCode = null;
        Pattern pattern = Pattern.compile(" [A-Z]{2} ");
        String helper = fullAddress.toUpperCase().substring(0, fullAddress.toUpperCase().indexOf("USA"));
        Matcher matcher = pattern.matcher(helper);
        while (matcher.find())
            stateCode = matcher.group().trim();

        return stateCode;
    }


    //MAPS
    public static boolean isGooglePlayServicesAvailable(Activity activity) {

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(activity);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(activity, result,
                        Constants.ERROR_DIALOG_REQUEST).show();
            }

            return false;
        }

        return true;
    }

    public static Address geoCodeAddress(Context context, String location) throws IOException {

        Geocoder gc = new Geocoder(context);
        List<Address> list = gc.getFromLocationName(location, 1);

        if (list != null && list.size() > 0) {

            return list.get(0);
        }

        return null;

    }

    public static LatLngBounds createBoundsWithMinDiagonal(LatLng firstMarker, LatLng secondMarker) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(firstMarker);
        builder.include(secondMarker);

        LatLngBounds tmpBounds = builder.build();
        /** Add 2 points 1000m northEast and southWest of the center.
         * They increase the bounds only, if they are not already larger
         * than this.
         * 1000m on the diagonal translates into about 709m to each direction. */
        LatLng center = tmpBounds.getCenter();
        LatLng northEast = move(center, 709, 709);
        LatLng southWest = move(center, -709, -709);
        builder.include(southWest);
        builder.include(northEast);
        return builder.build();
    }

    private static final double EARTHRADIUS = 6366198;
    /**
     * Create a new LatLng which lies toNorth meters north and toEast meters
     * east of startLL
     */
    private static LatLng move(LatLng startLL, double toNorth, double toEast) {
        double lonDiff = meterToLongitude(toEast, startLL.latitude);
        double latDiff = meterToLatitude(toNorth);
        return new LatLng(startLL.latitude + latDiff, startLL.longitude
                + lonDiff);
    }

    private static double meterToLongitude(double meterToEast, double latitude) {
        double latArc = Math.toRadians(latitude);
        double radius = Math.cos(latArc) * EARTHRADIUS;
        double rad = meterToEast / radius;
        return Math.toDegrees(rad);
    }


    private static double meterToLatitude(double meterToNorth) {
        double rad = meterToNorth / EARTHRADIUS;
        return Math.toDegrees(rad);
    }

    public static final int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public interface completionInterfaceMessage {
        public void onComplete(String error, Message message);
    }

    public interface completionInterfaceMessageDialog {
        public void onComplete(String error, MessageDialog message);
    }

    public interface completionInterfaceDialog {
        public void onComplete(String error, Dialog dialog);
    }

    public interface completionInterfaceUser {
        public void onComplete(String error, User user);
    }

    public interface completionInterface {
        public void onComplete(String error);
    }

}
