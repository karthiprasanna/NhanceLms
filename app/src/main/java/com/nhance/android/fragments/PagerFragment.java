package com.nhance.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;

public class PagerFragment extends NhanceBaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_pager, container, false);
        String activeFragment = getArguments().getString("activeFragment");
        TextView fragmentValue = (TextView) view.findViewById(R.id.fragment_value);
        fragmentValue.setText(activeFragment);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
    }

}
