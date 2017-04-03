package com.example.appdaddy.bizmi.controller;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
import com.example.appdaddy.bizmi.Fragments.ViewCustomersFragment;
import com.example.appdaddy.bizmi.POJO.RetrieveAllFollowersEvent;
import com.example.appdaddy.bizmi.R;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.Util;
import com.example.appdaddy.bizmi.widgets.CustomRecyclerView;
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by Alex on 3/3/2017.
 */

public class SelectCustomerDialog extends DialogFragment {

    @BindView(R.id.recycler_view) CustomRecyclerView mRecyclerView;
    @BindView(R.id.empty_list) TextView mEmptyList;

    private FirebaseUser mCurrentUser;
    private FirebaseRecyclerAdapter mAdapter;

    private OnCustomerSelected mListener;

    public SelectCustomerDialog() {
    }

    public static SelectCustomerDialog newInstance() {
        return new SelectCustomerDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_customer, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);

        mCurrentUser = AuthService.getInstance().getCurrentUser();

        if(mCurrentUser != null){
            if(FBDataService.getInstance().getAllFollowers().size() < 1){
                FBDataService.getInstance().retrieveAllFollowers(mCurrentUser.getUid());
            }else{
                setupRecyclerView();
            }
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

        mAdapter = new FirebaseIndexRecyclerAdapter<User, SelectCustomerDialog.CustomerHolder>(User.class, R.layout.row_customer, SelectCustomerDialog.CustomerHolder.class,
                FBDataService.getInstance().businessFollowersRef().child(mCurrentUser.getUid()), FBDataService.getInstance().usersRef()) {
            @Override
            public void populateViewHolder(final SelectCustomerDialog.CustomerHolder customerViewHolder, final User user, int position) {
                customerViewHolder.setName(user.getFullName());
                customerViewHolder.setDuration(FBDataService.getInstance().getAllFollowersTime().get(user.getUUID()));
                customerViewHolder.updateProfilePicture(getActivity(), Util.getImagePathPNG(user.getUUID()));

                customerViewHolder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.OnCustomerSelected(user);
                        dismiss();
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

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @OnClick(R.id.cancel_btn)
    public void onCancelBtnPressed() {
        dismiss();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        NewReservationActivity a;
        if (context instanceof Activity){
            a = (NewReservationActivity) context;
            mListener = (OnCustomerSelected) a;
        }
    }

    public interface OnCustomerSelected {
        void OnCustomerSelected(User customer);
    }
}
