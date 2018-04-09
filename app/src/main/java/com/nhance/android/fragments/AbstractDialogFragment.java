package com.nhance.android.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ViewGroup.LayoutParams;

import com.nhance.android.utils.ViewUtils;

public class AbstractDialogFragment extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        getDialog().getWindow().setLayout(
                ViewUtils.getOrientationSpecificWidth(getActivity()),
                LayoutParams.MATCH_PARENT);

        super.onCreate(savedInstanceState);
    }

}
