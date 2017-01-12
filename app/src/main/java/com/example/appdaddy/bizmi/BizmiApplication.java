package com.example.appdaddy.bizmi;

import  android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


/**
 * Created by AppDaddy on 12/29/16.
 */

public class BizmiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

    }


}
