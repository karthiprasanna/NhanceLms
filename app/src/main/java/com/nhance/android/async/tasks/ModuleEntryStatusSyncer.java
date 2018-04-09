package com.nhance.android.async.tasks;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ModuleStatusDataManager;
import com.nhance.android.db.models.UserModuleEntryStatus;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.SrcEntity;
import com.nhance.android.pojos.VedantuDBResult;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.utils.VedantuWebUtils;

public class ModuleEntryStatusSyncer extends AbstractVedantuJSONAsyncTask {

    private final String TAG = "ModuleEntryStatusSyncer";
    public int syncCount;

    public String moduleId;
    private ITaskProcessor<JSONObject> postExecuteProcessor;


    public ModuleEntryStatusSyncer(SessionManager session, ProgressBar progressUpdater,
                                   String moduleId, ITaskProcessor<JSONObject> taskProcessor) {

        super(session, progressUpdater, new HashMap<String, Object>());
        this.url = session.getApiUrl("moduleEntryStatusSyncer");
        this.moduleId = moduleId;

        this.postExecuteProcessor = taskProcessor;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        if (params != null) {
            SessionManager.addListParams(Arrays.asList(params), ConstantGlobal.IDS, httpParams);
        }
        if (moduleId == null) {
            ContentDataManager contentDataManager = new ContentDataManager(session.getContext());
            VedantuDBResult<LibraryContentRes> result = contentDataManager.getLibraryContents(session.getSessionStringValue(ConstantGlobal.USER_ID), session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), null, null, "MODULE", null, null, -1, null, null, SQLDBUtil.NO_START, SQLDBUtil.NO_START, null, null, null);
            for (LibraryContentRes entity : result.entities) {
                httpParams.clear();
                syncModule(entity.id);
            }
        } else {
            syncModule(moduleId);
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        if (postExecuteProcessor != null) {
            postExecuteProcessor.onTaskPostExecute(!isCancelled() && result != null, result);
        }
    }

    private void syncModule(String moduleId) {

        ModuleStatusDataManager moduleStatusDataManager = new ModuleStatusDataManager(
                session.getContext());

        List<UserModuleEntryStatus> usersModuleEntryStatus = moduleStatusDataManager
                .getModuleEntriesStatus(session.getSessionStringValue(ConstantGlobal.USER_ID),
                        session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), moduleId);

        httpParams.put(ConstantGlobal.MODULE_ID, moduleId);

        if (usersModuleEntryStatus != null) {
            for (int i = 0; i < usersModuleEntryStatus.size(); i++) {
                httpParams
                        .put("entities[" + i + "].id", usersModuleEntryStatus.get(i).entityId);
                httpParams.put("entities[" + i + "].type",
                        usersModuleEntryStatus.get(i).entityType);
            }
            session.addSessionParams(httpParams);
            JSONObject res = null;
            try {
                res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                error = checkForError(res);
                syncModuleEntry(res, moduleId);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                error = true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private JSONObject syncModuleEntry(JSONObject res, String moduleId) throws Exception {

        res = JSONUtils.getJSONObject(res, VedantuWebUtils.KEY_RESULT);

        @SuppressWarnings("unchecked")
        List<SrcEntity> entities = (List<SrcEntity>) JSONUtils.getJSONAwareCollection(
                SrcEntity.class, res, "entities");

        ModuleStatusDataManager moduleStatusDataManager = new ModuleStatusDataManager(
                session.getContext());
        moduleStatusDataManager.syncModuleEntryStatus(
                session.getSessionStringValue(ConstantGlobal.USER_ID),
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), moduleId, entities, true);
        return res;
    }
}
