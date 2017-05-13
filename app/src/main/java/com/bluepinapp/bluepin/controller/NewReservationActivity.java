package com.bluepinapp.bluepin.controller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bluepinapp.bluepin.DataService.AuthService;
import com.bluepinapp.bluepin.DataService.FBDataService;
import com.bluepinapp.bluepin.POJO.UserCastEvent;
import com.bluepinapp.bluepin.R;
import com.bluepinapp.bluepin.model.Reservation;
import com.bluepinapp.bluepin.model.User;
import com.bluepinapp.bluepin.util.Constants;
import com.bluepinapp.bluepin.util.Dialog;
import com.bluepinapp.bluepin.util.L;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ServerValue;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.parceler.Parcels;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NewReservationActivity extends AppCompatActivity implements SelectCustomerDialog.OnCustomerSelected, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.select_customer_btn) TextView mSelectCustomerBtn;
    @BindView(R.id.select_time_btn) TextView mSelectTimeBtn;
    @BindView(R.id.create_appointment_btn) Button mCreateAppointmentBtn;

    private int mYear;
    private int mMonth;
    private int mDay;
    private Calendar mCalendar;
    private User mUser = null;
    private boolean mDateSet;
    private String mScheduledTime;

    private MaterialDialog progressDialog;
    private Reservation mReservation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_reservation);
        ButterKnife.bind(this);
 
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("New Reservation");
        }

        mDateSet = false;
        mCalendar = Calendar.getInstance();
        progressDialog = Dialog.showProgressIndeterminateDialog(this, "Loading...", "Creating Reservation...", false);

        if (getIntent().getExtras() != null){
            mReservation = Parcels.unwrap(getIntent().getExtras().getParcelable(Constants.EXTRA_RESERVATION_PARCEL));
            User.castUser(getIntent().getExtras().getString(Constants.EXTRA_USER_ID));
            mCalendar.setTimeInMillis(mReservation.getAppointmentTimeInterval().longValue());
            mSelectCustomerBtn.setEnabled(false);
            mCreateAppointmentBtn.setEnabled(false);
            mCreateAppointmentBtn.setText("Change Reservation");
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserCastCallBack(UserCastEvent event) {
        if (event.getError() == null) {
            mUser = event.getUser();
            mSelectCustomerBtn.setText(mUser.getFullName());
            mCreateAppointmentBtn.setEnabled(true);
        } else {
            Dialog.showDialog(this, "Authentication Error", event.getError(), "Okay");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        mReservation = null;
        mUser = null;
        super.onStop();
    }

    @OnClick(R.id.select_customer_btn)
    public void onSelectCustomerBtnPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SelectCustomerDialog selectCustomerDialog = SelectCustomerDialog.newInstance();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(android.R.id.content, selectCustomerDialog)
                .addToBackStack(null).commit();
    }

    @OnClick(R.id.select_time_btn)
    public void onSelectTimeBtnPressed() {
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                this,
                mCalendar.get(Calendar.YEAR),
                mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dpd.vibrate(true);
        dpd.autoDismiss(true);
        dpd.setVersion(DatePickerDialog.Version.VERSION_1);
        dpd.setTitle("Choose Appointment Date");
        dpd.dismissOnPause(true);
        dpd.setMinDate(Calendar.getInstance());
        dpd.show(getFragmentManager(), "Datepickerdialog");
    }

    @OnClick(R.id.create_appointment_btn)
    public void onCreateAppointBtnPressed() {

        if(mUser != null && mDateSet){

            if ((AuthService.getInstance().getCurrentUser()) != null){

                final String currentUserID = AuthService.getInstance().getCurrentUser().getUid();
                final String key = getKey();

                mReservation = new Reservation(
                        key,
                        Constants.PENDING_STATUS,
                        mScheduledTime,
                        mUser.getUUID(),
                        currentUserID,
                        (double) mCalendar.getTimeInMillis(),
                        mUser.getFullName());

                progressDialog.show();
                FBDataService.getInstance().reservationsRef().child(key).setValue(mReservation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            FBDataService.getInstance().reservationsRef().child(key).child(Constants.RESERVATION_TIMESTAMP).setValue(ServerValue.TIMESTAMP);
                            FBDataService.getInstance().userReservationsRef().child(mUser.getUUID()).child(key).setValue(mCalendar.getTimeInMillis());
                            FBDataService.getInstance().userReservationsRef().child(currentUserID).child(key).setValue(mCalendar.getTimeInMillis());
                            finish();
                        }else{
                            Toast.makeText(NewReservationActivity.this, "Failed to create reservation. Please try again... ", Toast.LENGTH_LONG).show();

                        }
                    }
                });

            }else{
                Toast.makeText(NewReservationActivity.this, "Could not retrieve current user... ", Toast.LENGTH_LONG).show();
            }

        }else{
            Toast.makeText(NewReservationActivity.this, "One or more field is empty... ", Toast.LENGTH_LONG).show();
        }

    }

    private String getKey(){
        return (mReservation != null) ? mReservation.getUUID() : FBDataService.getInstance().reservationsRef().push().getKey();
    }

    @Override
    public void OnCustomerSelected(User customer) {
        mUser = customer;
        mSelectCustomerBtn.setText(mUser.getFullName());
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        mYear = year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;

        TimePickerDialog tpd = TimePickerDialog.newInstance(
                this,
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                false
        );
        tpd.vibrate(true);
        tpd.setVersion(TimePickerDialog.Version.VERSION_1);
        tpd.setTitle("Choose Appointment Time");
        tpd.dismissOnPause(true);
        tpd.show(getFragmentManager(), "Timepickerdialog");

    }

    @Override
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        mCalendar.set(mYear, mMonth, mDay, hourOfDay, minute);
        SimpleDateFormat sdf = new SimpleDateFormat("E, d MMM yyyy hh:mm a", Locale.US);
        mScheduledTime = sdf.format(mCalendar.getTimeInMillis());
        mSelectTimeBtn.setText(mScheduledTime);
        mDateSet = true;
        L.m("You picked the following date: " + mCalendar.getTimeInMillis());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
