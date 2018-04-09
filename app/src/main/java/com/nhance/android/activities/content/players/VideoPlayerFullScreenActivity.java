 package com.nhance.android.activities.content.players;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.async.tasks.AbstractVedantuAsynTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.enums.EncryptionLevel;
import com.nhance.android.enums.EntityType;
import com.nhance.android.managers.FileManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.processors.FileMaskProcessor;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.R;

public class VideoPlayerFullScreenActivity extends NhanceBaseActivity {

    private static final String TAG           = "VideoPlayerFullScreenActivity";
    VideoView                   mEmbededPlayer;
    WebView                     mWebPlayer;
    private MediaController     mMediaController;
    View                        mProgressView;
    private String              videoUrl      = null;
    private boolean             useHtmlPlayer = false;
    private boolean             downloaded    = false;
    private boolean             mIsSDCard;
    private LibraryContentRes   mVideo;
    private ContentDataManager          mContentDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_VIDEO);
        GoogleAnalyticsUtils.setCustomDimensions(
                getIntent().getStringExtra(ConstantGlobal.ENTITY_ID),
                getIntent().getStringExtra(ConstantGlobal.NAME), ConstantGlobal.GA_SCREEN_VIDEO);

        startVServer();



        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_video_player_full_screen);

        String linkId = getIntent().getStringExtra(ConstantGlobal.LINK_ID);
        mIsSDCard = getIntent().getBooleanExtra("SD_CARD", false);
        mContentDataManager = new ContentDataManager(getApplicationContext());
        mVideo = mContentDataManager.getLibraryContentRes(linkId);

        mEmbededPlayer = (VideoView) findViewById(R.id.video_player_embeded);
        mWebPlayer = (WebView) findViewById(R.id.video_player_web);
        mProgressView = findViewById(R.id.progress_container);
        setUpWebView();
        videoUrl = getIntent().getStringExtra(ConstantGlobal.VIDEO_URL);
        if (videoUrl.startsWith("//")) {
            // as in case of some youtube videos the url was coming as
            // //www.youtube.com/watch?v=videoId 
            videoUrl = "http:" + videoUrl;
        }

        downloaded = getIntent().getBooleanExtra(ConstantGlobal.DOWNLOADED, false);
        useHtmlPlayer = "ADDED".equals(getIntent().getStringExtra(ConstantGlobal.LINK_TYPE));
        if (useHtmlPlayer) {
            mWebPlayer.setVisibility(View.VISIBLE);
            mEmbededPlayer.setVisibility(View.GONE);
            mProgressView.setVisibility(View.GONE);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(videoUrl));
            try {
                startActivity(intent);
                finish();
            } catch (ActivityNotFoundException e) {
                mWebPlayer.loadUrl(videoUrl);
            }

        } else {
            mProgressView.setVisibility(View.VISIBLE);
            mEmbededPlayer.setVisibility(View.GONE);
            mWebPlayer.setVisibility(View.GONE);
        }

        Log.d(TAG, "video url: " + videoUrl);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView() {

        if (mWebPlayer != null) {
            mWebPlayer.getSettings().setJavaScriptEnabled(true);
            mWebPlayer.getSettings().setAppCacheEnabled(true);
            mWebPlayer.getSettings().setDomStorageEnabled(true);
            mWebPlayer.getSettings().setPluginState(PluginState.ON);
            mWebPlayer.getSettings().setUseWideViewPort(true);
            mWebPlayer.getSettings().setLoadWithOverviewMode(true);
            mWebPlayer.setWebChromeClient(new WebChromeClient() {});
            mWebPlayer.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    view.loadUrl(url);
                    return true;
                }
            });
        }

    }

    @Override
    protected void onResume() {

        super.onResume();
        if (!useHtmlPlayer) {
            if (mIsSDCard) {
                File file = new File(videoUrl);
                EncryptionLevel encLevel = EncryptionLevel.valueOfKey(mVideo.encLevel);
                new ContentDecrypter(this, mVideo, encLevel, file).executeTask(false);
            } else {
                resumeVideo();
            }

        } else if (mWebPlayer != null) {
            try {
                mWebPlayer.getClass().getMethod("onResume", (Class[]) null)
                        .invoke(mWebPlayer, (Object[]) null);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

        }
        GoogleAnalyticsUtils.sendPageViewDataToGA();
    }

    int     mPosition = 0;
    boolean paused;

    @Override
    protected void onPause() {

        super.onPause();
        // Pause the video if it is playing
        if (!useHtmlPlayer) {
            pauseVideo();
        } else if (mWebPlayer != null) {
            try {
                mWebPlayer.getClass().getMethod("onPause", (Class[]) null)
                        .invoke(mWebPlayer, (Object[]) null);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            // mWebPlayer.loadUrl("about:blank");
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }
    private void pauseVideo() {

        // Save the current video position
        mPosition = mEmbededPlayer.getCurrentPosition();

        if (mEmbededPlayer.isPlaying()) {
            mEmbededPlayer.pause();
            paused = true;
        }

        Log.d(TAG, "video position : " + mPosition);
    }

    private void resumeVideo() {

        mProgressView.setVisibility(View.VISIBLE);
        if (paused) {
            mEmbededPlayer.resume();
            Log.d(TAG, "seek to value: " + mPosition);
            mEmbededPlayer.seekTo(mPosition);
            mEmbededPlayer.setVisibility(View.VISIBLE);
            mEmbededPlayer.requestFocus();
            paused = false;
            mProgressView.setVisibility(View.GONE);
            return;
        }

        mMediaController = new MediaController(this) {

        };
        mEmbededPlayer.setMediaController(mMediaController);
        mMediaController.setAnchorView(mEmbededPlayer);
        mProgressView.setVisibility(View.VISIBLE);
        startVideo();
        // Restore the video position
//        mEmbededPlayer.requestFocus();

    }

    private void startVideo() {

        if (mEmbededPlayer != null) {
            if (mEmbededPlayer.isPlaying()) {
                mEmbededPlayer.stopPlayback();
            }

            Log.d(TAG, "starting video : " + videoUrl);
            if (downloaded) {
                final String fileName = StringUtils.substringAfterLast(videoUrl, File.separator);
                final String filePath = TextUtils.join(File.separator, new String[] {
                        ContentDataManager.getContentDir(EntityType.VIDEO.name().toLowerCase()),
                        fileName });
                Log.d(TAG, "video is avalilable locally : " + filePath);
                final File localFile = new File(filePath);
                if (!localFile.exists()) {
                    Log.e(TAG, "file : " + filePath + " does not exist");
                    Toast.makeText(getBaseContext(),
                            "file : " + filePath + " does not exist, please download it again",
                            Toast.LENGTH_SHORT).show();
                    onStop();
                    return;
                }
                // mEmbededPlayer.setVideoPath("http://localhost:8080/vedantu/video2.mp4");
                mEmbededPlayer.setVideoPath(videoUrl);
                
             //   mEmbededPlayer.setVideoPath("http://localhost:8080/vedantu/video/test.mp4");
                
            } else if(videoUrl.contains(FileManager.baseUrl)){
                mEmbededPlayer.setVideoPath(videoUrl);
//                 mEmbededPlayer.setVideoURI(Uri.parse(videoUrl));
            } else {
                mEmbededPlayer.setVideoURI(Uri.parse(videoUrl));
            }

            mEmbededPlayer.setVisibility(View.VISIBLE);
            mEmbededPlayer.requestFocus();
            mEmbededPlayer.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    mProgressView.setVisibility(View.GONE);
                    mEmbededPlayer.start();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {

        Log.d(TAG, "on back button is pressed");
        super.onBackPressed();
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (mEmbededPlayer != null) {
            mEmbededPlayer.stopPlayback();
            Log.d(TAG, "onDestroy method is called");
            mPosition = 0;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        Log.d(TAG, "== onKeyUp== : " + keyCode + ", event: " + event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return super.onKeyUp(keyCode, event);
    }


    class ContentDecrypter extends AbstractVedantuAsynTask<Void, Void, Boolean> {

        Context context;
        LibraryContentRes doc;
        EncryptionLevel encLevel;
        File              file;
        private File      tempFile;
        private String    rPassword;
        byte[] mBuffer;
        private ContentDecrypter(Context context, LibraryContentRes doc, EncryptionLevel encLevel, File file) {

            super();
            this.context = context;
            this.doc = doc;
            this.encLevel = encLevel;
            this.file = file;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                FileManager.addDecryptedPassPhraseToCache(context, doc.passphrase, encLevel, file.getAbsolutePath());
                String decryptedPassphrase = FileMaskProcessor.getPassPhrase(file.getAbsolutePath());
                if (decryptedPassphrase == null) {
                    Log.e(TAG, "can not display the content");
                    return false;
                }

                tempFile = File.createTempFile(file.getName(), "mp4");
                tempFile.deleteOnExit();
                /*tempFile = new File(ContentDataManager.getTempContentDir(), file.getName());

                if (tempFile.getAbsolutePath().equals(file.getAbsolutePath())) {
                    tempFile = new File(ContentDataManager.getTempContentDir(), "tmp." + file.getName());
                    tempFile.deleteOnExit();
                }*/

                Log.d(TAG, "temp output file is: " + tempFile.getAbsolutePath());
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                FileMaskProcessor fileMaskProcessor = new FileMaskProcessor(decryptedPassphrase);
                fileMaskProcessor.process(file, 1024 * 16, tempFile);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
//            m_cObjWebView.loadUrl("file://" + decryptFile.getAbsolutePath()+"/index.html");
//            dycriptionInProgress = false;
            videoUrl = tempFile.getAbsolutePath();
            resumeVideo();
        }
    }
}
