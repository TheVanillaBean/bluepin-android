package com.example.appdaddy.bizmi.controller;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.appdaddy.bizmi.DataService.AuthService;
import com.example.appdaddy.bizmi.DataService.FBDataService;
import com.example.appdaddy.bizmi.R;
import com.example.appdaddy.bizmi.model.User;
import com.example.appdaddy.bizmi.util.Constants;
import com.example.appdaddy.bizmi.util.L;
import com.example.appdaddy.bizmi.util.Util;
import com.firebase.ui.storage.images.FirebaseImageLoader;

import org.greenrobot.eventbus.EventBus;
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

public class CustomerInfoDialog extends DialogFragment {

    @BindView(R.id.customer_image) ImageView mCustomerProfileImage;
    @BindView(R.id.customer_name_label) TextView mNameLabel;
    @BindView(R.id.customer_duration_label) TextView mDurationLabel;
    @BindView(R.id.messages_sent_labeel) TextView mMessagesSentLabel;
    @BindView(R.id.reservations_made_label) TextView mReservationsMadeLabel;

    private User mCustomerUser;
    private Long mDuration;

    public CustomerInfoDialog() {
    }

    public static CustomerInfoDialog newInstance(Bundle args) {
        CustomerInfoDialog customerInfoDialog = new CustomerInfoDialog();
        if(Parcels.unwrap(args.getParcelable(Constants.EXTRA_USER_PARCEL)) != null){
            customerInfoDialog.mCustomerUser = Parcels.unwrap(args.getParcelable(Constants.EXTRA_USER_PARCEL));
            customerInfoDialog.mDuration = args.getLong(Constants.EXTRA_DURATION);
        }
        return customerInfoDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_customer_info, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mCustomerUser != null) {
            populateDataFields();
        }
    }

    private void populateDataFields() {
        mNameLabel.setText(mCustomerUser.getFullName());
        setDuration(mDuration);
        setCustomerProfileImage(getActivity(), Util.getImagePathPNG(mCustomerUser.getUUID()));

    }

    private void setCustomerProfileImage(Context context, String path){
        Glide.with(context)
                .using(new FirebaseImageLoader())
                .load(FBDataService.getInstance().profilePicsStorageRef().child(path))
                .placeholder(R.drawable.people_grey)
                .bitmapTransform(new RoundedCornersTransformation(context, 48, 0))
                .into(mCustomerProfileImage);
    }

    public void setDuration(Long duration) {
        Date date = new Date(duration);
        DateFormat formatter = new SimpleDateFormat("MMM dd yyyy", Locale.US);
        String dateFormatted = formatter.format(date);
        mDurationLabel.setText("Customer since: " + dateFormatted);
    }

    @OnClick(R.id.cancel_btn)
    public void onCancelBtnPressed() {
        dismiss();
    }

    @OnClick(R.id.message_btn)
    public void onMessageBtnPressed() {
        dismiss();
    }
}
