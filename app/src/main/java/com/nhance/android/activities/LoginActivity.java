package com.nhance.android.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.fragments.LoginPageFragment;
import com.nhance.android.R;

public class LoginActivity extends NhanceBaseActivity {

    ViewPager                   mPager;
    private static final String TAG = "LoginActivity";
    private boolean fromNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        if (getSupportActionBar() == null) {
            return;
        }

        fromNotification = getIntent().getBooleanExtra("fromNotification", false);
        Log.e("fromNotification",""+fromNotification);
        getSupportActionBar().hide();
        mPager = (ViewPager) findViewById(R.id.login_view_pager);
        final LinearLayout pageTrackerLayout = (LinearLayout) findViewById(R.id.login_page_tracker);

        // Capture ViewPager page swipes
        ViewPager.SimpleOnPageChangeListener viewPagerListener = new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                super.onPageSelected(position);
                for (int k = 0; k < 2; k++) {
                    View view = pageTrackerLayout.getChildAt(k);
                    int drawableResId = (k == position) ? R.drawable.dot_darker_grey
                            : R.drawable.dot_gray;
                    view.setBackgroundResource(drawableResId);
                }
            }
        };

        int sideMargin = (int) getResources().getDimension(R.dimen.login_side_margin);
        Log.d(TAG, "Putting a side margin of " + sideMargin);
        mPager.setPageMargin(sideMargin);

        mPager.setOnPageChangeListener(viewPagerListener);

        LoginViewPagerAdapter viewPagerAdapter = new LoginViewPagerAdapter(
                getSupportFragmentManager(), 2);
        mPager.setAdapter(viewPagerAdapter);
        mPager.setOffscreenPageLimit(2);
        for (int k = 0; k < 2; k++) {
            View view = new View(this);
            int dotDimen = (int) (getResources().getDimension(R.dimen.tracker_dot_dimen));
            LayoutParams params = new LayoutParams(dotDimen, dotDimen);
            params.setMargins(8, 0, 0, 0);
            view.setLayoutParams(params);
            view.setBackgroundResource(R.drawable.dot_gray);
            pageTrackerLayout.addView(view);
        }
        pageTrackerLayout.getChildAt(0).setBackgroundResource(R.drawable.dot_darker_grey);
        boolean fromSignupAndLoginPage = getIntent().getBooleanExtra("fromSignupAndLoginPage",
                false);
        if (fromSignupAndLoginPage) {
            mPager.setCurrentItem(4);
        }
    }

    class LoginViewPagerAdapter extends FragmentStatePagerAdapter {

        int loginPageCount;

        public LoginViewPagerAdapter(FragmentManager fm, int loginPageCount) {

            super(fm);
            this.loginPageCount = loginPageCount;
        }

        @Override
        public Fragment getItem(int position) {

            Fragment f = LoginPageFragment.newInstance(position);
            return f;
        }

        @Override
        public int getCount() {

            return loginPageCount;
        }

    }

}

// Capture ViewPager page swipes

// Capture ViewPager page swipes
