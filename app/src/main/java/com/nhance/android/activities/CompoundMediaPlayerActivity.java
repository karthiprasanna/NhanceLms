package com.nhance.android.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.R;

public class CompoundMediaPlayerActivity extends NhanceBaseActivity {

    private static final String TAG = "CompoundMediaPlayerActivity";

    WebView    mPlayer;

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        startVServer();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compound_media_player);
        mPlayer = (WebView) findViewById(R.id.media_player);
        mPlayer.getSettings().setJavaScriptEnabled(true);
        mPlayer.getSettings().setAllowFileAccess(true);
        mPlayer.getSettings().setPluginState(PluginState.ON);
        mPlayer.getSettings().setUseWideViewPort(true);
        mPlayer.getSettings().setBuiltInZoomControls(true);
        mPlayer.getSettings().setLoadWithOverviewMode(true);
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onStart() {

        super.onStart();
        String mediaUrl = getIntent().getStringExtra(ConstantGlobal.MEDIA_URL);
        Log.d(TAG, "media url : " + mediaUrl);
        mPlayer.loadUrl(mediaUrl);
    }

    @Override
    protected void onStop() {

        super.onStop();
        mPlayer.loadUrl("about:blank");
    }

}
