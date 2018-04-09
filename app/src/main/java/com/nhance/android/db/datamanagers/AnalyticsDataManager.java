package com.nhance.android.db.datamanagers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.assignment.async.AssignmentISyncableContainer;
import com.nhance.android.assignment.comparators.AssignmentQuestionNoListComparator;
import com.nhance.android.assignment.db.models.analytics.AssignmentAnalytics;
import com.nhance.android.assignment.db.models.analytics.AssignmentBoardAnalytics;
import com.nhance.android.assignment.db.models.analytics.AssignmentCourseLevelAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.async.AsynExecutorService;
import com.nhance.android.async.ISyncableContainer;
import com.nhance.android.comparators.TestQuestionNoListComparator;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.AttemptTimeTaken;
import com.nhance.android.db.models.analytics.QuestionAnalytics;
import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.analytics.TestAvg;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.db.models.analytics.TestCourseLevelAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.enums.BoardType;
import com.nhance.android.enums.QuestionLevel;
import com.nhance.android.enums.QuestionType;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.pojos.tests.BoardQus;
import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.utils.SQLDBUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AnalyticsDataManager extends AbstractDataManager implements ISyncableContainer,AssignmentISyncableContainer {

    /**
     *
     */

    private static final String TAG = "AnalyticsDataManager";

    public static final String TEST_ID_OVERALL_SYSTEM = "overall";
    public static final String ASSIGNMENT_ID_OVERALL_SYSTEM = "Assignmentoverall";

    public static final int STRENGTH_TYPE_STRONG = -1;
    public static final int STRENGTH_TYPE_WEAK = -2;

    public static final String TABLE_ENTITY_ANALYTICS = "entity_analytics";
    private static final String TABLE_ENTITY_BOARD_ANALYTICS = "entity_board_analytics";
    private static final String TABLE_ENTITY_BOARD_LEVEL_ANALYTICS = "entity_board_level_analytics";
    public static final String TABLE_QUESTION_ANALYTICS = "qus_analytics";
    public static final String TABLE_ATTEMPT_TIME_TAKEN = "entity_attempt_time_taken";

    public AnalyticsDataManager(Context context) {

        super(context);

    }

    public void createTestAnalytics(Content test, TestExtendedInfo info, String userId) {

        TestAnalytics tA = getTestAnalytics(test.orgKeyId, userId, test.id, test.type);
        if (tA != null) {
            Log.d(TAG, "test analytics already present for test:" + test);
            return;
        }
        tA = new TestAnalytics(test.orgKeyId, userId, 0, -1, test.id, test.type, info.totalMarks,
                info.qusCount, 0, 0, info.duration);
        createTestAnalytics(tA);

    }

    public void createTestAnalytics(TestAnalytics testAnalytics) {

        try {
            ContentValues values = testAnalytics.toContentValues();
            insert(TABLE_ENTITY_ANALYTICS, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * <blockquote>createTestCourseAnalytics will create empty analytics for all courses and their
     * topics</blockquote>
     *
     * @param test
     * @param testCourseMetadata
     */
    public void createTestCourseAnalytics(Content test, List<TestMetadata> testCourseMetadata,
                                          String userId) {

        if (testCourseMetadata != null) {
            for (TestMetadata cMData : testCourseMetadata) {
                createTestCourseAnalytics(test, cMData, userId);
            }
        }
    }

    private void createTestCourseAnalytics(Content test, TestMetadata cMData, String userId) {

        if (TextUtils.isEmpty(userId)) {
            return;
        }

        TestBoardAnalytics courseAnalytics = getTestSingleBoardAnalytics(test.orgKeyId, userId,
                test.id, test.type, cMData.id, BoardType.COURSE.name(), null);
        if (courseAnalytics != null) {
            return;
        }

        courseAnalytics = new TestBoardAnalytics(test.orgKeyId, userId, 0, 0, test.id, test.type,
                cMData.totalMarks, cMData.qusCount, 0, 0, cMData.id, BoardType.COURSE.name(),
                cMData.name, null);
        try {
            ContentValues values = courseAnalytics.toContentValues();
            insert(TABLE_ENTITY_BOARD_ANALYTICS, values);
            createTestTopicAnalytics(test, (TestExtendedInfo) test.toContentExtendedInfo(),
                    cMData.id, cMData.children, userId);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void createTestTopicAnalytics(Content test, TestExtendedInfo testInfo, String parentId,
                                          List<BoardQus> topicsMetadata, String userId) {

        for (BoardQus tMData : topicsMetadata) {

            TestBoardAnalytics topicAnalytics = getTestSingleBoardAnalytics(test.orgKeyId, userId,
                    test.id, test.type, tMData.id, BoardType.TOPIC.name(), parentId);

            if (topicAnalytics != null) {
                continue;
            }

            topicAnalytics = new TestBoardAnalytics(test.orgKeyId, userId, 0, 0, test.id,
                    test.type, tMData.totalMarks, tMData.qusCount, 0, 0, tMData.id,
                    BoardType.TOPIC.name(), tMData.name, parentId);
            try {
                ContentValues values = topicAnalytics.toContentValues();
                insert(TABLE_ENTITY_BOARD_ANALYTICS, values);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public TestAnalytics getTestAnalytics(int orgKeyId, String userId, String entityId,
                                          String entityType) {

        TestAnalytics analytics = null;
        if (TextUtils.isEmpty(entityId) || TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(entityType)) {
            Log.e(TAG, "empty entityId : " + entityId + ", or userId: " + userId);
            return analytics;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_ANALYTICS);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            Log.d(TAG, "total count: " + cursor.getCount());
            analytics = SQLDBUtil.convertToValues(cursor, TestAnalytics.class, null);
        }
        closeCursor(cursor);

        return analytics;
    }

    public List<TestAnalytics> getAllTestAnalytics(int orgKeyId, String userId, String entityType,
                                                   String[] fields) {

        return getAllTestAnalytics(orgKeyId, userId, null, entityType, fields, false);
    }

    public List<TestAnalytics> getAllTestAnalytics(int orgKeyId, String userId,
                                                   Collection<String> entityIds, String entityType, String[] fields, boolean endedOnly) {

        List<TestAnalytics> testAnalytics = new ArrayList<TestAnalytics>();
        if (TextUtils.isEmpty(userId)) {
            return testAnalytics;
        }

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_ANALYTICS, fields, null);
        sb.append("WHERE ");
        if (endedOnly) {
            sb.append(ConstantGlobal.END_TIME).append("!='" + 0 + "' ");
            sb.append(" AND ");

        }

        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }

        if (entityIds != null && !entityIds.isEmpty()) {
            sb.append(ConstantGlobal.ENTITY_ID).append(" in (")
                    .append(LocalManager.joinString(entityIds, "'")).append(") ");
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        addLimitFilter(sb, ConstantGlobal.TIME_CREATED, SQLDBUtil.ORDER_DESC, SQLDBUtil.NO_START,
                SQLDBUtil.NO_LIMIT);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            Set<String> cColumnNames = new HashSet<String>(Arrays.asList(cursor.getColumnNames()));
            while (!cursor.isAfterLast()) {
                TestAnalytics tA = SQLDBUtil.convertToValues(cursor, TestAnalytics.class, fields,
                        cColumnNames);
                testAnalytics.add(tA);
                cursor.moveToNext();
            }
            cColumnNames.clear();
            cColumnNames = null;
        }
        closeCursor(cursor);
        return testAnalytics;
    }

    public List<TestAnalytics> getUnSyncTestAnalytics(int limit) {

        List<TestAnalytics> testAnalytics = new ArrayList<TestAnalytics>();

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_ANALYTICS);
        sb.append("WHERE ");
        if (addBooleanEqualSQLQuery(ConstantGlobal.SYNCED, false, sb)) {
            sb.append(" AND ");
        }
        sb.append(ConstantGlobal.END_TIME).append("!='" + 0 + "' ");

        addLimitFilter(sb, ConstantGlobal.TIME_CREATED, SQLDBUtil.ORDER_DESC, SQLDBUtil.NO_START,
                limit);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                TestAnalytics tA = SQLDBUtil.convertToValues(cursor, TestAnalytics.class, null);
                testAnalytics.add(tA);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        return testAnalytics;
    }

    @SuppressWarnings("unchecked")
    public Future<List<TestBoardAnalytics>> getTestBoardAnalyticsFuture(final int orgKeyId,
                                                                        final String userId, final String entityId, final String entityType,
                                                                        final String parentId, final BoardType brdType) {

        return AsynExecutorService.getInstance().COMPLETION_SERVICE
                .submit(new Callable<List<TestBoardAnalytics>>() {

                    @Override
                    public List<TestBoardAnalytics> call() throws Exception {

                        return getTestBoardAnalytics(orgKeyId, userId, entityId, entityType,
                                parentId, brdType);
                    }
                });

    }

    public TestBoardAnalytics getTestSingleBoardAnalytics(int orgKeyId, String userId,
                                                          String entityId, String entityType, String brdId, String brdType, String parentId) {

        TestBoardAnalytics boardAnalytics = null;
        if (TextUtils.isEmpty(userId)) {
            return boardAnalytics;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_BOARD_ANALYTICS);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ID, brdId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.TYPE, brdType, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.PARENT_ID, parentId, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            boardAnalytics = SQLDBUtil.convertToValues(cursor, TestBoardAnalytics.class, null);
        }
        closeCursor(cursor);
        return boardAnalytics;
    }

    public ArrayList<TestBoardAnalytics> getTestBoardAnalytics(int orgKeyId, String userId,
                                                               String entityId, String entityType, String parentId, BoardType brdType) {

        return getTestBoardAnalytics(orgKeyId, userId, entityId, entityType, parentId, brdType,
                null, null);
    }

    public ArrayList<TestBoardAnalytics> getTestBoardAnalytics(int orgKeyId, String userId,
                                                               String entityId, String entityType, String parentId, BoardType brdType, String orderBy,
                                                               String sortOrder) {

        ArrayList<TestBoardAnalytics> boardAnalytics = new ArrayList<TestBoardAnalytics>();
        if (TextUtils.isEmpty(userId)) {
            return boardAnalytics;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_BOARD_ANALYTICS);
        sb.append("WHERE ");

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        if (brdType != null
                && addStringEqualSQLQuery(ConstantGlobal.TYPE, brdType.name(), sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.PARENT_ID, parentId, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        orderBy = orderBy == null ? ConstantGlobal.NAME : orderBy;
        sortOrder = sortOrder == null ? SQLDBUtil.ORDER_ASC : sortOrder;

        addLimitFilter(sb, orderBy, sortOrder, SQLDBUtil.NO_START, SQLDBUtil.NO_LIMIT);

        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                TestBoardAnalytics analytics = SQLDBUtil.convertToValues(cursor,
                        TestBoardAnalytics.class, null);
                boardAnalytics.add(analytics);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.v(TAG, "========boardAnalytics:  " + boardAnalytics);
        return boardAnalytics;
    }

    public ArrayList<TestBoardAnalytics> getTestBoardAnalyticsStrength(int orgKeyId, String userId,
                                                                       String entityId, String entityType, BoardType brdType, String parentId,
                                                                       int strengthType, String[] fields, int size) {

        if (strengthType != STRENGTH_TYPE_STRONG && strengthType != STRENGTH_TYPE_WEAK) {
            Log.d(TAG, "invalid strength type: " + strengthType);
            return null;
        }
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        ArrayList<TestBoardAnalytics> boardAnalytics = new ArrayList<TestBoardAnalytics>();
        String customFilterField = "(correct*100)/attempted ";

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_BOARD_ANALYTICS, fields, customFilterField + " as "
                + ConstantGlobal.ACCURACY);

        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        if (brdType != null) {
            addStringEqualSQLQuery(ConstantGlobal.TYPE, brdType.name(), sb, false);
            sb.append("AND ");
        }
        if (brdType == BoardType.TOPIC) {
            addStringEqualSQLQuery(ConstantGlobal.PARENT_ID, parentId, sb, false);
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        // start of strong or weak select query definition
        int filterValue = strengthType == STRENGTH_TYPE_STRONG ? 70 : 40;
        sb.append("AND ");
        sb.append(ConstantGlobal.ACCURACY);
        if (strengthType == STRENGTH_TYPE_STRONG) {
            sb.append(" > ").append(filterValue);
        } else {
            sb.append(" < ").append(filterValue);
        }
        sb.append(" ");
        // end of query definition
        String orderType = strengthType == STRENGTH_TYPE_STRONG ? SQLDBUtil.ORDER_DESC
                : SQLDBUtil.ORDER_ASC;
        addLimitFilter(sb, ConstantGlobal.ACCURACY, orderType, SQLDBUtil.NO_START, size);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            Set<String> cColumnNames = new HashSet<String>(Arrays.asList(cursor.getColumnNames()));
            while (!cursor.isAfterLast()) {
                TestBoardAnalytics analytics = SQLDBUtil.convertToValues(cursor,
                        TestBoardAnalytics.class, fields, cColumnNames);
                boardAnalytics.add(analytics);
                cursor.moveToNext();
            }
            cColumnNames.clear();
            cColumnNames = null;
        }
        closeCursor(cursor);
        return boardAnalytics;
    }

    public long createTestCourseLevelAnalytics(TestCourseLevelAnalytics courseLevelAnalytics) {

        try {
            ContentValues values = courseLevelAnalytics.toContentValues();
            return insert(TABLE_ENTITY_BOARD_LEVEL_ANALYTICS, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return -1;
    }

    public TestCourseLevelAnalytics getTestCourseLevelAnalytics(int orgKeyId, String userId,
                                                                String entityId, String entityType, String brdId, String brdType, QuestionLevel level) {

        TestCourseLevelAnalytics levelAnalytics = null;
        if (TextUtils.isEmpty(userId)) {
            return levelAnalytics;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_BOARD_LEVEL_ANALYTICS);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.LEVEL, level.name(), sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ID, brdId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.TYPE, brdType, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            levelAnalytics = SQLDBUtil
                    .convertToValues(cursor, TestCourseLevelAnalytics.class, null);
        }
        closeCursor(cursor);
        return levelAnalytics;
    }

    public List<TestCourseLevelAnalytics> getTestCourseLevelWiseAnalytics(int orgKeyId,
                                                                          String userId, String entityId, String entityType, String brdId, String brdType,
                                                                          String[] fields) {

        List<TestCourseLevelAnalytics> levelAnalytics = new ArrayList<TestCourseLevelAnalytics>();

        if (TextUtils.isEmpty(brdId) || TextUtils.isEmpty(userId) || TextUtils.isEmpty(entityId)) {
            return levelAnalytics;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_BOARD_LEVEL_ANALYTICS, fields);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ID, brdId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.TYPE, brdType, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        addLimitFilter(sb, ConstantGlobal.LEVEL, SQLDBUtil.ORDER_ASC, SQLDBUtil.NO_START,
                SQLDBUtil.NO_LIMIT);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            Set<String> cColumnNames = new HashSet<String>(Arrays.asList(cursor.getColumnNames()));
            while (!cursor.isAfterLast()) {
                TestCourseLevelAnalytics analytics = SQLDBUtil.convertToValues(cursor,
                        TestCourseLevelAnalytics.class, fields, cColumnNames);
                levelAnalytics.add(analytics);
                cursor.moveToNext();
            }
            cColumnNames.clear();
            cColumnNames = null;
        }
        closeCursor(cursor);
        return levelAnalytics;
    }

    public static Map<String, List<String>>
    getBoardToQidsMap(List<TestMetadata> testCoursesMetadata) {

        Map<String, List<String>> brdIdToQIdsMap = new LinkedHashMap<String, List<String>>();
        for (TestMetadata mData : testCoursesMetadata) {

            if (brdIdToQIdsMap.get(mData.id) == null) {
                brdIdToQIdsMap.put(mData.id, new ArrayList<String>());
            }
            brdIdToQIdsMap.get(mData.id).addAll(mData.qIds);
        }
        Log.d(TAG, "returning brdIdToQIdsMap:" + brdIdToQIdsMap);
        return brdIdToQIdsMap;
    }

    public LinkedHashMap<TestBoardAnalytics, List<TakeTestQuestionWithAnswerGiven>>
    getTestCourseWiseQuestionAnswerGivenMap(final List<TestMetadata> testCoursesMetadata,
                                            int orgKeyId, String userId, String entityId, String entityType) {

        if (testCoursesMetadata == null || TextUtils.isEmpty(entityId) || TextUtils.isEmpty(userId)) {
            Log.d(TAG, "entityId: " + entityId + ",userId:" + userId);
            return null;
        }
        LinkedHashMap<TestBoardAnalytics, List<TakeTestQuestionWithAnswerGiven>> boardWiseQuestionanswerGivenAnalytics = new LinkedHashMap<TestBoardAnalytics, List<TakeTestQuestionWithAnswerGiven>>();

        Future<List<TestBoardAnalytics>> futureBoardAnalytics = getTestBoardAnalyticsFuture(
                orgKeyId, userId, entityId, entityType, null, BoardType.COURSE);

        Map<String, List<String>> brdIdToQIdsMap = getBoardToQidsMap(testCoursesMetadata);
        List<String> qids = new ArrayList<String>();
        for (String key : brdIdToQIdsMap.keySet()) {
            if (brdIdToQIdsMap.get(key) != null) {
                qids.addAll(brdIdToQIdsMap.get(key));
            }
        }

        Map<String, TakeTestQuestionWithAnswerGiven> qidsToAnswerGivenAnalyticsMap = getQuestionWithAnswerAndQuestionMap(
                orgKeyId, userId, qids);
        if (qidsToAnswerGivenAnalyticsMap == null) {
            Log.d(TAG, "no qids to question and answer map found, qids:" + qids + ", userId:"
                    + userId);
            return null;
        }

        List<QuestionAnalytics> userQuestionAnalytics = getQuestionAnalytics(orgKeyId, userId,
                entityId, entityType, qids, new String[]{ConstantGlobal.ID,
                        ConstantGlobal.CORRECT, ConstantGlobal.ANSWER_GIVEN, ConstantGlobal.SCORE,
                        ConstantGlobal.TIME_TAKEN, "qusNo"});
        if (userQuestionAnalytics != null) {

            for (QuestionAnalytics qA : userQuestionAnalytics) {

                TakeTestQuestionWithAnswerGiven ansGiven = qidsToAnswerGivenAnalyticsMap.get(qA.id);
                if (ansGiven != null) {
                    ansGiven.answerGiven = qA.answerGiven;
                    ansGiven.correct = qA.correct;
                    ansGiven.setTimeTaken(qA.timeTaken);
                    ansGiven.setScore(qA.score);
                    ansGiven.setQusNo(qA.qusNo);
                    ansGiven.setStatus(AttemptStatus.SAVED);
                }
            }
        }
        Log.d(TAG, "userQuestionAnalytics: " + userQuestionAnalytics);
        List<TestBoardAnalytics> boardAnalytics = null;
        try {
            boardAnalytics = futureBoardAnalytics.get();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (boardAnalytics != null) {
            Map<String, TestBoardAnalytics> brdIdToBoardAnalyticMap = new HashMap<String, TestBoardAnalytics>();
            for (TestBoardAnalytics brdAnalytics : boardAnalytics) {
                brdIdToBoardAnalyticMap.put(brdAnalytics.id, brdAnalytics);
            }
            int qusNo = 1;
            for (Entry<String, List<String>> entry : brdIdToQIdsMap.entrySet()) {

                List<String> brdQids = entry.getValue();
                TestBoardAnalytics brdAnalytics = brdIdToBoardAnalyticMap.get(entry.getKey());

                List<TakeTestQuestionWithAnswerGiven> brdWiseAnswerGivenList = boardWiseQuestionanswerGivenAnalytics
                        .get(brdAnalytics);
                if (brdQids != null) {
                    if (brdWiseAnswerGivenList == null) {
                        brdWiseAnswerGivenList = new ArrayList<TakeTestQuestionWithAnswerGiven>();
                        boardWiseQuestionanswerGivenAnalytics.put(brdAnalytics,
                                brdWiseAnswerGivenList);
                    }

                    for (String qid : brdQids) {
                        if (qidsToAnswerGivenAnalyticsMap.get(qid) != null) {
                            brdWiseAnswerGivenList.add(qidsToAnswerGivenAnalyticsMap.get(qid));
                            if (qidsToAnswerGivenAnalyticsMap.get(qid).getQusNo() < 1) {
                                qidsToAnswerGivenAnalyticsMap.get(qid).setQusNo(qusNo);
                            }
                            qusNo++;
                        }
                    }
                    Collections.sort(brdWiseAnswerGivenList, new TestQuestionNoListComparator());
                }
            }

            brdIdToBoardAnalyticMap.clear();
            boardAnalytics.clear();
            brdIdToBoardAnalyticMap = null;
            boardAnalytics = null;
        }
        Log.d(TAG, "returning boardWiseQuestionanswerGivenAnalytics: "
                + boardWiseQuestionanswerGivenAnalytics);
        return boardWiseQuestionanswerGivenAnalytics;
    }

    public Map<String, TakeTestQuestionWithAnswerGiven> getQuestionWithAnswerAndQuestionMap(
            int orgKeyId, String userId, List<String> qids) {

        if (TextUtils.isEmpty(userId) || qids == null || qids.isEmpty()) {
            return null;
        }

        Map<String, TakeTestQuestionWithAnswerGiven> qidToQuestionAnalyticsAnswerGivenMap = new HashMap<String, TakeTestQuestionWithAnswerGiven>();

        StringBuilder sb = new StringBuilder();
        sb.append("select q.id, q.type,q.orgKeyId, a.answer, a.matrixAnswer from ")
                .append(ContentDataManager.TABLE_QUESTION).append(" as q ");

        sb.append("left join ").append(ContentDataManager.TABLE_ANSWER).append(" as a ");
        sb.append("on q.id=a.qId and q.userId=a.userId and q.orgKeyId=a.orgKeyId ");
        sb.append("where ");
        if (addStringEqualSQLQuery("q." + ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append(" AND ");
        }
        addIntEqualSQLQuery("q." + ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        sb.append(" AND ");

        String sqlQids = LocalManager.joinString(qids, "'");
        sb.append("q.id in (").append(sqlQids).append("); ");
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                TakeTestQuestionWithAnswerGiven qAnsGiven = new TakeTestQuestionWithAnswerGiven();
                qAnsGiven.qId = SQLDBUtil.getString(cursor, ConstantGlobal.ID);
                qAnsGiven.type = QuestionType.valueOfKey(SQLDBUtil.getString(cursor,
                        ConstantGlobal.TYPE));
                qAnsGiven.orgKeyId = SQLDBUtil.getInteger(cursor, ConstantGlobal.ORG_KEY_ID);
                qAnsGiven.correctAnswer = SQLDBUtil.getString(cursor, ConstantGlobal.ANSWER);
                qAnsGiven.correctMatrixAnswer = SQLDBUtil.getString(cursor, "matrixAnswer");

                qidToQuestionAnalyticsAnswerGivenMap.put(qAnsGiven.qId, qAnsGiven);
                cursor.moveToNext();
            }
        }

        closeCursor(cursor);
        return qidToQuestionAnalyticsAnswerGivenMap;
    }

    public QuestionAnalytics getQuestionAnalytics(int orgKeyId, String userId, String entityId,
                                                  String entityType, String qid) {

        QuestionAnalytics quesitonAnalytics = null;
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_QUESTION_ANALYTICS, null, null);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ID, qid, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            quesitonAnalytics = SQLDBUtil.convertToValues(cursor, QuestionAnalytics.class, null);
        }
        closeCursor(cursor);
        return quesitonAnalytics;
    }

    public Map<String, QuestionAnalytics> getQuestionAnalyticsMap(int orgKeyId, String userId,
                                                                  String entityId, String entityType, List<String> qids, String[] fields) {

        Map<String, QuestionAnalytics> qusAnalyticsMap = new HashMap<String, QuestionAnalytics>();
        List<QuestionAnalytics> questionsAnalytics = getQuestionAnalytics(orgKeyId, userId,
                entityId, entityType, qids, fields);
        for (QuestionAnalytics qAnalytics : questionsAnalytics) {
            qusAnalyticsMap.put(qAnalytics.id, qAnalytics);
        }
        return qusAnalyticsMap;
    }

    public List<QuestionAnalytics> getQuestionAnalytics(int orgKeyId, String userId,
                                                        String entityId, String entityType, List<String> qids, String[] fields) {

        List<QuestionAnalytics> quesitonAnalytics = new ArrayList<QuestionAnalytics>();
        if (TextUtils.isEmpty(userId) || qids == null || qids.isEmpty()) {
            return quesitonAnalytics;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_QUESTION_ANALYTICS, fields, null);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        sb.append("AND ");
        sb.append(ConstantGlobal.ID).append(" in (");
        sb.append(LocalManager.joinString(qids, "'"));
        sb.append(") ");
        addLimitFilter(sb, "qusNo", SQLDBUtil.ORDER_ASC, SQLDBUtil.NO_START, SQLDBUtil.NO_LIMIT);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            Set<String> cColumnNames = new HashSet<String>(Arrays.asList(cursor.getColumnNames()));
            while (!cursor.isAfterLast()) {
                QuestionAnalytics qA = SQLDBUtil.convertToValues(cursor, QuestionAnalytics.class,
                        fields, cColumnNames);
                quesitonAnalytics.add(qA);
                cursor.moveToNext();
            }
            cColumnNames.clear();
            cColumnNames = null;
        }
        closeCursor(cursor);
        return quesitonAnalytics;
    }

    @Override
    public void createDummyData() {

    }

    public int getTotalTestAttemptedCount(int orgKeyId, String userId) {

        if (TextUtils.isEmpty(userId)) {
            return -1;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(entityId) as count from ").append(TABLE_ENTITY_ANALYTICS)
                .append(" ");
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        int count = 0;
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        closeCursor(cursor);
        return count;
    }

    public int removeQuestionAnalytics(QuestionAnalytics questionAnalytics) {

        ContentValues values = null;
        try {
            values = questionAnalytics.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }

        return delete(TABLE_QUESTION_ANALYTICS, "_id=" + questionAnalytics._id, null);

    }

    public int updateQuestionAnalytics(QuestionAnalytics questionAnalytics) {

        ContentValues values = null;
        try {
            values = questionAnalytics.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE_QUESTION_ANALYTICS, values, "_id=" + questionAnalytics._id, null);
    }

    public int updateTestAnalytics(TestAnalytics testAnalytics) {

        return updateTestAnalytics(testAnalytics, false);
    }

    public synchronized int updateTestAnalytics(TestAnalytics testAnalytics, boolean sync) {

        ContentValues values = null;
        try {
            values = testAnalytics.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        int id = update(TABLE_ENTITY_ANALYTICS, values, "_id=" + testAnalytics._id, null);
        if (sync) {
            sync();
        }
        return id;
    }

    public int updateTestBoardAnalytics(TestBoardAnalytics testBoardAnalytics) {

        ContentValues values = null;
        try {
            values = testBoardAnalytics.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE_ENTITY_BOARD_ANALYTICS, values, "_id=" + testBoardAnalytics._id, null);
    }

    public int updateCourseLevelAnalytics(TestCourseLevelAnalytics levelAnalytics) {

        ContentValues values = null;
        try {
            values = levelAnalytics.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE_ENTITY_BOARD_LEVEL_ANALYTICS, values, "_id=" + levelAnalytics._id, null);
    }

    public void createQuestionAnalytics(Content test,
                                        TakeTestQuestionWithAnswerGiven questionAnsIfo, String userId) {

        if (TextUtils.isEmpty(userId)) {
            return;
        }

        Log.v(TAG, "questionAnsIfo.answerGiven : " + questionAnsIfo.answerGiven);
        Log.e(TAG, "contentQuestion : " + test.toString());
        if (questionAnsIfo.type == QuestionType.MCQ) {
            List<String> ans = new ArrayList<String>(Arrays.asList(TextUtils.split(
                    questionAnsIfo.answerGiven, SQLDBUtil.SEPARATOR)));
            Collections.sort(ans);
            questionAnsIfo.answerGiven = TextUtils.join(SQLDBUtil.SEPARATOR, ans.toArray());
        }

        QuestionAnalytics qAnalytics = new QuestionAnalytics(test.orgKeyId, userId, test.id,
                test.type, questionAnsIfo.getScore(), questionAnsIfo.getTimeTaken(),
                questionAnsIfo.qId, questionAnsIfo.correct, questionAnsIfo.answerGiven,
                questionAnsIfo.getQusNo());
        qAnalytics.synced = questionAnsIfo.isSynced();
        try {
            ContentValues values = qAnalytics.toContentValues();
            insert(TABLE_QUESTION_ANALYTICS, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void createTestBoardAnalytics(TestBoardAnalytics boardAnalytics) {

        try {
            ContentValues values = boardAnalytics.toContentValues();
            insert(TABLE_ENTITY_BOARD_ANALYTICS, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void createAttempt(TestBoardAnalytics boardAnalytics) {

        try {
            ContentValues values = boardAnalytics.toContentValues();
            insert(TABLE_ENTITY_BOARD_ANALYTICS, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void updateEntityAttemptTimeTaken(int orgKeyId, String userId, String entityType,
                                             String entityId, boolean remove, int value/** timeTaken in seconds **/
    ) {

        String key = getEntityAttemptTimeTakenKey(userId, entityType, entityId);
        AttemptTimeTaken attemptTimeTaken = new AttemptTimeTaken(orgKeyId, value, key);
        ContentValues values = null;
        try {
            values = attemptTimeTaken.toContentValues();
            if (remove) {
                delete(TABLE_ATTEMPT_TIME_TAKEN, "key='" + key + "'", null);
            } else {
                int updated = update(TABLE_ATTEMPT_TIME_TAKEN, values, "key='" + key + "'", null);
                if (updated <= 0) {
                    try {
                        insert(TABLE_ATTEMPT_TIME_TAKEN, values);
                    } catch (Throwable et) {
                        Log.e(TAG, et.getMessage(), et);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public int getEntityAttemptTimeTaken(int orgKeyId, String userId, String entityType,
                                         String entityId) {

        int value = 0;
        String key = getEntityAttemptTimeTakenKey(userId, entityType, entityId);
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ATTEMPT_TIME_TAKEN);
        sb.append("WHERE ");
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        sb.append(" AND ");
        addStringEqualSQLQuery(ConstantGlobal.KEY, key, sb, false);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            value = cursor.getInt(cursor.getColumnIndex(ConstantGlobal.TIME_TAKEN));
            closeCursor(cursor);
        }
        return value;
    }

    private String getEntityAttemptTimeTakenKey(String userId, String entityType, String entityId) {

        return "timeTaken_" + TextUtils.join("/", new String[]{userId, entityType, entityId});
    }

    @Override
    public void cleanData() {

        Log.d(TAG, "cleaning analytics content");
        try {
            execSQL("DROP TABLE " + TABLE_QUESTION_ANALYTICS);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        try {
            execSQL("DROP TABLE " + TABLE_ENTITY_ANALYTICS);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        try {
            execSQL("DROP TABLE " + TABLE_ENTITY_BOARD_ANALYTICS);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        try {
            execSQL("DROP TABLE " + TABLE_ENTITY_BOARD_LEVEL_ANALYTICS);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        try {
            execSQL("DROP TABLE " + TABLE_ATTEMPT_TIME_TAKEN);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void createTable(List<String> createTables) {

        createTestAnalyticsTable(createTables);
        createBoardAnalyticsTable(createTables);
        createTables.add(createCourseLevelAnalyticsTable());
        createQuestionAnalyticsTable(createTables);
        createTables.add(createTableAttemptTimeTaken());
    }

    private static String createTableAttemptTimeTaken() {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE_ATTEMPT_TIME_TAKEN);
        addAbstractAbstractDataModelFeildsRow(sb);

        sb.append(ConstantGlobal.KEY).append(" text not null,");
        sb.append(ConstantGlobal.TIME_TAKEN).append(" integer");
        endCreateTableQuery(sb);
        return sb.toString();
    }

    private static void createTestAnalyticsTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE_ENTITY_ANALYTICS);

        addAbstractTestAnalyticsFeildsRow(sb);
        sb.append(ConstantGlobal.DURATION).append(" integer,");
        sb.append(ConstantGlobal.END_TIME).append(" text not null,");
        sb.append(ConstantGlobal.SYNCED).append(" integer");

        endCreateTableQuery(sb);
        createTables.add(sb.toString());
        createTables.add(createIndexQuery(TABLE_ENTITY_ANALYTICS, "index_entity_analytics", true,
                ConstantGlobal.ORG_KEY_ID, ConstantGlobal.USER_ID, ConstantGlobal.ENTITY_ID,
                ConstantGlobal.ENTITY_TYPE));
    }

    private static void createBoardAnalyticsTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE_ENTITY_BOARD_ANALYTICS);

        sb.append(ConstantGlobal.ID).append(" text not null,");
        sb.append(ConstantGlobal.TYPE).append(" text not null,");
        sb.append(ConstantGlobal.NAME).append(" text not null,");

        addAbstractTestAnalyticsFeildsRow(sb);
        sb.append(ConstantGlobal.PARENT_ID).append(" text, ");
        // no unique constant is defined
        addUniqueFields(sb, new String[]{ConstantGlobal.ORG_KEY_ID, ConstantGlobal.USER_ID,
                ConstantGlobal.ENTITY_ID, ConstantGlobal.ENTITY_TYPE, ConstantGlobal.ID,
                ConstantGlobal.TYPE, ConstantGlobal.PARENT_ID});
        endCreateTableQuery(sb);
        createTables.add(sb.toString());
    }

    private static String createCourseLevelAnalyticsTable() {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE_ENTITY_BOARD_LEVEL_ANALYTICS);

        sb.append(ConstantGlobal.ID).append(" text not null,");
        sb.append(ConstantGlobal.TYPE).append(" text not null,");
        sb.append(ConstantGlobal.NAME).append(" text not null,");

        addAbstractTestAnalyticsFeildsRow(sb);
        sb.append(ConstantGlobal.LEVEL).append(" text not null");
        // no unique constant is defined

        endCreateTableQuery(sb);
        return sb.toString();
    }

    private static void createQuestionAnalyticsTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE_QUESTION_ANALYTICS);

        sb.append(ConstantGlobal.ID).append(" text not null,");
        sb.append(ConstantGlobal.ENTITY_ID).append(" text not null,");
        sb.append(ConstantGlobal.ENTITY_TYPE).append(" text not null,");
        sb.append(ConstantGlobal.ANSWER_GIVEN).append(" text,");
        addAbstractAnalyticsFeildsRow(sb);
        sb.append(ConstantGlobal.CORRECT).append(" integer,");
        sb.append("qusNo").append(" integer,");
        sb.append(ConstantGlobal.SYNCED).append(" integer");
        // no unique constant is defined
        endCreateTableQuery(sb);
        createTables.add(sb.toString());
        createTables.add(createIndexQuery(TABLE_QUESTION_ANALYTICS, "question_analytics_index",
                true, ConstantGlobal.ORG_KEY_ID, ConstantGlobal.USER_ID, ConstantGlobal.ENTITY_ID,
                ConstantGlobal.ENTITY_TYPE, ConstantGlobal.ID));

    }

    private static void addAbstractTestAnalyticsFeildsRow(StringBuilder sb) {

        sb.append(ConstantGlobal.ENTITY_ID).append(" text not null,");
        sb.append(ConstantGlobal.ENTITY_TYPE).append(" text not null,");
        sb.append(ConstantGlobal.TOTAL_MARKS).append(" integer,");
        sb.append(ConstantGlobal.QUS_COUNT).append(" integer,");
        sb.append(ConstantGlobal.ATTEMPTED).append(" integer,");
        sb.append(ConstantGlobal.CORRECT).append(" integer,");
        addAbstractAnalyticsFeildsRow(sb);
    }

    private static void addAbstractAnalyticsFeildsRow(StringBuilder sb) {

        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.USER_ID).append(" text not null,");
        sb.append(ConstantGlobal.SCORE).append(" integer,");
        sb.append(ConstantGlobal.TIME_TAKEN).append(" integer,");
    }

    @Override
    public void sync() {

        super.triggerSync();

    }
    @Override
    public void assignmentSync() {

        super.assignmentTriggerSync();

    }

    //Himank Shah
    public AssignmentAnalytics getAssignmentAnalytics(int orgKeyId, String userId, String entityId,
                                                      String entityType) {

        AssignmentAnalytics analytics = null;
        if (TextUtils.isEmpty(entityId) || TextUtils.isEmpty(userId)
                || TextUtils.isEmpty(entityType)) {
            Log.e(TAG, "empty entityId : " + entityId + ", or userId: " + userId);
            return analytics;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_ANALYTICS);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            Log.d(TAG, "total count: " + cursor.getCount());
            analytics = SQLDBUtil.convertToValues(cursor, AssignmentAnalytics.class, null);
        }
        closeCursor(cursor);

        return analytics;
    }

    public void createAssignmentAnalytics(Content assignment, AssignmentExtendedInfo info, String userId) {

        AssignmentAnalytics tA = getAssignmentAnalytics(assignment.orgKeyId, userId, assignment.id, assignment.type);
        if (tA != null) {
            Log.d(TAG, "assignment analytics already present for assignment:" + assignment);
            return;
        }
        tA = new AssignmentAnalytics(assignment.orgKeyId, userId, 0, -1, assignment.id, assignment.type, info.totalMarks,
                info.qusCount, 0, 0, info.duration);
        createAssignmentAnalytics(tA);

    }

    public void createAssignmentAnalytics(AssignmentAnalytics assignmentAnalytics) {

        try {
            ContentValues values = assignmentAnalytics.toContentValues();
            insert(TABLE_ENTITY_ANALYTICS, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public int updateAssignmentAnalytics(AssignmentAnalytics assignmentAnalytics) {

        return updateAssignmentAnalytics(assignmentAnalytics, false);
    }

    public synchronized int updateAssignmentAnalytics(AssignmentAnalytics assignmentAnalytics, boolean sync) {

        ContentValues values = null;
        try {
            values = assignmentAnalytics.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        int id = update(TABLE_ENTITY_ANALYTICS, values, "_id=" + assignmentAnalytics._id, null);
        if (sync) {
            assignmentSync();
        }
        return id;
    }

    public AssignmentBoardAnalytics getAssignmentSingleBoardAnalytics(int orgKeyId, String userId,
                                                                      String entityId, String entityType, String brdId, String brdType, String parentId) {

        AssignmentBoardAnalytics boardAnalytics = null;
        if (TextUtils.isEmpty(userId)) {
            return boardAnalytics;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_BOARD_ANALYTICS);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ID, brdId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.TYPE, brdType, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.PARENT_ID, parentId, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            boardAnalytics = SQLDBUtil.convertToValues(cursor, AssignmentBoardAnalytics.class, null);
        }
        closeCursor(cursor);
        return boardAnalytics;
    }

    public void createAssignmentBoardAnalytics(AssignmentBoardAnalytics boardAnalytics) {

        try {
            ContentValues values = boardAnalytics.toContentValues();
            insert(TABLE_ENTITY_BOARD_ANALYTICS, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public int updateAssignmentBoardAnalytics(AssignmentBoardAnalytics assignmentBoardAnalytics) {

        ContentValues values = null;
        try {
            values = assignmentBoardAnalytics.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE_ENTITY_BOARD_ANALYTICS, values, "_id=" + assignmentBoardAnalytics._id, null);
    }

    public AssignmentCourseLevelAnalytics getAssignmentCourseLevelAnalytics(int orgKeyId, String userId,
                                                                            String entityId, String entityType, String brdId, String brdType, QuestionLevel level) {

        AssignmentCourseLevelAnalytics levelAnalytics = null;
        if (TextUtils.isEmpty(userId)) {
            return levelAnalytics;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_BOARD_LEVEL_ANALYTICS);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.LEVEL, level.name(), sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ID, brdId, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.TYPE, brdType, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            levelAnalytics = SQLDBUtil
                    .convertToValues(cursor, AssignmentCourseLevelAnalytics.class, null);
        }
        closeCursor(cursor);
        return levelAnalytics;
    }

    public long createAssignmentCourseLevelAnalytics(AssignmentCourseLevelAnalytics courseLevelAnalytics) {

        try {
            ContentValues values = courseLevelAnalytics.toContentValues();
            return insert(TABLE_ENTITY_BOARD_LEVEL_ANALYTICS, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return -1;
    }
    public int updateCourseLevelAnalytics(AssignmentCourseLevelAnalytics levelAnalytics) {

        ContentValues values = null;
        try {
            values = levelAnalytics.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE_ENTITY_BOARD_LEVEL_ANALYTICS, values, "_id=" + levelAnalytics._id, null);
    }

    public void createQuestionAnalytics(Content assignment,
                                        TakeAssignmentQuestionWithAnswerGiven questionAnsIfo, String userId) {

        if (TextUtils.isEmpty(userId)) {
            return;
        }
        Log.v(TAG, "questionAnsIfo.answerGiven : " + questionAnsIfo.answerGiven);
        if (questionAnsIfo.type == QuestionType.MCQ) {
            List<String> ans = new ArrayList<String>(Arrays.asList(TextUtils.split(
                    questionAnsIfo.answerGiven, SQLDBUtil.SEPARATOR)));
            Collections.sort(ans);
            questionAnsIfo.answerGiven = TextUtils.join(SQLDBUtil.SEPARATOR, ans.toArray());
        }

        QuestionAnalytics qAnalytics = new QuestionAnalytics(assignment.orgKeyId, userId, assignment.id,
                assignment.type, questionAnsIfo.getScore(), questionAnsIfo.getTimeTaken(),
                questionAnsIfo.qId, questionAnsIfo.correct, questionAnsIfo.answerGiven,
                questionAnsIfo.getQusNo());
        qAnalytics.synced = questionAnsIfo.isSynced();
        try {
            ContentValues values = qAnalytics.toContentValues();
            insert(TABLE_QUESTION_ANALYTICS, values);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public Map<String, TakeAssignmentQuestionWithAnswerGiven> getAssignmentQuestionWithAnswerAndQuestionMap(
            int orgKeyId, String userId, List<String> qids) {

        if (TextUtils.isEmpty(userId) || qids == null || qids.isEmpty()) {
            return null;
        }

        Map<String, TakeAssignmentQuestionWithAnswerGiven> qidToQuestionAnalyticsAnswerGivenMap = new HashMap<String, TakeAssignmentQuestionWithAnswerGiven>();

        StringBuilder sb = new StringBuilder();
        sb.append("select q.id, q.type,q.orgKeyId, a.answer, a.matrixAnswer from ")
                .append(ContentDataManager.TABLE_QUESTION).append(" as q ");

        sb.append("left join ").append(ContentDataManager.TABLE_ANSWER).append(" as a ");
        sb.append("on q.id=a.qId and q.userId=a.userId and q.orgKeyId=a.orgKeyId ");
        sb.append("where ");
        if (addStringEqualSQLQuery("q." + ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append(" AND ");
        }
        addIntEqualSQLQuery("q." + ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        sb.append(" AND ");

        String sqlQids = LocalManager.joinString(qids, "'");
        sb.append("q.id in (").append(sqlQids).append("); ");
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                TakeAssignmentQuestionWithAnswerGiven qAnsGiven = new TakeAssignmentQuestionWithAnswerGiven();
                qAnsGiven.qId = SQLDBUtil.getString(cursor, ConstantGlobal.ID);
                qAnsGiven.type = QuestionType.valueOfKey(SQLDBUtil.getString(cursor,
                        ConstantGlobal.TYPE));
                qAnsGiven.orgKeyId = SQLDBUtil.getInteger(cursor, ConstantGlobal.ORG_KEY_ID);
                qAnsGiven.correctAnswer = SQLDBUtil.getString(cursor, ConstantGlobal.ANSWER);
                qAnsGiven.correctMatrixAnswer = SQLDBUtil.getString(cursor, "matrixAnswer");

                qidToQuestionAnalyticsAnswerGivenMap.put(qAnsGiven.qId, qAnsGiven);
                cursor.moveToNext();
            }
        }

        closeCursor(cursor);
        return qidToQuestionAnalyticsAnswerGivenMap;
    }
    public void createAssignmentCourseAnalytics(Content assignment, List<AssignmentMetadata> assignmentCourseMetadata,
                                                String userId) {

        if (assignmentCourseMetadata != null) {
            for (AssignmentMetadata cMData : assignmentCourseMetadata) {
                createAssignmentCourseAnalytics(assignment, cMData, userId);
            }
        }
    }
    private void createAssignmentCourseAnalytics(Content assignment, AssignmentMetadata cMData, String userId) {

        if (TextUtils.isEmpty(userId)) {
            return;
        }

        AssignmentBoardAnalytics courseAnalytics = getAssignmentSingleBoardAnalytics(assignment.orgKeyId, userId,
                assignment.id, assignment.type, cMData.id, BoardType.COURSE.name(), null);
        if (courseAnalytics != null) {
            return;
        }

        courseAnalytics = new AssignmentBoardAnalytics(assignment.orgKeyId, userId, 0, 0, assignment.id, assignment.type,
                cMData.totalMarks, cMData.qusCount, 0, 0, cMData.id, BoardType.COURSE.name(),
                cMData.name, null);
        try {
            ContentValues values = courseAnalytics.toContentValues();
            insert(TABLE_ENTITY_BOARD_ANALYTICS, values);
            createAssignmentTopicAnalytics(assignment, (AssignmentExtendedInfo) assignment.toContentExtendedInfo(),
                    cMData.id, cMData.children, userId);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void createAssignmentTopicAnalytics(Content assignment, AssignmentExtendedInfo assignmentInfo, String parentId,
                                                List<BoardQus> topicsMetadata, String userId) {

        for (BoardQus tMData : topicsMetadata) {

            AssignmentBoardAnalytics topicAnalytics = getAssignmentSingleBoardAnalytics(assignment.orgKeyId, userId,
                    assignment.id, assignment.type, tMData.id, BoardType.TOPIC.name(), parentId);

            if (topicAnalytics != null) {
                continue;
            }

            topicAnalytics = new AssignmentBoardAnalytics(assignment.orgKeyId, userId, 0, 0, assignment.id,
                    assignment.type, tMData.totalMarks, tMData.qusCount, 0, 0, tMData.id,
                    BoardType.TOPIC.name(), tMData.name, parentId);
            try {
                ContentValues values = topicAnalytics.toContentValues();
                insert(TABLE_ENTITY_BOARD_ANALYTICS, values);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    public LinkedHashMap<AssignmentBoardAnalytics, List<TakeAssignmentQuestionWithAnswerGiven>>
    getAssignmentCourseWiseQuestionAnswerGivenMap(final List<AssignmentMetadata> assignmentCoursesMetadata,
                                                  int orgKeyId, String userId, String entityId, String entityType) {

        if (assignmentCoursesMetadata == null || TextUtils.isEmpty(entityId) || TextUtils.isEmpty(userId)) {
            Log.d(TAG, "entityId: " + entityId + ",userId:" + userId);
            return null;
        }
        LinkedHashMap<AssignmentBoardAnalytics, List<TakeAssignmentQuestionWithAnswerGiven>> boardWiseQuestionanswerGivenAnalytics = new LinkedHashMap<AssignmentBoardAnalytics, List<TakeAssignmentQuestionWithAnswerGiven>>();

        Future<List<AssignmentBoardAnalytics>> futureBoardAnalytics = getAssignmentBoardAnalyticsFuture(
                orgKeyId, userId, entityId, entityType, null, BoardType.COURSE);

        Map<String, List<String>> brdIdToQIdsMap = getAssignmentBoardToQidsMap(assignmentCoursesMetadata);
        List<String> qids = new ArrayList<String>();
        for (String key : brdIdToQIdsMap.keySet()) {
            if (brdIdToQIdsMap.get(key) != null) {
                qids.addAll(brdIdToQIdsMap.get(key));
            }
        }

        Map<String, TakeAssignmentQuestionWithAnswerGiven> qidsToAnswerGivenAnalyticsMap = getAssignmentQuestionWithAnswerAndQuestionMap(
                orgKeyId, userId, qids);
        if (qidsToAnswerGivenAnalyticsMap == null) {
            Log.d(TAG, "no qids to question and answer map found, qids:" + qids + ", userId:"
                    + userId);
            return null;
        }

        List<QuestionAnalytics> userQuestionAnalytics = getQuestionAnalytics(orgKeyId, userId,
                entityId, entityType, qids, new String[]{ConstantGlobal.ID,
                        ConstantGlobal.CORRECT, ConstantGlobal.ANSWER_GIVEN, ConstantGlobal.SCORE,
                        ConstantGlobal.TIME_TAKEN, "qusNo"});
        if (userQuestionAnalytics != null) {

            for (QuestionAnalytics qA : userQuestionAnalytics) {

                TakeAssignmentQuestionWithAnswerGiven ansGiven = qidsToAnswerGivenAnalyticsMap.get(qA.id);
                if (ansGiven != null) {
                    ansGiven.answerGiven = qA.answerGiven;
                    ansGiven.correct = qA.correct;
                    ansGiven.setTimeTaken(qA.timeTaken);
                    ansGiven.setScore(qA.score);
                    ansGiven.setQusNo(qA.qusNo);
                    ansGiven.setStatus(AssignementAttemptStatus.SAVED);
                }
            }
        }
        Log.d(TAG, "userQuestionAnalytics: " + userQuestionAnalytics);
        List<AssignmentBoardAnalytics> boardAnalytics = null;
        try {
            boardAnalytics = futureBoardAnalytics.get();
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (boardAnalytics != null) {
            Map<String, AssignmentBoardAnalytics> brdIdToBoardAnalyticMap = new HashMap<String, AssignmentBoardAnalytics>();
            for (AssignmentBoardAnalytics brdAnalytics : boardAnalytics) {
                brdIdToBoardAnalyticMap.put(brdAnalytics.id, brdAnalytics);
            }
            int qusNo = 1;
            for (Entry<String, List<String>> entry : brdIdToQIdsMap.entrySet()) {

                List<String> brdQids = entry.getValue();
                AssignmentBoardAnalytics brdAnalytics = brdIdToBoardAnalyticMap.get(entry.getKey());

                List<TakeAssignmentQuestionWithAnswerGiven> brdWiseAnswerGivenList = boardWiseQuestionanswerGivenAnalytics
                        .get(brdAnalytics);
                if (brdQids != null) {
                    if (brdWiseAnswerGivenList == null) {
                        brdWiseAnswerGivenList = new ArrayList<TakeAssignmentQuestionWithAnswerGiven>();
                        boardWiseQuestionanswerGivenAnalytics.put(brdAnalytics,
                                brdWiseAnswerGivenList);
                    }

                    for (String qid : brdQids) {
                        if (qidsToAnswerGivenAnalyticsMap.get(qid) != null) {
                            brdWiseAnswerGivenList.add(qidsToAnswerGivenAnalyticsMap.get(qid));
                            if (qidsToAnswerGivenAnalyticsMap.get(qid).getQusNo() < 1) {
                                qidsToAnswerGivenAnalyticsMap.get(qid).setQusNo(qusNo);
                            }
                            qusNo++;
                        }
                    }
                    Collections.sort(brdWiseAnswerGivenList, new AssignmentQuestionNoListComparator());
                }
            }

            brdIdToBoardAnalyticMap.clear();
            boardAnalytics.clear();
            brdIdToBoardAnalyticMap = null;
            boardAnalytics = null;
        }
        Log.d(TAG, "returning boardWiseQuestionanswerGivenAnalytics: "
                + boardWiseQuestionanswerGivenAnalytics);
        return boardWiseQuestionanswerGivenAnalytics;
    }

    @SuppressWarnings("unchecked")
    public Future<List<AssignmentBoardAnalytics>> getAssignmentBoardAnalyticsFuture(final int orgKeyId,
                                                                                    final String userId, final String entityId, final String entityType,
                                                                                    final String parentId, final BoardType brdType) {

        return AsynExecutorService.getInstance().COMPLETION_SERVICE
                .submit(new Callable<List<AssignmentBoardAnalytics>>() {

                    @Override
                    public List<AssignmentBoardAnalytics> call() throws Exception {

                        return getAssignmentBoardAnalytics(orgKeyId, userId, entityId, entityType,
                                parentId, brdType);
                    }
                });

    }

    public ArrayList<AssignmentBoardAnalytics> getAssignmentBoardAnalytics(int orgKeyId, String userId,
                                                                           String entityId, String entityType, String parentId, BoardType brdType) {

        return getAssignmentBoardAnalytics(orgKeyId, userId, entityId, entityType, parentId, brdType,
                null, null);
    }

    public ArrayList<AssignmentBoardAnalytics> getAssignmentBoardAnalytics(int orgKeyId, String userId,
                                                                           String entityId, String entityType, String parentId, BoardType brdType, String orderBy,
                                                                           String sortOrder) {

        ArrayList<AssignmentBoardAnalytics> boardAnalytics = new ArrayList<AssignmentBoardAnalytics>();
        if (TextUtils.isEmpty(userId)) {
            return boardAnalytics;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_BOARD_ANALYTICS);
        sb.append("WHERE ");

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        if (brdType != null
                && addStringEqualSQLQuery(ConstantGlobal.TYPE, brdType.name(), sb, false)) {
            sb.append("AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.PARENT_ID, parentId, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        orderBy = orderBy == null ? ConstantGlobal.NAME : orderBy;
        sortOrder = sortOrder == null ? SQLDBUtil.ORDER_ASC : sortOrder;

        addLimitFilter(sb, orderBy, sortOrder, SQLDBUtil.NO_START, SQLDBUtil.NO_LIMIT);

        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                AssignmentBoardAnalytics analytics = SQLDBUtil.convertToValues(cursor,
                        AssignmentBoardAnalytics.class, null);
                boardAnalytics.add(analytics);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.v(TAG, "========boardAnalytics:  " + boardAnalytics);
        return boardAnalytics;
    }
    public static Map<String, List<String>>
    getAssignmentBoardToQidsMap(List<AssignmentMetadata> assignmentCoursesMetadata) {

        Map<String, List<String>> brdIdToQIdsMap = new LinkedHashMap<String, List<String>>();
        for (AssignmentMetadata mData : assignmentCoursesMetadata) {

            if (brdIdToQIdsMap.get(mData.id) == null) {
                brdIdToQIdsMap.put(mData.id, new ArrayList<String>());
            }
            brdIdToQIdsMap.get(mData.id).addAll(mData.qIds);
        }
        Log.d(TAG, "returning brdIdToQIdsMap:" + brdIdToQIdsMap);
        return brdIdToQIdsMap;
    }

    public List<AssignmentAnalytics> getUnSyncAssignmentAnalytics(int limit) {

        List<AssignmentAnalytics> assignmentAnalytics = new ArrayList<AssignmentAnalytics>();

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_ANALYTICS);
        sb.append("WHERE ");
        if (addBooleanEqualSQLQuery(ConstantGlobal.SYNCED, false, sb)) {
            sb.append(" AND ");
        }
        sb.append(ConstantGlobal.END_TIME).append("!='" + 0 + "' ");

        addLimitFilter(sb, ConstantGlobal.TIME_CREATED, SQLDBUtil.ORDER_DESC, SQLDBUtil.NO_START,
                limit);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                AssignmentAnalytics tA = SQLDBUtil.convertToValues(cursor, AssignmentAnalytics.class, null);
                assignmentAnalytics.add(tA);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        return assignmentAnalytics;
    }

    public List<AssignmentAnalytics> getAllAssignmentAnalytics(int orgKeyId, String userId,
                                                               Collection<String> entityIds, String entityType, String[] fields, boolean endedOnly) {

        List<AssignmentAnalytics> AssignmentAnalytics = new ArrayList<AssignmentAnalytics>();
        if (TextUtils.isEmpty(userId)) {
            return AssignmentAnalytics;
        }

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_ANALYTICS, fields, null);
        sb.append("WHERE ");
        if (endedOnly) {
            sb.append(ConstantGlobal.END_TIME).append("!='" + 0 + "' ");
            sb.append(" AND ");

        }

        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }

        if (entityIds != null && !entityIds.isEmpty()) {
            sb.append(ConstantGlobal.ENTITY_ID).append(" in (")
                    .append(LocalManager.joinString(entityIds, "'")).append(") ");
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        addLimitFilter(sb, ConstantGlobal.TIME_CREATED, SQLDBUtil.ORDER_DESC, SQLDBUtil.NO_START,
                SQLDBUtil.NO_LIMIT);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            Set<String> cColumnNames = new HashSet<String>(Arrays.asList(cursor.getColumnNames()));
            while (!cursor.isAfterLast()) {
                AssignmentAnalytics tA = SQLDBUtil.convertToValues(cursor, AssignmentAnalytics.class, fields,
                        cColumnNames);
                AssignmentAnalytics.add(tA);
                cursor.moveToNext();
            }
            cColumnNames.clear();
            cColumnNames = null;
        }
        closeCursor(cursor);
        return AssignmentAnalytics;
    }
    public ArrayList<TestAvg> getTestAvgMark(int orgKeyId, String userId) {

        return getTestAvgMarkAnalytics(orgKeyId, userId);
    }

    public ArrayList<TestAvg> getTestAvgMarkAnalytics(int orgKeyId, String userId) {

        ArrayList<TestAvg> testAvgMark = new ArrayList<TestAvg>();
        if (TextUtils.isEmpty(userId)) {
            return testAvgMark;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT totalMarks, score, attempted from ").append(TABLE_ENTITY_ANALYTICS)
                .append(" ");
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                TestAvg testAvgMrk = SQLDBUtil.convertToValues(cursor,
                        TestAvg.class, null);
                testAvgMark.add(testAvgMrk);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.v(TAG, "========boardAnalytics:  " + testAvgMark);
        return testAvgMark;
    }
    public ArrayList<TestAvg> getTestAvgPercentage(int orgKeyId, String userId,String couseName) {

        return getTestAvgPercentageAnalytics(orgKeyId, userId,couseName);
    }

    public ArrayList<TestAvg> getTestAvgPercentageAnalytics(int orgKeyId, String userId, String couseName) {

        ArrayList<TestAvg> testAvgMark = new ArrayList<TestAvg>();
        if (TextUtils.isEmpty(userId)) {
            return testAvgMark;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT totalMarks, score, attempted from ").append(TABLE_ENTITY_BOARD_LEVEL_ANALYTICS)
                .append(" ");
        sb.append("WHERE name="+"'"+couseName+"' AND ");
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);
        Log.e("HHHHHHHHH", "" + sb.toString());
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                TestAvg testAvgMrk = SQLDBUtil.convertToValues(cursor,
                        TestAvg.class, null);
                testAvgMark.add(testAvgMrk);
                cursor.moveToNext();
            }
        }
        closeCursor(cursor);
        Log.v(TAG, "========boardAnalytics:  " + testAvgMark);
        return testAvgMark;
    }

}


