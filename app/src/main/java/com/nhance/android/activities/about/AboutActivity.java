package com.nhance.android.activities.about;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.WindowManager;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.fragments.AboutPagesFragment;
import com.nhance.android.fragments.AboutPagesFragment.IAboutPage;
import com.nhance.android.R;

public class AboutActivity extends NhanceBaseActivity implements IAboutPage {

    private int currentAboutPageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setTitle(getResources().getString(R.string.about));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        updateFragment(0);
    }

    @Override
    public void updateFragment(int position) {

        currentAboutPageIndex = position;
        AboutPagesFragment fragment = AboutPagesFragment.newInstance(position);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.about_pages_framelayout, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        case android.R.id.home:
            // NavUtils.navigateUpFromSameTask(this);
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onBackPressed() {

        if (currentAboutPageIndex > 0) {
            updateFragment(0);
        } else {
            super.onBackPressed();
        }
    }
}
