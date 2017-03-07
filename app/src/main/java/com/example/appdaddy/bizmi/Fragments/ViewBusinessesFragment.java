package com.example.appdaddy.bizmi.Fragments;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.appdaddy.bizmi.DataService.FBDataService;
import com.example.appdaddy.bizmi.R;
import com.example.appdaddy.bizmi.controller.BusinessInfoDialog;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.L;
import com.example.appdaddy.bizmi.util.Util;
import com.example.appdaddy.bizmi.widgets.CustomRecyclerView;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DatabaseError;

import org.parceler.Parcels;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.grantland.widget.AutofitHelper;

public class ViewBusinessesFragment extends Fragment {

    private BaseFragment.OnFragmentInteractionListener mListener;

    @BindView(R.id.recycler_view) CustomRecyclerView mRecyclerView;
    @BindView(R.id.empty_list) TextView mEmptyList;

    private FirebaseRecyclerAdapter mAdapter;

    public ViewBusinessesFragment() {
    }

    public static ViewBusinessesFragment newInstance() {
        return new ViewBusinessesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_businesses, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
    }

    private void setupRecyclerView(){
        mRecyclerView.showIfEmpty(mEmptyList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new FirebaseIndexRecyclerAdapter<User, BusinessHolder>(User.class, R.layout.row_business, BusinessHolder.class,
                FBDataService.getInstance().businessUserRef(), FBDataService.getInstance().usersRef()) {
            @Override
            public void populateViewHolder(final BusinessHolder businessViewHolder, final User user, int position) {
                businessViewHolder.setName(user.getBusinessName());
                businessViewHolder.setType(user.getBusinessType());
                businessViewHolder.updateProfilePicture(getActivity(), Util.getImagePathPNG(user.getUUID()));
                businessViewHolder.setLocation(getActivity(), user);

                businessViewHolder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogInfo(user);
                    }
                });
            }
        };

        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    public void showDialogInfo(User user) {

        Bundle bundle = new Bundle();
        Parcelable wrapped = Parcels.wrap(user);
        bundle.putParcelable(Constants.EXTRA_USER_PARCEL, wrapped);

        FragmentManager fragmentManager = getFragmentManager();
        BusinessInfoDialog businessInfoDialog = BusinessInfoDialog.newInstance(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, businessInfoDialog)
                .addToBackStack(null).commit();

    }

    public static class BusinessHolder extends CustomRecyclerView.ViewHolder {
        private final TextView mNameField;
        private final TextView mTypeField;
        private final TextView mLocationField;
        private final ImageView mProfilePicImg;
        GeoFire geoFire;
        private View mView;

        public BusinessHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            mNameField = (TextView) itemView.findViewById(R.id.business_name_label);
            mTypeField = (TextView) itemView.findViewById(R.id.business_type_label);
            mLocationField = (TextView) itemView.findViewById(R.id.business_location_label);
            mProfilePicImg = (ImageView) itemView.findViewById(R.id.profile_image);
            mView = itemView;

        }

        View getView(){
            return mView;
        }

        void updateProfilePicture(Context context, String path){
            Glide.with(context)
                    .using(new FirebaseImageLoader())
                    .load(FBDataService.getInstance().profilePicsStorageRef().child(path))
                    .placeholder(R.drawable.people_grey)
                    .bitmapTransform(new RoundedCornersTransformation(context, 48, 0))
                    .into(mProfilePicImg);
        }

        void setName(String name) {
            mNameField.setText(name);
        }

        void setType(String type) {
            mTypeField.setText(type);
        }

        void setLocation(final Context context, final User user) {
            geoFire = new GeoFire(FBDataService.getInstance().usersRef().child(user.getUUID()));
            geoFire.getLocation(Constants.BUSINESS_LOC, new LocationCallback() {
                @Override
                public void onLocationResult(String key, GeoLocation location) {
                    if (location != null) {
                        Address address;
                        try {
                            address = Util.reverseGeoCodeAddress(context, location);

                            mLocationField.setText(address.getLocality());

                        } catch (IOException e) {
                            Toast.makeText(context, "Could not retrieve business location for " + user.getBusinessName(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "A location has not been set by " + user.getBusinessName(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}


