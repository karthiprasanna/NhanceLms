package com.nhance.android.db.datamanagers;





import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.db.models.entity.QuestionStatus;
import com.nhance.android.utils.SQLDBUtil;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by administrator on 8/14/17.
 */


public class QuestionAttemptStatusDataManager extends AbstractDataManager {

    private static final String TABLE_QUESTION_STATUS = "question_status";
    private final String       TAG            = "QuestionAttemptStatusDataManager";
    /**
     * pass this constructor a applicationContext
     *
     *
     * @param context
     */
    public QuestionAttemptStatusDataManager(Context context) {
        super(context);
    }

    @Override
    public void createDummyData() {

    }

    @Override
    public void cleanData() {

    }


    public static void createTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE_QUESTION_STATUS);

        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(ConstantGlobal.ID).append(" text not null,");
                sb.append(ConstantGlobal.ATTEMPTS).append(" text,");
        sb.append(ConstantGlobal.ATTEMPTED).append(" text,");
        sb.append(ConstantGlobal.CORRCT_ANSWER).append(" text,");
        sb.append(ConstantGlobal.ANSWER_GIVEN).append(" text,");
        sb.append(ConstantGlobal.TIME_TAKEN).append(" text,");
        sb.append(ConstantGlobal.QUESTION_TYPE).append(" text," );
        sb.append(ConstantGlobal.SYNC_STATUS).append(" text " );





        endCreateTableQuery(sb);
        createTables.add(sb.toString());
        createTables.add(createIndexQuery(TABLE_QUESTION_STATUS, "question_status_index", true, ConstantGlobal.ID,
                ConstantGlobal.USER_ID));
    }



    public QuestionStatus getQuestionStatus(String id, JSONArray correctanswer, boolean attempted,JSONArray answerGiven, int attempts, String questionType, String timeCreated, long  timeTaken, int syncStatus) {

        QuestionStatus content = null;
        if (TextUtils.isEmpty(id)) {
            return content;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_QUESTION_STATUS);
        sb.append("WHERE ");


     sb.append(ConstantGlobal.ID).append("=").append(id);
        sb.append(ConstantGlobal.CORRCT_ANSWER).append("=").append(correctanswer);

        sb.append(ConstantGlobal.ATTEMPTED).append("=").append(attempted);
        sb.append(ConstantGlobal.ANSWER_GIVEN).append("=").append(answerGiven);
        sb.append(ConstantGlobal.ATTEMPTS).append("=").append(attempts);
        sb.append(ConstantGlobal.QUESTION_TYPE).append("=").append(questionType);
        sb.append(ConstantGlobal.TIME_TAKEN).append("=").append(timeTaken);
        sb.append(ConstantGlobal.TIME_CREATED).append("=").append(timeCreated);
        sb.append(ConstantGlobal.SYNC_STATUS).append("=").append(syncStatus);



        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, id, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }
        //sb.append(ConstantGlobal.ORG_KEY_ID).append("=").append(orgKeyId);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            content = SQLDBUtil.convertToValues(cursor, QuestionStatus.class, null);
        }
        closeCursor(cursor);
        Log.v(TAG, "returning content:" + content);

        Log.e("returningcontent","...."+content);
        return content;
    }

    public QuestionStatus getQuestionStatus(String id) {

        Log.e("questionStatus", "called");
        QuestionStatus questionStatus = null;
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_QUESTION_STATUS);
        sb.append("WHERE ");

        addStringEqualSQLQuery(ConstantGlobal.ID, id, sb, true);

        Cursor cursor = rawQuery(sb.toString(), null);


        if (cursor != null && cursor.moveToFirst()) {
            questionStatus = SQLDBUtil.convertToValues(cursor, QuestionStatus.class, null);
            Log.e("questionStatus", ".."+questionStatus);
        }
        closeCursor(cursor);
        if(questionStatus != null)
        Log.v(TAG, "returning content:" + questionStatus.toString());
        return questionStatus;

    }

    public void insertContent(QuestionStatus questionStatus) throws Exception {

        Log.v(TAG, "inserting into QuestionStatus table"+"..."+questionStatus.toString());
        ContentValues values = questionStatus.toContentValues();
        questionStatus._id = (int) insert(TABLE_QUESTION_STATUS, values);
    }

    public int updateQuestionStatus(String qId, QuestionStatus questionStatus) {

        ContentValues values = null;
        try {
            values = questionStatus.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        Log.v(TAG, "updating into QuestionStatus table"+"..."+questionStatus.toString());

        return update(TABLE_QUESTION_STATUS, values, "id='" + qId + "'", null);
    }



    public List<QuestionStatus> getUnSyncQuestions(int limit) {

        List<QuestionStatus> questionStatusList = new ArrayList<QuestionStatus>();

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_QUESTION_STATUS);
        sb.append("WHERE ");
        addIntEqualSQLQuery(ConstantGlobal.SYNC_STATUS, -1, sb);

        addLimitFilter(sb, ConstantGlobal.TIME_CREATED, SQLDBUtil.ORDER_DESC, SQLDBUtil.NO_START,
                limit);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                QuestionStatus questionStatus = SQLDBUtil.convertToValues(cursor, QuestionStatus.class, null);
                questionStatusList.add(questionStatus);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.e("unsyncedQ@uestions", ".."+questionStatusList.toString());
        return questionStatusList;
    }


    }

