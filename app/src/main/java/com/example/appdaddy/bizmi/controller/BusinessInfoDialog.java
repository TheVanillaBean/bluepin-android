package com.example.appdaddy.bizmi.controller;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.appdaddy.bizmi.DataService.AuthService;
import com.example.appdaddy.bizmi.DataService.FBDataService;
import com.example.appdaddy.bizmi.Fragments.ViewWebsiteFragment;
import com.example.appdaddy.bizmi.POJO.AuthEvent;
import com.example.appdaddy.bizmi.POJO.RetrieveSubscriptionStatusEvent;
import com.example.appdaddy.bizmi.R;
import com.example.appdaddy.bizmi.ViewBusinessDirectionsFragment;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.L;
import com.example.appdaddy.bizmi.util.Util;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class BusinessInfoDialog extends DialogFragment implements OnMapReadyCallback {

    @BindView(R.id.cancel_btn) ImageButton mCancelBtn;
    @BindView(R.id.business_image) ImageView mBusinessProfileImage;
    @BindView(R.id.business_name_label) TextView mNameLabel;
    @BindView(R.id.business_type_label) TextView mTypeLabel;
    @BindView(R.id.business_location_btn) LinearLayout mLocationBtn;
    @BindView(R.id.business_location_label) TextView mLocationLabel;
    @BindView(R.id.message_btn) LinearLayout mMessagebtn;
    @BindView(R.id.phone_btn) LinearLayout mPhoneBtn;
    @BindView(R.id.phone_label) TextView mPhoneLabel;
    @BindView(R.id.website_btn) LinearLayout mWebsiteBtn;
    @BindView(R.id.hours_btn) LinearLayout mHoursBtn;
    @BindView(R.id.about_lbl) TextView mAboutLabel;
    @BindView(R.id.business_desc_label) TextView mDescLabel;
    @BindView(R.id.subscribe_btn) Button mSubscribeBtn;

    private User mBusinessUser;
    private GeoFire mGeoFire;
    private GeoLocation mLocation;

    private GoogleMap mMap;

    public BusinessInfoDialog() {
    }

    public static BusinessInfoDialog newInstance(Bundle args) {
        BusinessInfoDialog businessInfoDialog = new BusinessInfoDialog();
        if(Parcels.unwrap(args.getParcelable(Constants.EXTRA_USER_PARCEL)) != null){
            businessInfoDialog.mBusinessUser = Parcels.unwrap(args.getParcelable(Constants.EXTRA_USER_PARCEL));
        }
        return businessInfoDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_business_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        if (mBusinessUser != null) {
            populateDataFields();
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void populateDataFields() {
        mNameLabel.setText(mBusinessUser.getBusinessName());
        mTypeLabel.setText(mBusinessUser.getBusinessType());
        mPhoneLabel.setText(mBusinessUser.getPhoneNumber());
        mDescLabel.setText(mBusinessUser.getBusinessDesc());
        mAboutLabel.setText(getFormattedAbout());
        if(AuthService.getInstance().getCurrentUser() != null){
            mSubscribeBtn.setText("Loading...");
            mSubscribeBtn.setEnabled(false);
            L.m("subscribe button" + mBusinessUser.getBusinessName());
            FBDataService.getInstance().retrieveSubscriptionStatus(AuthService.getInstance().getCurrentUser().getUid(), mBusinessUser.getUUID());
        }
        setBusinessProfileImage(getActivity(), Util.getImagePathPNG(mBusinessUser.getUUID()));
        setLocation(getActivity());
        setupMap();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSubscriptionStatusRetrieved(RetrieveSubscriptionStatusEvent event) {
        mSubscribeBtn.setEnabled(true);
        if (event.getError() == null){
            if(event.getStatus()){
                mSubscribeBtn.setText("Unsubscribe from Business");
                mSubscribeBtn.setBackgroundColor(Util.getColor(getActivity(), R.color.colorAccent));
            }else{
                mSubscribeBtn.setText("Subscribe to Business");
                mSubscribeBtn.setBackgroundColor(Util.getColor(getActivity(), R.color.colorPrimaryDark));
            }
        }else{
            mSubscribeBtn.setText("Subscribe to Business");
            Toast.makeText(getActivity(), "Could not retrieve subscription status...", Toast.LENGTH_LONG).show();
        }
    }

    private void setupMap() {
        if (Util.isGooglePlayServicesAvailable(getActivity())) {
            initMap();
        } else {
            Toast.makeText(getActivity(), "Could not initiate map...", Toast.LENGTH_LONG).show();
        }
    }

    private void initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_view);
            mapFragment.getMapAsync(this);
        }
    }

    @NonNull
    private String getFormattedAbout() {
        return "About " + mBusinessUser.getBusinessName();
    }

    private void setBusinessProfileImage(Context context, String path){
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(FBDataService.getInstance().profilePicsStorageRef().child(path))
                .placeholder(R.drawable.people_grey)
                .bitmapTransform(new RoundedCornersTransformation(context, 48, 0))
                .into(mBusinessProfileImage);
    }

    public void setLocation(final Context context) {
        mGeoFire = new GeoFire(FBDataService.getInstance().usersRef().child(mBusinessUser.getUUID()));
        mGeoFire.getLocation(Constants.BUSINESS_LOC, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    mLocation = location;
                    Address address;
                    try {
                        address = Util.reverseGeoCodeAddress(context, location);
                        mLocationLabel.setText( address.getAddressLine(0) + ", " + address.getLocality() + ", " + Util.getUSStateCode(address) + " " + address.getPostalCode() );
                    } catch (IOException e) {
                        Toast.makeText(context, "Could not retrieve business location for " + mBusinessUser.getBusinessName(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "A location has not been set by " + mBusinessUser.getBusinessName(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocation != null) {
            goToLocation();
        }
    }

    private void goToLocation() {
        LatLng latLng = new LatLng(mLocation.latitude, mLocation.longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.moveCamera(update);
    }

    public void showMapDialog() {

        Bundle bundle = new Bundle();
        bundle.putDouble(Constants.EXTRA_LOCATION_LAT, mLocation.latitude);
        bundle.putDouble(Constants.EXTRA_LOCATION_LONG, mLocation.longitude);

        FragmentManager fragmentManager = getFragmentManager();
        ViewBusinessDirectionsFragment directionsFragment = ViewBusinessDirectionsFragment.newInstance(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, directionsFragment)
                .addToBackStack(null).commit();

    }

    public void showWebDialog() {

        if(mBusinessUser.getBusinessWebsite().length() > 3){
            Bundle bundle = new Bundle();
            bundle.putString(Constants.EXTRA_WEBSITE, mBusinessUser.getBusinessWebsite());

            FragmentManager fragmentManager = getFragmentManager();
            ViewWebsiteFragment websiteFragment = ViewWebsiteFragment.newInstance(bundle);
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, websiteFragment)
                    .addToBackStack(null).commit();
        }else{
            Toast.makeText(getActivity(), mBusinessUser.getBusinessName() + " does not currently have a website...", Toast.LENGTH_LONG).show();
        }

    }

    @OnClick(R.id.cancel_btn)
    public void onCancelBtnPressed() {
        dismiss();
    }

    @OnClick(R.id.business_location_btn)
    public void onLocBtnPressed() {
        showMapDialog();
    }

    @OnClick(R.id.message_btn)
    public void onMessageBtnPressed() {
        dismiss();
    }

    @OnClick(R.id.phone_btn)
    public void onPhoneBtnPressed() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + mBusinessUser.getPhoneNumber()));
        startActivity(intent);     }

    @OnClick(R.id.website_btn)
    public void onWebsiteSignUpBtnPressed() {
        showWebDialog();
    }

    @OnClick(R.id.hours_btn)
    public void onHoursBtnPressed() {
        com.example.appdaddy.bizmi.util.Dialog.showDialog(getActivity(),"Business Hours", mBusinessUser.getBusinessHours(), "Okay");
    }

    @OnClick(R.id.subscribe_btn)
    public void onSubscribeBtnPressed() {
        if(AuthService.getInstance().getCurrentUser() != null){
            mSubscribeBtn.setText("Loading...");
            mSubscribeBtn.setEnabled(false);
            FBDataService.getInstance().subscribeToBusiness(AuthService.getInstance().getCurrentUser().getUid(), mBusinessUser.getUUID());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
