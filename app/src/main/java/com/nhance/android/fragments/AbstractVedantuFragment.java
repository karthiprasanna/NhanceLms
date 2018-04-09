package com.nhance.android.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;

public abstract class AbstractVedantuFragment extends NhanceBaseFragment {

    private static final String TAG = "AbstractVedantuFragment";
    protected TabHost mTabHost;

    protected int mTabLayout = R.layout.tabs_layout_lightgrey_bg;

    protected void addTab(final String tabId, String tabName) {

        final View tabview = createTabView(mTabHost.getContext(), tabName, tabId);
        TabSpec setContent = mTabHost.newTabSpec(tabId).setIndicator(tabview).setContent(new TabContentFactory() {

            @Override
            public View createTabContent(String tag) {

                TextView view = new TextView(mTabHost.getContext());
                view.setText(tag);
                return view;
            }
        });
        Log.d("Host", "adding tab host: " + tabId + ", tabHost: " + mTabHost);
        mTabHost.addTab(setContent);
    }

    protected void updateTab(int id, String tabId, Fragment newFragment, FragmentManager fm) {

        if (newFragment == null) {
            return;
        }
        if (fm.findFragmentByTag(tabId) == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(id, newFragment);
            ft.commit();
        }
    }

    protected View createTabView(final Context context, final String text, final Object tag) {

        View view = LayoutInflater.from(context).inflate(mTabLayout, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        tv.setTag(tag);
        // if (textSize > 1) {
        // tv.setTextSize(textSize);
        // }
        return view;
    }

    protected void destoryAndCreateFragmentView() {

        ViewGroup container = (ViewGroup) getView();
        container.removeAllViews();
        View view = onCreateView(getLayoutInflater(getArguments()), (ViewGroup) getView(),
                getArguments());
        container.addView(view);
    }

    protected MenuItem progressMenuItem;

    protected void showProgressBar() {

        Log.i(TAG, "showProgressBar: method is called");
        if (progressMenuItem != null) {
            progressMenuItem.setActionView(R.layout.progress_bar_in_action_bar);
            progressMenuItem.setTitle(R.string.loading);
        }
    }

    protected void showRefreshButton() {

        Log.i(TAG, "showRefreshButton: method is called");
        if (progressMenuItem != null) {
            progressMenuItem.setActionView(null);
            progressMenuItem.setIcon(R.drawable.refresh);
            progressMenuItem.setTitle(R.string.refresh);
        }
    }
}
