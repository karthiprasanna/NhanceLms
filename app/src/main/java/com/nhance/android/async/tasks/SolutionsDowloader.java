package com.nhance.android.async.tasks;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.enums.EntityType;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.QuestionImageUtil;
import com.nhance.android.utils.VedantuWebUtils;

public class SolutionsDowloader extends AbstractLibraryLoader {

	private static final String TAG = "SolutionsDowloader";
	List<String> qIds;
	protected ITaskProcessor<JSONObject> taskProcessor;

	public SolutionsDowloader(SessionManager session,
			ProgressBar progressUpdater, List<String> qIds,
			ITaskProcessor<JSONObject> taskProcessor) {

		super(session, progressUpdater, null);
		this.url = session.getApiUrl("getQuestionsSolutions");
		this.qIds = qIds;
		this.taskProcessor = taskProcessor;
	}

	@Override
	protected JSONObject doInBackground(String... params) {

		JSONObject jsonRes = null;
		session.addSessionParams(httpParams);
		httpParams.put("verifiedOnly", String.valueOf(true));
		SessionManager.addListParams(qIds, ConstantGlobal.QIDS, httpParams);
		try {
			jsonRes = VedantuWebUtils.getJSONData(url,
					VedantuWebUtils.POST_REQ, httpParams);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(), e);
		}

		if (VedantuWebUtils.checkErrorMsg(jsonRes, url)) {
			return null;
		}
		if (taskProcessor != null) {
			taskProcessor.onTaskStart(jsonRes);
		}
		jsonRes = JSONUtils.getJSONObject(jsonRes, VedantuWebUtils.KEY_RESULT);
		jsonRes = JSONUtils.getJSONObject(jsonRes, "solutions");
		@SuppressWarnings("unchecked")
		Iterator<String> it = jsonRes.keys();
		while (it.hasNext()) {
			String key = it.next();
			try {
				saveQuestionSolution(JSONUtils.getJSONArray(jsonRes, key)
						.getJSONObject(0));
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		return jsonRes;
	}

	@Override
	protected void onPostExecute(JSONObject result) {

		super.onPostExecute(result);
		if (taskProcessor != null) {
			taskProcessor.onTaskPostExecute(!isCancelled() && result != null,
					result);
		}
	}

	private void saveQuestionSolution(JSONObject solJSON) throws JSONException {

		if (solJSON == null) {
			return;
		}
		ContentDataManager cDataManager = new ContentDataManager(
				session.getContext());
		Set<String> urls = new HashSet<String>();
		solJSON.put(ConstantGlobal.CONTENT, QuestionImageUtil
				.removeImageSrcUrl(
						JSONUtils.getString(solJSON, ConstantGlobal.CONTENT),
						urls, EntityType.QUESTION));
		String qId = JSONUtils.getString(solJSON, ConstantGlobal.QID);
		ContentValues values = new ContentValues();
		values.put(ConstantGlobal.SOLUTION, solJSON.toString());
		cDataManager.updateAnswer(qId, values);
		downloadImages(urls,
				ContentDataManager.getContentDir(EntityType.QUESTION.name()));
	}

	@Override
	protected boolean downloadImage(String imageUrl, String toDir, String type) {
		return false;
	}

}
