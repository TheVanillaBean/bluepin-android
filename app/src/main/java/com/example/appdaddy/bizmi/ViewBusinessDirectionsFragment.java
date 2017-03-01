package com.example.appdaddy.bizmi;

import android.app.Dialog;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.Util;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ViewBusinessDirectionsFragment extends DialogFragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private LatLng mBusinessLocation;
    private Location mUserLocation;
    private GoogleApiClient mLocationClient;
    private Marker mBusinessMarker;

    @BindView(R.id.directions_btn) LinearLayout mDirectionsBtn;
    @BindView(R.id.directions_label) TextView mDirectionsLabel;

    public ViewBusinessDirectionsFragment() {
    }

    public static ViewBusinessDirectionsFragment newInstance(Bundle args) {
        ViewBusinessDirectionsFragment directionsFragment = new ViewBusinessDirectionsFragment();
        Double latitude = args.getDouble(Constants.EXTRA_LOCATION_LAT);
        Double longitude = args.getDouble(Constants.EXTRA_LOCATION_LONG);
        directionsFragment.mBusinessLocation = new LatLng(latitude, longitude);
        return directionsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_view_business_directions, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMap();
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

    private void goToLocation() {
        LatLng latLng = new LatLng(mBusinessLocation.latitude, mBusinessLocation.longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.moveCamera(update);
    }

    @OnClick(R.id.cancel_btn)
    public void onCancelBtnPressed() {
        dismiss();
    }

    @OnClick(R.id.directions_btn)
    public void onDirectionsBtnPressed() {
        String uri = "http://maps.google.com/maps?f=d&hl=en&saddr=" + mBusinessLocation.latitude + "," + mBusinessLocation.longitude+"&daddr=" + mUserLocation.getLatitude()+ "," + mUserLocation.getLongitude();
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(Intent.createChooser(intent, "Select an application"));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mBusinessLocation != null) {
            goToLocation();
        }

        ViewBusinessDirectionsFragmentPermissionsDispatcher.connectLocationClientWithCheck(this);

    }

    private void addMarker(String add, double lat, double lng) {
        MarkerOptions options = new MarkerOptions()
                .title(add)
                .position(new LatLng(lat, lng))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_small));

        options.snippet("USA");
        mBusinessMarker = mMap.addMarker(options);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        addMarker("", mBusinessLocation.latitude, mBusinessLocation.longitude);
        ViewBusinessDirectionsFragmentPermissionsDispatcher.showCurrentLocationWithCheck(this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "Failed to connect to maps...", Toast.LENGTH_LONG).show();
    }

    private void showLocationWithMarker(){
        LatLng userLocation = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());
        LatLng businessLocation = new LatLng(mBusinessMarker.getPosition().latitude , mBusinessMarker.getPosition().longitude);

        LatLngBounds bounds = Util.createBoundsWithMinDiagonal(userLocation, businessLocation);

        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, 250);
        mMap.animateCamera(update);

    }

    private void showLocationWithOutMarker(){
        LatLng latLng = new LatLng(mBusinessLocation.latitude, mBusinessLocation.longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 50);
        mMap.moveCamera(update);
    }

    @NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    public void showCurrentLocation() {
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient); //PermissionDispatcher handles Permission check
        if (currentLocation == null) {
            Toast.makeText(getActivity(), "Couldn't find your location...", Toast.LENGTH_SHORT).show();
        } else {
            mUserLocation = currentLocation;
            showLocationWithMarker();
        }
    }


    @NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    public void connectLocationClient() {
        mLocationClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationClient.connect();

        mMap.setMyLocationEnabled(true); //PermissionDispatcher handles Permission check

    }

    @OnShowRationale({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    void showRationaleForLocation(final PermissionRequest request) {
        showRationaleDialog("Access Location", request);
    }

    @OnPermissionDenied({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    void showDeniedForLocation() {
        Toast.makeText(getActivity(), "Location Permission Denied", Toast.LENGTH_SHORT).show();
        showLocationWithOutMarker();
    }

    @OnNeverAskAgain({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    void showNeverAskFoLocation() {
        Toast.makeText(getActivity(), "Location Permission Denied Always", Toast.LENGTH_SHORT).show();
        showLocationWithOutMarker();
    }

    private void showRationaleDialog(String messageResId, final PermissionRequest request) {

        new MaterialDialog.Builder(getActivity())
                .title(messageResId)
                .content("Bizmi needs to access your location.")
                .positiveText("Yes")
                .negativeText("No")
                .autoDismiss(false)
                .cancelable(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        request.proceed();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        request.cancel();
                    }
                })
                .typeface("Roboto-Regular.ttf", "Roboto-Light.ttf")
                .show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        ViewBusinessDirectionsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

}
