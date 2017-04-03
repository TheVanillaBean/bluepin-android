package com.example.appdaddy.bizmi.controller;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.appdaddy.bizmi.Fragments.BaseFragment;
import com.example.appdaddy.bizmi.Fragments.BusinessProfileFragment;
import com.example.appdaddy.bizmi.Fragments.ConversationsFragment;
import com.example.appdaddy.bizmi.Fragments.ViewCustomersFragment;
import com.example.appdaddy.bizmi.Fragments.ViewReservationsBusinessFragment;
import com.example.appdaddy.bizmi.POJO.UploadFileEvent;
import com.example.appdaddy.bizmi.POJO.UploadProgressEvent;
import com.example.appdaddy.bizmi.POJO.UserUpdateEvent;
import com.example.appdaddy.bizmi.PhoneVerificationActivity;
import com.example.appdaddy.bizmi.R;
import com.example.appdaddy.bizmi.util.Constants;
import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BusinessMainActivity extends AppCompatActivity implements BaseFragment.OnFragmentInteractionListener, FragNavController.TransactionListener, FragNavController. RootFragmentListener {

    Toolbar mToolbar;

    private BottomBar mBottomBar;
    private FragNavController mNavController;

    private final int INDEX_CHATS = FragNavController.TAB1;
    private final int INDEX_CUSTOMERS = FragNavController.TAB2;
    private final int INDEX_RESERVATIONS = FragNavController.TAB3;
    private final int INDEX_PROFILE = FragNavController.TAB4;

    private OnListenerCallBacks mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Bizmi");

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mBottomBar.selectTabAtPosition(INDEX_CHATS);

        mNavController =
                new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.container, this, 4, INDEX_CHATS);
        mNavController.setTransactionListener(this);

        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.bb_menu_messages:
                        mNavController.switchTab(INDEX_CHATS);
                        break;
                    case R.id.bb_menu_customers:
                        mNavController.switchTab(INDEX_CUSTOMERS);
                        break;
                    case R.id.bb_menu_reservations:
                        mNavController.switchTab(INDEX_RESERVATIONS);
                        break;
                    case R.id.bb_menu_profile:
                        mNavController.switchTab(INDEX_PROFILE);
                        break;
                }
            }
        });

        mBottomBar.setOnTabReselectListener(new OnTabReselectListener() {
            @Override
            public void onTabReSelected(@IdRes int tabId){
                mNavController.clearStack();
            }
        });
    }

    @Override
    public void onBackPressed() {
       super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mNavController != null) {
            mNavController.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onTabTransaction(Fragment fragment, int i) {

    }

    @Override
    public void onFragmentTransaction(Fragment fragment) {

    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case INDEX_CHATS:
                return ConversationsFragment.newInstance();
            case INDEX_CUSTOMERS:
                return ViewCustomersFragment.newInstance();
            case INDEX_RESERVATIONS:
                return ViewReservationsBusinessFragment.newInstance();
            case INDEX_PROFILE:
                return BusinessProfileFragment.newInstance();

        }
        throw new IllegalStateException("Illegal Index");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadProgressCallBack(UploadProgressEvent event) {
        mListener.OnUploadProgressCallBack(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadFileCallBack(UploadFileEvent event) {
        mListener.OnUploadFileCallBack(event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserUpdateCallBack(UserUpdateEvent event) {
        mListener.OnUserUpdateCallBack(event);
    }

    public void setListener(OnListenerCallBacks listener){
        this.mListener = listener;
    }

    public interface OnListenerCallBacks {
        void OnUploadProgressCallBack(UploadProgressEvent event);
        void OnUploadFileCallBack(UploadFileEvent event);
        void OnUserUpdateCallBack(UserUpdateEvent event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.business_main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_new_reservation:
                Intent intent = new Intent(BusinessMainActivity.this, NewReservationActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
