package com.nhance.android.activities.tests;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.fragments.tests.TestPostAttemptPageFragment;
import com.nhance.android.R;

public class TestPostAttemptPageActivity extends NhanceBaseActivity {
//TODO: remove this activity class
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_test_post_attempt_page);
        Bundle args = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
        // args.putSerializable(ConstantGlobal.MANAGER, new AnalyticsDataManager(
        // getApplicationContext()));
        TestPostAttemptPageFragment fragment = new TestPostAttemptPageFragment();
        fragment.setArguments(args);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.test_post_attempt_page_fragment_layout, fragment);
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
