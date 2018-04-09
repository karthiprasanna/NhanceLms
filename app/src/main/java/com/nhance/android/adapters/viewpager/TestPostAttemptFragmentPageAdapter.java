package com.nhance.android.adapters.viewpager;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.enums.MemberProfile;
import com.nhance.android.fragments.tests.TestRankListFragment;
import com.nhance.android.fragments.tests.TestStudentAnalaticsFragment;
import com.nhance.android.fragments.tests.TestStudentAnswersFragment;
import com.nhance.android.fragments.tests.TestYourAnalyticsFragment;
import com.nhance.android.fragments.tests.TestYourAnswersFragment;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.R;

public class TestPostAttemptFragmentPageAdapter extends FragmentStatePagerAdapter {

    private String               entityId;
    private String               entityType;
    private TestExtendedInfo     testInfo;
    private AnalyticsDataManager analyticsDataManager;
    private Context              callerContext;

    public TestPostAttemptFragmentPageAdapter(FragmentManager fm,
            AnalyticsDataManager analyticsDataManager, TestExtendedInfo testInfo, String entityId,
            String entityType, Context context) {

        super(fm);
        this.entityId = entityId;
        this.entityType = entityType;
        this.testInfo = testInfo;
        this.analyticsDataManager = analyticsDataManager;
        this.callerContext = context;
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
                TestYourAnalyticsFragment aFragment = new TestYourAnalyticsFragment();
                fragment = aFragment;
            } else {
                TestStudentAnalaticsFragment aFragment = new TestStudentAnalaticsFragment();
                fragment = aFragment;
            }
            break;
        case 1:
            fragment = new TestRankListFragment();
            values.putInt(ConstantGlobal.TOTAL_MARKS, testInfo.totalMarks);
            break;
        case 2:
            if (isStudent) {
                fragment = new TestYourAnswersFragment();
                values.putSerializable(ConstantGlobal.TEST_METADATA,
                        (ArrayList<TestMetadata>) testInfo.metadata);
            } else {
                fragment = new TestStudentAnswersFragment();
                values.putSerializable(ConstantGlobal.TEST_METADATA,testInfo);
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
                .getStringArray(R.array.test_post_page_tabs).length;
    }

    private void addArgumentValues(Bundle bundle) {

        bundle.putString(ConstantGlobal.ENTITY_ID, entityId);
        bundle.putString(ConstantGlobal.ENTITY_TYPE, entityType);
    }
}
