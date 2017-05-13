package com.bluepinapp.bluepin;

import  android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.bluepinapp.bluepin.util.Constants;
import com.sinch.verification.Config;
import com.sinch.verification.SinchVerification;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


/**
 * Created by AppDaddy on 12/29/16.
 */

public class BizmiApplication extends Application {

    private static Config mConfig;

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        mConfig = SinchVerification.config().applicationKey(Constants.SINCH_API_KEY).context(getApplicationContext()).build();
    }

    public static Config getSinchConfig(){
        return mConfig;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
