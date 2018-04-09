package com.nhance.android.recentActivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.nhance.android.R;

public class WebActivity extends AppCompatActivity {

    private WebView mPlayer;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mPlayer = (WebView) findViewById(R.id.you_tube_player);
        mPlayer.getSettings().setJavaScriptEnabled(true);
        mPlayer.getSettings().setAllowFileAccess(true);
        mPlayer.getSettings().setPluginState(WebSettings.PluginState.ON);
        mPlayer.getSettings().setUseWideViewPort(true);
        mPlayer.getSettings().setBuiltInZoomControls(true);
        mPlayer.getSettings().setLoadWithOverviewMode(true);
        url = getIntent().getStringExtra("url");
        mPlayer.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("viewCount", 1);
        setResult(302, intent);
        finish();
    }


}
