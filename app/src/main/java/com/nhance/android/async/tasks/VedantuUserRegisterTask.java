package com.nhance.android.async.tasks;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.factory.CMDSUrlFactory;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;

public class VedantuUserRegisterTask extends AbstractVedantuJSONAsyncTask {

    private static final String        TAG = "VedantuCommunicatorTask";
    private ITaskProcessor<JSONObject> taskProcessor;
    ReqAction                          reqAction;

    public VedantuUserRegisterTask(SessionManager session, ProgressBar progressUpdater,
            ReqAction reqAction, ITaskProcessor<JSONObject> taskProcessor) {

        super(session, progressUpdater);
        this.taskProcessor = taskProcessor;
        this.reqAction = reqAction;
    }

    /**
     * params[0] will be the cmds url if (reqAction != ReqAction.GET_SECTION_BY_ACCESS_CODE)
     */
    @Override
    protected JSONObject doInBackground(String... params) {

        JSONObject res = null;
        String separator = SQLDBUtil.SEPARATOR;
        String cmdsUrl = params != null && params.length > 0 ? params[0] : null;
        if (reqAction == ReqAction.GET_SECTION_BY_ACCESS_CODE) {
            String accessCode = (String) httpParams.get(ConstantGlobal.ACCESS_CODE);

            String envMode = accessCode.contains(separator) ? StringUtils.substringBefore(
                    accessCode, separator) : null;
            String aCode = accessCode.contains(separator) ? StringUtils.substringAfterLast(
                    accessCode, separator) : accessCode;
            if (envMode != null && envMode.equalsIgnoreCase("ip")) {
                cmdsUrl = StringUtils.substringBetween(accessCode, separator);
            } else {
                cmdsUrl = SessionManager.getCMDSUrlFromEnvMode(envMode, session.getContext());
            }
            httpParams.put(ConstantGlobal.ACCESS_CODE, aCode);
        }
        this.url = reqAction.getUrl(cmdsUrl);

        try {
            res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            error = true;
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
        taskProcessor = null;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);

        if (error) {
            String msg = errorMsg == null && errorCode.equals("INVALID_ACCESS_CODE") ? session
                    .getContext().getString(R.string.error_access_code_invalid) : (errorCode
                    .equals("USER_ALREADY_EXISTS") ? session.getContext().getString(
                    R.string.error_empty_already_registred) : errorMsg);
            Toast.makeText(session.getContext(), msg, Toast.LENGTH_LONG).show();
        }

        if (taskProcessor != null) {
            boolean isSuccess = !error && !isCancelled() && result != null;
            if (isSuccess && reqAction == ReqAction.GET_SECTION_BY_ACCESS_CODE) {
                try {
                    result.put(ConstantGlobal.ACCESS_CODE,
                            httpParams.get(ConstantGlobal.ACCESS_CODE));
                } catch (Exception e) {}
            }
            if (result == null) {
                Toast.makeText(session.getContext(), R.string.error_unknown, Toast.LENGTH_LONG)
                        .show();
            }
            taskProcessor.onTaskPostExecute(isSuccess, result);
        }
        clear();
    }

    public static enum ReqAction {
        GET_SECTION_BY_ACCESS_CODE {

            @Override
            public String getUrl(String cmdsUrl) {

                return CMDSUrlFactory.INSTANCE.getCMDSUrl(cmdsUrl, "getSectionByAccessCode");
            }
        },
        ADD_MEMBER_WITH_ACCESS_CODE {

            @Override
            public String getUrl(String cmdsUrl) {

                return CMDSUrlFactory.INSTANCE.getCMDSUrl(cmdsUrl, "addMemberWithAccessCode");
            }
        };

        public abstract String getUrl(String cmdsUrl);
    }
}
