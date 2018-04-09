package com.nhance.android.async.tasks.doubts;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;

public class DoubtListRetreiverTask extends AbstractVedantuJSONAsyncTask {

    private static final String        TAG          = "DoubtRetreiverTask";
    private ITaskProcessor<JSONObject> taskProcessor;
    private String                     orderBy;
    private String                     sortOrder;
    private List<String>               brdIds;
    private String                     query;
    private String                     resultType;
    private boolean                    facet;
    private int                        start;
    private boolean                    reloadTopicFacets;
    private int                        size;
    private boolean					   allBrds = false;


    public DoubtListRetreiverTask(SessionManager session, ProgressBar progressUpdater,
            ITaskProcessor<JSONObject> postTaskProcessor, String query, List<String> brdIds,
            String orderBy, String sortOrder, String resultType, boolean facet,
            boolean reloadTopicFacets, int start, int size, boolean allBrds) {

        super(session, progressUpdater, new HashMap<String, Object>());
        this.taskProcessor = postTaskProcessor;
        this.url = session.getApiUrl("getDiscussions");
        this.query = query;
        this.brdIds = brdIds;
        this.orderBy = orderBy;
        this.sortOrder = sortOrder;
        this.resultType = resultType;
        this.facet = facet;        
        this.reloadTopicFacets = reloadTopicFacets;
        this.start=start;
        this.size = size;
        this.allBrds = allBrds;

    }

    @Override
    protected JSONObject doInBackground(String... params) {

        session.addSessionParams(httpParams);
        session.addContentSrcParams(httpParams);
        httpParams.put("orderBy", orderBy);
        httpParams.put("sortOrder", sortOrder);
        httpParams.put("query", query);
        if (resultType == null) {
            httpParams.put("resultType", "ALL");
        } else {
            httpParams.put("resultType", resultType);
        }

        httpParams.put("facet", String.valueOf(facet));
        httpParams.put("start", start);
        httpParams.put("size", size);
        httpParams.put("allBrds", allBrds);
        SessionManager.addListParams(brdIds, ConstantGlobal.BRD_IDS, httpParams);
        JSONObject res = null;
        try {
            res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.GET_REQ, httpParams);

            Log.d("getDiscussions","getDiscussions"+res);
            Log.d("FetchinggetDiscussions","FetchinggetDiscussions"+httpParams);


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
        if (brdIds != null) {
            brdIds.clear();
            brdIds = null;
        }
        taskProcessor = null;
        orderBy = null;
        sortOrder = null;
        query = null;
        resultType = null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        try {
            if (result != null) {
                result.put("reloadTopicFacets", reloadTopicFacets);
                result.put("start", start);
            }
        } catch (Exception e) {
            Log.d(TAG, "Error in reload facets setting");
        }
        if (taskProcessor != null) {
            taskProcessor.onTaskPostExecute(!error && !isCancelled() && result != null, result);
        }

        clear();
    }

    @Override
    protected void onCancelled(JSONObject result) {

        super.onCancelled(result);
        clear();
    }

}
