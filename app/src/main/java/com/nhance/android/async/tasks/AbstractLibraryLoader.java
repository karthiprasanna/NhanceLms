package com.nhance.android.async.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.http.AndroidHttpClient;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.db.models.entity.Answer;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.ContentLink;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.downloader.DownloadInfo;
import com.nhance.android.downloader.DownloadInfo.DownloadState;
import com.nhance.android.enums.EntityType;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.SrcEntity;
import com.nhance.android.utils.IOUtils;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.QuestionImageUtil;
import com.nhance.android.utils.SQLDBUtil;

public abstract class AbstractLibraryLoader extends AbstractVedantuJSONAsyncTask {

    public enum SyncType {
        ONLINE, IMPORT;
    }

    private final String             TAG = "AbstractLibraryLoader";
    protected ContentDataManager     contentManager;
    protected BoardDataManager       boardManager;
    protected ContentLinkDataManager contentLinkManager;
    protected int                    totalContentFetched;

    public AbstractLibraryLoader(SessionManager session, ProgressBar progressUpdater,
            Map<String, Object> httpParams) {

        super(session, progressUpdater, httpParams);
        this.contentManager = new ContentDataManager(session.getContext());
        this.contentLinkManager = new ContentLinkDataManager(session.getContext());
        this.boardManager = new BoardDataManager(session.getContext());
    }

