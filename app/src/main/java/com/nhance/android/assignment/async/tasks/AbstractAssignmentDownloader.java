package com.nhance.android.assignment.async.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.async.tasks.AbstractLibraryLoader;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.downloader.DownloadInfo;
import com.nhance.android.enums.EntityType;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AbstractAssignmentDownloader extends AbstractLibraryLoader {

    private final String TAG = "AbstractAssignmentDownloader";
    protected Content assignment;
    protected Context context;
    protected DownloadHistoryManager dHistoryManager;
    protected DownloadInfo dInfo;

    public boolean                   runOnCallingThread;

    public AbstractAssignmentDownloader(Content Assignment, Context context) {

        super(SessionManager.getInstance(context), null, new HashMap<String, Object>());
        this.assignment = Assignment;
        this.context = context;
        this.url = session.getApiUrl("getContents");
        if (this.assignment != null) {
            this.dHistoryManager = new DownloadHistoryManager(context);
            this.dInfo = new DownloadInfo(Assignment.orgKeyId, "", "", 1);
        }

    }

    @Override
    protected JSONObject doInBackground(String... params) {

        Log.d(TAG, "doInBackground : ");
        return download();
    }

    public JSONObject download() {

        session.addSessionParams(httpParams);

        AssignmentExtendedInfo AssignmentInfo = (AssignmentExtendedInfo) assignment.toContentExtendedInfo();
        Log.d(TAG, "AssignmentInfo: " + AssignmentInfo);
        List<String> qIds = AssignmentInfo.__getAllQIds();

        httpParams.put(ConstantGlobal.TYPE, EntityType.QUESTION.name());
        httpParams.put("addAnswer", true);
        SessionManager.addListParams(qIds, ConstantGlobal.IDS, httpParams);
        Log.d(TAG, "qIds: " + qIds);

        if (dInfo.state == DownloadInfo.DownloadState.TOSTART || dInfo.state == DownloadInfo.DownloadState.STARTED) {
            dInfo.state = DownloadInfo.DownloadState.STARTED;
            dInfo.lastStarted = System.currentTimeMillis();
        }
        JSONObject json = null;
        try {
            Log.d(TAG, "doInBackground Before http req: ");
            json = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        error = VedantuWebUtils.checkErrorMsg(json, url);
        if (!error) {
            try {
                json = JSONUtils.getJSONObject(json, "result");
                Log.d(TAG, "updateQuestions background : " + json.toString());
                updateQuestions(json);
                return json;
            } catch (Exception e) {
                dInfo.state = DownloadInfo.DownloadState.STOPPED;
                Log.e(TAG, e.getMessage(), e);
            } finally {
                updateDownloadHistory(false);
            }
        }
        return null;
    }

    private void updateQuestions(JSONObject json) throws Exception {

        Log.d(TAG, "updateQuestions : " + json.toString());
        JSONArray jArray = JSONUtils.getJSONArray(json, "list");
        int total = jArray.length();
        dInfo.total = total;
        updateDownloadHistory(false);
        for (int i = 0; i < total; i++) {
            JSONObject contentJSON = jArray.getJSONObject(i);
            dInfo.downloaded++;
            updateQuestion(contentJSON, SyncType.ONLINE);
            updateProgress();

        }
    }

    protected void updateProgress() {

        int percentage = (int) ((dInfo.downloaded * 100) / dInfo.total);
        Log.d(TAG, "completed percentage : " + percentage);
        if (!runOnCallingThread) {
            publishProgress(percentage);
        }

        if (percentage == 100) {
            markDownloadComplete();
        }

        long timePassed = System.currentTimeMillis() - dInfo.lastStarted;
        if (timePassed % 1000 == 0) {
            updateDownloadHistory(true);
        }
        updateDownloadHistory(false);
    }

    protected void updateDownloadHistory(boolean partialUpdate) {

        try {
            if (partialUpdate) {
                ContentValues values = new ContentValues();
                values.put(ConstantGlobal.DOWNLOADED, String.valueOf(dInfo.downloaded));
                dHistoryManager.updateDownloadHistory((int) dInfo.rowId, values);
            } else {
                dHistoryManager.updateDownloadInfo(assignment._id, dInfo);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void markDownloadComplete() {

        boolean completed = dInfo.downloaded == dInfo.total;
        if (completed && !runOnCallingThread) {
            publishProgress(100);
        }

        if (dInfo.state == DownloadInfo.DownloadState.STARTED && completed) {
            dInfo.elapsedTime += System.currentTimeMillis() - dInfo.lastStarted;
            dInfo.lastStarted = 0;
            dInfo.state = DownloadInfo.DownloadState.FINISHED;
        }
    }

    @Override
    protected boolean downloadImage(String imageUrl, String toDir, String type) {

        return false;
    }

    public boolean isDownloadFinished() {

        return dInfo.state == DownloadInfo.DownloadState.FINISHED;
    }

}
