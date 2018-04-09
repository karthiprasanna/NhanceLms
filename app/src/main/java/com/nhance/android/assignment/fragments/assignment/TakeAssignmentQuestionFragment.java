package com.nhance.android.assignment.fragments.assignment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.assignment.activity.TakeAssignmentActivity;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.jsinterfaces.AbstractJSInterface;
import com.nhance.android.utils.QuestionImageUtil;
import com.nhance.android.utils.ViewUtils;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class TakeAssignmentQuestionFragment extends NhanceBaseFragment {

    private static final String TAG = "TakeAssignmentQuestionFragment";
    private Question question;
    private NotifyTakeAssignmentFragment notifyTakeAssignmentActivity;
    private String answerGiven;
    private WebView questionWebView;
    private ViewGroup fragmentRootView;
    private boolean fragmentAttached;

    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragmentRootView = (ViewGroup) inflater.inflate(R.layout.fragment_take_assignment_question,
                container, false);
        return fragmentRootView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        setQuestionDataFromBundle(getArguments());
        question = ((TakeAssignmentActivity) getActivity()).getCurrentQuestionData();
        questionWebView = (WebView) fragmentRootView.findViewById(R.id.new_assignment_web_view);
        questionWebView.setWebChromeClient(ViewUtils.webChromeClientInstance());
        questionWebView.getSettings().setJavaScriptEnabled(true);
        questionWebView.addJavascriptInterface(new TakeAssignmentJSInterface(questionWebView),
                "TakeAssignmentJSInterface");
        questionWebView.requestFocus(View.FOCUS_DOWN);
        questionWebView.loadUrl("file:///android_asset/html/take_assignment_question.html");
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
                Log.d("Take Assignment Question Fragment", "aappending data to htmlPage content:"
                        + content + ", options:" + options + ", questiontype " + questionType
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

    final class TakeAssignmentJSInterface extends AbstractJSInterface {

        private Handler mHandler = new Handler();

        public TakeAssignmentJSInterface(WebView webView) {

        }

        @JavascriptInterface
        public void onAnswerAdded(final String answer) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    Log.d(TAG, "Got answer from webview " + answer);
                    if (notifyTakeAssignmentActivity != null) {
                        notifyTakeAssignmentActivity.setAnswerForCurrentQuestion(answer);
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
        if (getActivity() instanceof NotifyTakeAssignmentFragment) {
            notifyTakeAssignmentActivity = (NotifyTakeAssignmentFragment) getActivity();
        }
        super.onAttach(activity);
    }

    public interface NotifyTakeAssignmentFragment {

        public void setAnswerForCurrentQuestion(String newAnswer);
    }

    @Override
    public void onDetach() {

        fragmentAttached = false;
        super.onDetach();
    }

}
