package com.example.appdaddy.bizmi.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * Created by Alex on 1/25/2017.
 */

public class Dialog {

    public static void showDialog(Context context, String title, String content, String positiveText){
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText(positiveText)
                .autoDismiss(true)
                .typeface("Roboto-Regular.ttf", "Roboto-Light.ttf")
                .show();
    }


    public static MaterialDialog showProgressIndeterminateDialog(Context context, String title, String content, boolean horizontal){
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .progress(true, 0)
                .progressIndeterminateStyle(horizontal);

        return builder.build();
    }
}
