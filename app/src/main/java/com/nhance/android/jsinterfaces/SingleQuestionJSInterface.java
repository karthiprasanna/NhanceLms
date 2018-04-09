package com.nhance.android.jsinterfaces;

import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;

import com.nhance.android.db.models.entity.Question;
import com.nhance.android.utils.QuestionImageUtil;
import com.nhance.android.utils.SQLDBUtil;

public class SingleQuestionJSInterface extends AbstractJSInterface {

    private static final String TAG      = "SingleQuestionJSInterface";
    private Handler             mHandler = new Handler();
    WebView                     mWebView;

    public SingleQuestionJSInterface(WebView mWebView) {

        this.mWebView = mWebView;
    }

    /**
     * <blockquote>all these params should not be htmlEncoded using {@link TextUtils.htmlEncode()}
     * </blockquote>
     * 
     * @param test
     * @param options
     *            <b> should be {@link SQLDBUtil.SEPARATOR} separated join string of question
     *            options </b>
     * @param questionNo
     **/

    public void addQuestionData(final Question question, final int questionNo,
            final String solution, final boolean showSolution) {

        mHandler.post(new Runnable() {

            @Override
            public void run() {

                String content = QuestionImageUtil.annotateQuestionContentWithHtml(question.name);
                String sol = solution == null ? null : solution;
                String options = QuestionImageUtil
                        .annotateQuestionOptionsWithHtml(question.options);

                Log.d(TAG, "appending data to htmlPage content:" + content + ", options:" + options
                        + ", solution:" + solution);

                mWebView.loadUrl("javascript:addQuestionData('" + question.id + "','" + content
                        + "',[" + options + " ]," + questionNo + ","
                        + (sol == null ? "''" : ("'" + sol + "'")) + ", " + showSolution + ")");
            }
        });
    }

}
