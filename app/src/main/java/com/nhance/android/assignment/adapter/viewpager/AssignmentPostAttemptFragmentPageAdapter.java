package com.nhance.android.assignment.adapter.viewpager;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.nhance.android.R;
import com.nhance.android.assignment.TeacherModule.AssignmentStudentAnalaticsFragment;
import com.nhance.android.assignment.TeacherModule.AssignmentUserAnalyticsFragment;
import com.nhance.android.assignment.TeacherModule.StudentQuestionListFragment;
import com.nhance.android.assignment.fragments.assignment.AssignmentYourAnswersFragment;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.enums.MemberProfile;
import com.nhance.android.library.utils.LibraryUtils;

import java.util.ArrayList;

/**
 * Created by Himank Shah on 12/7/2016.
 */

public class AssignmentPostAttemptFragmentPageAdapter extends FragmentStatePagerAdapter {

    private String entityId;
    private String entityType;
    private AssignmentExtendedInfo assignmentInfo;
    private AnalyticsDataManager analyticsDataManager;
    private Context callerContext;
    private String assignmentName;

    public AssignmentPostAttemptFragmentPageAdapter(FragmentManager fm, AnalyticsDataManager analyticsDataManager, AssignmentExtendedInfo assignmentInfo, String entityId,
                                                    String entityType, Context context, String assignmentName) {

        super(fm);
        this.entityId = entityId;
        this.entityType = entityType;
        this.assignmentInfo = assignmentInfo;
        this.analyticsDataManager = analyticsDataManager;
        this.callerContext = context;
        this.assignmentName =  assignmentName;
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;
        Bundle values = new Bundle();
        addArgumentValues(values);
        MemberProfile profile = LibraryUtils._getMemberProfile(callerContext);
        boolean isStudent = MemberProfile.STUDENT == profile
                || MemberProfile.OFFLINE_USER == profile;
        switch (position) {
           case 0:
                if (isStudent) {

                    AssignmentUserAnalyticsFragment aFragment= new AssignmentUserAnalyticsFragment();
                            fragment=aFragment;


                    values.putSerializable(ConstantGlobal.ASSIGNMENT_METADATA,
                            (ArrayList<AssignmentMetadata>) assignmentInfo.metadata);
                } else {
                    AssignmentStudentAnalaticsFragment aFragment = new AssignmentStudentAnalaticsFragment();
                    fragment = aFragment;
                    values.putSerializable(ConstantGlobal.ASSIGNMENT_METADATA,
                            (ArrayList<AssignmentMetadata>) assignmentInfo.metadata);
                    values.putString(ConstantGlobal.ASSIGNMENT_NAME,
                            assignmentName);
                }
                break;

            case 1:
                if (isStudent) {
                    fragment = new AssignmentYourAnswersFragment();
                    values.putSerializable(ConstantGlobal.ASSIGNMENT_METADATA,
                            (ArrayList<AssignmentMetadata>) assignmentInfo.metadata);
                    values.putString(ConstantGlobal.ASSIGNMENT_NAME,
                            assignmentName);
                } else {
              fragment = new StudentQuestionListFragment();
                    values.putSerializable(ConstantGlobal.ASSIGNMENT_METADATA,assignmentInfo);
                    values.putString(ConstantGlobal.ASSIGNMENT_NAME,
                            assignmentName);
                }
                break;
            default:
                break;
        }
        if (fragment != null) {
            fragment.setArguments(values);
        }
        return fragment;
    }

    @Override
    public int getCount() {

        return analyticsDataManager.getContext().getResources()
                .getStringArray(R.array.assignment_post_page_tabs).length;
    }

    private void addArgumentValues(Bundle bundle) {

        bundle.putString(ConstantGlobal.ENTITY_ID, entityId);
        bundle.putString(ConstantGlobal.ENTITY_TYPE, entityType);
        bundle.putString(ConstantGlobal.ASSIGNMENT_NAME, assignmentName);
        Log.e("kkkkk","..."+assignmentName);
        Log.e("llkkkkk","..."+entityId);
    }
}
