package com.nhance.android.message.Utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.nhance.android.R;
//import com.readystatesoftware.systembartint.SystemBarTintManager;

public class Utility {
   public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
   @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
   public static boolean checkPermission(final Context context) {
       int currentAPIVersion = Build.VERSION.SDK_INT;
       if (currentAPIVersion >= Build.VERSION_CODES.M) {
           if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
               if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                   AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                   alertBuilder.setCancelable(true);
                   alertBuilder.setTitle("Permission necessary");
                   alertBuilder.setMessage("External storage permission is necessary");
                   alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                       @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                       public void onClick(DialogInterface dialog, int which) {
                           ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                       }
                   });
                   AlertDialog alert = alertBuilder.create();
                   alert.show();
               } else {
                   ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
               }
               return false;
           } else {
               return true;
           }
       } else {
           return true;
       }
   }

//    public static void initStatusBar(Activity activity) {
//        Window window = activity.getWindow();
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            SystemBarTintManager tintManager = new SystemBarTintManager(activity);
//            tintManager.setStatusBarTintEnabled(true);
//            tintManager.setTintColor(ContextCompat.getColor(activity, R.color.orange));
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//            window.setStatusBarColor(ContextCompat.getColor(activity, R.color.orange));
//        }
//
//
//    }
}