package com.bluepinapp.bluepin.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bluepinapp.bluepin.DataService.AuthService;
import com.bluepinapp.bluepin.DataService.FBDataService;
import com.bluepinapp.bluepin.POJO.UserCastEvent;
import com.bluepinapp.bluepin.R;
import com.bluepinapp.bluepin.controller.NewReservationActivity;
import com.bluepinapp.bluepin.model.Reservation;
import com.bluepinapp.bluepin.model.User;
import com.bluepinapp.bluepin.util.Constants;
import com.bluepinapp.bluepin.util.Dialog;
import com.bluepinapp.bluepin.util.Util;
import com.bluepinapp.bluepin.widgets.CustomRecyclerView;
import com.firebase.ui.database.FirebaseIndexRecyclerAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;


public class ViewReservationsBusinessFragment extends Fragment {
    private BaseFragment.OnFragmentInteractionListener mListener;

    @BindView(R.id.recycler_view) CustomRecyclerView mRecyclerView;
    @BindView(R.id.empty_list) TextView mEmptyList;

    private User mCurrentUser;

    private FirebaseIndexRecyclerAdapter mAdapter;

    public ViewReservationsBusinessFragment() {
    }

    public static ViewReservationsBusinessFragment newInstance() {
        return new ViewReservationsBusinessFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_reservations_business, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(AuthService.getInstance().getCurrentUser() != null){
            User.castUser(AuthService.getInstance().getCurrentUser().getUid());
        }else{
            Toast.makeText(getActivity(), "Error retrieving current user...", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    private void setupRecyclerView(){
        mRecyclerView.showIfEmpty(mEmptyList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new FirebaseIndexRecyclerAdapter<Reservation, ReservationHolder>(Reservation.class, R.layout.row_customer_reservation, ViewReservationsBusinessFragment.ReservationHolder.class,
                FBDataService.getInstance().userReservationsRef().child(mCurrentUser.getUUID()).orderByValue().limitToLast(25), FBDataService.getInstance().reservationsRef()) {
            @Override
            public void populateViewHolder(final ViewReservationsBusinessFragment.ReservationHolder customerViewHolder, final Reservation reservation, int position) {

                customerViewHolder.setName(reservation.getLeaderName());
                customerViewHolder.setAppointmentDate(reservation.getScheduledTime());
                customerViewHolder.setStatus(getActivity(), reservation.getStatus());
                customerViewHolder.updateProfilePicture(getActivity(), Util.getImagePathPNG(reservation.getLeaderID()));

                customerViewHolder.getView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(reservation.getStatus().equals(Constants.PENDING_STATUS) || reservation.getStatus().equals(Constants.ACTIVE_STATUS)){
                            showChangeReservationAlertDialog(reservation, reservation.getLeaderID(), reservation.getLeaderName());
                        }
                    }
                });
            }

        };

        mRecyclerView.setAdapter(mAdapter);
    }

    private void showChangeReservationAlertDialog(final Reservation reservation, final String userID, final String leaderName){
        new MaterialDialog.Builder(getActivity())
                .title("Change Reservation")
                .content("Do you want to change this reservation for " + leaderName)
                .positiveText("Yes, Change")
                .negativeText("Yes, Delete")
                .neutralText("No")
                .autoDismiss(true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        navigateToReservationActivity(reservation, userID);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showDeleteReservationAlertDialog(reservation);
                    }
                })
                .typeface("Roboto-Regular.ttf", "Roboto-Light.ttf")
                .show();
    }

    private void showDeleteReservationAlertDialog(final Reservation reservation){
        new MaterialDialog.Builder(getActivity())
                .title("Delete Reservation")
                .content("Are you sure you want to delete this reservation?")
                .positiveText("Yes, Delete")
                .negativeText("No, Don't Delete")
                .autoDismiss(true)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                         FBDataService.getInstance().removeReservationForUser(reservation.getUUID(), reservation.getBusinessID(), reservation.getLeaderID());
                    }
                })
                .typeface("Roboto-Regular.ttf", "Roboto-Light.ttf")
                .show();
    }

    private void navigateToReservationActivity(Reservation reservation, String userID){
        Bundle bundle = new Bundle();
        Parcelable wrapped = Parcels.wrap(reservation);
        bundle.putParcelable(Constants.EXTRA_RESERVATION_PARCEL, wrapped);
        bundle.putString(Constants.EXTRA_USER_ID, userID);

        Intent intent = new Intent(getActivity(), NewReservationActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserCastCallBack(UserCastEvent event) {
        if (event.getError() == null) {
            mCurrentUser = event.getUser();
            setupRecyclerView();
        } else {
            Dialog.showDialog(getActivity(), "Authentication Error", event.getError(), "Okay");
        }
    }

    public static class ReservationHolder extends CustomRecyclerView.ViewHolder {
        private final TextView mNameField;
        private final TextView mAppointmentDateField;
        private final ImageView mProfilePicImg;
        private final TextView mStatusField;
        private View mView;

        public ReservationHolder(View itemView) {
            super(itemView);
            itemView.setClickable(true);
            mNameField = (TextView) itemView.findViewById(R.id.reservation_leader_name_label);
            mAppointmentDateField = (TextView) itemView.findViewById(R.id.appointment_date_label);
            mProfilePicImg = (ImageView) itemView.findViewById(R.id.profile_image);
            mStatusField = (TextView) itemView.findViewById(R.id.status_label);
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

        void setAppointmentDate(String appointmentDate) {
            mAppointmentDateField.setText(String.format("Appointment Date: \n%s", appointmentDate));
        }

        void setStatus(Context context, String status) {
            mStatusField.setText(status);
            switch (status) {
                case Constants.PENDING_STATUS:
                    mStatusField.setTextColor(Util.getColor(context, R.color.colorAccent));
                    break;
                case Constants.ACTIVE_STATUS:
                    mStatusField.setTextColor(Util.getColor(context, android.R.color.holo_green_dark));
                    break;
                case Constants.INACTIVE_STATUS:
                    mStatusField.setTextColor(Util.getColor(context, android.R.color.holo_red_dark));
                    break;
                case Constants.DECLINED_STATUS:
                    mStatusField.setTextColor(Util.getColor(context, android.R.color.holo_red_dark));
                    break;
            }
        }

    }
}
