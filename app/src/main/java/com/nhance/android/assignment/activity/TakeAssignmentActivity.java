package com.nhance.android.assignment.activity;

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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.assignment.adapter.TakeAssignmentGridViewQuestionListAdapter;
import com.nhance.android.assignment.db.models.analytics.AssignmentAnalytics;
import com.nhance.android.assignment.fragments.assignment.TakeAssignmentQuestionFragment;
import com.nhance.android.assignment.fragments.assignment.TakeAssignmentQuestionFragment.NotifyTakeAssignmentFragment;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.assignment.processors.analytics.sync.AssignmentQuestionAnalyticsProcessor;
import com.nhance.android.assignment.receiver.AssignmentNetworkMonitor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.customviews.NonScrollableGridView;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.analytics.QuestionAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.AttemptState;
import com.nhance.android.enums.QuestionType;
import com.nhance.android.utils.GoogleAnalyticsUtils;
import com.nhance.android.utils.ViewUtils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeAssignmentActivity extends NhanceBaseActivity implements NotifyTakeAssignmentFragment {

    private static final String TAG = "TakeNewAssignmentActivity";
    private Content assignment;
    private ContentDataManager cDataManager;
    List<String> qids;

    GridView gridView;

    long duration;
//    TextView timerTextView;
    long questionStartTimerPosition;
    TextView currentCourseTextView;
    CountDownTimer countDownTimer;
    final Map<String, Integer> currentQuestionIndexCourseWise = new HashMap<String, Integer>();                              // 0
    // base
    Map<String, List<String>> brdIdToQuestionIdsMap;
    final Map<String, List<TakeAssignmentQuestionWithAnswerGiven>> brdIdsToQuestionsAttemptInfoListMap = new HashMap<String, List<TakeAssignmentQuestionWithAnswerGiven>>();
    final Map<String, AssignmentMetadata> courseIdToCourseMDataMap = new HashMap<String, AssignmentMetadata>();

    String currentCourseBrdId;
    TakeAssignmentQuestionWithAnswerGiven currentQuestionAttemptInfo;
    List<String> qTypeSpinnerItems = new ArrayList<String>();

    SparseIntArray activeBgSelectorMap = new SparseIntArray();
    SparseIntArray inactiveColorMap = new SparseIntArray();

    AssignmentQuestionAnalyticsProcessor qAnalyticsProcessor;
    private TextView currentQuestionNumView;
    private TextView totalQuestionNumView;
    private TextView questionTypeView;
    private Question currentQuestionData;
    private TakeAssignmentQuestionFragment currentQuestionFragment;
    private String tempAnswerForCurrentQuestion;
    private AssignmentExtendedInfo assignmentInfo;
    private Spinner ndSubjectsSpinner;
    private List<String> assignmentSubjects;
    private boolean loadedSubjectsSpinner = false;
    private int assignmentContentId;
    private List<TakeAssignmentQuestionWithAnswerGiven> questionsAttemptInfoListForGridView = new ArrayList<TakeAssignmentQuestionWithAnswerGiven>();
    AnalyticsDataManager aDataManager;
    int updatePersistantTimeTakenCount = 0;

    // in milliseconds
    int timerPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_TAKE_ASSIGNMENT);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideStatusBar();

        setContentView(R.layout.activity_take_assignment);
        cDataManager = new ContentDataManager(getApplicationContext());
        aDataManager = new AnalyticsDataManager(getApplicationContext());
        assignmentContentId = getIntent().getIntExtra(ConstantGlobal.CONTENT_ID, -1);
        assignment = cDataManager.getContent(assignmentContentId);

        if (assignment == null || !assignment.downloaded) {
            Toast.makeText(getBaseContext(), "Assignment not downloaded", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        GoogleAnalyticsUtils.setCustomDimensions(assignment.id, assignment.name, ConstantGlobal.ASSIGNMENT);

        assignmentInfo = (AssignmentExtendedInfo) assignment.toContentExtendedInfo();

        if (assignmentInfo.metadata == null || assignmentInfo.metadata.size() == 0) {
            Log.e(TAG, "no course metadata found");
            finish();
            return;
        } else if (assignmentInfo.attempteState == AttemptState.ATTEMPTED) {
            Log.e(TAG, "You have already attempted this assignment");
            finish();
        }

        duration = assignmentInfo.duration;
        assignmentSubjects = new ArrayList<String>();
        for (AssignmentMetadata cData : assignmentInfo.metadata) {
            courseIdToCourseMDataMap.put(cData.id, cData);
            assignmentSubjects.add(cData.name);
        }

        brdIdToQuestionIdsMap = assignmentInfo.getBoardWiseQidMap();
        currentCourseBrdId = assignmentInfo.metadata.get(0).id;

        makeBrdIdsToQuestionsAttemptInfoListMap();

//        timerTextView = (TextView) findViewById(R.id.take_assignment_time_counter);
        currentQuestionNumView = (TextView) findViewById(R.id.take_assignment_current_ques_no);
        totalQuestionNumView = (TextView) findViewById(R.id.take_assignment_current_total_subject_quesns);
        questionTypeView = (TextView) findViewById(R.id.take_assignment_current_question_type);
        totalQuestionNumView.setText("/" + Integer.toString(assignmentInfo.qusCount));
        initButtonViews();
        setUpNavigationDrawerContent();
        findViewById(R.id.take_assignment_open_nav_drawer).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mDrawerLayout.openDrawer(mDrawerContent);
            }
        });

        // load the first question of first subject
        timerPosition = aDataManager.getEntityAttemptTimeTaken(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID), assignment.type, assignment.id);

        Log.d(TAG, "timerPosition : " + timerPosition);
        setUpQuestion(0, false);
        qAnalyticsProcessor = new AssignmentQuestionAnalyticsProcessor(getBaseContext(), assignment,
                assignmentInfo.metadata, session.getSessionStringValue(ConstantGlobal.USER_ID));

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                mDrawerLayout.openDrawer(mDrawerContent);
            }
        }, 750);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void hideStatusBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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
    private TakeAssignmentGridViewQuestionListAdapter questionsGridViewAdapter;

    private AdapterView.OnItemClickListener getOnGridItemClickListener() {

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
        mDrawerLayout = (DrawerLayout) findViewById(R.id.take_assignment_drawer_layout);
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
        mDrawerContent = (ScrollView) findViewById(R.id.take_assignment_navigation_drawer);
        mDrawerContent.getLayoutParams().width = ViewUtils.getOrientationSpecificWidth(this);
        navigationDrawerTimerTextView = (TextView) mDrawerContent.findViewById(R.id.nd_time_count);

        View.OnClickListener endAssignmentListner = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                showWarningToEndAssignment();
            }
        };
        mDrawerContent.findViewById(R.id.take_assignment_end_assignment_button).setOnClickListener(
                endAssignmentListner);

        findViewById(R.id.take_assignment_end_assignment_from_top_bar).setOnClickListener(endAssignmentListner);
        ((TextView) mDrawerContent.findViewById(R.id.take_assignment_name)).setText(assignment.name);
        mDrawerContent.findViewById(R.id.take_assignment_view_instructions).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        prepareInstructionsPopup();
                    }
                });
        setUpNavigationDrawerSubjectsSpinner();
        questionsAttemptInfoListForGridView.addAll(brdIdsToQuestionsAttemptInfoListMap
                .get(currentCourseBrdId));
        questionsGridViewAdapter = new TakeAssignmentGridViewQuestionListAdapter(this,
                R.layout.grid_item_view_take_assignment_question, questionsAttemptInfoListForGridView);
        NonScrollableGridView questionsGridView = (NonScrollableGridView) mDrawerContent
                .findViewById(R.id.take_assignment_questions_grid_view);
        questionsGridView.setAdapter(questionsGridViewAdapter);
        questionsGridView.setOnItemClickListener(getOnGridItemClickListener());
    }

    private void setUpNavigationDrawerSubjectsSpinner() {
//karthi
        ndSubjectsSpinner = (Spinner) findViewById(R.id.nd_subjects_spinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, assignmentSubjects);
        ndSubjectsSpinner.setAdapter(spinnerAdapter);
        //spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ndSubjectsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (loadedSubjectsSpinner) {
                    // changing the current brd id
                    currentCourseBrdId = assignmentInfo.metadata.get(position).id;
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

    TakeAssignmentActivity.EndAssignmentAlertDialogFragment endAssignmentDialogFragment;

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(mDrawerContent)) {
            mDrawerLayout.closeDrawer(mDrawerContent);
        } else {
            showWarningToEndAssignment();
        }
    }

    private void showWarningToEndAssignment() {

        endAssignmentDialogFragment = TakeAssignmentActivity.EndAssignmentAlertDialogFragment.newInstance();
        endAssignmentDialogFragment.show(getSupportFragmentManager(), null);
        endAssignmentDialogFragment.setCancelable(false);
    }

    @Override
    protected void onPause() {

        Log.d(TAG, "Pausing the assignment");
        super.onPause();
        // endAssignment();
    }

    @Override
    public void finish() {

        super.finish();
        Log.e("finish","endAssignment");
        endAssignment();
    }

    private void makeBrdIdsToQuestionsAttemptInfoListMap() {

        // TODO: verify this method
        int qusNo = 1;
        Map<String, QuestionAnalytics> qusAnalyticsMap = new HashMap<String, QuestionAnalytics>();

        for (Map.Entry<String, List<String>> entry : brdIdToQuestionIdsMap.entrySet()) {
            Map<String, TakeAssignmentQuestionWithAnswerGiven> ansInfoMap = aDataManager
                    .getAssignmentQuestionWithAnswerAndQuestionMap(
                            session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                            session.getSessionStringValue(ConstantGlobal.USER_ID), entry.getValue());
            // fetch previously attempted question analytics data
            qusAnalyticsMap.putAll(aDataManager.getQuestionAnalyticsMap(
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                    session.getSessionStringValue(ConstantGlobal.USER_ID), assignment.id, assignment.type,
                    entry.getValue(), new String[]{ConstantGlobal.ID, ConstantGlobal.CORRECT,
                            ConstantGlobal.ANSWER_GIVEN, ConstantGlobal.SCORE,
                            ConstantGlobal.TIME_TAKEN, "qusNo"}));

            List<TakeAssignmentQuestionWithAnswerGiven> ansInfoValues = new ArrayList<TakeAssignmentQuestionWithAnswerGiven>();
            for (String id : entry.getValue()) {
                TakeAssignmentQuestionWithAnswerGiven ansGiven = null;
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
                        ansGiven.setStatus(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.SAVED);
                    }
                }
                qusNo++;
                ansInfoValues.add(ansGiven);
            }
            brdIdsToQuestionsAttemptInfoListMap.put(entry.getKey(), ansInfoValues);
            qusAnalyticsMap.clear();
        }
    }


    private View.OnClickListener getOnActionButtonsClickListener() {

        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                processActionButtonClick(v.getId());
            }
        };
    }

    private void processActionButtonClick(int buttonId) {

        TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus qPreviousStatus = currentQuestionAttemptInfo.getStatus();

        switch (buttonId) {

            case R.id.take_assignment_reset_button:
                currentQuestionFragment.resetAnswer();
                hideAllButtons();
                resetButtonsContent();
                skipButton.setVisibility(View.VISIBLE);
                markForReviewButton.setVisibility(View.VISIBLE);
                currentQuestionAttemptInfo.setStatus(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.RESET);
                tempAnswerForCurrentQuestion = null;
                if (qPreviousStatus.equals(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.SAVED)) {
                    calculateTimeTaken();
                    qAnalyticsProcessor.process(currentQuestionAttemptInfo, TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.RESET,
                            currentQuestionData);
                    currentQuestionAttemptInfo.answerGiven = null;
                    currentQuestionAttemptInfo.correct = false;
                    currentQuestionAttemptInfo.setScore(0);
                    currentQuestionAttemptInfo.setAnalysed(false);
                    currentQuestionAttemptInfo.setTimeTaken(0);
                }
                questionsGridViewAdapter.notifyDataSetChanged();
                break;

            case R.id.take_assignment_mark_for_review_button:
                if (qPreviousStatus.equals(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.REVIEW)) {
                    currentQuestionAttemptInfo.setStatus(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.SKIP);
                    markForReviewButton.setText(R.string.mark_for_review);
                    questionsGridViewAdapter.notifyDataSetChanged();
                } else {
                    currentQuestionAttemptInfo.setStatus(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.REVIEW);
                    markForReviewButton.setText(R.string.unmark_for_review);
                    moveToNextQuestion();
                }
                break;

            case R.id.take_assignment_save_and_next_button:
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

                if (!qPreviousStatus.equals(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.SAVED)) {
                    calculateTimeTaken();
                    Log.d(TAG, "The answer given is " + tempAnswerForCurrentQuestion);
                    currentQuestionAttemptInfo.answerGiven = tempAnswerForCurrentQuestion;
                    boolean isCorrect = qType.isCorrect(currentQuestionAttemptInfo.correctAnswer,
                            currentQuestionAttemptInfo.answerGiven);

                    AssignmentMetadata mdata = courseIdToCourseMDataMap.get(currentCourseBrdId);

//                    int marks = isCorrect ? mdata.getMarks(currentQuestionData.id).positive : -(mdata
//                            .getMarks(currentQuestionData.id).negative);

//                    currentQuestionAttemptInfo.setScore(marks);
//                    currentQuestionAttemptInfo
//                            .setMaxMarks(mdata.getMarks(currentQuestionData.id).positive);
                    Log.d(TAG, "question attempted correctly: " + isCorrect);
                    currentQuestionAttemptInfo.correct = isCorrect;
                    currentQuestionAttemptInfo.setAnalysed(true);
                    qAnalyticsProcessor.process(currentQuestionAttemptInfo, TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.SAVED,
                            currentQuestionData);
                    // setting time to 0 since the time is used up in the above request
                    currentQuestionAttemptInfo.setTimeTaken(0);
                }
                currentQuestionAttemptInfo.setStatus(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.SAVED);
                moveToNextQuestion();
                break;
            default:
                currentQuestionAttemptInfo.setStatus(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.SKIP);
                moveToNextQuestion();
                break;
        }
    }

    private TextView resetButton;
    private TextView markForReviewButton;
    private TextView saveAndNextButton;
    private TextView skipButton;

    private void initButtonViews() {

        resetButton = (TextView) findViewById(R.id.take_assignment_reset_button);
        markForReviewButton = (TextView) findViewById(R.id.take_assignment_mark_for_review_button);
        saveAndNextButton = (TextView) findViewById(R.id.take_assignment_save_and_next_button);
        skipButton = (TextView) findViewById(R.id.take_assignment_skip_button);
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
            int subjectsCount = assignmentInfo.metadata.size();
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
                        "You have seen all questions, review or end assignment ", Toast.LENGTH_LONG)
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
        // if (attemptStatus != AssignementAttemptStatus.SAVED && attemptStatus != AssignementAttemptStatus.RESET) {
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

    boolean endedAssignment = false;

    private void endAssignment() {

        if (endedAssignment) {
            return;
        }

        for (Map.Entry<String, List<TakeAssignmentQuestionWithAnswerGiven>> entry : brdIdsToQuestionsAttemptInfoListMap
                .entrySet()) {
            for (TakeAssignmentQuestionWithAnswerGiven qusAns : entry.getValue()) {
                if (qusAns.answerGiven == null || !qusAns.isAnalysed()) {
                    qAnalyticsProcessor.process(qusAns, TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.SKIP,
                            qusAns.getQuestion(assignment.userId, cDataManager));
                }
            }
        }

        AssignmentAnalytics assignmentAnalytic = aDataManager.getAssignmentAnalytics(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID), assignment.id, assignment.type);
        if (assignmentAnalytic != null) {
            assignmentAnalytic.timeTaken = timerPosition / 1000;
            assignmentAnalytic.endTime = String.valueOf(Long.valueOf(assignmentAnalytic.timeCreated)
                    + assignmentAnalytic.timeTaken);
            aDataManager.updateAssignmentAnalytics(assignmentAnalytic, true);
        }

        Toast.makeText(getBaseContext(), "Assignment Ended", Toast.LENGTH_SHORT).show();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        endedAssignment = true;
        aDataManager.updateEntityAttemptTimeTaken(assignment.orgKeyId, assignment.userId, assignment.type, assignment.id,
                true, 0);
        sendBroadcast(new Intent(getApplicationContext(), AssignmentNetworkMonitor.class));

        Intent intent = new Intent(this, AssignmentPreAttemptPageActivity.class);
        intent.putExtra("showPostAssignmentPages", true);
        intent.putExtra(ConstantGlobal.CONTENT_ID, assignmentContentId);
        startActivity(intent);

        super.finish();
    }

    public void setUpQuestion(int position, boolean calculateTime) {

        // TODO time taken
        if (calculateTime) {
            calculateTimeTaken();
        }
        TakeAssignmentQuestionWithAnswerGiven questionAttemptInfo = brdIdsToQuestionsAttemptInfoListMap
                .get(currentCourseBrdId).get(position);

        tempAnswerForCurrentQuestion = questionAttemptInfo.answerGiven;
        questionStartTimerPosition = timerPosition;
        currentQuestionAttemptInfo = questionAttemptInfo;
        currentQuestionNumView.setText(Integer.toString(questionAttemptInfo.getQusNo()));
        questionTypeView.setText(questionAttemptInfo.type.descText());
        currentQuestionIndexCourseWise.put(currentCourseBrdId, position);
        currentQuestionData = currentQuestionAttemptInfo.getQuestion(
                session.getSessionStringValue(ConstantGlobal.USER_ID), cDataManager);
        currentQuestionFragment = new TakeAssignmentQuestionFragment();
        Bundle bundle = new Bundle();
        // bundle.putSerializable(ConstantGlobal.QUESTION, currentQuestionData);
        bundle.putString(ConstantGlobal.ANSWER_GIVEN, questionAttemptInfo.answerGiven);

        currentQuestionFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.take_assignment_questions_frame_layout, currentQuestionFragment).commit();

