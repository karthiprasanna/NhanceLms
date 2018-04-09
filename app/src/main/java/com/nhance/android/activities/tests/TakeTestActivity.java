package com.nhance.android.activities.tests;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.adapters.tests.TakeTestGridViewQuestionListAdapter;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.NonScrollableGridView;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.analytics.QuestionAnalytics;
import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.AttemptState;
import com.nhance.android.enums.QuestionType;
import com.nhance.android.fragments.tests.TakeTestQuestionFragment;
import com.nhance.android.fragments.tests.TakeTestQuestionFragment.NotifyTakeTestFragment;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.processors.anaytics.QuestionAnalyticsProcessor;
import com.nhance.android.receivers.NetworkMonitor;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.ViewUtils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TakeTestActivity extends NhanceBaseActivity implements NotifyTakeTestFragment {

    private static final String TAG = "TakeNewTestActivity";
    private Content test;
    private ContentDataManager cDataManager;
    List<String> qids;

    GridView gridView;

    long duration;
    TextView timerTextView;
    long questionStartTimerPosition;
    TextView currentCourseTextView;
    CountDownTimer countDownTimer;
    final Map<String, Integer> currentQuestionIndexCourseWise = new HashMap<String, Integer>();                              // 0
    // base
    Map<String, List<String>> brdIdToQuestionIdsMap;
    final Map<String, List<TakeTestQuestionWithAnswerGiven>> brdIdsToQuestionsAttemptInfoListMap = new HashMap<String, List<TakeTestQuestionWithAnswerGiven>>();
    final Map<String, TestMetadata> courseIdToCourseMDataMap = new HashMap<String, TestMetadata>();

    String currentCourseBrdId;
    TakeTestQuestionWithAnswerGiven currentQuestionAttemptInfo;
    List<String> qTypeSpinnerItems = new ArrayList<String>();

    SparseIntArray activeBgSelectorMap = new SparseIntArray();
    SparseIntArray inactiveColorMap = new SparseIntArray();

    QuestionAnalyticsProcessor qAnalyticsProcessor;
    private TextView currentQuestionNumView;
    private TextView totalQuestionNumView;
    private TextView questionTypeView;
    private Question currentQuestionData;
    private TakeTestQuestionFragment currentQuestionFragment;
    private String tempAnswerForCurrentQuestion;
    private TestExtendedInfo testInfo;
    private Spinner ndSubjectsSpinner;
    private List<String> testSubjects;
    private boolean loadedSubjectsSpinner = false;
    private int testContentId;
    private List<TakeTestQuestionWithAnswerGiven> questionsAttemptInfoListForGridView = new ArrayList<TakeTestQuestionWithAnswerGiven>();
    AnalyticsDataManager aDataManager;
    int updatePersistantTimeTakenCount = 0;

    // in milliseconds
    int timerPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TAKE_TEST);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideStatusBar();

        setContentView(R.layout.activity_take_test);
        cDataManager = new ContentDataManager(getApplicationContext());
        aDataManager = new AnalyticsDataManager(getApplicationContext());
        testContentId = getIntent().getIntExtra(ConstantGlobal.CONTENT_ID, -1);
        test = cDataManager.getContent(testContentId);

        if (test == null || !test.downloaded) {
            Toast.makeText(getBaseContext(), "Test not downloaded", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        GoogleAnalyticsUtils.setCustomDimensions(test.id, test.name, ConstantGlobal.TEST);

        testInfo = (TestExtendedInfo) test.toContentExtendedInfo();

        if (testInfo.metadata == null || testInfo.metadata.size() == 0) {
            Log.e(TAG, "no course metadata found");
            finish();
            return;
        } else if (testInfo.attempteState == AttemptState.ATTEMPTED) {
            Log.e(TAG, "You have already attempted this test");
            finish();
        }

        duration = testInfo.duration;
        testSubjects = new ArrayList<String>();
        for (TestMetadata cData : testInfo.metadata) {
            courseIdToCourseMDataMap.put(cData.id, cData);
            testSubjects.add(cData.name);
        }

        brdIdToQuestionIdsMap = testInfo.getBoardWiseQidMap();
        currentCourseBrdId = testInfo.metadata.get(0).id;

        makeBrdIdsToQuestionsAttemptInfoListMap();

        timerTextView = (TextView) findViewById(R.id.take_test_time_counter);
        currentQuestionNumView = (TextView) findViewById(R.id.take_test_current_ques_no);
        totalQuestionNumView = (TextView) findViewById(R.id.take_test_current_total_subject_quesns);
        questionTypeView = (TextView) findViewById(R.id.take_test_current_question_type);
        totalQuestionNumView.setText("/" + Integer.toString(testInfo.qusCount));
        initButtonViews();
        setUpNavigationDrawerContent();
        findViewById(R.id.take_test_open_nav_drawer).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mDrawerLayout.openDrawer(mDrawerContent);
            }
        });

        // load the first question of first subject
        timerPosition = aDataManager.getEntityAttemptTimeTaken(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID), test.type, test.id);

        Log.d(TAG, "timerPosition : " + timerPosition);
        setUpQuestion(0, false);

        startTimer();

        qAnalyticsProcessor = new QuestionAnalyticsProcessor(getBaseContext(), test,
                testInfo.metadata, session.getSessionStringValue(ConstantGlobal.USER_ID));

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                mDrawerLayout.openDrawer(mDrawerContent);
            }
        }, 750);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void hideStatusBar() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }
    }

    private DrawerLayout mDrawerLayout;
    private ScrollView mDrawerContent;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView navigationDrawerTimerTextView;
    private TakeTestGridViewQuestionListAdapter questionsGridViewAdapter;

    private OnItemClickListener getOnGridItemClickListener() {

        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (currentQuestionIndexCourseWise.get(currentCourseBrdId) == position) {
                    return;
                }

                goToDirectQuestion(position);
                mDrawerLayout.closeDrawer(mDrawerContent);
            }
        };
    }

    private void setUpNavigationDrawerContent() {

        getSupportActionBar().hide();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.take_test_drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_navigation_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {

            @Override
            public void onDrawerClosed(View view) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerContent = (ScrollView) findViewById(R.id.take_test_navigation_drawer);
        mDrawerContent.getLayoutParams().width = ViewUtils.getOrientationSpecificWidth(this);
        navigationDrawerTimerTextView = (TextView) mDrawerContent.findViewById(R.id.nd_time_count);

        OnClickListener endTestListner = new OnClickListener() {

            @Override
            public void onClick(View v) {

                showWarningToEndTest();
            }
        };
        mDrawerContent.findViewById(R.id.take_test_end_test_button).setOnClickListener(
                endTestListner);

        findViewById(R.id.take_test_end_test_from_top_bar).setOnClickListener(endTestListner);
        ((TextView) mDrawerContent.findViewById(R.id.take_test_name)).setText(test.name);
        mDrawerContent.findViewById(R.id.take_test_view_instructions).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        prepareInstructionsPopup();
                    }
                });
        setUpNavigationDrawerSubjectsSpinner();
        questionsAttemptInfoListForGridView.addAll(brdIdsToQuestionsAttemptInfoListMap
                .get(currentCourseBrdId));
        questionsGridViewAdapter = new TakeTestGridViewQuestionListAdapter(this,
                R.layout.grid_item_view_take_test_question, questionsAttemptInfoListForGridView);
        NonScrollableGridView questionsGridView = (NonScrollableGridView) mDrawerContent
                .findViewById(R.id.take_test_questions_grid_view);
        questionsGridView.setAdapter(questionsGridViewAdapter);
        questionsGridView.setOnItemClickListener(getOnGridItemClickListener());
    }

    private void setUpNavigationDrawerSubjectsSpinner() {

        ndSubjectsSpinner = (Spinner) findViewById(R.id.nd_subjects_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, testSubjects);
        ndSubjectsSpinner.setAdapter(spinnerAdapter);
      //  spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ndSubjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (loadedSubjectsSpinner) {
                    // changing the current brd id
                    currentCourseBrdId = testInfo.metadata.get(position).id;
                    setUpQuestion(0, true);
                    questionsAttemptInfoListForGridView.clear();
                    questionsAttemptInfoListForGridView.addAll(brdIdsToQuestionsAttemptInfoListMap
                            .get(currentCourseBrdId));
                    Log.d(TAG, brdIdsToQuestionsAttemptInfoListMap.toString());
                    Log.d(TAG, "Current Course Brd Id is " + currentCourseBrdId);
                    questionsGridViewAdapter.setCurrentQus(0);
                    questionsGridViewAdapter.notifyDataSetChanged();
                }
                loadedSubjectsSpinner = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void prepareInstructionsPopup() {

        // PopupWindow popup=getPopupWindow(getBaseContext(),);
    }

    EndTestAlertDialogFragment endTestDialogFragment;

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(mDrawerContent)) {
            mDrawerLayout.closeDrawer(mDrawerContent);
        } else {
            showWarningToEndTest();
        }
    }

    private void showWarningToEndTest() {

        endTestDialogFragment = EndTestAlertDialogFragment.newInstance();
        endTestDialogFragment.show(getSupportFragmentManager(), null);
        endTestDialogFragment.setCancelable(false);
    }

    @Override
    protected void onPause() {

        Log.d(TAG, "Pausing the test");
        super.onPause();
        // endTest();
    }

    @Override
    public void finish() {

        super.finish();
        endTest();
    }

    private void makeBrdIdsToQuestionsAttemptInfoListMap() {

        // TODO: verify this method
        int qusNo = 1;
        Map<String, QuestionAnalytics> qusAnalyticsMap = new HashMap<String, QuestionAnalytics>();

        for (Entry<String, List<String>> entry : brdIdToQuestionIdsMap.entrySet()) {
            Map<String, TakeTestQuestionWithAnswerGiven> ansInfoMap = aDataManager
                    .getQuestionWithAnswerAndQuestionMap(
                            session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                            session.getSessionStringValue(ConstantGlobal.USER_ID), entry.getValue());
            // fetch previously attempted question analytics data
            qusAnalyticsMap.putAll(aDataManager.getQuestionAnalyticsMap(
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                    session.getSessionStringValue(ConstantGlobal.USER_ID), test.id, test.type,
                    entry.getValue(), new String[]{ConstantGlobal.ID, ConstantGlobal.CORRECT,
                            ConstantGlobal.ANSWER_GIVEN, ConstantGlobal.SCORE,
                            ConstantGlobal.TIME_TAKEN, "qusNo"}));

            List<TakeTestQuestionWithAnswerGiven> ansInfoValues = new ArrayList<TakeTestQuestionWithAnswerGiven>();
            for (String id : entry.getValue()) {
                TakeTestQuestionWithAnswerGiven ansGiven = null;
                if ((ansGiven = ansInfoMap.get(id)) != null) {
                    ansGiven.setQusNo(qusNo);
                    QuestionAnalytics qA = qusAnalyticsMap.get(id);
                    if (qA != null) {
                        ansGiven.answerGiven = qA.answerGiven;
                        ansGiven.correct = qA.correct;
                        ansGiven.setAnalysed(true);
                        ansGiven.setScore(qA.score);
                        ansGiven.setTimeTaken(0);
                        ansGiven.setQusNo(qA.qusNo);
                        ansGiven.setStatus(AttemptStatus.SAVED);
                    }
                }
                qusNo++;
                ansInfoValues.add(ansGiven);
            }
            brdIdsToQuestionsAttemptInfoListMap.put(entry.getKey(), ansInfoValues);
            qusAnalyticsMap.clear();
        }
    }

    private void startTimer() {

        Log.d(TAG, "timer duration : " + duration + " ms");

        Log.d(TAG, "=========== timer start position : " + timerPosition);
        final long timerDuration = duration - timerPosition;

        countDownTimer = new CountDownTimer(timerDuration, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

                int remainingSecValue = (int) millisUntilFinished / 1000;
                String time = LocalManager.getDurationString(remainingSecValue);
                timerTextView.setText(time);
                navigationDrawerTimerTextView.setText(time);

                timerPosition = (int) (duration - millisUntilFinished);

                int updateTimeTakenRate = 5 * 1000;

                Log.d(TAG, "====== timerPostion: " + timerPosition + ", tickCount:" + timerPosition
                        / updateTimeTakenRate);
                if (timerPosition / updateTimeTakenRate > updatePersistantTimeTakenCount) {
                    updatePersistantTimeTakenCount++;
                    aDataManager.updateEntityAttemptTimeTaken(
                            session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                            session.getSessionStringValue(ConstantGlobal.USER_ID), test.type,
                            test.id, false, timerPosition);
                }
            }

            @Override
            public void onFinish() {

                Log.d(TAG, "timerTextView: " + timerTextView);
                // if (timerTextView != null)
                timerTextView.setText("0:00");
                navigationDrawerTimerTextView.setText("0:00");
                timerPosition = (int) duration;
                endTest();
            }
        };
        countDownTimer.start();
    }

    private OnClickListener getOnActionButtonsClickListener() {

        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                processActionButtonClick(v.getId());
            }
        };
    }

    private void processActionButtonClick(int buttonId) {

        AttemptStatus qPreviousStatus = currentQuestionAttemptInfo.getStatus();

        switch (buttonId) {

            case R.id.take_test_reset_button:
                currentQuestionFragment.resetAnswer();
                hideAllButtons();
                resetButtonsContent();
                skipButton.setVisibility(View.VISIBLE);
                markForReviewButton.setVisibility(View.VISIBLE);
                currentQuestionAttemptInfo.setStatus(AttemptStatus.RESET);
                tempAnswerForCurrentQuestion = null;
                if (qPreviousStatus.equals(AttemptStatus.SAVED)) {
                    calculateTimeTaken();
                    qAnalyticsProcessor.process(currentQuestionAttemptInfo, AttemptStatus.RESET,
                            currentQuestionData);
                    currentQuestionAttemptInfo.answerGiven = null;
                    currentQuestionAttemptInfo.correct = false;
                    currentQuestionAttemptInfo.setScore(0);
                    currentQuestionAttemptInfo.setAnalysed(false);
                    currentQuestionAttemptInfo.setTimeTaken(0);
                }
                questionsGridViewAdapter.notifyDataSetChanged();
                break;

            case R.id.take_test_mark_for_review_button:
                if (qPreviousStatus.equals(AttemptStatus.REVIEW)) {
                    currentQuestionAttemptInfo.setStatus(AttemptStatus.SKIP);
                    markForReviewButton.setText(R.string.mark_for_review);
                    questionsGridViewAdapter.notifyDataSetChanged();
                } else {
                    currentQuestionAttemptInfo.setStatus(AttemptStatus.REVIEW);
                    markForReviewButton.setText(R.string.unmark_for_review);
                    moveToNextQuestion();
                }
                break;

            case R.id.take_test_save_and_next_button:
                QuestionType qType = currentQuestionAttemptInfo.type;
                if (StringUtils.isNotEmpty(tempAnswerForCurrentQuestion)) {
                    if (qType.equals(QuestionType.NUMERIC)) {
                        try {
                            Float.parseFloat(tempAnswerForCurrentQuestion);
                        } catch (Exception e) {
                            Toast.makeText(this, "Please enter a numeric answer", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                    }
                } else {
                    // this should not happen, but just to be sure;
                    setAnswerForCurrentQuestion(ConstantGlobal.EMPTY);
                    Toast.makeText(this, "Please answer the question first", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!qPreviousStatus.equals(AttemptStatus.SAVED)) {
                    calculateTimeTaken();
                    Log.d(TAG, "The answer given is " + tempAnswerForCurrentQuestion);
                    currentQuestionAttemptInfo.answerGiven = tempAnswerForCurrentQuestion;
                    boolean isCorrect = qType.isCorrect(currentQuestionAttemptInfo.correctAnswer,
                            currentQuestionAttemptInfo.answerGiven);

                    TestMetadata mdata = courseIdToCourseMDataMap.get(currentCourseBrdId);

                    int marks = isCorrect ? mdata.getMarks(currentQuestionData.id).positive : -(mdata
                            .getMarks(currentQuestionData.id).negative);

                    currentQuestionAttemptInfo.setScore(marks);
                    currentQuestionAttemptInfo
                            .setMaxMarks(mdata.getMarks(currentQuestionData.id).positive);
                    Log.d(TAG, "question attempted correctly: " + isCorrect);
                    currentQuestionAttemptInfo.correct = isCorrect;
                    currentQuestionAttemptInfo.setAnalysed(true);
                    qAnalyticsProcessor.process(currentQuestionAttemptInfo, AttemptStatus.SAVED,
                            currentQuestionData);
                    // setting time to 0 since the time is used up in the above request
                    currentQuestionAttemptInfo.setTimeTaken(0);
                }
                currentQuestionAttemptInfo.setStatus(AttemptStatus.SAVED);
                moveToNextQuestion();
                break;
            default:
                currentQuestionAttemptInfo.setStatus(AttemptStatus.SKIP);
                moveToNextQuestion();
                break;
        }
    }

    private TextView resetButton;
    private TextView markForReviewButton;
    private TextView saveAndNextButton;
    private TextView skipButton;

    private void initButtonViews() {

        resetButton = (TextView) findViewById(R.id.take_test_reset_button);
        markForReviewButton = (TextView) findViewById(R.id.take_test_mark_for_review_button);
        saveAndNextButton = (TextView) findViewById(R.id.take_test_save_and_next_button);
        skipButton = (TextView) findViewById(R.id.take_test_skip_button);
        resetButton.setOnClickListener(getOnActionButtonsClickListener());
        markForReviewButton.setOnClickListener(getOnActionButtonsClickListener());
        saveAndNextButton.setOnClickListener(getOnActionButtonsClickListener());
        skipButton.setOnClickListener(getOnActionButtonsClickListener());
    }

    private void moveToNextQuestion() {

        int newIndex = currentQuestionIndexCourseWise.get(currentCourseBrdId) + 1;
        goToDirectQuestion(newIndex);
    }

    private void goToDirectQuestion(int newIndex) {

        if (newIndex > brdIdsToQuestionsAttemptInfoListMap.get(currentCourseBrdId).size() - 1) {
            Log.d(TAG, "Reached the end of the subject");
            int subjectsCount = testInfo.metadata.size();
            int currentSpinnerPos = ndSubjectsSpinner.getSelectedItemPosition();
            if (currentSpinnerPos < (subjectsCount - 1)) {
                ndSubjectsSpinner.setSelection(currentSpinnerPos + 1);
            } else {
                Log.d(TAG, "Reached the last question");
                if (subjectsCount > 1) {
                    ndSubjectsSpinner.setSelection(0);
                } else {
                    goToDirectQuestion(0);
                }
                mDrawerLayout.openDrawer(mDrawerContent);
                Toast.makeText(getBaseContext(),
                        "You have seen all questions, review or end test ", Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            Log.d(TAG, "Loading the next question: " + newIndex);
            Log.d(TAG, "go to duirecc");
            setUpQuestion(newIndex, true);
        }
    }

    // this will calculate time taken in seconds
    private void calculateTimeTaken() {

        long timeTaken = (timerPosition - questionStartTimerPosition) / 1000;
        // if (attemptStatus != AttemptStatus.SAVED && attemptStatus != AttemptStatus.RESET) {
        // // this is because in case of reset and saved the timeTaken is
        // // already being saved in database, hence only new time taken is
        // // provided
        // timeTaken += currentQuestionAttemptInfo.getTimeTaken();
        // }
        if (currentQuestionAttemptInfo != null) {
            timeTaken += currentQuestionAttemptInfo.getTimeTaken();
            currentQuestionAttemptInfo.setTimeTaken((int) timeTaken);
        }
    }

    boolean endedTest = false;

    private void endTest() {

        if (endedTest) {
            return;
        }

        for (Entry<String, List<TakeTestQuestionWithAnswerGiven>> entry : brdIdsToQuestionsAttemptInfoListMap
                .entrySet()) {
            for (TakeTestQuestionWithAnswerGiven qusAns : entry.getValue()) {
                if (qusAns.answerGiven == null || !qusAns.isAnalysed()) {
                    qAnalyticsProcessor.process(qusAns, AttemptStatus.SKIP,
                            qusAns.getQuestion(test.userId, cDataManager));
                }
            }
        }

        TestAnalytics testAnalytic = aDataManager.getTestAnalytics(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID), test.id, test.type);
        if (testAnalytic != null) {
            testAnalytic.timeTaken = timerPosition/1000;
            testAnalytic.endTime = String.valueOf(Long.valueOf(testAnalytic.timeCreated) + (int) timerPosition);
            aDataManager.updateTestAnalytics(testAnalytic, true);
        }

        Toast.makeText(getBaseContext(), "Test Ended", Toast.LENGTH_SHORT).show();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        endedTest = true;
        aDataManager.updateEntityAttemptTimeTaken(test.orgKeyId, test.userId, test.type, test.id,
                true, 0);
        sendBroadcast(new Intent(getApplicationContext(), NetworkMonitor.class));

        Intent intent = new Intent(this, TestPreAttemptPageActivity.class);
        intent.putExtra("showPostTestPages", true);
        intent.putExtra(ConstantGlobal.CONTENT_ID, testContentId);
        startActivity(intent);

        super.finish();
    }

    public void setUpQuestion(int position, boolean calculateTime) {

        // TODO time taken
        if (calculateTime) {
            calculateTimeTaken();
        }
        TakeTestQuestionWithAnswerGiven questionAttemptInfo = brdIdsToQuestionsAttemptInfoListMap
                .get(currentCourseBrdId).get(position);

        tempAnswerForCurrentQuestion = questionAttemptInfo.answerGiven;
        questionStartTimerPosition = timerPosition;
        currentQuestionAttemptInfo = questionAttemptInfo;
        currentQuestionNumView.setText(Integer.toString(questionAttemptInfo.getQusNo()));
        questionTypeView.setText(questionAttemptInfo.type.descText());
        currentQuestionIndexCourseWise.put(currentCourseBrdId, position);
        currentQuestionData = currentQuestionAttemptInfo.getQuestion(
                session.getSessionStringValue(ConstantGlobal.USER_ID), cDataManager);
        currentQuestionFragment = new TakeTestQuestionFragment();
        Bundle bundle = new Bundle();
        // bundle.putSerializable(ConstantGlobal.QUESTION, currentQuestionData);
        bundle.putString(ConstantGlobal.ANSWER_GIVEN, questionAttemptInfo.answerGiven);

        currentQuestionFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.take_test_questions_frame_layout, currentQuestionFragment).commit();

        TestMetadata mdata = courseIdToCourseMDataMap.get(currentCourseBrdId);

        int positveMarks = mdata.getMarks(currentQuestionData.id).positive;
        int negativeMarks = mdata.getMarks(currentQuestionData.id).negative;

        ((TextView) findViewById(R.id.take_test_question_positive_marks)).setText("+"
                + positveMarks);
        if (negativeMarks > 0) {
            ((TextView) findViewById(R.id.take_test_question_negative_marks)).setText("-"
                    + negativeMarks);
        } else {
            ((TextView) findViewById(R.id.take_test_question_negative_marks)).setText("");
        }

        hideAllButtons();
        resetButtonsContent();

        if (questionAttemptInfo.getStatus() == null) {
            questionAttemptInfo.setStatus(AttemptStatus.SKIP);
        }
        AttemptStatus status = questionAttemptInfo.getStatus();
        if (status.equals(AttemptStatus.SAVED)) {
            resetButton.setVisibility(View.VISIBLE);
            saveAndNextButton.setVisibility(View.VISIBLE);
            saveAndNextButton.setText(R.string.next);
        } else {
            skipButton.setVisibility(View.VISIBLE);
            markForReviewButton.setVisibility(View.VISIBLE);
            if (status.equals(AttemptStatus.REVIEW)) {
                markForReviewButton.setText(R.string.unmark_for_review);
            }
        }
        questionsGridViewAdapter.setCurrentQus(position);
        Log.d(TAG, "Notifiedin");
        questionsGridViewAdapter.notifyDataSetChanged();
        // TODO update a single instead of redrawing all the items
    }

    public void hideAllButtons() {

        resetButton.setVisibility(View.GONE);
        saveAndNextButton.setVisibility(View.GONE);
        markForReviewButton.setVisibility(View.GONE);
        skipButton.setVisibility(View.GONE);
    }

    public void resetButtonsContent() {

        saveAndNextButton.setText(R.string.save_and_next);
        saveAndNextButton.setBackgroundResource(R.color.darkestgrey);
        saveAndNextButton.setTextColor(getResources().getColor(R.color.white));
        markForReviewButton.setText(R.string.mark_for_review);
    }

    @Override
    public void setAnswerForCurrentQuestion(String answer) {

        hideAllButtons();
        if (StringUtils.isEmpty(answer)) {
            markForReviewButton.setVisibility(View.VISIBLE);
            skipButton.setVisibility(View.VISIBLE);
        } else {
            resetButton.setVisibility(View.VISIBLE);
            saveAndNextButton.setVisibility(View.VISIBLE);
            saveAndNextButton.setBackgroundResource(R.color.green);
            saveAndNextButton.setTextColor(getResources().getColor(R.color.darkgrey));
        }
        tempAnswerForCurrentQuestion = answer;
    }

    public Question getCurrentQuestionData() {

        return currentQuestionData;
    }

    public void doPositiveClick() {

        endTest();
        endTestDialogFragment.dismiss();

    }

    public void doNegativeClick() {

        endTestDialogFragment.dismiss();
    }

    final public static class EndTestAlertDialogFragment extends DialogFragment {

        public static EndTestAlertDialogFragment newInstance() {

            EndTestAlertDialogFragment frag = new EndTestAlertDialogFragment();
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())
                    .setTitle("Do you want to end the test ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {

                            ((TakeTestActivity) getActivity()).endTest();
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).create();
        }
    }

    @Override
    public void onResume() {

        super.onResume();
        hideStatusBar();
        GoogleAnalyticsUtils.sendPageViewDataToGA();
    }
}
