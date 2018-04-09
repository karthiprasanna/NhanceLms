package com.nhance.android.db.datamanagers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.async.ISyncableContainer;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.Note;
import com.nhance.android.db.models.StudyHistory;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.NoteType;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.pojos.content.infos.StudyHistoryDetails;
import com.nhance.android.utils.SQLDBUtil;

public class UserActivityDataManager extends AbstractDataManager implements ISyncableContainer {

    private static final String TAG                 = "UserActivityDataManager";
    public static final String  TABLE_STUDY_HISTORY = "study_history";
    public static final String  TABLE_NOTE          = "note";

    public UserActivityDataManager(Context context) {

        super(context);
    }

    public void insertStudyHistory(StudyHistory studyHistory) throws Exception {

        Log.d(TAG, "inserting into studyHistory table");
        ContentValues values = studyHistory.toContentValues();
        insert(TABLE_STUDY_HISTORY, values);
        sync();
    }

    public StudyHistory getStudyHistory(int id) {

        StudyHistory studyHistory = null;
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_STUDY_HISTORY);
        sb.append("WHERE ");

        addIntEqualSQLQuery(ConstantGlobal._ID, id, sb);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            studyHistory = SQLDBUtil.convertToValues(cursor, StudyHistory.class, null);
        }
        closeCursor(cursor);
        Log.d(TAG, "returning studyHistory:" + studyHistory);
        return studyHistory;

    }

    public int updateStudyHistory(StudyHistory studyHistory) {

        ContentValues values = null;
        try {
            values = studyHistory.toContentValues();
        } catch (Exception e) {}
        return updateStudyHistory(studyHistory._id, values);
    }

    public int updateStudyHistory(int id, ContentValues tobeUpdatedValues) {

        if (tobeUpdatedValues == null) {
            return -1;
        }
        return update(TABLE_STUDY_HISTORY, tobeUpdatedValues, "_id=" + id, null);
    }

    public List<StudyHistory> getUnSyncStudyHistory(String userId) {

        List<StudyHistory> history = new ArrayList<StudyHistory>();
        if (userId == null) {
            return history;
        }
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_STUDY_HISTORY);
        sb.append("WHERE ");
        addIntEqualSQLQuery(ConstantGlobal.SYNCED, 0, sb);
        if (!TextUtils.isEmpty(userId)) {
            sb.append("AND ");
            addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false);
        }
        addLimitFilter(sb, ConstantGlobal.TIME_CREATED, SQLDBUtil.ORDER_ASC, SQLDBUtil.NO_START,
                SQLDBUtil.NO_LIMIT);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                StudyHistory entity = SQLDBUtil.convertToValues(cursor, StudyHistory.class, null);
                history.add(entity);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.d(TAG, "returning histody : " + history);
        return history;
    }

    public void insertNote(Note note) throws Exception {

        Log.d(TAG, "inserting into note table");
        ContentValues values = note.toContentValues();
        insert(TABLE_NOTE, values);
    }

    public int updatetNote(Note note) throws Exception {

        Log.d(TAG, "updating note table");
        ContentValues values = note.toContentValues();
        return update(TABLE_NOTE, values, "_id=" + note._id, null);
    }

    public List<StudyHistoryDetails> getStudyHistoryDetails(int orgKeyId, String userId) {

        return getStudyHistoryDetails(orgKeyId, userId, SQLDBUtil.NO_LIMIT, SQLDBUtil.NO_LIMIT);
    }

    public List<StudyHistoryDetails> getStudyHistoryDetails(int orgKeyId, String userId, int start,
            int size) {

        List<StudyHistoryDetails> history = new ArrayList<StudyHistoryDetails>();
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_STUDY_HISTORY);
        sb.append("WHERE ");
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        sb.append("AND ");
        addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false);
        addLimitFilter(sb, ConstantGlobal.TIME_CREATED, SQLDBUtil.ORDER_DESC, start, size);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            ContentDataManager cDataManager = new ContentDataManager(getContext());
            while (!cursor.isAfterLast()) {

                StudyHistory studyHistory = SQLDBUtil.convertToValues(cursor, StudyHistory.class,
                        null);
                Content content = cDataManager.getContent(studyHistory.contentId);
                if (content != null) {
                    StudyHistoryDetails studyDetails = new StudyHistoryDetails(studyHistory,
                            content.name, content.id, EntityType.valueOfKey(content.type));
                    history.add(studyDetails);
                }
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.d(TAG, "returning histody : " + history);
        return history;
    }

    public List<Note> getNotes(String userId, String progId, String entityId, NoteType noteType,
            String orderBy, String sortOrder, String query, Collection<String> entityIds) {

        List<Note> notes = new ArrayList<Note>();
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_NOTE);
        sb.append("WHERE ");
        addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false);
        if (!TextUtils.isEmpty(progId) && entityIds == null) {
            // TODO: progId is not need if the entityIds are provided
            // sb.append("AND ");
            // addStringEqualSQLQuery(ConstantGlobal.PROG_ID, progId, sb,
            // false);
        }
        if (!TextUtils.isEmpty(entityId)) {
            // if entityId is provided than only provide notes for that entity
            // only..
            entityIds = new HashSet<String>();
            entityIds.add(entityId);
        }
        if (entityIds != null && entityIds.size() > 0) {
            sb.append("AND ");
            sb.append(ConstantGlobal.ENTITY_ID).append(" in (")
                    .append(LocalManager.joinString(entityIds, "'")).append(") ");
        }

        if (noteType != null) {
            sb.append("AND ");
            addStringEqualSQLQuery(ConstantGlobal.NOTE_TYPE, noteType.name(), sb, false);
        }
        if (!TextUtils.isEmpty(query)) {
            sb.append("AND ");
            sb.append(ConstantGlobal.DESC).append(" like '%").append(query).append("%' ");
        }

        addLimitFilter(sb, orderBy, sortOrder, SQLDBUtil.NO_LIMIT, SQLDBUtil.NO_LIMIT);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                Note entity = SQLDBUtil.convertToValues(cursor, Note.class, null);
                notes.add(entity);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.d(TAG, "returning notes : " + notes);
        return notes;
    }

    @Override
    public void createDummyData() {

        // don't do anything here
    }

    @Override
    public void cleanData() {

        try {
            execSQL("DROP TABLE " + TABLE_STUDY_HISTORY);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        try {
            execSQL("DROP TABLE " + TABLE_NOTE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void createTable(List<String> createTables) {

        createTables.add(createStudyHistoryTable());
        createTables.add(createNoteTable());
    }

    private static String createStudyHistoryTable() {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE_STUDY_HISTORY);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.USER_ID + " text not null,");
        sb.append(ConstantGlobal.CONTENT_ID + " integer,");
        sb.append(ConstantGlobal.LINK_ID + " text not null,");
        sb.append(ConstantGlobal.SYNCED + " integer");
        endCreateTableQuery(sb);
        return sb.toString();
    }

    private static String createNoteTable() {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE_NOTE);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.NAME + " text,");
        sb.append(ConstantGlobal.BY + " text,");
        sb.append(ConstantGlobal.USER_ID + " text not null,");
        sb.append(ConstantGlobal.DESC + " text not null,");
        sb.append(ConstantGlobal.NOTE_TYPE + " text not null,");
        sb.append(ConstantGlobal.LAST_VIEWED + " text not null,");
        sb.append(ConstantGlobal.THUMB + " text,");
        sb.append(ConstantGlobal.PROG_ID + " text,");
        sb.append(ConstantGlobal.ENTITY_ID + " text,");
        sb.append(ConstantGlobal.ENTITY_TYPE + " text,");
        sb.append(ConstantGlobal.CONTENT_TYPE + " text,");
        sb.append(ConstantGlobal.COURSE_BRD_ID + " text,");
        sb.append(ConstantGlobal.COURSE_BRD_NAME + " text");
        endCreateTableQuery(sb);
        return sb.toString();
    }

    @Override
    public void sync() {

        super.triggerSync();

    }

}
