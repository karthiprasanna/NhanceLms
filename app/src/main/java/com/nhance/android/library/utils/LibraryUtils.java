package com.nhance.android.library.utils;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nhance.android.QuestionCount.QuestionBasicInfo;
import com.nhance.android.QuestionCount.QuestionCountActivity;
import com.nhance.android.activities.CompoundMediaPlayerActivity;
import com.nhance.android.activities.ModuleActivity;
import com.nhance.android.activities.content.players.DocumentPlayerActivity;
import com.nhance.android.activities.content.players.HTMLContentDisplayActivity;
import com.nhance.android.activities.content.players.VideoPlayerActivity;
import com.nhance.android.activities.tests.TestPreAttemptPageActivity;
import com.nhance.android.activities.tests.TestTeacherPageActivity;
import com.nhance.android.assignment.activity.AssigmentTeacherPageActivity;
import com.nhance.android.assignment.activity.AssignmentPreAttemptPageActivity;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.MemberProfile;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.pojos.content.infos.AssignmentBasicInfo;
import com.nhance.android.pojos.content.infos.IContentInfo;
import com.nhance.android.pojos.content.infos.TestBasicInfo;
import com.nhance.android.pojos.content.infos.VideoBasicInfo;
import com.nhance.android.R;

public class LibraryUtils {

