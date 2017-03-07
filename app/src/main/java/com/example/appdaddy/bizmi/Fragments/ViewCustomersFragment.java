package com.example.appdaddy.bizmi.Fragments;

import android.content.Context;
import android.location.Address;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.appdaddy.bizmi.DataService.AuthService;
import com.example.appdaddy.bizmi.DataService.FBDataService;
import com.example.appdaddy.bizmi.POJO.AuthEvent;
import com.example.appdaddy.bizmi.POJO.RetrieveAllFollowersEvent;
import com.example.appdaddy.bizmi.R;
import com.example.appdaddy.bizmi.controller.BusinessInfoDialog;
import com.example.appdaddy.bizmi.controller.BusinessSignUpActivity;
import com.example.appdaddy.bizmi.controller.CustomerInfoDialog;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.Dialog;
import com.example.appdaddy.bizmi.util.L;
import com.example.appdaddy.bizmi.util.Util;
import com.example.appdaddy.bizmi.widgets.CustomRecyclerView;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


public class ViewCustomersFragment extends Fragment {
    private BaseFragment.OnFragmentInteractionListener mListener;

    @BindView(R.id.recycler_view) CustomRecyclerView mRecyclerView;
    @BindView(R.id.empty_list) TextView mEmptyList;

    private FirebaseUser mCurrentUser;

    private FirebaseRecyclerAdapter mAdapter;

    public ViewCustomersFragment() {
    }

    public static ViewCustomersFragment newInstance() {
        return new ViewCustomersFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_customers, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCurrentUser = AuthService.getInstance().getCurrentUser();

        if(mCurrentUser != null){
            FBDataService.getInstance().retrieveAllFollowers(mCurrentUser.getUid());
        }else{
            Toast.makeText(getActivity(), "Error retrieving current user...", Toast.LENGTH_LONG).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFollowersRetrieved(RetrieveAllFollowersEvent event) {

        if (event.getError() == null) {
            setupRecyclerView();
        }
    }

    private void setupRecyclerView(){
        mRecyclerView.showIfEmpty(mEmptyList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new FirebaseIndexRecyclerAdapter<User, ViewCustomersFragment.CustomerHolder>(User.class, R.layout.row_customer, ViewCustomersFragment.CustomerHolder.class,
                FBDataService.getInstance().businessFollowersRef().child(mCurrentUser.getUid()), FBDataService.getInstance().usersRef()) {
            @Override
            public void populateViewHolder(final ViewCustomersFragment.CustomerHolder customerViewHolder, final User user, int position) {
                customerViewHolder.setName(user.getFullName());
                customerViewHolder.setDuration(FBDataService.getInstance().getAllFollowersTime().get(user.getUUID()));
                customerViewHolder.updateProfilePicture(getActivity(), Util.getImagePathPNG(user.getUUID()));

                customerViewHolder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogInfo(user, FBDataService.getInstance().getAllFollowersTime().get(user.getUUID()));
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

    public void showDialogInfo(User user, Long duration) {

        Bundle bundle = new Bundle();
        Parcelable wrapped = Parcels.wrap(user);
        bundle.putParcelable(Constants.EXTRA_USER_PARCEL, wrapped);
        bundle.putLong(Constants.EXTRA_DURATION, duration);

        FragmentManager fragmentManager = getFragmentManager();
        CustomerInfoDialog customerInfoDialog = CustomerInfoDialog.newInstance(bundle);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, customerInfoDialog)
                .addToBackStack(null).commit();

    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public static class CustomerHolder extends CustomRecyclerView.ViewHolder {
        private final TextView mNameField;
        private final TextView mDurationField;
        private final ImageView mProfilePicImg;
        private View mView;

        public CustomerHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            mNameField = (TextView) itemView.findViewById(R.id.customer_name_label);
            mDurationField = (TextView) itemView.findViewById(R.id.following_duration_label);
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

        void setDuration(Long duration) {
            Date date = new Date(duration);
            DateFormat formatter = new SimpleDateFormat("MMM dd yyyy", Locale.US);
            String dateFormatted = formatter.format(date);
            mDurationField.setText("Customer since: " + dateFormatted);
        }

    }
}
