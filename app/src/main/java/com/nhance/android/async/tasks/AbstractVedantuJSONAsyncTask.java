package com.nhance.android.async.tasks;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.text.TextUtils;
import android.widget.ProgressBar;

import com.nhance.android.interfaces.IClearable;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;

public abstract class AbstractVedantuJSONAsyncTask extends
        AbstractVedantuAsynTask<String, Integer, JSONObject> implements IClearable {

    protected Map<String, Object> httpParams;
    public boolean                error;
    public String                 errorMsg;
    public String                 errorCode;
    protected ProgressBar         progressUpdater;
    protected SessionManager      session;
    protected String              url;

    public AbstractVedantuJSONAsyncTask(SessionManager session, ProgressBar progressUpdater) {

        this(session, progressUpdater, null);
    }

    public AbstractVedantuJSONAsyncTask(SessionManager session, ProgressBar progressUpdater,
            Map<String, Object> httpParams) {

        super();
        this.session = session;
        this.httpParams = httpParams;
        if (this.httpParams == null) {
            this.httpParams = new HashMap<String, Object>();
        }
        this.progressUpdater = progressUpdater;

    }

    @Override
    public void clear() {

        if (httpParams != null) {
            httpParams.clear();
            httpParams = null;
        }
        url = null;
        session = null;

    }

    public void addExtraParams(String key, Object value) {

        httpParams.put(key, value);
    }

    /**
     * this function may be used for adding additional params to the http request
     * 
     * @param extraParams
     */
    public void addExtraParams(Map<String, Object> extraParams) {

        httpParams.putAll(extraParams);
    }

    public boolean checkForError(JSONObject res) {

        boolean error = res == null;
        if (error) {
            errorCode = "CONNECTION_REFUSED";
            errorMsg = session.getContext().getString(R.string.error_connecting_to_internet);
        }
        if (!error
                && (errorCode = JSONUtils.getString(res, VedantuWebUtils.KEY_ERROR_CODE))
                        .equals("SERVICE_ERROR")) {
            errorCode = session.getContext().getString(R.string.error_unknown);
        }

        return !error
                && !TextUtils.isEmpty(errorCode = JSONUtils.getString(res,
                        VedantuWebUtils.KEY_ERROR_CODE));
    }

}
