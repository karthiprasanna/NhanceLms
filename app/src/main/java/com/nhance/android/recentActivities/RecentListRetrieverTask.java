package com.nhance.android.recentActivities;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class RecentListRetrieverTask extends AbstractVedantuJSONAsyncTask {

    private static final String        TAG          = "ReRetreiverTask";
    private String newsActivityId;
    private ITaskProcessor<JSONObject> taskProcessor;
    private int sizeOfList;
    private ProgressDialog pDialog;
    private String eType, eId;

    public RecentListRetrieverTask(SessionManager session, ProgressBar progressUpdater,
                                   ITaskProcessor<JSONObject> postTaskProcessor, int size, String apiUrl, String activityId, ProgressDialog dialog, String eType, String eId) {

        super(session, progressUpdater, new HashMap<String, Object>());
        this.taskProcessor = postTaskProcessor;
        this.url = session.getApiUrl(apiUrl);
        sizeOfList = size;
        newsActivityId = activityId;
        this.eType = eType;
        this.eId = eId;
        Log.e("url of activites..", ".."+url);
        pDialog = dialog;
        pDialog.show();
    }

    @Override
    protected JSONObject doInBackground(String... params) {


        session.addSessionParams(httpParams);
//        session.addContentSrcParams(httpParams);
        httpParams.put("userRole", session.getOrgMemberInfo().profile);
        httpParams.put("orgId", session.getOrgMemberInfo().orgId);
        httpParams.put("eType",eType);
        httpParams.put("userId",session.getOrgMemberInfo().userId);
        httpParams.put("eId",eId);
        httpParams.put("beforeNewsActivityId", newsActivityId);
//        httpParams.put("year","2007");
        httpParams.put("needClustered","true");
        httpParams.put("start",sizeOfList);
        httpParams.put("size", "10");
        session.addContentSrcParams(httpParams);

        JSONObject res = null;
        try {
            res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);

            Log.d("getActivities","getActivities"+res);
            Log.d("FetchinggetActivities",".."+httpParams);


        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            error = checkForError(res);
        }

        if (isCancelled()) {
            Log.i(TAG, "task cancled by user");
            return null;
        }

        if (taskProcessor != null) {
            taskProcessor.onTaskStart(res);
        }

        return res;
    }

    @Override
    public void clear() {

        super.clear();

    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);

        if (taskProcessor != null) {
            taskProcessor.onTaskPostExecute(!error && !isCancelled() && result != null, result);
        }
        pDialog.dismiss();
        clear();
    }

    @Override
    protected void onCancelled(JSONObject result) {

        super.onCancelled(result);
        pDialog.dismiss();
        clear();
    }

}
