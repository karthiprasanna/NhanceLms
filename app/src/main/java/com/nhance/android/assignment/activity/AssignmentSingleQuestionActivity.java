package com.nhance.android.assignment.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.activities.content.players.VideoPlayerFullScreenActivity;
import com.nhance.android.assignment.TeacherModule.AssignmentSolutionsDowloader;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.entity.Answer;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.EntityType;
import com.nhance.android.jsinterfaces.AbstractJSInterface;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.QuestionImageUtil;
import com.nhance.android.web.VedantuWebChromeClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class AssignmentSingleQuestionActivity extends NhanceBaseActivity {

    public static final String SINGLE_QUES_DATA = "SINGLE_QUES_DATA";
    public static final String SHOW_ANS_GIVEN   = "SHOW_ANS_GIVEN";
    public static final String SHOW             = "SHOW";
    public static final String HIDE             = "HIDE";
    private TakeAssignmentQuestionWithAnswerGiven ansGiven;
    private boolean                         showAnsGiven     = true;
    private Question question;
    private WebView quesContainer;
    private boolean                         isWebViewLoaded  = false;
    private boolean                         isDestroyed      = false;
    private final String TAG              = "AssignmentSingleQuestionActivity";
    private SingleQuesJSInterface           jsInterface;
    protected ContentDataManager cDataManager;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_assignment_single_question);
        getSupportActionBar().hide();
        showLoading();

        Intent intent = getIntent();
        cDataManager = new ContentDataManager(this);
        try {
            String showAnsGivenStr = (String) intent.getSerializableExtra(SHOW_ANS_GIVEN);
            if (!TextUtils.isEmpty(showAnsGivenStr) && HIDE.equals(showAnsGivenStr)) {
                showAnsGiven = false;
            } else {
                showAnsGiven = true;
            }
            Log.d(TAG, "Show Ans Given By User == " + showAnsGiven + ", is true = "
                    + (showAnsGiven == true));
            ansGiven = (TakeAssignmentQuestionWithAnswerGiven) intent
                    .getSerializableExtra(SINGLE_QUES_DATA);
            String userId = SessionManager.getInstance(getApplicationContext())
                    .getSessionStringValue(ConstantGlobal.USER_ID);
            question = ansGiven.getQuestion(userId, cDataManager);
            Log.d(TAG, "Question data = " + question);
        } catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        setUpWebView();
    }

    private void showLoading() {

        dialog = new ProgressDialog(AssignmentSingleQuestionActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.loading));
        dialog.show();
    }

    private void hideLoading() {

        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView() {

        quesContainer = (WebView) findViewById(R.id.assignment_single_question_container);
        quesContainer.getSettings().setJavaScriptEnabled(true);
        quesContainer.setWebChromeClient(new VedantuWebChromeClient());

        jsInterface = new SingleQuesJSInterface(quesContainer);

        quesContainer.addJavascriptInterface(jsInterface, "quesJSInterface");
        quesContainer.loadUrl("file:///android_asset/html/single_question_view.html");
        quesContainer.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {

                Log.d(TAG, "Static Html page load finished.");
                super.onPageFinished(view, url);
                isWebViewLoaded = true;
                jsInterface.loadDimensionFile();
                jsInterface.showQues(showAnsGiven);
                hideLoading();
                setUpButton();
            }
        });
    }

    private void setUpButton() {

        final Button showSolutionBtn = (Button) findViewById(R.id.assignment_view_solution_btn);
        showSolutionBtn.setTag("hidden");
        showSolutionBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String tag = (String) showSolutionBtn.getTag();
                if ("hidden".equals(tag)) {
                    JSONObject solution = getSolution(question);
                    if (solution == null) {
                        fetchAndShowSolution();
                        return;
                    } else {
                        jsInterface.showSolution(solution);
                        showSolutionBtn.setText(getResources().getString(R.string.hide_solution));
                        showSolutionBtn.setTag("shown");
                    }

                } else {
                    jsInterface.hideSolution();
                    showSolutionBtn.setText(getResources().getString(R.string.view_solution));
                    showSolutionBtn.setTag("hidden");
                }
            }
        });
    }

    private JSONObject getSolution(Question question) {

        Answer ans = cDataManager.getAnswer(question.orgKeyId, question.userId, question.id);
        JSONObject jSol = ans == null ? null : ans.solution;
        return getAnnotatedJSON(jSol);
    }

    private JSONObject getAnnotatedJSON(JSONObject jSol) {

        if (jSol == null || jSol.length() < 1) {
            return null;
        }
        String solution = jSol == null ? null : JSONUtils.getString(jSol, ConstantGlobal.CONTENT);

        String sol = solution == null ? null : QuestionImageUtil
                .annotateQuestionContentWithHtml(solution);
        try {
            jSol.put(ConstantGlobal.CONTENT, sol);
        } catch (Throwable e) {}
        return jSol;
    }

    private void scrollViewTo(final int direction) {

//        ScrollView scroll = (ScrollView) findViewById(R.id.scroll_question_view);
//        scroll.fullScroll(direction);
    }

    private void fetchAndShowSolution() {

        final String qId = question.id;
        if (isDestroyed)
            return;
        if (!SessionManager.isOnline()) {
            Toast.makeText(getApplicationContext(), getString(R.string.no_internet_msg),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        showLoading();
        AssignmentSolutionsDowloader solDowloader = new AssignmentSolutionsDowloader(
                SessionManager.getInstance(quesContainer.getContext()), null,
                Arrays.asList(new String[] { qId }), new ITaskProcessor<JSONObject>() {

            @Override
            public void onTaskStart(JSONObject result) {

            }

            @Override
            public void onTaskPostExecute(boolean success, JSONObject result) {

                if (isDestroyed)
                    return;
                hideLoading();
                if (!success) {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.solution_not_available),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject solJSON = null;
                try {
                    solJSON = JSONUtils.getJSONArray(result, qId).getJSONObject(0);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.solution_not_available),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                JSONObject solution = getAnnotatedJSON(solJSON);
                if (solution != null) {
                    jsInterface.showSolution(solution);
                    final Button showSolutionBtn = (Button) findViewById(R.id.view_solution_btn);
                    showSolutionBtn.setText(getResources()
                            .getString(R.string.hide_solution));
                    showSolutionBtn.setTag("shown");
                } else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.solution_not_available),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        solDowloader.executeTask(false);
    }

    final class SingleQuesJSInterface extends AbstractJSInterface {

        protected Handler mHandler = new Handler();
        private WebView quesContainer;

        public SingleQuesJSInterface(WebView quesContainer) {

            super();
            this.quesContainer = quesContainer;
        }

        @SuppressLint("SetJavaScriptEnabled")
        public void loadDimensionFile() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    int dimension = (int) getResources().getDimension(
                            R.dimen.device_dimension_value);
                    quesContainer.loadUrl("javascript:loadDimensionFile(" + dimension + ")");
                }
            });
        }

        @SuppressLint("SetJavaScriptEnabled")
        public void showQues(final boolean showAnsGiven) {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    String content = QuestionImageUtil
                            .annotateQuestionContentWithHtml(question.name);
                    String options = QuestionImageUtil
                            .annotateQuestionOptionsWithHtml(question.options);
                    Log.d(TAG, "options after anno " + options);
                    AssignementAttemptStatus attemptStatus = ansGiven.getStatus();
                    boolean isCorrect = ansGiven.correct;
                    int questionNo = ansGiven.getQusNo();
                    Log.d(TAG, "data to Question htmlPage content:" + content + ", options:"
                            + options + ", answer Given == " + ansGiven.answerGiven
                            + ", correct answer === " + ansGiven.correctAnswer
                            + ", attempt status === " + attemptStatus);
                    quesContainer.loadUrl("javascript:drawQuestion('" + question.type + "','"
                            + content + "',[" + options + " ]," + questionNo + ",'"
                            + ansGiven.answerGiven + "','" + ansGiven.correctAnswer + "','"
                            + attemptStatus + "'," + isCorrect + "," + String.valueOf(showAnsGiven)
                            + ")");
                }
            });
        }

        @SuppressLint("SetJavaScriptEnabled")
        public void showSolution(final JSONObject solution) {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    Log.d(TAG, "SENDING SOL :: " + solution);
                    quesContainer.loadUrl("javascript:showSolution(" + solution + ",'"
                            + JSONUtils.getString(solution, ConstantGlobal.CONTENT) + "')");
                }
            });
        }

        @SuppressLint("SetJavaScriptEnabled")
        public void hideSolution() {

            if (!isWebViewLoaded)
                return;
            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    quesContainer.loadUrl("javascript:hideSolution()");
                }
            });
        }

        @JavascriptInterface
        public void showMessage(final String message) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void openAttachment(final String dataJSONStr) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    try {
                        JSONObject data = new JSONObject(dataJSONStr);
                        EntityType entityType = EntityType.valueOfKey(data.getString("type"));
                        if (entityType.equals(EntityType.VIDEO)) {
                            String videoUrl = data.getString("linkUrl");
                            String linkType = data.getString("linkType");
                            String thumbnail = data.getString("thumbnail");
                            Intent intent = new Intent(AssignmentSingleQuestionActivity.this,
                                    VideoPlayerFullScreenActivity.class);
                            intent.putExtra(ConstantGlobal.LINK_TYPE, linkType);
                            intent.putExtra(ConstantGlobal.DOWNLOADED, false);
                            intent.putExtra(ConstantGlobal.THUMB, thumbnail);
                            intent.putExtra(ConstantGlobal.VIDEO_URL, videoUrl);
                            startActivity(intent);
                        } else {
                            Toast.makeText(AssignmentSingleQuestionActivity.this,
                                    "Only video attachments are allowed", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(AssignmentSingleQuestionActivity.this,
                                "Some error occured in opening the attachment", Toast.LENGTH_SHORT)
                                .show();
                        Log.d(TAG, "Exception in opening attachment  " + e.getMessage());
                    }
                }
            });
        }

        @JavascriptInterface
        public void scrollTo(final String dir) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed)
                        return;
                    if ("TOP".equals(dir)) {
                        scrollViewTo(View.FOCUS_UP);
                    } else if ("BOTTOM".equals(dir)) {
                        scrollViewTo(View.FOCUS_DOWN);
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {

        if (dialog != null) {
            dialog.dismiss();
        }
        isWebViewLoaded = false;
        isDestroyed = true;
        super.onDestroy();
    }
}

