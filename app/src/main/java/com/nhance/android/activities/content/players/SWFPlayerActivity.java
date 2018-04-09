package com.nhance.android.activities.content.players;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.R;

public class SWFPlayerActivity extends NhanceBaseActivity {

    WebView swfPlayer;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        startVServer();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swfplayer);
        swfPlayer = (WebView) findViewById(R.id.swf_player);
        swfPlayer.getSettings().setJavaScriptEnabled(true);
        swfPlayer.getSettings().setAllowFileAccess(true);
        swfPlayer.getSettings().setPluginState(PluginState.ON);
        swfPlayer.getSettings().setUseWideViewPort(true);
        swfPlayer.getSettings().setLoadWithOverviewMode(true);
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onStart() {

        super.onStart();
        String url = getIntent().getStringExtra(ConstantGlobal.VIDEO_URL);
        swfPlayer.loadUrl(url);
    }

    @Override
    protected void onStop() {

        super.onStop();
        swfPlayer.loadUrl("about:blank");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        getMenuInflater().inflate(R.menu.activity_swfplayer, menu);
        return true;
    }

}
