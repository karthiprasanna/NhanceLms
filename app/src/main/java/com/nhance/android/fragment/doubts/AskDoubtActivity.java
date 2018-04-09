package com.nhance.android.fragment.doubts;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.doubts.DoubtPosterTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask.ReqAction;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.jsinterfaces.DoubtJSInterface;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.web.VedantuWebChromeClient;
import com.nhance.android.R;

public class AskDoubtActivity extends AbstractDoubtActivity {

    private WebView             doubtContainer;
    private AskDoubtJSInterface jsInterface;
    private boolean             isWebViewLoaded = false;
    private boolean             isDestroyed     = false;
    private final String        TAG             = "SingleDoubtActivity";
    // private static final String ENTITY_DISCUSSION = "DISCUSSION";
    private String              brdType;
    private JSONObject          doubtJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_ADD_DOUBT);
        session = SessionManager.getInstance(getApplicationContext());
        boolean isLoggedIn = session.checkLogin();
        if (!isLoggedIn)
            return;
        setContentView(R.layout.activity_ask_doubt);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpWebView();
    }

    private void setUpButton() {

        Button postDoubtBtn = (Button) findViewById(R.id.post_doubt_btn);
        postDoubtBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                jsInterface.getDoubtJson();
            }
        });
        postDoubtBtn.setVisibility(View.VISIBLE);
    }

    private void postDoubt(JSONObject jsonObj) {

        Log.d(TAG, "DOUBT JSON FROM JS == " + jsonObj);
        if (jsonObj != null) {
            doubtJson = jsonObj;
            final ProgressDialog dialog = new ProgressDialog(AskDoubtActivity.this);
            dialog.setCancelable(false);
            dialog.setMessage("Posting Doubt...");
            dialog.show();
            try {
                String name = doubtJson.getString("name");
                String content = doubtJson.getString("content");
                JSONArray brdIds = doubtJson.getJSONArray("brdIds");
                List<String> selectedBrdIds = new ArrayList<String>();
                for (int i = 0; i < brdIds.length(); i++) {
                    String brdId = brdIds.getString(i);
                    selectedBrdIds.add(brdId);
                }
                DoubtPosterTask doubtPosterTask = new DoubtPosterTask(session, null, name, content,
                        selectedBrdIds, null, new PostDoubtProcessor(dialog));
                doubtPosterTask.executeTask(false);
            } catch (JSONException e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
        }
    };

    final class PostDoubtProcessor implements ITaskProcessor<JSONObject> {

        ProgressDialog dialog;

        public PostDoubtProcessor(ProgressDialog dialog2) {

            dialog = dialog2;
        }

        @Override
        public void onTaskStart(JSONObject result) {

        }

        @Override
        public void onTaskPostExecute(boolean success, JSONObject resp) {

            if (dialog != null) {
                dialog.cancel();
            }
            if (resp == null || !success) {
                Toast.makeText(getApplicationContext(), R.string.doubt_post_failure,
                        Toast.LENGTH_LONG).show();
                return;
            } else {
                Toast.makeText(getApplicationContext(), R.string.doubt_post_success,
                        Toast.LENGTH_LONG).show();
                Intent intent = getIntent();
                JSONObject result = JSONUtils.getJSONObject(resp, VedantuWebUtils.KEY_RESULT);
                intent.putExtra(DoubtsFragment.ADDED_DOUBT, result.toString());
                setResult(RESULT_OK, intent);
            }
            finish();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView() {

        doubtContainer = (WebView) findViewById(R.id.ask_doubt_container);

        doubtContainer.getSettings().setJavaScriptEnabled(true);
        doubtContainer.setWebChromeClient(new VedantuWebChromeClient());

        jsInterface = new AskDoubtJSInterface(doubtContainer);
        mJSI = jsInterface;

        doubtContainer.addJavascriptInterface(jsInterface, "doubtJSInterface");
        doubtContainer.loadUrl("file:///android_asset/html/doubt_add_view.html");
        doubtContainer.setFocusable(true);
        doubtContainer.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                Log.d(TAG, "Static Html page load finished.");
                super.onPageFinished(view, url);
                isWebViewLoaded = true;
                jsInterface.loadDimensionFiles();
                loadBoards();
                setUpButton();
                doubtContainer.requestFocus(View.FOCUS_DOWN);
            }
        });
    }

    private void loadBoards() {

        loadBoards(null, null);
    }

    private void loadBoards(String parentType, String parentId) {

        WebCommunicatorTask boardsFetcher = new WebCommunicatorTask(
                SessionManager.getInstance(getApplicationContext()), null, ReqAction.GET_BOARDS,
                new BoardsRetreiverProcessor(), null, null, 0);
        if (parentType == null) {
            brdType = ConstantGlobal.COURSE_KEY;
        } else if (parentId != null) {
            brdType = parentType;
            boardsFetcher.addExtraParams("parentId", parentId);
        }
        boardsFetcher.addExtraParams("recordState", "ACTIVE");
        boardsFetcher.addExtraParams("type", brdType);
        boardsFetcher.addExtraParams("context", ConstantGlobal.ORG_KEY);
        boardsFetcher.addExtraParams("ownerId",
                session.getSessionStringValue(ConstantGlobal.ORG_ID));
        boardsFetcher.executeTask(false);
    }

    final class BoardsRetreiverProcessor implements ITaskProcessor<JSONObject> {

        @Override
        public void onTaskStart(JSONObject result) {

        }

        @Override
        public void onTaskPostExecute(boolean success, JSONObject result) {

            jsInterface.showBoards(success, result, brdType);
        }
    }

    final class AskDoubtJSInterface extends DoubtJSInterface {

        public AskDoubtJSInterface(WebView myWebView) {

            super(myWebView);
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
        public void fetchTopics(final String parentType, final String parentId) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    loadBoards(parentType, parentId);
                }
            });
        }

        @SuppressLint("SetJavaScriptEnabled")
        public void showBoards(final boolean success, final JSONObject result,
                final String boardType) {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    myWebView.loadUrl("javascript:showBoards(" + success + "," + result + ",'"
                            + boardType + "')");
                }
            });
        }

        @JavascriptInterface
        public void addImageClick() {

            if (isDestroyed)
                return;
            showSelectedPictureDialogue();
        }

        @JavascriptInterface
        public void showMessage(final String message) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @SuppressLint("SetJavaScriptEnabled")
        public void getDoubtJson() {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    myWebView.loadUrl("javascript:getDoubtJson()");
                }
            });
        }

        @JavascriptInterface
        public void setDoubtJson(final String jsonStr) {

            Log.d(TAG, "JSON RECIEVED ==== " + jsonStr);
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    JSONObject jsonObj;
                    try {
                        jsonObj = new JSONObject(jsonStr);
                        postDoubt(jsonObj);
                    } catch (JSONException e) {
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                }
            });
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
