package com.nhance.android.assignment.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.assignment.async.tasks.AssignmentDownloader;
import com.nhance.android.assignment.fragments.assignment.AssignmentPostAttemptPageFragment;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.async.tasks.IDownloadCompleteProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.utils.GoogleAnalyticsUtils;

import org.json.JSONObject;

/**
 * Created by administrator on 12/22/16.
 */

public class AssigmentTeacherPageActivity extends NhanceBaseActivity {


    private ContentDataManager cDataManager;
    private Content assignment;
    private AssignmentExtendedInfo assignmentInfo;
    private static final String TAG                     = "AssignmentTeacherPageActivity";
    private ProgressDialog dialog;
    private boolean             isDestroyed             = false;
    private boolean             mDestroyedForReCreation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_assignment_teacher_page);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GoogleAnalyticsUtils.setCustomDimensions(
                getIntent().getStringExtra(ConstantGlobal.ENTITY_ID),
                getIntent().getStringExtra(ConstantGlobal.NAME), ConstantGlobal.ASSIGNMENT);
        String title = getIntent().getStringExtra(ConstantGlobal.ASSIGNMENT_NAME);

        Log.e("title","........"+title);
        //getSupportActionBar().setTitle(title);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       // getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(title);

        fillAssignmentContent();
        loadOrDownloadAssignment();
    }

    private void loadOrDownloadAssignment() {

        if (!assignment.downloaded) {
            dialog = new ProgressDialog(this);
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {

                    finish();
                }
            });
            dialog.setMessage(getResources().getString(R.string.downloading_assignment_result_msg));
            dialog.show();
            AssignmentDownloader assignmentDownloader = new AssignmentDownloader(assignment, getApplicationContext(),
                    new IDownloadCompleteProcessor<JSONObject>() {

                        @Override
                        public JSONObject onComplete(JSONObject result, boolean completed) {

                            if (completed && !isDestroyed) {
                                assignment.downloaded = true;
                                ContentValues tobeUpdatedValues = new ContentValues();
                                tobeUpdatedValues.put(ConstantGlobal.DOWNLOADED, 1);
                                cDataManager.updateContent(assignment._id, tobeUpdatedValues);
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                                loadPostAssignmentPageContent();
                            }
                            return null;
                        }
                    });
            assignmentDownloader.executeTask(false);
        } else {
            loadPostAssignmentPageContent();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {

        mDestroyedForReCreation = true;
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onResume() {

        if (mDestroyedForReCreation) {
            mDestroyedForReCreation = false;
            loadOrDownloadAssignment();

        }
        super.onResume();
    }

    private void fillAssignmentContent() {

        cDataManager = new ContentDataManager(getApplicationContext());
        int contentId = getIntent().getIntExtra(ConstantGlobal.CONTENT_ID, -1);
        if (contentId == -1) {
            Toast.makeText(getApplicationContext(), "There is some problem in opening the assignment.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        assignment = cDataManager.getContent(contentId);
        assignmentInfo = assignment == null ? null : (AssignmentExtendedInfo) assignment.toContentExtendedInfo();
    }

    private void loadPostAssignmentPageContent() {

        if (mDestroyedForReCreation) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(ConstantGlobal.ENTITY_ID, assignment.id);
        args.putString(ConstantGlobal.ENTITY_TYPE, assignment.type);
        args.putString(ConstantGlobal.ASSIGNMENT_NAME, assignment.name);
        Log.d(TAG, "ASSIGNMENT INFO ========== " + assignmentInfo);
        args.putSerializable(ConstantGlobal.ASSIGNMENT_METADATA, assignmentInfo);

        AssignmentPostAttemptPageFragment fragment = new AssignmentPostAttemptPageFragment();
        fragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.assignment_teacher_fragment_layout, fragment);
        ft.addToBackStack("AssignmentPostAttemptPageFragment");
        ft.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {

        isDestroyed = true;
        super.onDestroy();
    }





}