    protected String getAndSaveBoardTree(JSONObject contentJSON) {

        Log.d(TAG, "inside getAndSaveBoardTree");
        String brdIds = "";
        try {
            brdIds = TextUtils.join(SQLDBUtil.SEPARATOR,
                    saveBoards(contentJSON.getJSONArray("boardTree")));
            if (brdIds != null) {
                brdIds = SQLDBUtil.SEPARATOR + brdIds + SQLDBUtil.SEPARATOR;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return brdIds;
    }

    private Set<Integer> saveBoards(JSONArray boardTree) throws Exception {

        return saveBoards(boardTree, -1);
    }

    private Set<Integer> saveBoards(JSONArray boardTree, int parentId) throws Exception {

        Log.d(TAG, "inside saveBoards");
        Set<Integer> brdIds = new HashSet<Integer>();
        for (int i = 0; i < boardTree.length(); i++) {
            JSONObject bJSON = boardTree.getJSONObject(i);
            BoardModel boardModel = new BoardModel(
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), JSONUtils.getString(
                            bJSON, ConstantGlobal.NAME), JSONUtils.getString(bJSON,
                            ConstantGlobal.ID), parentId, JSONUtils.getString(bJSON,
                            ConstantGlobal.TYPE), JSONUtils.getString(bJSON, ConstantGlobal.CODE));
            synchronized ((boardModel.id + boardModel.type + boardModel.orgKeyId + parentId)
                    .intern()) {
                boardModel = boardManager.upsertBoard(boardModel);
                brdIds.add(boardModel._id);
                Set<Integer> children = saveBoards(JSONUtils.getJSONArray(bJSON, "children"),
                        boardModel._id);
                brdIds.addAll(children);
            }
        }
        Log.d(TAG, "returning board ids: " + brdIds);
        return brdIds;
    }

    protected String getTags(JSONObject contentJSON) {

        Log.d(TAG, "inside getTags");
        String tags = null;
        JSONArray tagsJSON = JSONUtils.getJSONArray(contentJSON, ConstantGlobal.TAGS);
        for (int t = 0; t < tagsJSON.length(); t++) {
            try {
                if (tags == null) {
                    tags = tagsJSON.getString(t);
                } else {

                    tags += SQLDBUtil.SEPARATOR + tagsJSON.getString(t);
                }
            } catch (JSONException e) {

            }
        }
        return tags;
    }

    protected void updateLibraryContentLinkData(JSONObject linkJSON, SyncType syncType,
            Set<SrcEntity> subLibrary, boolean loadingSubLib) {

        Log.d(TAG, "inside updateLibraryContentLinkData");
        JSONObject contentJSON = JSONUtils.getJSONObject(linkJSON, ConstantGlobal.CONTENT);
        if (contentJSON == null || contentJSON.length() < 1) {
            Log.e(TAG, "no content found linkJSON: " + linkJSON);
            return;
        }

        String id = JSONUtils.getString(contentJSON, ConstantGlobal.ID);
        String type = JSONUtils.getString(contentJSON, ConstantGlobal.TYPE);

        if (EntityType.MODULE.name().equals(type) && !loadingSubLib) {
            // this is a sublibrary
            subLibrary.add(new SrcEntity(EntityType.MODULE, id));
        } else if (EntityType.QUESTION.name().equals(type)) {
            Log.w(TAG,
                    "question will not be stored in content model, it will be stored in question model");
            try {
                updateQuestion(contentJSON, syncType);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
//            return;
        }
        String ownerId = JSONUtils.getString(contentJSON, ConstantGlobal.USER_ID);

        Content content = null;
        synchronized ((id + type + session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID)).intern()) {

            // 1st upsert the contentLink and then add the content to local database

            boolean downloadable = type.equalsIgnoreCase(EntityType.TEST.name())
                    || type.equalsIgnoreCase(EntityType.ASSIGNMENT.name()) ? true : JSONUtils
                    .getBoolean(linkJSON, ConstantGlobal.DOWNLOADABLE);
            if (type.equalsIgnoreCase(EntityType.TEST.name())
                    && JSONUtils.getString(contentJSON, "subType").equalsIgnoreCase("OFFLINE")) {
                downloadable = false;
            }

            try {
                linkJSON.put(ConstantGlobal.DOWNLOADABLE, downloadable);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }

            updateContentLink(linkJSON, id, type, false,
                    JSONUtils.getString(contentJSON, "subType"));

            content = contentManager.getContent(id, type,
                    session.getSessionStringValue(ConstantGlobal.USER_ID),
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
            String thumb = JSONUtils.getString(contentJSON, ConstantGlobal.THUMBNAIL);
            if (EntityType.QUESTION.name().equals(type)) {
                JSONObject user = JSONUtils.getJSONObject(contentJSON, "user");
              thumb = JSONUtils.getString(user, ConstantGlobal.THUMBNAIL);
            }



            if (syncType == SyncType.IMPORT) {
                downloadImage(thumb, ContentDataManager.getContentDir(ConstantGlobal.THUMB),
                        ConstantGlobal.THUMB);
            }

            if (content != null) {
                if (syncType == SyncType.IMPORT) {
                    if (content.type.equalsIgnoreCase("VIDEO")
                            && JSONUtils.getString(content.info, "linkType").equalsIgnoreCase(
                                    "ADDED")) {
                        content.downloaded = false;
                    } else if (type.equalsIgnoreCase(EntityType.TEST.name())
                            && JSONUtils.getString(contentJSON, "subType").equals("OFFLINE")) {
                        content.downloaded = false;
                    } else {
                        content.downloaded = true;
                    }
                }
                Log.d(TAG, "content [id:" + id + ",type:" + type + "] already present for userId: "
                        + session.getSessionStringValue(ConstantGlobal.USER_ID));
                updateContent(content, contentJSON, downloadable, linkJSON);
                importedContentFiles(content, syncType);
                if (!loadingSubLib) {
                    totalContentFetched++;
                }
                return;
            }

            JSONObject user = JSONUtils.getJSONObject(contentJSON, "user");
            String ownerName = user == null ? null : TextUtils.join(
                    " ",
                    new String[] { JSONUtils.getString(user, ConstantGlobal.FIRST_NAME),
                            JSONUtils.getString(user, ConstantGlobal.LAST_NAME) });

            String brdIds = getAndSaveBoardTree(contentJSON);

            String tags = getTags(contentJSON);

            try {
                if (!loadingSubLib) {
                    totalContentFetched++;
                }
                content = new Content(
                        String.valueOf(JSONUtils.getLong(contentJSON, ConstantGlobal.TIME_CREATED)),
                        session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                        String.valueOf(JSONUtils.getLong(contentJSON, ConstantGlobal.LAST_UPDATED)),
                        brdIds, tags, session.getSessionStringValue(ConstantGlobal.USER_ID),
                        ownerId, ownerName, id, type, JSONUtils.getString(contentJSON,
                        ConstantGlobal.NAME), JSONUtils.getString(contentJSON,
                        ConstantGlobal.DESC), new JSONObject(JSONUtils.getString(
                        contentJSON, ConstantGlobal.INFO)), JSONUtils.getString(
                        contentJSON, "subType"), thumb, JSONUtils.getString(contentJSON,
                        ConstantGlobal.FILE)) {
                    @Override
                    public boolean equalsIgnoreCase(String attempted) {
                        return false;
                    }
                };
                // TODO: add targetIds and targetNames
                if (syncType == SyncType.IMPORT) {
                    if (content.type.equalsIgnoreCase("VIDEO")
                            && JSONUtils.getString(content.info, "linkType").equalsIgnoreCase(
                                    "ADDED")) {
                        content.downloaded = false;
                    } else {
                        content.downloaded = true;
                    }
                }
                contentManager.insertContent(content);
                importedContentFiles(content, syncType);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    protected void updateContentAndContentLink(String entityId, String entityType, String targetId,
            String targetType) {

        Log.d(TAG, "inside updateContentAndContentLink");
        Content content = contentManager.getContent(entityId, entityType,
                session.getSessionStringValue(ConstantGlobal.USER_ID),
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID));

        if (content == null) {
            Log.d(TAG,
                    "content [id:" + entityId + ",type:" + entityType
                            + "] already deleted for userId: "
                            + session.getSessionStringValue(ConstantGlobal.USER_ID));
            return;
        }

        boolean removeContent = updateContentDownloadHistory(content, targetId, targetType);
        if (removeContent) {
            contentManager.removeContent(content._id);
        }
    }

    private void importedContentFiles(Content content, SyncType syncType) {

        Log.d(TAG, "inside importedContentFiles");
        if (syncType == SyncType.IMPORT) {
            if (content.type.equalsIgnoreCase("VIDEO")
                    && JSONUtils.getString(content.info, "linkType").equalsIgnoreCase("ADDED")) {
                content.downloaded = false;
            } else {
                content.downloaded = true;
            }

            if (content.downloaded) {
                String url = JSONUtils.getString(content.info, "url");
                String file = ContentDataManager.getContentDir(content.type) + File.separator
                        + StringUtils.substringAfterLast(url, File.separator);
                boolean imported = downloadImage(url,
                        ContentDataManager.getContentDir(content.type), content.type);
                Log.i(TAG, "imported url[" + url + "] :  " + imported);
                if (imported) {
                    DownloadHistoryManager dhManager = new DownloadHistoryManager(
                            session.getContext());

                    DownloadHistory dHistory = new DownloadHistory(content._id, new DownloadInfo(
                            content.orgKeyId, url, file, 1));
                    dHistory.downloaded = String.valueOf(1);
                    dHistory.total = String.valueOf(1);
                    dHistory.endTime = String.valueOf(System.currentTimeMillis());
                    dHistory.status = DownloadState.FINISHED.toInt();
                    try {
                        dhManager.upsertDownloadHistory(dHistory);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }
    }

    protected void updateContent(Content content, JSONObject contentJSON, boolean downloadable,
            JSONObject linkJSON) {

        Log.d(TAG, "....inside updateContent .........."+content.toString());
        JSONObject user = JSONUtils.getJSONObject(contentJSON, "user");
        String ownerName = user == null ? null : TextUtils.join(
                " ",
                new String[] { JSONUtils.getString(user, ConstantGlobal.FIRST_NAME),
                        JSONUtils.getString(user, ConstantGlobal.LAST_NAME) });

        String thumb = JSONUtils.getString(contentJSON, ConstantGlobal.THUMBNAIL);
        String type = JSONUtils.getString(contentJSON, ConstantGlobal.TYPE);
        if (EntityType.QUESTION.name().equals(type)) {
            thumb = JSONUtils.getString(user, ConstantGlobal.THUMBNAIL);
        }

        String brdIds = getAndSaveBoardTree(contentJSON);

        String tags = getTags(contentJSON);

        try {
            content.lastUpdated = String.valueOf(JSONUtils.getLong(contentJSON,
                    ConstantGlobal.LAST_UPDATED));
            content.brdIds = brdIds;
            content.tags = tags;
            content.ownerName = ownerName;
            content.name = JSONUtils.getString(contentJSON, ConstantGlobal.NAME);
            content.desc = JSONUtils.getString(contentJSON, ConstantGlobal.DESC);
            content.info = new JSONObject(JSONUtils.getString(contentJSON, ConstantGlobal.INFO));
            content.subType = JSONUtils.getString(contentJSON, "subType");
            content.thumb = thumb;
            content.file = JSONUtils.getString(contentJSON, ConstantGlobal.FILE);

            if (!downloadable && content.downloaded) {
                JSONObject target = JSONUtils.getJSONObject(linkJSON, "target");
                String targetId = JSONUtils.getString(target, ConstantGlobal.ID);
                String targetType = JSONUtils.getString(target, ConstantGlobal.TYPE);
                updateContentDownloadHistory(content, targetId, targetType);
            }

            contentManager.updateContent(content);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    protected boolean updateContentDownloadHistory(Content content, String targetId,
            String targetType) {

        // if the content was previously downloaded and now the download has been disabled
        // in the current library
        // then remove this content from local disk, if it is not added to any other library
        // and downloaded

        List<ContentLink> downloadedContents = contentLinkManager.getContentLinks(content.orgKeyId,
                session.getSessionStringValue(ConstantGlobal.USER_ID), content.id, content.type,
                null);

        int remainingLinksCount = downloadedContents.size();

        if (remainingLinksCount == 0) {
            // FOR now if the content is downloaded then it will be deleted only if all the contents
            // links are deleted

            // delete the file as no other library has reference to it
            DownloadHistoryManager dHistoryManager = new DownloadHistoryManager(
                    contentManager.getContext());
            DownloadHistory dHistory = dHistoryManager.getDownloadHistory(content._id);
            if (dHistory != null) {
                File file = new File(dHistory.file);
                boolean deleted = file.exists() && file.delete();
                Log.d(TAG, "removing file: " + ", deleted:" + deleted + file.getAbsolutePath()
                        + ", deleted:" + deleted + ", exissts: " + file.exists() + " for content: "
                        + content.name + ", targetId:" + targetId + ", targetType:" + targetType);

                dHistoryManager.deleteDownloadHistory(dHistory._id);
                content.downloaded = false;

                // check if any decrypted version of this file present in temp folder
                File tempFile = new File(ContentDataManager.getTempContentDir(), file.getName());
                if (tempFile.exists()) {
                    Log.d(TAG, "remving temp file[" + tempFile.getAbsolutePath() + "] : "
                            + tempFile.delete());
                }

                return true;
            }
            dHistory = null;
            dHistoryManager = null;
        }
        return false;
    }

    protected boolean updateContentLink(JSONObject linkJSON, String entityId, String entityType,
            boolean remove, String contentSubType) {

        Log.d(TAG, "....inside updateContentLink......");

        JSONObject target = JSONUtils.getJSONObject(linkJSON, "target");
        if (target == null || target.length() < 1) {
            return false;
        }
        String type = JSONUtils.getString(target, ConstantGlobal.TYPE);
        String encLevel = JSONUtils.getString(linkJSON, ConstantGlobal.ENC_LEVEL);
        String passphrase = JSONUtils.getString(linkJSON, ConstantGlobal.PASSPHRASE);
        int position = JSONUtils.getInt(linkJSON, ConstantGlobal.POSITION);

        ContentLink contentLink = contentLinkManager.getContentLink(entityId, entityType,
                session.getSessionStringValue(ConstantGlobal.USER_ID),
                JSONUtils.getString(target, ConstantGlobal.ID), type,
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
        if (!remove && contentLink == null) {
            contentLink = new ContentLink(JSONUtils.getString(linkJSON, ConstantGlobal.ID),
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), String.valueOf(JSONUtils
                            .getLong(linkJSON, ConstantGlobal.TIME_CREATED)),
                    String.valueOf(JSONUtils.getLong(linkJSON, ConstantGlobal.LAST_UPDATED)),
                    session.getSessionStringValue(ConstantGlobal.USER_ID), entityId, entityType,
                    JSONUtils.getString(target, ConstantGlobal.ID), type, JSONUtils.getBoolean(
                            linkJSON, ConstantGlobal.DOWNLOADABLE), JSONUtils.getJSONArray(
                            linkJSON, ConstantGlobal.DOWNLOADABLE_ENTITIES), position);

            contentLink.encLevel = encLevel;
            contentLink.passphrase = passphrase;
            Log.d(TAG, contentLink.downloadableEntities + "abcdef");
            try {
                contentLinkManager.insertLink(contentLink);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

        } else if (!remove) {
            contentLink.downloadable = JSONUtils.getBoolean(linkJSON, ConstantGlobal.DOWNLOADABLE);
            contentLink.downloadableEntities = JSONUtils.getJSONArray(linkJSON,
                    ConstantGlobal.DOWNLOADABLE_ENTITIES);
            contentLink.encLevel = TextUtils.isEmpty(encLevel) ? contentLink.encLevel : encLevel;
            contentLink.passphrase = TextUtils.isEmpty(passphrase) ? contentLink.passphrase
                    : passphrase;
            contentLink.position = position;
            contentLink.lastUpdated = String.valueOf(JSONUtils.getLong(linkJSON,
                    ConstantGlobal.LAST_UPDATED));
            contentLinkManager.updateLink(contentLink);
        } else if (remove && contentLink != null) {

            contentLinkManager.deleteLink(contentLink._id);
            // in case of SLP before deleting, ensure that all the children are also deleted
            if (contentLink.entityType.equals(EntityType.MODULE.name())) {
                List<ContentLink> moduleLibraryLinks = contentLinkManager.getLibraryContentLinks(
                        contentLink.orgKeyId, contentLink.userId, contentLink.entityId,
                        contentLink.entityType);
                for (ContentLink moduleLibraryLink : moduleLibraryLinks) {
                    contentLinkManager.deleteLink(moduleLibraryLink._id);
                    updateContentAndContentLink(moduleLibraryLink.entityId,
                            moduleLibraryLink.entityType, moduleLibraryLink.targetId,
                            moduleLibraryLink.targetType);
                }
                session.updateContentLastSyncTime(0, contentLink.entityType, contentLink.entityId);
            }

        }
        return true;
    }

    protected void updateQuestion(JSONObject contentJSON, SyncType syncType) throws Exception {

        Log.d(TAG, "....inside updateQuestion......"+contentJSON.toString());

        try {
            String id = JSONUtils.getString(contentJSON, ConstantGlobal.ID);
            String type = JSONUtils.getString(contentJSON, "subType");
            String ownerId = JSONUtils.getString(contentJSON, ConstantGlobal.USER_ID);
            JSONObject user = JSONUtils.getJSONObject(contentJSON, "user");
            String ownerName = user == null ? null : TextUtils.join(
                    " ",
                    new String[] { JSONUtils.getString(user, ConstantGlobal.FIRST_NAME),
                            JSONUtils.getString(user, ConstantGlobal.LAST_NAME) });

            String brdIds = getAndSaveBoardTree(contentJSON);

            String tags = getTags(contentJSON);

            Question question = contentManager.getQuestion(id,
                    session.getSessionStringValue(ConstantGlobal.USER_ID),
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
            JSONObject infoJSON = new JSONObject(JSONUtils.getString(contentJSON, "info"));
            if (question == null) {
                question = new Question(
                        session.getSessionStringValue(ConstantGlobal.USER_ID),
                        String.valueOf(JSONUtils.getLong(contentJSON, ConstantGlobal.TIME_CREATED)),
                        session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID), JSONUtils.getString(
                        contentJSON, ConstantGlobal.DESC), id, type, JSONUtils.getString(
                        contentJSON, "difficulty"), TextUtils.join(SQLDBUtil.SEPARATOR,
                        JSONUtils.getList(infoJSON, ConstantGlobal.OPTIONS)), JSONUtils
                        .getJSONObject(infoJSON, "matrix"), JSONUtils.getString(infoJSON,
                        ConstantGlobal.CODE), JSONUtils.getBoolean(infoJSON, "hasAns"),
                        JSONUtils.getString(infoJSON, "source"), String.valueOf(JSONUtils.getLong(
                        contentJSON, ConstantGlobal.LAST_UPDATED)), brdIds, tags) {
                    @Override
                    public boolean equalsIgnoreCase(String attempted) {
                        return false;
                    }
                };
                question.ownerId = ownerId;
                question.ownerName = ownerName;
                Log.d(TAG, "....inside updateQuestion" + contentJSON.toString());
                downloadQuestionImages(question, syncType);
                if (syncType == SyncType.IMPORT) {
                    question.downloaded = true;
                }
                contentManager.insertQuestion(question);

            } else {
                Log.v(TAG, "question[" + id + "] already added in device");
            }

            JSONObject ansJSON = null;
            JSONObject solJSON = null;
            ansJSON = JSONUtils.getJSONObject(infoJSON, "answer");
            solJSON = JSONUtils.getJSONObject(infoJSON, "solution");
            if (ansJSON != null) {
                Answer answer = contentManager.getAnswer(question.orgKeyId,
                        session.getSessionStringValue(ConstantGlobal.USER_ID), question.id);
                if (answer == null) {
                    answer = new Answer(String.valueOf(JSONUtils.getLong(ansJSON,
                            ConstantGlobal.TIME_CREATED)), question.orgKeyId, JSONUtils.getString(
                            ansJSON, ConstantGlobal.ID),
                            StringUtils.join(JSONUtils.getList(ansJSON, ConstantGlobal.ANSWER),
                                    SQLDBUtil.SEPARATOR), JSONUtils.getJSONObject(ansJSON,
                                    "matrixAnswer"), solJSON,
                            session.getSessionStringValue(ConstantGlobal.USER_ID), question.id); // TODO
                    Log.d(TAG, "answer is " + answer.toString());
                    contentManager.insertAnswer(answer);
                } else {
                    answer.answer = StringUtils.join(
                            JSONUtils.getList(ansJSON, ConstantGlobal.ANSWER), SQLDBUtil.SEPARATOR);
                    answer.matrixAnswer = JSONUtils.getJSONObject(ansJSON, "matrixAnswer");
                    contentManager.updateAnswer(answer);
                }
            }
        } finally {
            totalContentFetched++;
        }
    }

    private void downloadQuestionImages(Question question, SyncType syncType) throws Exception {

        Log.d(TAG, "==== downloading question ========== " + question);
        Set<String> urls = new HashSet<String>();
        question.name = QuestionImageUtil.removeImageSrcUrl(question.name, urls,
                EntityType.QUESTION);
        question.options = QuestionImageUtil.removeImageSrcUrl(question.options, urls,
                EntityType.QUESTION);

        if (question.matrix != null) {
            @SuppressWarnings("unchecked")
            Iterator<String> it = question.matrix.keys();
            String key = null;
            while (it.hasNext()) {
                key = it.next();
                JSONArray jArray = updateQuestionArray(question.matrix.getJSONArray(key), urls);
                question.matrix.put(key, jArray);
            }
        }
        if (!urls.isEmpty()) {
            String toDir = ContentDataManager.getContentDir(EntityType.QUESTION.name());
            if (syncType == SyncType.ONLINE) {
                downloadImages(urls, toDir);
            } else if (syncType == SyncType.IMPORT) {
                for (String url : urls) {
                    downloadImage(url, toDir, EntityType.QUESTION.name());
                }
            }
        }
    }

    protected void downloadImages(Set<String> urls, String toDir) {

        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");

        for (String url : urls) {
            final HttpGet getRequest = new HttpGet(url);
            File file = null;

            try {
                HttpResponse response = client.execute(getRequest);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
                    Log.w(TAG, "Error " + statusCode + " while downloading file from " + url);
                    return;
                }

                int count = 0;

                final HttpEntity entity = response.getEntity();
                if (entity != null) {

                    InputStream inputStream = null;
                    byte data[] = new byte[1024];
                    // Output stream to write file
                    file = new File(toDir, StringUtils.substringAfterLast(url, File.separator));
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdir();
                    }
                    Log.d(TAG, "saving file to : " + file.getAbsolutePath());
                    OutputStream output = new FileOutputStream(file);
                    try {
                        inputStream = new BufferedInputStream(entity.getContent());
                        while ((count = inputStream.read(data)) != -1) {
                            // writing data to file
                            output.write(data, 0, count);
                        }
                    } finally {
                        IOUtils.closeStream(inputStream);
                        if (output != null) {
                            output.flush();
                        }
                        IOUtils.closeStream(output);
                        entity.consumeContent();
                    }
                }
            } catch (Exception e) {
                getRequest.abort();
                Log.e(TAG,
                        "Error while downloading file from [" + url + "], errorMessage:"
                                + e.getMessage(), e);
            }
        }

        if (client != null) {
            client.close();
        }
    }

    private JSONArray updateQuestionArray(JSONArray jsonArray, Set<String> urls)
            throws JSONException {

        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonArray.put(i, QuestionImageUtil.removeImageSrcUrl(jsonArray.getString(i), urls,
                        EntityType.QUESTION));
            }
        }
        return jsonArray;
    }

    protected abstract boolean downloadImage(String imageUrl, String toDir, String type);

    @Override
    public void clear() {

        super.clear();
        contentLinkManager = null;
        contentManager = null;
        boardManager = null;
    }

}
