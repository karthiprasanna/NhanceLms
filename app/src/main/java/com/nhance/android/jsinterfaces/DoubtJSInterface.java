package com.nhance.android.jsinterfaces;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.socials.SocialActionPosterTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask;
import com.nhance.android.async.tasks.socials.WebCommunicatorTask.ReqAction;
import com.nhance.android.fragment.doubts.SingleDoubtActivity;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.OrgMemberInfo;

@SuppressLint("NewApi")
public abstract class DoubtJSInterface extends AbstractJSInterface {

    private final static String TAG      = "DoubtJSInterface";
    protected Handler           mHandler = new Handler();
    protected WebView           myWebView;

    public DoubtJSInterface(WebView myWebView) {

        this.myWebView = myWebView;
    }
    @SuppressLint("SetJavaScriptEnabled")
    public abstract void loadDimensionFiles();
    
    @SuppressLint("SetJavaScriptEnabled")
    public void appendDoubt(final int index, final JSONObject doubtJSON) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl("javascript:appendDoubt(" + index + "," + doubtJSON + ")");
            }
        });
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void updateDoubt(final int index, final JSONObject doubtJSON) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl("javascript:updateDoubt(" + index + "," + doubtJSON + ")");
            }
        });
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void prependDoubt(final JSONObject doubtJSON) {

        mHandler.post(new Runnable() {

            @Override
            public void run() {

                myWebView.loadUrl("javascript:prependDoubt(" + doubtJSON + ")");

            }
        });
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void showSingleDoubt(final JSONObject doubt) {

        mHandler.post(new Runnable() {

            @Override
            public void run() {

                Log.d(TAG, "showing doubt in full page");
                myWebView.loadUrl("javascript:showSingleDoubt(" + doubt + ")");

            }
        });

    }
    @SuppressLint("SetJavaScriptEnabled")
    public void loadDoubtPageAnswers(final boolean success, final JSONObject answersRes,final int start) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl("javascript:populateAnswers(" + success + "," + answersRes + ","+ start +")");
            }
        });
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void loadDoubtPageFollowers(final boolean success, final JSONObject answersRes,final int start) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl("javascript:populateFollowers(" + success + "," + answersRes + ","+ start +")");
            }
        });
    }

    @JavascriptInterface
    public void getAnswerReplies(final String answerId) {
        WebCommunicatorTask repliesFetcher = new WebCommunicatorTask(
                SessionManager.getInstance(myWebView.getContext().getApplicationContext()), null,
                ReqAction.GET_COMMENTS, new ITaskProcessor<JSONObject>() {
                    @Override
                    public void onTaskStart(JSONObject result) {

                    }
                    @Override
                    public void onTaskPostExecute(final boolean success, final JSONObject response) {

                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {

                                myWebView.loadUrl("javascript:populateAnswerReplies('" + answerId
                                        + "'," + success + "," + response + ")");
                            }
                        });
                    }
                }, null, null, 0);
        repliesFetcher.addExtraParams("parent.id", answerId);
        repliesFetcher.addExtraParams("parent.type", "COMMENT");
        repliesFetcher.executeTask(false);
    }

    @JavascriptInterface
    public void likeAnswer(final String answerId) {
        SocialActionPosterTask likeAnswerReq = new SocialActionPosterTask(
                SessionManager.getInstance(myWebView.getContext().getApplicationContext()), null,
                com.nhance.android.async.tasks.socials.SocialActionPosterTask.ReqAction.UP_VOTE,
                new ITaskProcessor<JSONObject>() {

                    @Override
                    public void onTaskStart(JSONObject result) {

                    }

                    @Override
                    public void onTaskPostExecute(final boolean success, final JSONObject response) {

                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {

                                myWebView.loadUrl("javascript:checkLikeAnswerResp('" + answerId
                                        + "'," + success + "," + response + ")");
                            }
                        });
                    }
                });
        likeAnswerReq.addExtraParams("entity.id", answerId);
        likeAnswerReq.addExtraParams("entity.type", "COMMENT");
        likeAnswerReq.executeTask(false);
    }

    @JavascriptInterface
    public void postAnswerReply(final String doubtId, final String answerId, final String content) {
        final SessionManager sessionManagerInstance = SessionManager.getInstance(myWebView
                .getContext().getApplicationContext());
        SocialActionPosterTask postAnswerReplyReq = new SocialActionPosterTask(
                sessionManagerInstance,
                null,
                com.nhance.android.async.tasks.socials.SocialActionPosterTask.ReqAction.ADD_COMMENT,
                new ITaskProcessor<JSONObject>() {
                    @Override
                    public void onTaskStart(JSONObject result) {
                    }
                    @Override
                    public void onTaskPostExecute(final boolean success, final JSONObject response) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {

                                OrgMemberInfo orgMemberInfo = sessionManagerInstance
                                        .getOrgMemberInfo();
                                String name = orgMemberInfo.firstName + " "
                                        + orgMemberInfo.lastName;
                                String thumbnail = orgMemberInfo.thumbnail;
                                myWebView.loadUrl("javascript:checkPostAnswerReplyResp('"
                                        + answerId + "'," + success + "," + response + ",'"
                                        + content + "','" + TextUtils.htmlEncode(name) + "','"
                                        + thumbnail + "')");
                            }
                        });
                    }
                });
        postAnswerReplyReq.addExtraParams("parent.id", answerId);
        postAnswerReplyReq.addExtraParams("parent.type", "COMMENT");
        postAnswerReplyReq.addExtraParams("type", "REPLY");
        postAnswerReplyReq.addExtraParams("root.id", doubtId);
        postAnswerReplyReq.addExtraParams("root.type", "DISCUSSION");
        postAnswerReplyReq.addExtraParams("base.type", doubtId);
        postAnswerReplyReq.addExtraParams("content", content);
        postAnswerReplyReq.addExtraParams("base.type", "DISCUSSION");
        postAnswerReplyReq.executeTask(false);
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void appendUploadedImgToRTE(final String imgHtml) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl("javascript:appendUploadedImgToRTE('" + imgHtml + "')");
            }
        });
    }
    @SuppressLint("SetJavaScriptEnabled")
    public void postDoubtFromJS(final String title) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                myWebView.loadUrl("javascript:postDoubtFromJS('" + title + "')");
            }
        });
    }
}
