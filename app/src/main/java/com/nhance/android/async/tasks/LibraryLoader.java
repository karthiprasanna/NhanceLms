package com.nhance.android.async.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.factory.CMDSUrlFactory;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.SrcEntity;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class LibraryLoader extends AbstractLibraryLoader {

    private final String            TAG                        = "LibraryLoader";
    private static final int        REQ_SIZE                   = 50;

    private ITaskProcessor<Integer> postExecuteProcessor;

    int                             overallHits;
    int                             totalHits;

    private String                  targetId;
    private String                  targetType;
    ProgressDialog                  progressDialog;

    // 1st fetch on size=1 content as it will tell you the total no of content in the library so
    // that we can show the progress bar

    Set<SrcEntity>                  subLibrary                 = new HashSet<SrcEntity>();
    boolean                         loadingSubLibs             = false;
    boolean                         isFirstReqOfSubLibsContent = false;

    public LibraryLoader(SessionManager session, ProgressBar progressContainer,
            Map<String, Object> httpParams, ITaskProcessor<Integer> postProcessor, String targetId,
            String targetType) {

        super(session, progressContainer, httpParams);
        contentLinkManager = new ContentLinkDataManager(session.getContext());
        this.postExecuteProcessor = postProcessor;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    boolean isFirstReq = true;
    String  errorMsg   = null;

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        if (session == null) {
            Log.e(TAG, "no session manager found ");
            return null;
        }
        if (isFirstReq && !SessionManager.isOnline()) {
            errorMsg = "internet connection is require to perform this operation";
            Log.e(TAG, errorMsg);
            error = true;
            return null;
        }

        if (isCancelled()) {
            errorMsg = "task cancled by user";
            Log.e(TAG, errorMsg);
            return null;
        }
        if (isFirstReq && session.getLatestLibContentTime(targetId, targetType) > 0) {
            updateDeletedLinks(0, REQ_SIZE, params);
        }

        httpParams.put("target.id", targetId);
        httpParams.put("target.type", targetType);

        if (!loadingSubLibs || !isFirstReqOfSubLibsContent) {
            httpParams.put("addedAfter", session.getLatestLibContentTime(targetId, targetType));
        }

        if (isFirstReqOfSubLibsContent) {
            httpParams.put("addedAfter", 0);
            isFirstReqOfSubLibsContent = false;
        }

        httpParams.put("start", 0);
        httpParams.put("size", REQ_SIZE);
        httpParams.put("orderBy", ConstantGlobal.LAST_UPDATED);
        httpParams.put("sortOrder", "ASC");
        String getLinkUrl = CMDSUrlFactory.INSTANCE.getCMDSUrl(params[0], "getContentLinks");
        JSONObject json = null;
        try {
            json = VedantuWebUtils.getJSONData(getLinkUrl, VedantuWebUtils.GET_REQ, httpParams);

            Log.d("jsonFetchContentLinks","jsonFetchContentLinks"+json);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        Log.e("FetchContentLinks","FetchContentLinks"+httpParams);

        error = VedantuWebUtils.checkErrorMsg(json, getLinkUrl);
        if (!error) {
            json = JSONUtils.getJSONObject(json, VedantuWebUtils.KEY_RESULT);
            totalHits = JSONUtils.getInt(json, "totalHits");
            if (overallHits <= 0) {
                overallHits = totalHits;
            }
            Log.d(TAG, "successfully received library data");

            updateLibraryDataAndReturnSubLibraryList(json, subLibrary);
            this.subLibrary.addAll(subLibrary);
            isFirstReq = false;
            session.updateLibraryLatestContentTime(JSONUtils.getLong(json, "latestContent"),
                    targetId, targetType);
            long serverTime = JSONUtils.getLong(json, "serverTime");
            session.updateContentLastSyncTime(serverTime, targetType, targetId);
            session.updateContentLastSyncTime(serverTime, "OVERALL", "OVERALL");
            publishProgress(Integer.valueOf(totalContentFetched));
            if (totalHits > 0) {
                doInBackground(params);
            }

            if (!loadingSubLibs) {
                loadingSubLibs = true;
                List<SrcEntity> subLibrary = new ArrayList<SrcEntity>(this.subLibrary);
                this.subLibrary.clear();
                // now we will be fetching all the subLibraryContent
                for (SrcEntity libTarget : subLibrary) {
                    isFirstReqOfSubLibsContent = true;
                    targetId = libTarget.id;
                    targetType = libTarget.type.name();
                    doInBackground(params);
                }
            }
        }
        return json;
    }

    private void updateLibraryDataAndReturnSubLibraryList(JSONObject json, Set<SrcEntity> subLibrary) {
        JSONArray jArray = JSONUtils.getJSONArray(json, "list");
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject linkJSON = null;
            try {
                linkJSON = jArray.getJSONObject(i);
                updateLibraryContentLinkData(linkJSON, SyncType.ONLINE, subLibrary, loadingSubLibs);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

    }

    private void updateDeletedLinks(int start, int size, String... params) {

        httpParams.put("start", start);
        httpParams.put("size", size);
        httpParams.put("addedAfter", session.getLatestLibContentTime(targetId, targetType));
        String getRemoveLinkUrl = CMDSUrlFactory.INSTANCE.getCMDSUrl(params[0],
                "getRemovedContentLinks");
        JSONObject json = null;
        try {
            json = VedantuWebUtils.getJSONData(getRemoveLinkUrl, VedantuWebUtils.GET_REQ,
                    httpParams);

            Log.d("FetchingRemovedContentLinks","FetchingRemovedContentLinks"+httpParams);
            Log.d("jsonFetchingRemovedContentLinks","FetchingRemovedContentLinks"+json);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        boolean error = VedantuWebUtils.checkErrorMsg(json, getRemoveLinkUrl);
        if (error) {
            Log.e(TAG, "error on retreving removeContentLinks data from server, httpParams: "
                    + httpParams);
            return;
        }

        int totalHits = JSONUtils.getInt(json, "totalHits");
        start += size;
        size = REQ_SIZE;

        json = JSONUtils.getJSONObject(json, VedantuWebUtils.KEY_RESULT);

        JSONArray jArray = JSONUtils.getJSONArray(json, "list");
        for (int i = 0; i < jArray.length(); i++) {
            JSONObject linkJSON = null;
            try {
                linkJSON = jArray.getJSONObject(i);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
            JSONObject contentJSON = JSONUtils.getJSONObject(linkJSON, ConstantGlobal.CONTENT);
            if (contentJSON == null || contentJSON.length() < 1) {
                Log.e(TAG, "no content found linkJSON: " + linkJSON);
                continue;
            }

            String id = JSONUtils.getString(contentJSON, ConstantGlobal.ID);
            String type = JSONUtils.getString(contentJSON, ConstantGlobal.TYPE);

            synchronized ((id + type + session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID))
                    .intern()) {

                // 1st remove the contentLink and then remove the content to local database

                try {
                    boolean deletedContentLink = updateContentLink(linkJSON, id, type, true, null);
                    Log.d(TAG, "saved contentLink : " + deletedContentLink);
                } catch (Exception e1) {
                    Log.e(TAG, e1.getMessage());
                }
                JSONObject target = JSONUtils.getJSONObject(linkJSON, "target");
                String targetId = JSONUtils.getString(target, ConstantGlobal.ID);
                String targetType = JSONUtils.getString(target, ConstantGlobal.TYPE);
                updateContentAndContentLink(id, type, targetId, targetType);
            }
        }
        if (start < totalHits) {
            updateDeletedLinks(start, size, params);
        }

    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        super.onProgressUpdate(values);
        if (progressUpdater != null && values != null && values[0] != null) {
            progressUpdater.setMax(overallHits);
            progressUpdater.setProgress(values[0].intValue());
        }
        if (progressDialog != null && values != null && values[0] != null) {
            progressDialog.setMax(overallHits);
            progressDialog.setProgress(values[0].intValue());
        }
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);

        if (error) {
            Toast.makeText(contentManager.getContext(),
                    errorMsg != null ? errorMsg : "error while receiving data from server",
                    Toast.LENGTH_SHORT).show();
        }
        if (postExecuteProcessor != null) {
            postExecuteProcessor.onTaskPostExecute(!error, Integer.valueOf(totalContentFetched));
        }
    }

    @Override
    protected boolean downloadImage(String imageUrl, String toDir, String type) {

        return false;
    }

    @Override
    public void clear() {

        super.clear();
        postExecuteProcessor = null;

    }

}
