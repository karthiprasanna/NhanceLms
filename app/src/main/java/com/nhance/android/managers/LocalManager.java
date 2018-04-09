package com.nhance.android.managers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.UserActivityDataManager;
import com.nhance.android.db.models.Note;
import com.nhance.android.db.models.StudyHistory;
import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.enums.EntityType;
import com.nhance.android.pojos.TestAnalyticsMiniPojo;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.utils.ThumbnailDownloader;

public class LocalManager {

    private static final String TAG        = "LocalManager";
    public static final String  EMPTY_TEXT = "";

    public static void recordStudyHistory(Context context, int orgKeyId, String userId,
            int contentId, String linkId, String entityId, EntityType entityType) {

        StudyHistory studyHistory = new StudyHistory(orgKeyId, userId, contentId, linkId, false);
        try {
            new UserActivityDataManager(context).insertStudyHistory(studyHistory);
            SessionManager.getInstance(context).recordActivity(entityType.name(), "OPEN", entityId,
                    entityType, studyHistory._id);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static String getNoteHeadingText(Note note) {

        StringBuilder sb = new StringBuilder();
        sb.append(note.courseBrdName);
        return sb.toString();
    }

    public static String joinString(Collection<String> values, String annotateWith) {

        return joinString(values, annotateWith, false);
    }

    public static String join(Collection<String> values, String seprator) {

        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (String s : values) {
            if (TextUtils.isEmpty(s)) {
                continue;
            }
            if (!start) {
                sb.append(seprator);
            }
            sb.append(s);
            start = false;
        }
        return sb.toString();
    }

    public static String joinString(Collection<String> values, String annotateWith,
            boolean htmlEscape) {

        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (String s : values) {
            if (TextUtils.isEmpty(s)) {
                continue;
            }
            if (!start) {
                sb.append(",");
            }
            if (htmlEscape) {
                s = TextUtils.htmlEncode(s);
            }
            sb.append(annotateWith);
            sb.append(s);
            sb.append(annotateWith);
            start = false;
        }
        return sb.toString();
    }

    public static String joinString(Collection<String> values) {

        StringBuilder sb = new StringBuilder();
        if (values == null) {
            return sb.toString();
        }
        boolean start = true;
        for (String s : values) {
            if (!start) {
                sb.append(",");
            }
            sb.append(s);
            start = false;
        }
        return sb.toString();
    }

    public static ArrayList<TestAnalyticsMiniPojo> getTestAnalyticsMiniInfo(int orgKeyId,
            String userId, Set<String> entityIds, String entityType, Context context,
            boolean endedOnly) {

        ArrayList<TestAnalyticsMiniPojo> analytics = new ArrayList<TestAnalyticsMiniPojo>();
        AnalyticsDataManager ad = new AnalyticsDataManager(context);
        List<TestAnalytics> testAnalytics = ad.getAllTestAnalytics(orgKeyId, userId, entityIds,
                entityType, new String[] { ConstantGlobal.ENTITY_ID, ConstantGlobal.ENTITY_TYPE,
                        ConstantGlobal.SCORE, ConstantGlobal.TIME_CREATED,
                        ConstantGlobal.TOTAL_MARKS }, endedOnly);
        if (entityIds == null) {
            entityIds = new HashSet<String>();
        }
        for (TestAnalytics testAnalytic : testAnalytics) {
            entityIds.add(testAnalytic.entityId);
        }
        Log.d(TAG, "entityIds : " + entityIds);
        if (entityIds.isEmpty()) {
            return analytics;
        }

        Map<String, Content> entityIdToMiniEntityInfoMap = new ContentDataManager(context)
                .getContentIdContentMiniInfoMap(orgKeyId, entityIds, entityType, new String[] {
                        ConstantGlobal._ID, ConstantGlobal.ID, ConstantGlobal.TYPE,
                        ConstantGlobal.NAME, ConstantGlobal.BRD_IDS, ConstantGlobal.INFO });
        Log.d(TAG, "test info map : " + entityIdToMiniEntityInfoMap);
        for (TestAnalytics testAnalytic : testAnalytics) {
            Content content = entityIdToMiniEntityInfoMap.get(entityType + "_"
                    + testAnalytic.entityId);
            if (content == null) {
                continue;
            }
            int percentage = testAnalytic.totalMarks < 1 ? 0 : (testAnalytic.score * 100)
                    / testAnalytic.totalMarks;
            TestExtendedInfo testInfo = (TestExtendedInfo) content.toContentExtendedInfo();
            TestAnalyticsMiniPojo analytic = new TestAnalyticsMiniPojo(content._id,
                    testAnalytic.entityId, content.name, percentage, testAnalytic.score,
                    testAnalytic.totalMarks, testInfo.metadata == null ? 0
                            : testInfo.metadata.size(), Long.valueOf(testAnalytic.timeCreated));
            analytics.add(analytic);
            Log.d(TAG, "test analytics pojo : " + analytic);
        }
        return analytics;
    }

    /**
     * <blockquote>this will return the date as DATE{SEPERATOR}MONTH{SEPARATOR}/YEAR. i.e
     * 14-Jan-2013(SEPERATOR == /) will be returned as 14/01/13</blockquote>
     * 
     * @param time
     * @param seperator
     * @return
     */
    public static String getDateStrign(long time, String seperator) {

        StringBuilder sb = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        if (calendar.get(Calendar.DATE) < 10) {
            sb.append(0);
        }
        sb.append(calendar.get(Calendar.DATE));
        sb.append(seperator);
        if (calendar.get(Calendar.MONTH) < 9) {
            sb.append(0);
        }
        sb.append(calendar.get(Calendar.MONTH) + 1);
        sb.append(seperator);
        sb.append(String.valueOf(calendar.get(Calendar.YEAR)).substring(2));
        return sb.toString();
    }

    public static String getDurationMinString(float durationInSeconds) {
        return ""+ Math.round((durationInSeconds / 60) * 100.0) / 100.0;
    }
    public static String secondsToString(int pTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, pTime);
        return new SimpleDateFormat("HH:mm:ss").format(calendar.getTime());
    }
    public static String getDurationString(int durationInSeconds) {

        int h = (int) Math.floor(durationInSeconds / 3600);
        int m = (int) Math.floor(durationInSeconds % 3600 / 60);
        int s = (int) Math.floor(durationInSeconds % 3600 % 60);
        return ((h > 0 ? h + ":" : "") + (m > 0 ? (h > 0 && m < 10 ? "0" : "") + m + ":" : "0:")
                + (s < 10 ? "0" : "") + s);

    }

    public static String getDurationSpecificString(int durationInMilliSeconds, boolean includeHrs,
            boolean appendSuffix) {

        int durationInSeconds = durationInMilliSeconds / 1000;
        int h = (int) Math.floor(durationInSeconds / 3600);
        int m = (int) Math.floor(durationInSeconds % 3600 / 60);
        int s = durationInSeconds - (h * 3600 + 60 * m);
        String finalSecsString = (s < 10 ? "0" : "") + s;
        String finalMinsString = (m < 10 ? "0" : "") + m;
        String finalHrsString = (h < 10 ? "0" : "") + h;
        if (h > 0 || includeHrs) {
            finalHrsString += ":" + finalMinsString;
            if (appendSuffix) {
                finalHrsString += " hrs";
            }
            return finalHrsString;
        } else {
            finalMinsString += ":" + finalSecsString;
            if (appendSuffix) {
                finalMinsString += " mins";
            }
            return finalMinsString;
        }
    }

    final static ThumbnailDownloader downloader = new ThumbnailDownloader();

    public static void downloadImage(String url, ImageView imageView) {

        downloadImage(url, imageView, false);
    }

    public static void downloadImage(String url, ImageView imageView, boolean useFileCache) {

        downloadImage(url, imageView, useFileCache, null);
    }

    public static void downloadImage(String url, ImageView imageView, boolean useFileCache,
            ITaskProcessor<Bitmap> taskProcessor) {

        downloader.download(url, imageView, useFileCache, taskProcessor);
    }

    public static void downloadVideoThumbnail(String filePath, ImageView imageView) {

        downloader.downloadVideoThumb(filePath, imageView);
    }

    public static String formatMathJaxString(String mathjax) {

        return mathjax.replace("\\", "\\\\");
    }
}
