package com.example.appdaddy.bizmi;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ncapdevi.fragnav.FragNavController;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabReselectListener;
import com.roughike.bottombar.OnTabSelectListener;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CustomerMainActivity extends AppCompatActivity implements BaseFragment.OnFragmentInteractionListener, FragNavController.TransactionListener, FragNavController.RootFragmentListener{

    Toolbar mToolbar;

    private BottomBar mBottomBar;
    private FragNavController mNavController;

    private final int INDEX_RECENTS = FragNavController.TAB1;
    private final int INDEX_FAVORITES = FragNavController.TAB2;
    private final int INDEX_NEARBY = FragNavController.TAB3;
    private final int INDEX_FRIENDS = FragNavController.TAB4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Bizmi");

        mBottomBar = (BottomBar) findViewById(R.id.bottomBar);
        mBottomBar.selectTabAtPosition(INDEX_RECENTS);

        mNavController =
                new FragNavController(savedInstanceState, getSupportFragmentManager(), R.id.container,this, 4, INDEX_RECENTS);
        mNavController.setTransactionListener(this);

        mBottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                switch (tabId) {
                    case R.id.bb_menu_recents:
                        mNavController.switchTab(INDEX_RECENTS);
                        break;
                    case R.id.bb_menu_favorites:
                        mNavController.switchTab(INDEX_FAVORITES);
                        break;
                    case R.id.bb_menu_nearby:
                        mNavController.switchTab(INDEX_NEARBY);
                        break;
                    case R.id.bb_menu_friends:
                        mNavController.switchTab(INDEX_FRIENDS);
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
        if (mNavController.isRootFragment()) {
            mNavController.pop();
        } else {
            super.onBackPressed();
        }
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
    public void onTabTransaction(Fragment fragment, int index) {
        // If we have a backstack, show the back button
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(mNavController.isRootFragment());
        }
    }

    @Override
    public void onFragmentTransaction(Fragment fragment) {
        //do fragmentty stuff. Maybe change title, I'm not going to tell you how to live your life
        // If we have a backstack, show the back button
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(mNavController.isRootFragment());
        }
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case INDEX_RECENTS:
                return ConversationsFragment.newInstance();
            case INDEX_FAVORITES:
                return ConversationsFragment.newInstance();
            case INDEX_NEARBY:
                return ConversationsFragment.newInstance();
            case INDEX_FRIENDS:
                return ConversationsFragment.newInstance();

        }
        throw new IllegalStateException("Need to send an index that we know");
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
