package com.nhance.android.assignment.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.assignment.fragments.assignment.AssignmentPostAttemptPageFragment;

public class AssignmentPostAttemptPageActivity extends NhanceBaseActivity {
    //TODO: remove this activity class
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment_post_attempt_page);
        Bundle args = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        // args.putSerializable(ConstantGlobal.MANAGER, new AnalyticsDataManager(
        // getApplicationContext()));
        AssignmentPostAttemptPageFragment fragment = new AssignmentPostAttemptPageFragment();
        fragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.assignment_post_attempt_page_fragment_layout, fragment);
        ft.commit();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putAll(getIntent().getExtras());
        super.onSaveInstanceState(outState);
//        getSherlock().dispatchSaveInstanceState(outState);
    }

}