//        AssignmentMetadata mdata = courseIdToCourseMDataMap.get(currentCourseBrdId);

//        int positveMarks = mdata.getMarks(currentQuestionData.id).positive;
//        int negativeMarks = mdata.getMarks(currentQuestionData.id).negative;
//
//        ((TextView) findViewById(R.id.take_assignment_question_positive_marks)).setText("+"
//                + positveMarks);
//        if (negativeMarks > 0) {
//            ((TextView) findViewById(R.id.take_assignment_question_negative_marks)).setText("-"
//                    + negativeMarks);
//        } else {
//            ((TextView) findViewById(R.id.take_assignment_question_negative_marks)).setText("");
//        }

        hideAllButtons();
        resetButtonsContent();

        if (questionAttemptInfo.getStatus() == null) {
            questionAttemptInfo.setStatus(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.SKIP);
        }
        TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus status = questionAttemptInfo.getStatus();
        if (status.equals(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.SAVED)) {
            resetButton.setVisibility(View.VISIBLE);
            saveAndNextButton.setVisibility(View.VISIBLE);
            saveAndNextButton.setText(R.string.next);
        } else {
            skipButton.setVisibility(View.VISIBLE);
            markForReviewButton.setVisibility(View.VISIBLE);
            if (status.equals(TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus.REVIEW)) {
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
        Log.e("dopositiveClick","endAssignment");

        endAssignment();
        endAssignmentDialogFragment.dismiss();

    }

    public void doNegativeClick() {

        endAssignmentDialogFragment.dismiss();
    }

    final public static class EndAssignmentAlertDialogFragment extends DialogFragment {

        public static TakeAssignmentActivity.EndAssignmentAlertDialogFragment newInstance() {

            TakeAssignmentActivity.EndAssignmentAlertDialogFragment frag = new TakeAssignmentActivity.EndAssignmentAlertDialogFragment();
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            return new AlertDialog.Builder(getActivity())
                    .setTitle("Do you want to end the assignment ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Log.e("o create dialog","endAssignment");

                            ((TakeAssignmentActivity) getActivity()).endAssignment();
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
