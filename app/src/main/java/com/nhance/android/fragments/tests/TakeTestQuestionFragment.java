package com.nhance.android.fragments.tests;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.activities.tests.TakeTestActivity;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.jsinterfaces.AbstractJSInterface;
import com.nhance.android.utils.QuestionImageUtil;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class TakeTestQuestionFragment extends NhanceBaseFragment {

    private static final String    TAG = "TakeTestQuestionFragment";
    private Question               question;
    private NotifyTakeTestFragment notifyTakeTestActivity;
    private String                 answerGiven;
    private WebView                questionWebView;
    private ViewGroup              fragmentRootView;
    private boolean                fragmentAttached;

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentRootView = (ViewGroup) inflater.inflate(R.layout.fragment_take_test_question,
                container, false);
        return fragmentRootView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        setQuestionDataFromBundle(getArguments());
        question = ((TakeTestActivity) getActivity()).getCurrentQuestionData();
        questionWebView = (WebView) fragmentRootView.findViewById(R.id.new_test_web_view);
        questionWebView.setWebChromeClient(ViewUtils.webChromeClientInstance());
        questionWebView.getSettings().setJavaScriptEnabled(true);
        questionWebView.addJavascriptInterface(new TakeTestJSInterface(questionWebView),
                "TakeTestJSInterface");
        questionWebView.requestFocus(View.FOCUS_DOWN);
        questionWebView.loadUrl("file:///android_asset/html/take_test_question.html");
        questionWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                if (!fragmentAttached) {
                    return;
                }
                super.onPageFinished(view, url);
                int dimension = (int) getResources().getDimension(R.dimen.device_dimension_value);
                questionWebView.loadUrl("javascript:loadDimensionFiles(" + dimension + ")");
                String content = QuestionImageUtil.annotateQuestionContentWithHtml(question.name);
                String options = QuestionImageUtil
                        .annotateQuestionOptionsWithHtml(question.options);

                String questionType = question.type;
                String answerToSend = "";
                if (answerGiven != null) {
                    answerToSend = answerGiven;
                }
                Log.d("Take Test Question Fragment", "aappending data to htmlPage content:"
                        + content +", options:" + options + ", questiontype " + questionType
                        + ", answerGiven: " + answerGiven);
                view.loadUrl("javascript:addQuestionData('" + content + "',[" + options + " ],'"
                        + questionType + "','" + answerToSend + "')");

            }
        });
        super.onActivityCreated(savedInstanceState);
    }

    private void setQuestionDataFromBundle(Bundle bundle) {

        // if (bundle == null || bundle.getSerializable(ConstantGlobal.QUESTION) == null) {
        // return;
        // }
        // question = (Question) bundle.getSerializable(ConstantGlobal.QUESTION);
        answerGiven = bundle.getString(ConstantGlobal.ANSWER_GIVEN);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }

    public void resetAnswer() {

        questionWebView.loadUrl("javascript:resetAnswer('" + question.type + "')");
    }

    final class TakeTestJSInterface extends AbstractJSInterface {

        private Handler mHandler = new Handler();

        public TakeTestJSInterface(WebView webView) {

        }

        @JavascriptInterface
        public void onAnswerAdded(final String answer) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    Log.d(TAG, "Got answer from webview " + answer);
                    if (notifyTakeTestActivity != null) {
                        notifyTakeTestActivity.setAnswerForCurrentQuestion(answer);
                    }
                }
            });
        }

        @JavascriptInterface
        public void makeToastToResetAnswer() {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    Toast.makeText(m_cObjNhanceBaseActivity, "To change the answer, reset it first",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    @Override
    public void onAttach(Activity activity) {

        fragmentAttached = true;
        Log.d(TAG, "Fragment attached to activity");
        if (getActivity() instanceof NotifyTakeTestFragment) {
            notifyTakeTestActivity = (NotifyTakeTestFragment) getActivity();
        }
        super.onAttach(activity);
    }

    public interface NotifyTakeTestFragment {

        public void setAnswerForCurrentQuestion(String newAnswer);
    }

    @Override
    public void onDetach() {

        fragmentAttached = false;
        super.onDetach();
    }

}
