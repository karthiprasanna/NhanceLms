package com.nhance.android.activities.baseclasses;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.LoginActivity;
import com.nhance.android.activities.SignupAndLoginActivity;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.services.VServerService;

import java.util.Date;

/**
 * Created by android on 11/4/2016.
 */
public abstract class NhanceBaseActivity extends AppCompatActivity {

    protected SessionManager session;
    protected TabHost mTabHost;
    protected int     mTabLayout = R.layout.tabs_layout_lightgrey_bg;

    //Satya Changes
    private AlertDialog m_cObjDialog;
    private int m_cDialogID;
    public UIHandler m_cObjUIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startVServer();
        super.onCreate(savedInstanceState);
        session = SessionManager.getInstance(getApplicationContext());
        m_cObjUIHandler = new UIHandler();
        prentInit();


        if(!hasPermissions(this, ConstantGlobal.PERMISSIONS)){
            ActivityCompat.requestPermissions(this, ConstantGlobal.PERMISSIONS, 1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        prentInit();

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    protected void addTab(final String tabId, String tabName) {

        final View tabview = createTabView(mTabHost.getContext(), tabName, tabId);
        TabHost.TabSpec setContent = mTabHost.newTabSpec(tabId).setIndicator(tabview)
                .setContent(new TabHost.TabContentFactory() {

                    @Override
                    public View createTabContent(String tag) {

                        TextView view = new TextView(mTabHost.getContext());
                        view.setText(tag);
                        return view;
                    }
                });
        Log.d("Host", "adding tab host: " + tabId + ", tabHost: " + mTabHost);
        mTabHost.addTab(setContent);
    }

    protected void updateTab(int id, String tabId, Fragment newFragment, FragmentManager fm) {
        if (newFragment == null) {
            return;
        }
        if (fm.findFragmentByTag(tabId) == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(id, newFragment);
            ft.commit();
        }
    }

    protected View createTabView(final Context context, final String text, final Object tag) {
        View view = LayoutInflater.from(context).inflate(mTabLayout, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        tv.setTag(tag);
        return view;
    }

    protected void startVServer() {
        Intent intent = new Intent(this, VServerService.class);
        startService(intent);
    }


    protected void animateAndStartActivity(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    protected void animateRightToLeftAndStartActivity(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void finishAndAnimateRightToLeft() {
        finish();
    }

    protected void addParentExtras(Intent intent) {
        intent.putExtras(getIntent());
    }

    protected View.OnClickListener getGoToPrevViewListner() {

        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                finishAndAnimateRightToLeft();
            }
        };
    }

    public boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null
                && (netInfo.getType() == ConnectivityManager.TYPE_WIFI ||
                (netInfo.getType() == ConnectivityManager.TYPE_MOBILE))
                && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public boolean showNoInternetMessageIfNotOnline() {

        boolean isOnlineVal = isOnline();
        if (!isOnlineVal) {
            Toast.makeText(getBaseContext(),
                    getResources().getString(R.string.connect_to_internet_msg), Toast.LENGTH_SHORT)
                    .show();
        }
        return isOnlineVal;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAndAnimateRightToLeft();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ConstantGlobal.SHOW_CHECK_SETTINGS) {
            hideDialog();
            prentInit();
        }
    }

    public void prentInit() {
        m_cObjUIHandler.sendEmptyMessageDelayed(ConstantGlobal.CHECK_SETTINGS, 500);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void showHighSetting(){
        try {
            boolean lDisplay = false;
            if (0 == Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME)) {
                lDisplay = true;
            } else if(0 == Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME_ZONE)){
                lDisplay = true;
            }
            if(lDisplay){
                displayErrorAlert(-1, "Please check Automatic Time Zone and Automatic date and time on device setting.", true);
            } else {
                checkExpiry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLowSetting(){
        try {
            boolean lDisplay = false;
            if (0 == android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.AUTO_TIME)) {
                lDisplay = true;
            } else if(0 == android.provider.Settings.System.getInt(getContentResolver(), Settings.System.AUTO_TIME_ZONE)){
                lDisplay = true;
            }
            if(lDisplay){
                displayErrorAlert(-1, "Please check Automatic Time Zone and Automatic date and time on device setting.", true);
            } else {
                checkExpiry();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog lRetVal = null;
        lRetVal = m_cObjDialog;
        return lRetVal;
    }

    public void hideDialog() {
        try {
            if(null != m_cObjDialog) {
                m_cObjDialog.dismiss();
                removeDialog(m_cDialogID);
            }
            m_cObjDialog = null;
            m_cDialogID =0;
        } catch (Exception e) {

        }
    }

    public void displayErrorAlert(int pStrResId, String pErrorMessage, int pId) {
        displayErrorAlert(pStrResId, pErrorMessage, false, pId);
    }

    public void displayErrorAlert(int pStrResId, String pErrorMessage, final boolean isTimeSetting) {
        displayErrorAlert(pStrResId, pErrorMessage, isTimeSetting, -1);
    }

    public void displayErrorAlert(int pStrResId, String pErrorMessage, final boolean isTimeSetting, final int pID) {
        String lMessage = (null != pErrorMessage)?pErrorMessage:getResources().getString(pStrResId);
        AlertDialog.Builder lObjBuilder = new AlertDialog.Builder(this);
        lObjBuilder.setCancelable(false);
        lObjBuilder.setTitle(R.string.app_name);
        lObjBuilder.setMessage(lMessage);
        lObjBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hideDialog();
                if(isTimeSetting){
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), ConstantGlobal.SHOW_CHECK_SETTINGS);
                }
                if(-1 != pID) {
                    m_cObjUIHandler.sendEmptyMessage(pID);
                }
            }
        });

        m_cObjDialog = lObjBuilder.create();
        m_cObjDialog.show();
    }

    public class UIHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ConstantGlobal.CHECK_SETTINGS:
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                        showHighSetting();
                    } else {
                        showLowSetting();
                    }
                    break;
                case ConstantGlobal.HANDLE_EXPIRY_ALERT:
                    session.logoutUser();
                    break;
                default:
                    break;
            }
        }
    }

    public void checkExpiry(){
        if(false == this instanceof LoginActivity && false == this instanceof SignupAndLoginActivity) {
            Date lExpireDate = ConstantGlobal.getExpiryDate(this);
            if (null != lExpireDate) {
                long lexp = lExpireDate.getTime();
                long lCurrentDate = new Date().getTime();
                if (lCurrentDate >= lexp) {
                    displayErrorAlert(-1, "Nhance LMS Expired, Please Login Again.", ConstantGlobal.HANDLE_EXPIRY_ALERT);
                }
            }
        }
    }
}