    public static void onLibraryItemClickListnerImpl(Context context, LibraryContentRes contentRes,
                                                     String moduleId) {

        if (contentRes == null) {
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putInt(ConstantGlobal.CONTENT_ID, contentRes._id);
        bundle.putString(ConstantGlobal.LINK_ID, contentRes.linkId);
        bundle.putString(ConstantGlobal.ENTITY_ID, contentRes.id);
        bundle.putString(ConstantGlobal.NAME, contentRes.name);
        bundle.putString(ConstantGlobal.MODULE_ID, moduleId);
        takeToContentPage(context, bundle, contentRes);
    }

    public static boolean _amIStudent(Context context) {

        MemberProfile profile = _getMemberProfile(context);
        boolean isStudent = MemberProfile.STUDENT == profile
                || MemberProfile.OFFLINE_USER == profile;
        return isStudent;
    }

    public static MemberProfile _getMemberProfile(Context context) {

        SessionManager session = SessionManager.getInstance(context);
        OrgMemberInfo orgMemberInfo = session.getOrgMemberInfo();
        return orgMemberInfo.profile;
    }

    public static void onLibraryItemClickListnerImpl(Context context, LibraryContentRes contentRes) {

        onLibraryItemClickListnerImpl(context, contentRes, null);
    }

    private static void takeToContentPage(Context context, Bundle bundle,
                                          LibraryContentRes contentRes) {

        EntityType type = contentRes.__getEntityType();

        Class<?> activityClass = null;

        MemberProfile profile = _getMemberProfile(context);

        if (EntityType.VIDEO.equals(type)) {
            activityClass = VideoPlayerActivity.class;
        } else if (EntityType.DOCUMENT.equals(type) || EntityType.FILE.equals(type)) {
            activityClass = DocumentPlayerActivity.class;
        } else if (EntityType.TEST.equals(type)) {
            if (profile == MemberProfile.STUDENT || profile == MemberProfile.OFFLINE_USER) {
                activityClass = TestPreAttemptPageActivity.class;
            } else {
                activityClass = TestTeacherPageActivity.class;
            }
        } else if (EntityType.ASSIGNMENT.equals(type)) {
            if (profile == MemberProfile.STUDENT || profile == MemberProfile.OFFLINE_USER) {
                activityClass = AssignmentPreAttemptPageActivity.class;
                bundle.putString(ConstantGlobal.ASSIGNMENT_NAME, contentRes.name);
            } else {
                activityClass = AssigmentTeacherPageActivity.class;
                bundle.putString(ConstantGlobal.ASSIGNMENT_NAME, contentRes.name);
            }
        } else if (EntityType.MODULE.equals(type)) {
            activityClass = ModuleActivity.class;
        } else if (EntityType.COMPOUNDMEDIA.equals(type)) {
            activityClass = CompoundMediaPlayerActivity.class;
            String fileUrl = "http://127.0.0.1:8080/gasoft/" + StringUtils.lowerCase(type.name())
                    + File.separator + contentRes.file;
            bundle.putString(ConstantGlobal.MEDIA_URL, fileUrl);

            if (contentRes.file.endsWith(".html")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.setDataAndType(Uri.parse(fileUrl), MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(StringUtils.substringAfterLast(fileUrl, ".")));
                context.startActivity(intent);
                return;
            }
        } else if (EntityType.HTMLCONTENT.equals(type)) {
            // String fileUrl = "http://10.20.30.119:19019/viewer/cview/cmdshtmlcontent/582f0f5ee4b0d1bfe91a74e6/index.html";
            //  bundle.putString(ConstantGlobal.MEDIA_URL, fileUrl);
            activityClass = HTMLContentDisplayActivity.class;
        } else if (EntityType.QUESTION.equals(type)) {
            activityClass = QuestionCountActivity.class;
        }

        if (activityClass == null) {
            Toast.makeText(context,
                    "Content not consumable. Only Videos, Documents, Files and Tests are allowed",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent i = new Intent(context, activityClass);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtras(bundle);
        context.startActivity(i);
        // this context is always of activity type,but just to be sure making
        // the check
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.slide_in_right,
                    R.anim.slide_out_left);
        }
    }

    public static void addStarViewStatus(View view, boolean starred) {

        ImageView starView = (ImageView) view.findViewById(R.id.library_item_starred);
        if (starred) {
            starView.setVisibility(View.VISIBLE);

        } else {
            starView.setVisibility(View.INVISIBLE);
        }
    }

    public static void setStatsView(Context ctx, LibraryContentRes contentRes, TextView statsView,
                                    EntityType type, boolean cannotHaveEmptyText) {

        IContentInfo info = contentRes.toContentBasicInfo();

        if (cannotHaveEmptyText) {
            statsView.setText(StringUtils.capitalize(type.name()));
        } else {
            statsView.setText(ConstantGlobal.EMPTY);
        }

        if (info == null) {
            return;
        }

        switch (type) {
            case TEST:
                TestBasicInfo tInfo = (TestBasicInfo) info;
                statsView.setText(LocalManager.getDurationSpecificString(tInfo.duration, false, true));
                break;

            case ASSIGNMENT:
                AssignmentBasicInfo aInfo = (AssignmentBasicInfo) info;
                int quesCount = aInfo.qusCount;
                String statsText = ((Integer) quesCount).toString() + " Question";
                if (quesCount != 1) {
                    statsText += "s";
                }
                statsView.setText(statsText);
                break;
            case DOCUMENT:
                break;
            case VIDEO:
                VideoBasicInfo vInfo = (VideoBasicInfo) info;
                statsView.setText(LocalManager.getDurationString(vInfo.duration / 1000));
                break;
            case FILE:
                break;
            case MODULE:
                String timeAdded = DateUtils.formatDateTime(ctx, Long.parseLong(contentRes.linkTime),
                        DateUtils.FORMAT_NUMERIC_DATE);
                //change >>> module to learning path
                statsView.setText(StringUtils.capitalize("LEARNING PATH") + " | Added on "
                        + timeAdded);
                break;
            case COMPOUNDMEDIA:
                break;
            //libutils
            case QUESTION:
                QuestionBasicInfo qInfo = (QuestionBasicInfo) info;
                int qInfoquesCount = qInfo.qusCount;
                String qInfostatsText = ((Integer) qInfoquesCount).toString() + " Question";
                if (qInfoquesCount != 1) {
                    qInfostatsText += "s";
                }
                statsView.setText(qInfostatsText);
                break;
            default:
                break;
        }

    }
}
