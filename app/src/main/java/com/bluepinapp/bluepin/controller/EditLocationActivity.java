package com.bluepinapp.bluepin.controller;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bluepinapp.bluepin.DataService.FBDataService;
import com.bluepinapp.bluepin.R;
import com.bluepinapp.bluepin.model.User;
import com.bluepinapp.bluepin.util.Constants;
import com.bluepinapp.bluepin.util.Util;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseError;

import org.parceler.Parcels;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@RuntimePermissions
public class EditLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.search_view) EditText mSearchView;

    private GoogleMap mMap;
    private LatLng mBusinessLocation;
    private Location mUserLocation;
    private GoogleApiClient mLocationClient;
    private Marker mBusinessMarker;
    private User mCurrentUser;

    private GeoFire mGeoFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_business_location);
        ButterKnife.bind(this);

        mSearchView.setEnabled(false);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Adjust Location");
        }

        if(Parcels.unwrap(getIntent().getExtras().getParcelable(Constants.EXTRA_USER_PARCEL)) != null){
            this.mCurrentUser = Parcels.unwrap(getIntent().getExtras().getParcelable(Constants.EXTRA_USER_PARCEL));
            mGeoFire = new GeoFire(FBDataService.getInstance().usersRef().child(mCurrentUser.getUUID()));
            mGeoFire.getLocation(Constants.BUSINESS_LOC, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    if (location != null) {
                        mBusinessLocation = new LatLng(location.latitude, location.longitude);
                    } else {
                        Toast.makeText(EditLocationActivity.this, "A location has not been set by " + mCurrentUser.getBusinessName(), Toast.LENGTH_SHORT).show();
                    }
                    setupMap();
                    mSearchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            boolean handled = false;
                            if (actionId == EditorInfo.IME_ACTION_DONE) {

                                centerLocation(v.getText().toString());
                                handled = true;
                                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                                if(imm.isAcceptingText()) { // verify if the soft keyboard is open
                                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                                }
                            }
                            return handled;
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(EditLocationActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void centerLocation(String addressSearch){

        if(addressSearch != null && addressSearch.length() > 5){
            Address address;
            try {
                address = Util.geoCodeAddress(this, addressSearch);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());
                goToLocation(location);
            } catch (IOException e) {
                Toast.makeText(this, "Could not find address", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void setupMap() {
        if (Util.isGooglePlayServicesAvailable(this)) {
            initMap();
        } else {
            Toast.makeText(this, "Could not initiate map...", Toast.LENGTH_LONG).show();
        }
    }

    private void initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
            mapFragment.getMapAsync(this);
        }
    }

    private void goToLocation(LatLng location) {
        LatLng latLng = new LatLng(location.latitude, location.longitude);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 20);
        mMap.animateCamera(update);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mBusinessLocation != null) {
            goToLocation(mBusinessLocation);
        }

        mSearchView.setEnabled(true);
        EditLocationActivityPermissionsDispatcher.connectLocationClientWithCheck(this);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        EditLocationActivityPermissionsDispatcher.showCurrentLocationWithCheck(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Failed to connect to maps...", Toast.LENGTH_LONG).show();
    }

    @NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    public void showCurrentLocation() {
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient); //PermissionDispatcher handles Permission check
        if (currentLocation == null) {
            Toast.makeText(this, "Couldn't find your location...", Toast.LENGTH_SHORT).show();
        } else {
            mUserLocation = currentLocation;
            if(mBusinessLocation != null){
                goToLocation(mBusinessLocation);
            }else{
                LatLng latLng = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());
                goToLocation(latLng);
            }
        }
    }

    @NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    public void connectLocationClient() {
        mLocationClient = new GoogleApiClient.Builder(this)
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
        Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
        if(mBusinessLocation != null){
            goToLocation(mBusinessLocation);
        }else if(mUserLocation != null){
            LatLng latLng = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());
            goToLocation(latLng);
        } else{
            LatLng latLng = new LatLng(0, 0);
            goToLocation(latLng);
        }
    }

    @OnNeverAskAgain({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    void showNeverAskFoLocation() {
        Toast.makeText(this, "Location Permission Denied Always", Toast.LENGTH_SHORT).show();
        if(mBusinessLocation != null){
            goToLocation(mBusinessLocation);
        }else if(mUserLocation != null){
            LatLng latLng = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());
            goToLocation(latLng);
        } else{
            LatLng latLng = new LatLng(0, 0);
            goToLocation(latLng);
        }
    }

    private void showRationaleDialog(String messageResId, final PermissionRequest request) {

        new MaterialDialog.Builder(this)
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
        EditLocationActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void saveLocationAndExit(){
        mSearchView.setEnabled(false);
        LatLng latLng = mMap.getCameraPosition().target;
        GeoLocation newLocation = new GeoLocation(latLng.latitude, latLng.longitude);
        mGeoFire.setLocation(Constants.BUSINESS_LOC, newLocation, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_location_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_done:
                saveLocationAndExit();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
