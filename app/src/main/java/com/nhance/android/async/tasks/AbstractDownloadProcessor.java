package com.nhance.android.async.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.downloader.DownloadInfo.DownloadState;
import com.nhance.android.enums.EncryptionLevel;
import com.nhance.android.enums.EntityType;
import com.nhance.android.factory.CMDSUrlFactory;
import com.nhance.android.managers.FileManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.content.infos.DocumentExtendedInfo;
import com.nhance.android.pojos.content.infos.FileExtendedInfo;
import com.nhance.android.pojos.content.infos.IContentInfo;
import com.nhance.android.pojos.content.infos.VideoExtendedInfo;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public abstract class AbstractDownloadProcessor implements ITaskProcessor<File> {

    private final String              TAG                              = "AbstractDownloadProcessor";
    protected Context                 context;
    protected LibraryContentRes       content;
    public SessionManager             sessionManager;
    public String                     errorMsg;

    private final static Set<Integer> activeFetchDownloanloadUrlForIds = new HashSet<Integer>();

    public AbstractDownloadProcessor(Context context, LibraryContentRes content) {

        super();
        this.context = context;
        this.content = content;
        this.sessionManager = SessionManager.getInstance(context);
    }

    public boolean startDownload(final boolean ignoreDownloadable) {

        Log.v(TAG, "starting download");
        if (content.downloaded) {
            errorMsg = "file is already downloaded";
            Log.d(TAG, errorMsg);
            return false;
        }

        if (!content.downloadable && !ignoreDownloadable) {
            Log.d(TAG, "downloading not allowed for content downloadable:" + content.downloadable);
            errorMsg = "downloading not allowed for content";
            return false;
        }

        if (!SessionManager.isOnline()) {
            errorMsg = "Internet connection is required to download or view the content";
            Log.d(TAG, errorMsg);
            return false;
        }

        EntityType eType = EntityType.valueOfKey(content.type);
        if (eType == EntityType.TEST || eType == EntityType.ASSIGNMENT) {
            setUpDownloaderTask(true, null);
            return true;
        }

        try {
            AbstractVedantuAsynTask<String, Void, String> task = new AbstractVedantuAsynTask<String, Void, String>() {

                DownloadHistory dHistory;

                @Override
                protected String doInBackground(String... params) {

                    if (activeFetchDownloanloadUrlForIds.contains(Integer.valueOf(content._id))) {
                        errorMsg = "a download already in progress/completed ";
                        Log.d(TAG, errorMsg);
                        return null;
                    }
                    DownloadHistoryManager dHManager = new DownloadHistoryManager(context);
                    dHistory = dHManager.getDownloadHistory(content._id);
                    if (dHistory != null && dHistory.status != DownloadState.STOPPED.toInt()
                            && new File(dHistory.file).exists()) {

                        errorMsg = "a download already in progress/completed status: "
                                + DownloadState.fromInt(dHistory.status);
                        Log.d(TAG, errorMsg);
                        return null;
                    }
                    activeFetchDownloanloadUrlForIds.add(Integer.valueOf(content._id));
                    String url = getDownloadUrl(true && !ignoreDownloadable);
                    activeFetchDownloanloadUrlForIds.remove(Integer.valueOf(content._id));
                    return url;
                }

                @Override
                protected void onPostExecute(String result) {

                    super.onPostExecute(result);

                    if (dHistory != null && dHistory.status == DownloadState.FINISHED.toInt()
                            && (new File(dHistory.file).exists())) {
                        File file = new File(dHistory.file);
                        String newPath = ContentDataManager.getContentDir(content.type)
                                + File.separator + file.getName();
                        dHistory.file = newPath;
                        file.renameTo(new File(newPath));
                        markAlreadyDownload(dHistory);
                        errorMsg = null;
                    } else {
                        setUpDownloaderTask(false, result);
                    }
                }
            };
            task.executeTask(false);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }
        return true;
    }

    private String getDownloadUrl(boolean fromCmds) {

        String url = null;
        Log.v(TAG, "getting Download URL fromCMDS: " + fromCmds);
        if (fromCmds) {
            String webUrl = CMDSUrlFactory.INSTANCE.getCMDSUrl(
                    sessionManager.getSessionStringValue(ConstantGlobal.CMDS_URL),
                    "getContentDownloadLink");
            Map<String, Object> httpParams = new HashMap<String, Object>();
            httpParams.put(ConstantGlobal.ENTITY_ID, content.id);
            httpParams.put(ConstantGlobal.ENTITY_TYPE, content.type);
            httpParams.put(ConstantGlobal.LINK_ID, content.linkId);
            sessionManager.addSessionParams(httpParams);
            Log.d(TAG, "fetching download url from : " + webUrl);
            try {
                JSONObject data = VedantuWebUtils.getJSONData(webUrl, VedantuWebUtils.GET_REQ,
                        httpParams);
                if (!TextUtils.isEmpty(JSONUtils.getString(data, "errorCode"))) {
                    errorMsg = JSONUtils.getString(data, "errorMessage");
                    return null;
                }
                JSONObject resultJSON = JSONUtils.getJSONObject(data, "result");
                if (resultJSON != null) {
                    url = resultJSON.isNull("url") ? null : resultJSON.getString("url");
                    String passphrase = JSONUtils.getString(resultJSON, ConstantGlobal.PASSPHRASE);
                    if (!TextUtils.isEmpty(passphrase)) {
                        content.passphrase = passphrase;
                        ContentValues values = new ContentValues();
                        values.put(ConstantGlobal.PASSPHRASE, content.passphrase);
                        new ContentLinkDataManager(context).updateContentLink(content.linkId,
                                values);
                    }

                    String encLevel = JSONUtils.getString(resultJSON, ConstantGlobal.ENC_LEVEL);
                    if (!TextUtils.isEmpty(encLevel)) {
                        content.encLevel = encLevel;
                        ContentValues values = new ContentValues();
                        values.put(ConstantGlobal.ENC_LEVEL, content.encLevel);
                        new ContentLinkDataManager(context).updateContentLink(content.linkId,
                                values);
                    }
                }

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            IContentInfo iInfo = content.toContentExtendedInfo();
            if (iInfo == null) {
                return null;
            }
            EntityType eType = content.__getEntityType();
            if (eType == EntityType.VIDEO) {
                VideoExtendedInfo videoInfo = (VideoExtendedInfo) iInfo;
                url = videoInfo.url;
            } else if (eType == EntityType.DOCUMENT) {
                DocumentExtendedInfo docInfo = (DocumentExtendedInfo) iInfo;
                url = docInfo.url;
            } else if (eType == EntityType.FILE) {
                FileExtendedInfo fInfo = (FileExtendedInfo) iInfo;
                url = fInfo.url;
            }
            // if the content is encrypted then download URL should be of encrypted content only
            if (EncryptionLevel.valueOfKey(content.encLevel) != EncryptionLevel.NA) {
                url = FileManager.getEncryptedFileName(url);
            }
        }

        Log.d(TAG, "returning download url for contentId : " + content._id + ",  url: " + url);
        return url;
    }

    public boolean removeFromLocalDisk() {

        Log.d(TAG, "removing content from local disk contentId: " + content._id);
        if (!content.downloaded) {
            Log.d(TAG, "content[ " + content._id + "] is not downloaded");
            return false;
        }

        DownloadHistoryManager dHManager = new DownloadHistoryManager(context);
        DownloadHistory dHistory = dHManager.getDownloadHistory(content._id);
        if (dHistory == null) {
            Log.e(TAG, "no download history found for content: " + content._id);
            ContentValues updatedValues = new ContentValues();
            updatedValues.put(ConstantGlobal.DOWNLOADED, 0);
            new ContentDataManager(context).updateContent(content._id, updatedValues);
            return false;
        }

        ContentValues updatedValues = new ContentValues();
        updatedValues.put(ConstantGlobal.DOWNLOADED, 0);
        new ContentDataManager(context).updateContent(content._id, updatedValues);

        File file = new File(dHistory.file);
        dHManager.deleteDownloadHistory(dHistory._id);

        Log.d(TAG, "removed content[ " + content._id + "] download history");

        boolean removed = file.delete();

        Log.d(TAG, "removed content[ " + content._id + "] file[" + file.getAbsolutePath()
                + "] from local disk : " + removed);
        // check if any decrypted version of this file present in temp folder
        File tempFile = new File(ContentDataManager.getTempContentDir(), file.getName());
        Log.d(TAG, "checking if any temp file exist : " + tempFile.getAbsolutePath());
        if (tempFile.exists()) {
            Log.d(TAG,
                    "remving temp file[" + tempFile.getAbsolutePath() + "] : " + tempFile.delete());
        }

        return true;
    }

    @Override
    public void onTaskPostExecute(boolean success, File result) {

        if (success) {

            if (result == null || !result.exists()) {
                Toast.makeText(context, "can not download file", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(TAG, "content successfully downloaded");
            Toast.makeText(context, "content successfully downloaded", Toast.LENGTH_SHORT).show();

        } else {
            Log.d(TAG, "content can not be downloaded");
            Toast.makeText(context, "content can not be downloaded", Toast.LENGTH_SHORT).show();
        }

    }

    protected abstract void setUpDownloaderTask(boolean skipUrlCheck, String downloadUrl);

    /**
     * @param downloadHistory
     *            if the file is already downloaded then do not download this again, just mark the
     *            content downloaded
     */
    protected void markAlreadyDownload(DownloadHistory downloadHistory) {

        ContentValues updatedValues = new ContentValues();
        updatedValues.put(ConstantGlobal.DOWNLOADED, 1);
        new ContentDataManager(context).updateContent(content._id, updatedValues);
        new DownloadHistoryManager(context).updateDownloadHistory(downloadHistory);
    }
}
