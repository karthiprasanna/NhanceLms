package com.nhance.android.fragment.doubts;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.activities.imageviewer.ImageViewActivity;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.socials.SocialActionPosterTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask.ReqAction;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.jsinterfaces.DoubtJSInterface;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.web.VedantuWebChromeClient;
import com.nhance.android.R;

public class SingleDoubtActivity extends AbstractDoubtActivity {

    private JSONObject             currDoubt;
    private int                    dbtIndex          = 0;
    private WebView                doubtContainer;
    private TextView               progressBar;
    private SingleDoubtJSInterface jsInterface;
    private boolean                isWebViewLoaded   = false;
    private boolean                isDestroyed       = false;
    private final String           TAG               = "SingleDoubtActivity";
    private static final String    ENTITY_DISCUSSION = "DISCUSSION";
    private static final String    ENTITY_COMMENT    = "COMMENT";
    private PopupWindow            popup;
    private DoubtJSInterface       popupJsInterface;
    private final int              SIZE              = 10;
    private int                    answerStart       = 0;
    private int					   followerStart	 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        Log.e("single doubt","page");
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_DOUBT);
        session = SessionManager.getInstance(getApplicationContext());
        boolean isLoggedIn = session.checkLogin();
        if (!isLoggedIn)
            return;
        setContentView(R.layout.activity_single_doubt);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Discussion");
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        fetchDoubt();
        setUpWebView();
    }

    private void fetchDoubt() {

        JSONObject doubt = null;
        Intent intent = getIntent();
        String dbtStr = intent.getStringExtra(DoubtsFragment.DOUBT_EXTRA_MESSAGE);
        dbtIndex = intent.getIntExtra(DoubtsFragment.DOUBT_INDEX, 0);
        try {
            doubt = new JSONObject(dbtStr);
            Log.d(TAG, "doubt data : " + doubt);
        } catch (JSONException e) {
            Log.d(TAG, "ERROR PARSING DOUBT DATA");
            finish();
            return;
        }
        currDoubt = doubt;
        GoogleAnalyticsUtils
                .setCustomDimensions(JSONUtils.getString(currDoubt, ConstantGlobal.ID),
                        JSONUtils.getString(currDoubt, ConstantGlobal.NAME),
                        ConstantGlobal.GA_SCREEN_DOUBT);
    }

    @Override
    public void onBackPressed() {

        Intent intent = getIntent();
        try {
            int ansCount = currDoubt.getInt("comments");
            // Log.d(TAG,"ANSWERS COUNT ON BACK ========== "+ansCount);
            intent.putExtra(DoubtsFragment.KEY_ADD_ANSWER, Integer.toString(ansCount));

            int followersCount = currDoubt.getInt("followers");
            // Log.d(TAG,"KEY_FOLLOW_UNFOLLOW ON BACK ========== "+followersCount);
            intent.putExtra(DoubtsFragment.KEY_FOLLOW_UNFOLLOW, Integer.toString(followersCount));

            String followType = currDoubt.getString("followType");
            intent.putExtra(DoubtsFragment.KEY_FOLLOW_UNFOLLOW_TYPE, followType);
        } catch (JSONException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        intent.putExtra(DoubtsFragment.DOUBT_INDEX, dbtIndex);
        setResult(RESULT_OK, intent);

        super.onBackPressed();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView() {

        doubtContainer = (WebView) findViewById(R.id.single_doubt_container);
        progressBar = (TextView) findViewById(R.id.single_doubt_progressBar);
        progressBar.setVisibility(View.VISIBLE);

        doubtContainer.getSettings().setJavaScriptEnabled(true);
        doubtContainer.setWebChromeClient(new VedantuWebChromeClient());

        jsInterface = new SingleDoubtJSInterface(doubtContainer);

        doubtContainer.addJavascriptInterface(jsInterface, "doubtJSInterface");
        doubtContainer.loadUrl("file:///android_asset/html/single_doubt_view.html");
        doubtContainer.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                Log.d(TAG, "Static Html page load finished.");
                super.onPageFinished(view, url);
                isWebViewLoaded = true;
                jsInterface.loadDimensionFiles();
                showSingleDoubt();
            }


        });
    }

    private void showSingleDoubt() {

        String id = JSONUtils.getString(currDoubt, ConstantGlobal.ID);

        SocialActionPosterTask socialActionPosterTask = new SocialActionPosterTask(session, null,
                SocialActionPosterTask.ReqAction.VIEW, null);
        socialActionPosterTask.addExtraParams("entity.id", id);
        socialActionPosterTask.addExtraParams("entity.type", ENTITY_DISCUSSION);
        socialActionPosterTask.executeTask(false);

        progressBar.setVisibility(View.GONE);
        jsInterface.showDoubt();
        jsInterface.showAnswersLoading();
        fetchAnswers(id, 0);
    }

    @SuppressLint("ShowToast")
    private void followThisDoubt(final String id, final boolean type, final int count) {

        SocialActionPosterTask.ReqAction ACTION_TYPE = SocialActionPosterTask.ReqAction.FOLLOW;
        if (!type) {
            ACTION_TYPE = SocialActionPosterTask.ReqAction.UNFOLLOW;
        }
        SocialActionPosterTask socialActionPosterTask = new SocialActionPosterTask(session, null,
                ACTION_TYPE, new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(boolean success, JSONObject result) {

                        if (success && result != null) {
                            String errorCode = JSONUtils.getString(result,
                                    VedantuWebUtils.KEY_ERROR_CODE);
                            if (!TextUtils.isEmpty(errorCode)) {
                                Toast.makeText(getApplicationContext(),
                                        R.string.doubt_follow_failed, Toast.LENGTH_LONG);
                                jsInterface.followDoubtFailed(id, type, count);
                                return;
                            }
                            try {
                                currDoubt.put("followers", count);
                                if (type) {
                                    currDoubt.put("followType", "FOLLOWING");
                                } else {
                                    currDoubt.put("followType", "NONE");
                                }
                            } catch (JSONException e) {}
                        } else {
                            jsInterface.followDoubtFailed(id, type, count);
                        }
                    }
                });
        socialActionPosterTask.addExtraParams("entity.id", id);
        socialActionPosterTask.addExtraParams("entity.type", ENTITY_DISCUSSION);
        socialActionPosterTask.executeTask(false);
    }

    private void fetchAnswers(String id, int start) {

        Log.d(TAG, "Load Answers >>>>>>>>>>>>>>>>>>> " + start);
        WebCommunicatorTask doubtAnswerListFetcher = new WebCommunicatorTask(
                SessionManager.getInstance(getApplicationContext()), null, ReqAction.GET_COMMENTS,
                new DoubtAnswersRetreiverProcessor(), null, null, start);
        answerStart = start;
        doubtAnswerListFetcher.addExtraParams("parent.id", id);
        doubtAnswerListFetcher.addExtraParams("parent.type", ENTITY_DISCUSSION);
        doubtAnswerListFetcher.executeTask(false);
    }
    private void fetchFollowers(int start) {
        Log.d(TAG, "Load Followers >>>>>>>>>>>>>>>>>>> " + start);
        WebCommunicatorTask doubtFollowerListFetcher = new WebCommunicatorTask(
                SessionManager.getInstance(getApplicationContext()), null, ReqAction.GET_FOLLOWERS,
                new DoubtFollowersRetreiverProcessor(), null, null, start);
        followerStart = start;
        String id = JSONUtils.getString(currDoubt, ConstantGlobal.ID);
        doubtFollowerListFetcher.addExtraParams("entity.id", id);
        doubtFollowerListFetcher.addExtraParams("entity.type", ENTITY_DISCUSSION);
        doubtFollowerListFetcher.executeTask(false);
    }

    private View createAnswerPopup() {

        final LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
        ViewGroup root = (ViewGroup) getWindow().getDecorView().findViewById(R.layout.activity_single_doubt);

        final View pageBlacker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.page_blacker, null);
        final PopupWindow pageBlackerPopup = new PopupWindow(pageBlacker,
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
        pageBlackerPopup.showAtLocation(pageBlacker, Gravity.CENTER, 0, 0);

        final View addAnswerView = layoutInflater.inflate(R.layout.doubt_add_answer, root, true);
        popup = new PopupWindow(SingleDoubtActivity.this);
        popup.setContentView(addAnswerView);
        popup.setWidth((int) getResources().getDimension(R.dimen.doubt_add_answer_width));
        popup.setHeight(LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        popup.setOutsideTouchable(true);
        popup.setBackgroundDrawable(getResources().getDrawable(R.color.popup_back_transparency));
        popup.showAtLocation(addAnswerView, Gravity.CENTER, 0, 0);
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {

                if (pageBlackerPopup.isShowing()) {
                    pageBlackerPopup.dismiss();
                }
            }
        });
        return addAnswerView;
    }

    final class DoubtAnswersRetreiverProcessor implements ITaskProcessor<JSONObject> {
        @Override
        public void onTaskStart(JSONObject result) {
        }
        @Override
        public void onTaskPostExecute(boolean success, JSONObject result) {
            jsInterface.loadDoubtPageAnswers(success, result, answerStart);
        }
    }
    final class DoubtFollowersRetreiverProcessor implements ITaskProcessor<JSONObject> {
        @Override
        public void onTaskStart(JSONObject result) {
        }
        @Override
        public void onTaskPostExecute(boolean success, JSONObject result) {
            jsInterface.loadDoubtPageFollowers(success, result, followerStart);
        }
    }

    final class SingleDoubtJSInterface extends DoubtJSInterface {

        public SingleDoubtJSInterface(WebView myWebView) {

            super(myWebView);
        }

        @SuppressLint("SetJavaScriptEnabled")
        public void followDoubtFailed(final String id, final boolean type, final int count) {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    myWebView.loadUrl("javascript:followDoubtFailed(" + type + ")");
                }
            });

        }

        @Override
        @SuppressLint("SetJavaScriptEnabled")
        public void loadDimensionFiles() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    int dimension = (int) getResources().getDimension(
                            R.dimen.device_dimension_value);
                    myWebView.loadUrl("javascript:loadDimensionFile(" + dimension + ")");
                }
            });
        }
        @JavascriptInterface
        public void loadAnswers(){
        	if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                	if(isDestroyed) return;
                	String id = JSONUtils.getString(currDoubt, ConstantGlobal.ID);
                    fetchAnswers(id, 0);
                }
            });
        }
        
        @JavascriptInterface
        public void loadMoreAnswers() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                	if(isDestroyed) return;
                    final int start = answerStart + SIZE;
                    String id = JSONUtils.getString(currDoubt, ConstantGlobal.ID);
                    Log.d(TAG, "Load More Answers==============");
                    fetchAnswers(id, start);
                }
            });
        }
        
        @JavascriptInterface
        public void loadFollowers(final int start){
        	if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                	if(isDestroyed) return;
                	fetchFollowers(start);
                }
            });
        }
        @JavascriptInterface
        public void loadMoreFollowers(){
        	if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                	if(isDestroyed) return;
                	final int start = followerStart + SIZE;
                	fetchFollowers(start);
                }
            });
        }
        
        @SuppressLint("SetJavaScriptEnabled")
        public void showAnswersLoading() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    int fontSize = (int) getResources().getDimension(
                            R.dimen.doubts_loading_font_size);
                    myWebView.loadUrl("javascript:showLoadingForAnswers(" + fontSize + ")");
                }
            });
        }

        @SuppressLint("SetJavaScriptEnabled")
        public void showDoubt() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    myWebView.loadUrl("javascript:showSingleDoubt(" + currDoubt + ")");
                }
            });
        }

        @JavascriptInterface
        public void closeAnswerPopup() {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (popup != null) {
                        popup.dismiss();
                    }
                }
            });
        }

        @JavascriptInterface
        public void followDoubt(final String id, final boolean type, final String count) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    followThisDoubt(id, type, Integer.parseInt(count));
                }
            });
        }

        @JavascriptInterface
        public void openImageViewer(final String url, String desc) {

            if (TextUtils.isEmpty(desc)) {
                try {
                    desc = currDoubt.getString("name");
                    String doubtTxt = getResources().getString(R.string.txt_doubt);
                    desc = (String) TextUtils.concat(doubtTxt, " : ", desc);
                } catch (JSONException e) {
                    Log.d(TAG, e.getLocalizedMessage());
                }
            }
            Intent imageIntent = new Intent(getApplicationContext(), ImageViewActivity.class);
            imageIntent.putExtra(ImageViewActivity.KEY_IMAGE_DESC, desc);
            imageIntent.putExtra(ImageViewActivity.KEY_IMAGE_URL, url);
            startActivity(imageIntent);
            /*
             * Intent intent = new Intent(); intent.setAction(Intent.ACTION_VIEW);
             * intent.setDataAndType(Uri.parse(url), "image/*"); intent.setFlags(MODE_PRIVATE);
             * startActivity(intent);
             */
        }

        @JavascriptInterface
        public void addImageClick() {

            showSelectedPictureDialogue();
        }

        @JavascriptInterface
        public void updateAnswerCount(final String count) {

            try {
                Log.d(TAG, "ANSWERS COUNT UPDATED >>>>>>>>>>>>> " + count);
                currDoubt.put("comments", Integer.parseInt(count));
            } catch (JSONException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }

        @SuppressLint("SetJavaScriptEnabled")
        @JavascriptInterface
        public void onClickAddAnswer(final String targetDoubtId) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    /*
                     * LayoutInflater inflater = (LayoutInflater)
                     * getSystemService(Context.LAYOUT_INFLATER_SERVICE); final View popupLayout =
                     * inflater.inflate(R.layout.doubt_add_answer, null); final View pageBlacker =
                     * inflater.inflate(R.layout.page_blacker, null); final PopupWindow
                     * pageBlackerPopup = new PopupWindow(pageBlacker, LayoutParams.MATCH_PARENT,
                     * LayoutParams.MATCH_PARENT, true);
                     * pageBlackerPopup.showAtLocation(pageBlacker, Gravity.CENTER, 0, 0); popup =
                     * new PopupWindow(popupLayout, (int) getResources().getDimension(
                     * R.dimen.doubt_add_answer_width), LayoutParams.WRAP_CONTENT, true);
                     */
                    final View popupLayout = createAnswerPopup();

                    WebView addAnswersWebView = (WebView) popupLayout
                            .findViewById(R.id.add_doubt_answer_input);
                    addAnswersWebView.getSettings().setJavaScriptEnabled(true);
                    addAnswersWebView.setWebChromeClient(new VedantuWebChromeClient());
                    popupJsInterface = new SingleDoubtJSInterface(addAnswersWebView);
                    addAnswersWebView.addJavascriptInterface(popupJsInterface, "doubtJSInterface");
                    addAnswersWebView.loadUrl("file:///android_asset/html/doubt_add_answer.html");
                    mJSI = popupJsInterface;
                    final View loader = popupLayout.findViewById(R.id.add_answer_loader);
                    addAnswersWebView.setWebViewClient(new WebViewClient() {

                        @Override
                        public void onPageFinished(WebView view, String url) {

                            super.onPageFinished(view, url);
                            popupJsInterface.loadDimensionFiles();
                            loader.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }

        @JavascriptInterface
        public void postAnswer(final String answerContent, boolean isRTEEmpty) {

            String doubtId = JSONUtils.getString(currDoubt, ConstantGlobal.ID);
            if (isRTEEmpty) {
                Toast.makeText(getApplicationContext(), R.string.error_empty_answer,
                        Toast.LENGTH_LONG).show();
                return;
            }

            final ProgressDialog dialog = new ProgressDialog(SingleDoubtActivity.this);
            dialog.setCancelable(false);
            // set a message text
            dialog.setMessage("Posting answer...");
            // show it
            dialog.show();
            final SessionManager sessionManagerInstance = SessionManager
                    .getInstance(getApplicationContext());
            SocialActionPosterTask postAnswerReq = new SocialActionPosterTask(
                    sessionManagerInstance,
                    null,
                    com.nhance.android.async.tasks.socials.SocialActionPosterTask.ReqAction.ADD_COMMENT,
                    new DoubtAnswersPosterProcessor(dialog, sessionManagerInstance));
            postAnswerReq.addExtraParams("parent.id", doubtId);
            postAnswerReq.addExtraParams("parent.type", ENTITY_DISCUSSION);
            postAnswerReq.addExtraParams("type", ENTITY_COMMENT);
            postAnswerReq.addExtraParams("root.id", doubtId);
            postAnswerReq.addExtraParams("root.type", ENTITY_DISCUSSION);
            postAnswerReq.addExtraParams("base.type", doubtId);
            postAnswerReq.addExtraParams("content", answerContent);
            postAnswerReq.addExtraParams("base.type", ENTITY_DISCUSSION);
            postAnswerReq.executeTask(false);

        }

        final class DoubtAnswersPosterProcessor implements ITaskProcessor<JSONObject> {

            private ProgressDialog dialog;
            private SessionManager sessionManagerInstance;

            public DoubtAnswersPosterProcessor(ProgressDialog dialog2,
                    SessionManager sessionManagerInstance2) {

                dialog = dialog2;
                sessionManagerInstance = sessionManagerInstance2;
            }

            @Override
            public void onTaskStart(JSONObject result) {

            }

            @Override
            public void onTaskPostExecute(final boolean success, final JSONObject response) {

                // VApp.getGaTracker().send(MapBuilder.createEvent(ENTITY_DISCUSSION,ConstantGlobal.ACTION_ADD_ANSWER,JSONUtils.getString(currDoubt,
                // ConstantGlobal.NAME),null).build());
                dialog.cancel();
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {

                        if (popup != null) {
                            popup.dismiss();
                        }
                        OrgMemberInfo orgMemberInfo = sessionManagerInstance.getOrgMemberInfo();
                        String name = orgMemberInfo.firstName + " " + orgMemberInfo.lastName;
                        String thumbnail = orgMemberInfo.thumbnail;
                        doubtContainer.loadUrl("javascript:checkPostAnswerResp(" + success + ","
                                + response + ",'" + TextUtils.htmlEncode(name) + "','" + thumbnail
                                + "')");
                    }
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {

        isWebViewLoaded = false;
        isDestroyed = true;
        super.onDestroy();
    }

    @Override
    public void onResume() {

        GoogleAnalyticsUtils.sendPageViewDataToGA();
        super.onResume();
    }
}
