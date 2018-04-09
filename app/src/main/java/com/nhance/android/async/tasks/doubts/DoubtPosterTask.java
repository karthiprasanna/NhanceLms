package com.nhance.android.async.tasks.doubts;

import java.io.IOException;
import java.util.List;

import org.json.JSONObject;

import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;

public class DoubtPosterTask extends AbstractVedantuJSONAsyncTask {

    private final String               TAG = "DoubtPosterTask";
    private String                     name/* title */;
    private String                     content;
    private List<String>               brdIds;
    private List<String>               tags;
    private ITaskProcessor<JSONObject> taskProcessor;

    public DoubtPosterTask(SessionManager session, ProgressBar progressUpdater,
            String name/* title */, String content, List<String> brdIds, List<String> tags,
            ITaskProcessor<JSONObject> taskProcessor) {

        super(session, progressUpdater);
        this.url = session.getApiUrl("addDiscussion");
        this.name = name;
        this.content = content;
        this.brdIds = brdIds;
        this.tags = tags;
        this.taskProcessor = taskProcessor;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        session.addSessionParams(httpParams);
        httpParams.put(ConstantGlobal.NAME, name);
        httpParams.put(ConstantGlobal.CONTENT, content);
        httpParams.put("scope", "ORG");

        session.addContentSrcParams(httpParams);

        SessionManager.addListParams(brdIds, ConstantGlobal.BRD_IDS, httpParams);
        SessionManager.addListParams(tags, ConstantGlobal.TAGS, httpParams);

        JSONObject res = null;

        try {
            res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            error = checkForError(res);
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

        if (tags != null) {
            tags.clear();
            tags = null;
        }
        taskProcessor = null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        if (taskProcessor != null) {
            taskProcessor.onTaskPostExecute(!error && !isCancelled() && result != null, result);
        }
        clear();
    }
}
