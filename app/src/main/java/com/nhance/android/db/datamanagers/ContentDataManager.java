package com.nhance.android.db.datamanagers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.entity.Answer;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.ContentLink;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.EntityType;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.SrcEntity;
import com.nhance.android.pojos.VedantuDBResult;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.SQLDBUtil;

public class ContentDataManager extends AbstractDataManager {

    public static final String TABLE_CONTENT = "content";

    public static final String TABLE_QUESTION = "entity_question";
    public static final String TABLE_ANSWER = "question_answer";

    private final String TAG = "ContentDataManager";

    public ContentDataManager(Context context) {
        super(context);
    }

    public int starContent(int id, boolean starred) {

        ContentValues tobeUpdatedValues = new ContentValues();
        tobeUpdatedValues.put(ConstantGlobal.STARRED, starred ? 1 : 0);
        return updateContent(id, tobeUpdatedValues);
    }

    public int updateLastViewed(int id) {

        ContentValues tobeUpdatedValues = new ContentValues();
        tobeUpdatedValues.put(ConstantGlobal.LAST_VIEWED,
                String.valueOf(System.currentTimeMillis()));
        return updateContent(id, tobeUpdatedValues);
    }

    public VedantuDBResult<LibraryContentRes> getLibraryContents(String userId, int orgKeyId, String targetId, String targetType, String type, String excludeType, String excludeQuestionStr, int brdId, String orderBy, String sortOrder, int start, int size, String query, Boolean downloaded, Boolean starred) {
        VedantuDBResult<LibraryContentRes> result = new VedantuDBResult<LibraryContentRes>(0);
        // removed in slp-modules branch but persisted from release
        if (TextUtils.isEmpty(userId)) {
            return result;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_CONTENT, new String[]{"count(*) as count"});
        sb.append(" AS c INNER JOIN " + ContentLinkDataManager.TABLE).append(" AS cl ON c.id=cl.entityId AND c.type=cl.entityType AND c.userId=cl.userId AND c.orgKeyId=cl.orgKeyId ");
        addContentFilters(sb, userId, orgKeyId, targetId, targetType, type, excludeType, excludeQuestionStr, brdId, orderBy, sortOrder, query, downloaded, starred);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor.moveToFirst()) {
            result.totalHits = cursor.getInt(0);
        }
        closeCursor(cursor);
        if (result.totalHits < 1) {
            return result;
        }
        sb = new StringBuilder();
        addSelectQuery(sb, TABLE_CONTENT, new String[]{"c.*", "cl.timeCreated as linkTime", "cl.downloadable", "cl.linkId, cl.encLevel, cl.position"});
        sb.append(" AS c INNER JOIN " + ContentLinkDataManager.TABLE).append(" AS cl ON c.id=cl.entityId AND c.type=cl.entityType AND c.userId=cl.userId AND c.orgKeyId=cl.orgKeyId ");
        addContentFilters(sb, userId, orgKeyId, targetId, targetType, type, excludeType, excludeQuestionStr, brdId, orderBy, sortOrder, query, downloaded, starred);
        addLimitFilter(sb, orderBy, sortOrder, start, size);
        try {
            cursor = rawQuery(sb.toString(), null);
            if (cursor != null) {
                // result.totalHits = cursor.getCount();
                Set<String> cColumnNames = new HashSet<String>(Arrays.asList(cursor.getColumnNames()));
                while (cursor.moveToNext()) {
                    LibraryContentRes content = SQLDBUtil.convertToValues(cursor, LibraryContentRes.class, null, cColumnNames);
                    result.entities.add(content);
                }
                cColumnNames.clear();
            }
        } finally {
            closeCursor(cursor);
        }
        Log.v(TAG, "returning contents result:" + result);
        return result;
    }


    private void addContentFilters(StringBuilder sb, String userId, int orgKeyId, String targetId, String targetType, String type, String excludeType, String excludeQuestionStr, int brdId, String orderBy, String sortOrder, String query, Boolean downloaded, Boolean starred) {
        sb.append("WHERE ");
        addStringEqualSQLQuery("c.userId", userId, sb, false);
        sb.append(SQL_KEYWORD_AND);
        sb.append("c.orgKeyId").append("=").append(orgKeyId).append(" ");
        if (!TextUtils.isEmpty(targetId)) {
            sb.append(SQL_KEYWORD_AND);
            addStringEqualSQLQuery(ConstantGlobal.TARGET_ID, targetId, sb, false);
        }
        if (!TextUtils.isEmpty(targetType)) {
            sb.append(SQL_KEYWORD_AND);
            addStringEqualSQLQuery(ConstantGlobal.TARGET_TYPE, targetType, sb, false);
        }
        if (downloaded != null) {
            sb.append(SQL_KEYWORD_AND);
            addBooleanEqualSQLQuery(ConstantGlobal.DOWNLOADED, downloaded.booleanValue(), sb);
        }
        if (starred != null) {
            sb.append(SQL_KEYWORD_AND);
            addBooleanEqualSQLQuery(ConstantGlobal.STARRED, starred.booleanValue(), sb);
        }
        if (!TextUtils.isEmpty(type)) {
            sb.append(SQL_KEYWORD_AND);
            addStringEqualSQLQuery(ConstantGlobal.TYPE, type, sb, false);
        }
        if (!TextUtils.isEmpty(excludeType)) {
            sb.append(SQL_KEYWORD_AND);
            sb.append(ConstantGlobal.TYPE).append("!='").append(excludeType).append("' ");
        }
        if (!TextUtils.isEmpty(excludeQuestionStr)) {
            sb.append(SQL_KEYWORD_AND);
            sb.append(ConstantGlobal.TYPE).append("!='").append(excludeQuestionStr).append("' ");
        }
        if (!TextUtils.isEmpty(query)) {
            sb.append(SQL_KEYWORD_AND);
            sb.append("LOWER(" + ConstantGlobal.NAME + ")").append(" LIKE '%" + query.trim().toLowerCase().replace("'", "''") + "%' ");
        }
        if (brdId != BoardDataManager.NO_BRD_ID) {
            sb.append(SQL_KEYWORD_AND);
            sb.append(ConstantGlobal.BRD_IDS).append(" LIKE '%" + SQLDBUtil.SEPARATOR + brdId + SQLDBUtil.SEPARATOR + "%' ");
        }
    }

    public LibraryContentRes getLibraryContentRes(String linkId) {
        Log.d(TAG, "in getLibraryContentRes");
        if (TextUtils.isEmpty(linkId)) {
            return null;
        }

        LibraryContentRes res = new LibraryContentRes();
        ContentLink cLink = new ContentLinkDataManager(getContext()).getContentLink(linkId);
        if (cLink == null) {
            Log.e(TAG, "no contentLink found for linkId : " + linkId);
            return null;
        }
        Content content = getContent(cLink.entityId, cLink.entityType, cLink.userId, cLink.orgKeyId);
        if (content == null) {
            Log.e(TAG, "no content found for link : " + cLink);
            return null;
        }
        List<SrcEntity> downloadableEntities = new ArrayList<SrcEntity>();

        Log.d(TAG, "getting downloadableEntities");

        JSONArray list = cLink.downloadableEntities;
        if (list != null) {
            for (int i = 0; i < list.length(); i++) {
                try {
                    JSONObject entity = list.getJSONObject(i);
                    Log.v(TAG, "entity : " + entity);
                    String id = JSONUtils.getString(entity, ConstantGlobal.ID);
                    String type = JSONUtils.getString(entity, ConstantGlobal.TYPE);
                    if (!TextUtils.isEmpty(id)) {
                        downloadableEntities.add(new SrcEntity(EntityType.valueOf(type), id));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        Log.d(TAG, downloadableEntities + "downloadableEntities");
        res._id = content._id;
        res.brdIds = content.brdIds;
        res.desc = content.desc;
        res.downloaded = content.downloaded;
        res.downloadableEntities = downloadableEntities;
        res.file = content.file;
        res.id = content.id;
        res.type = content.type;
        res.info = content.info;
        res.lastUpdated = content.lastUpdated;
        res.lastViewed = content.lastViewed;
        res.name = content.name;
        res.orgKeyId = content.orgKeyId;
        res.ownerId = content.ownerId;
        res.ownerName = content.ownerName;
        res.starred = content.starred;
        res.subType = content.subType;
        res.tags = content.tags;
        res.targetIds = content.targetIds;
        res.targetNames = content.targetNames;
        res.thumb = content.thumb;
        res.timeCreated = content.timeCreated;
        res.userId = content.userId;

        res.linkTime = cLink.timeCreated;
        res.passphrase = cLink.passphrase;
        res.linkId = cLink.linkId;

        res.downloadable = cLink.downloadable;
        res.encLevel = cLink.encLevel;
        return res;
    }

    public Content getContent(int id) {

        Content content = null;
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_CONTENT);
        sb.append("WHERE ");

        addIntEqualSQLQuery(ConstantGlobal._ID, id, sb);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            content = SQLDBUtil.convertToValues(cursor, Content.class, null);
        }
        closeCursor(cursor);
        Log.v(TAG, "returning content:" + content);
        return content;

    }


    public Content getContent(String id) {

        Content content = null;
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_CONTENT);
        sb.append("WHERE ");

        addStringEqualSQLQuery(ConstantGlobal.ID, id, sb, false);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            content = SQLDBUtil.convertToValues(cursor, Content.class, null);
        }
        closeCursor(cursor);
        Log.v(TAG, "returning content:" + content);
        return content;

    }
    public int updateContent(Content content) {

        ContentValues values = null;
        try {
            values = content.toContentValues();
        } catch (Exception e) {
        }
        return updateContent(content._id, values);
    }

    public int updateContent(int id, ContentValues tobeUpdatedValues) {

        if (tobeUpdatedValues == null) {
            return -1;
        }
        return update(TABLE_CONTENT, tobeUpdatedValues, "_id=" + id, null);
    }

    public int removeContent(int id) {

        return delete(TABLE_CONTENT, "_id=" + id, null);
    }

    public Content getContent(String id, String type, String userId, int orgKeyId) {

        Content content = null;
        if (TextUtils.isEmpty(id)) {
            return content;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_CONTENT);
        sb.append("WHERE ");
        addStringEqualSQLQuery(ConstantGlobal.ID, id, sb, false);
        sb.append(SQL_KEYWORD_AND);
        addStringEqualSQLQuery(ConstantGlobal.TYPE, type, sb, false);
        sb.append(SQL_KEYWORD_AND);
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }
        sb.append(ConstantGlobal.ORG_KEY_ID).append("=").append(orgKeyId);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            content = SQLDBUtil.convertToValues(cursor, Content.class, null);
        }
        closeCursor(cursor);
        Log.v(TAG, "returning content:" + content);
        return content;
    }

    public void insertContent(Content content) throws Exception {

        Log.v(TAG, "inserting into content table");
        ContentValues values = content.toContentValues();
        content._id = (int) insert(TABLE_CONTENT, values);
    }

    public Map<String, Integer> getBoardContentCountMap(String userId, int orgKeyId, int brdId,
                                                        String targetId, String targetType) {

        Map<String, Integer> boardContentMap = new HashMap<String, Integer>();
        // not in slp-modules persisted from release
        if (TextUtils.isEmpty(userId)) {
            return boardContentMap;
        }
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_CONTENT, new String[]{"type", "count(*) as count"});

        sb.append(" AS c INNER JOIN " + ContentLinkDataManager.TABLE)
                .append(" AS cl ON c.id=cl.entityId AND c.type=cl.entityType AND c.userId=cl.userId AND c.orgKeyId=cl.orgKeyId ");

        sb.append("WHERE ");
        if (addStringEqualSQLQuery("c.userId", userId, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }
        addIntEqualSQLQuery("c." + ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        sb.append(SQL_KEYWORD_AND);

        if (addStringEqualSQLQuery(ConstantGlobal.TARGET_ID, targetId, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(ConstantGlobal.TARGET_TYPE, targetType, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        sb.append(ConstantGlobal.BRD_IDS).append(
                " LIKE '%" + SQLDBUtil.SEPARATOR + brdId + SQLDBUtil.SEPARATOR + "%' ");
        sb.append(" group by ").append("c." + ConstantGlobal.TYPE);
        Cursor cursor = null;
        try {
            cursor = rawQuery(sb.toString(), null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String key = cursor.getString(0);
                    int value = cursor.getInt(1);
                    boardContentMap.put(key, Integer.valueOf(value));
                }
            }
        } finally {
            closeCursor(cursor);
        }
        Log.v(TAG, "returning boardContentMap:" + boardContentMap);

        return boardContentMap;
    }

    /**
     * @param orgKeyId
     * @param ids
     * @param type
     * @param fields
     * @return key of map will be {type_id}
     */
    public Map<String, Content> getContentIdContentMiniInfoMap(int orgKeyId, Set<String> ids,
                                                               String type, String[] fields) {

        Map<String, Content> contentIdToContentMap = new HashMap<String, Content>();
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_CONTENT, fields);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.TYPE, type, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        sb.append("AND ");

        String sqlEntityIds = LocalManager.joinString(ids, "'");

        sb.append(ConstantGlobal.ID).append(" in (").append(sqlEntityIds).append(")");

        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            Set<String> cColumnNames = new HashSet<String>(Arrays.asList(cursor.getColumnNames()));
            while (!cursor.isAfterLast()) {
                Content content = SQLDBUtil.convertToValues(cursor, Content.class, fields,
                        cColumnNames);
                contentIdToContentMap.put(content.type + "_" + content.id, content);
                cursor.moveToNext();
            }
            cColumnNames.clear();
            cColumnNames = null;
        }
        closeCursor(cursor);
        return contentIdToContentMap;
    }

    public static String getTempContentDir() {

        String tempDir = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/nhance/temp";
        File tempFile = new File(tempDir);
        if (!tempFile.exists() || !tempFile.isDirectory()) {
            synchronized (tempDir.intern()) {
                if (!tempFile.exists()) {
                    tempFile.mkdirs();
                }
            }
        }
        return tempDir;
    }

    public static String getContentDir(String type) {

        String dir = TextUtils.join(File.separator,
                new String[]{Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "nhance", type.toLowerCase()});
        if (type.equalsIgnoreCase("TEST") || type.equalsIgnoreCase("ASSIGNMENT")) {
            return null;
        }
        File f = new File(dir);
        if (!f.exists()) {
            synchronized (f.getAbsolutePath().intern()) {
                if (!f.exists()) {
                    f.mkdirs();
                }
            }
        }
        return dir;
    }

    public Answer getAnswer(int orgKeyId, String userId, String qId) {

        Answer answer = null;
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(qId)) {
            return answer;
        }
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_ANSWER);
        sb.append("WHERE ");

        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.QID, qId, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            answer = SQLDBUtil.convertToValues(cursor, Answer.class, null);
        }
        closeCursor(cursor);
        Log.v(TAG, "returning answer:" + answer);
        return answer;
    }

    public synchronized int updateAnswer(Answer answer) {

        ContentValues values = null;
        try {
            values = answer.toContentValues();
        } catch (Exception e) {
        }

        return update(TABLE_ANSWER, values, "_id=" + answer._id, null);
    }

    public int updateAnswer(String qId, ContentValues tobeUpdatedValues) {

        if (tobeUpdatedValues == null) {
            return -1;
        }

        return update(TABLE_ANSWER, tobeUpdatedValues, "qId='" + qId + "'", null);
    }

    public Question getQuestion(int _id) {

        Question question = null;

        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_QUESTION);
        sb.append("WHERE ");
        addIntEqualSQLQuery(ConstantGlobal._ID, _id, sb);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            question = SQLDBUtil.convertToValues(cursor, Question.class, null);
        }
        closeCursor(cursor);
        Log.v(TAG, "returning question:" + question);
        return question;
    }

    public List<Question> getQuestionList(String _id) {

        List<Question> questionList = new ArrayList<Question>();
        Question question = null;
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_QUESTION);
        sb.append("WHERE ");
        addStringEqualSQLQuery(ConstantGlobal.USER_ID, _id, sb, true);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null ){
            while (cursor.moveToNext()) {
                question = SQLDBUtil.convertToValues(cursor, Question.class, null);
                questionList.add(question);
            }

        }
        closeCursor(cursor);
        Log.v(TAG, "returning questionlist:" + questionList.toString());
        return questionList;
    }

    public Question getQuestion(String id, String userId, int orgKeyId) {

        Question question = null;
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(id)) {
            return question;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_QUESTION);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.ID, id, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }
        sb.append(ConstantGlobal.ORG_KEY_ID).append("=").append(orgKeyId);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            question = SQLDBUtil.convertToValues(cursor, Question.class, null);
        }
        closeCursor(cursor);
        Log.v(TAG, "returning question:" + question);
        return question;
    }

    public List<String> getDistinctQTypes(int orgKeyId, String userId, Collection<String> qIds) {

        List<String> qTypes = new ArrayList<String>();
        if (qIds == null || TextUtils.isEmpty(userId)) {
            return qTypes;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("select DISTINCT type from ").append(TABLE_QUESTION);
        sb.append(" WHERE ");
        sb.append("id IN (").append(LocalManager.joinString(qIds, "'")).append(")");
        sb.append(" AND ");
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append(" AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String qType = cursor.getString(0);
                qTypes.add(qType);
            }
        }
        closeCursor(cursor);

        return qTypes;
    }

    @Override
    public void cleanData() {

        Log.d(TAG, "cleaning library content");

        try {
            execSQL("DROP TABLE " + TABLE_CONTENT);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        try {
            execSQL("DROP TABLE " + TABLE_QUESTION);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        try {
            execSQL("DROP TABLE " + TABLE_ANSWER);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void createTable(List<String> createTables) {

        createContentTable(createTables);
        createQuestionTable(createTables);
        createAnswerTable(createTables);
    }

    private static void createContentTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE_CONTENT);

        addAbstractContentFeildsRow(sb);

        sb.append(ConstantGlobal.ID).append(" text not null,");
        sb.append(ConstantGlobal.TYPE).append(" text not null,");
        sb.append(ConstantGlobal.DESC).append(" text,");
        sb.append(ConstantGlobal.INFO).append(" text,");
        sb.append("subType").append(" text,");
        sb.append(ConstantGlobal.THUMB).append(" text,");
        sb.append(ConstantGlobal.FILE).append(" text");

        endCreateTableQuery(sb);
        createTables.add(sb.toString());
        createTables.add(createIndexQuery(TABLE_CONTENT, "content_index", true, ConstantGlobal.ID,
                ConstantGlobal.TYPE, ConstantGlobal.USER_ID, ConstantGlobal.ORG_KEY_ID));
    }

    private static void createQuestionTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE_QUESTION);

        addAbstractContentFeildsRow(sb);

        sb.append(ConstantGlobal.ID).append(" text not null,");
        sb.append(ConstantGlobal.TYPE).append(" text not null,");
        sb.append("difficulty").append(" text not null,");
        sb.append(ConstantGlobal.CODE).append(" text,");
        sb.append(ConstantGlobal.INFO).append(" text,");
        sb.append(ConstantGlobal.OPTIONS).append(" text,");
        sb.append("matrix").append(" text,");
        sb.append("hasAns").append(" integer,");
        sb.append(ConstantGlobal.SOURCE).append(" text");

        endCreateTableQuery(sb);
        createTables.add(sb.toString());
        createTables.add(createIndexQuery(TABLE_QUESTION, "question_index", true,
                ConstantGlobal.ID, ConstantGlobal.USER_ID, ConstantGlobal.ORG_KEY_ID));
    }

    private static void createAnswerTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE_ANSWER);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.USER_ID + " text not null,");
        sb.append(ConstantGlobal.QID).append(" text not null,");
        sb.append("ansId").append(" text not null,");

        sb.append(ConstantGlobal.ANSWER).append(" text not null,");
        sb.append("matrixAnswer").append(" text, ");
        sb.append(ConstantGlobal.SOLUTION).append(" text ");

        endCreateTableQuery(sb);
        createTables.add(sb.toString());

        createTables.add(createIndexQuery(TABLE_ANSWER, "answer_index", true, "ansId",
                ConstantGlobal.USER_ID, ConstantGlobal.ORG_KEY_ID));
    }

    private static void addAbstractContentFeildsRow(StringBuilder sb) {

        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.USER_ID + " text not null,");
        sb.append(ConstantGlobal.OWNER_ID + " text not null,");
        sb.append(ConstantGlobal.OWNER_NAME + " text ,");
        sb.append(ConstantGlobal.NAME + " text not null,");

        sb.append(ConstantGlobal.DOWNLOADED + " integer,");
        sb.append(ConstantGlobal.STARRED + " integer,");
        sb.append(ConstantGlobal.LAST_UPDATED + " text not null,");
        sb.append(ConstantGlobal.LAST_VIEWED + " text,");
        sb.append(ConstantGlobal.TAGS + " text,");

        sb.append(ConstantGlobal.BRD_IDS + " text not null,");
        sb.append(ConstantGlobal.TARGET_IDS + " text,");
        sb.append(ConstantGlobal.TARGET_NAMES + " text,");

    }

    public void insertAnswer(Answer answer) throws Exception {

        Log.d(TAG, "inserting into Answer table");
        ContentValues values = answer.toContentValues();
        answer._id = (int) insert(TABLE_ANSWER, values);
    }

    public void insertQuestion(Question question) throws Exception {

        Log.d(TAG, "inserting into Question table");
        ContentValues values = question.toContentValues();
        question._id = (int) insert(TABLE_QUESTION, values);
    }

    @Override
    public void createDummyData() {

    }

}
