package com.nhance.android.assignment.fragments.assignment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.assignment.adapter.AssignmentInstructionListAdapter;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.ViewUtils;

/**
 * Created by Himank Shah on 11/30/2016.
 */

public class AssignmentInstructionsDialogFragment extends DialogFragment {

    private static final String TAG = "AssignmentInstructionsDialogFragment";
    private View popupLayout;
    ViewPager mPager;
    int                         heightOfButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setStyle(STYLE_NO_TITLE, R.style.DialogFragmentStyle);
        setCancelable(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {

        super.onStart();
        if (getArguments() != null) {
            heightOfButton = getArguments().getInt("heightOfButton");
        }
        int finalHeightOfDialog = ViewUtils.getMeasuredHeightInPx(getActivity())
                - (heightOfButton/2);
        getDialog().getWindow().setLayout(
                ViewUtils.getOrientationSpecificWidth(getActivity(), 90, 65),
                finalHeightOfDialog);
        getDialog().setCanceledOnTouchOutside(true);
    }

    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        popupLayout = inflater.inflate(R.layout.assignment_instructions_popup, container, false);
        return popupLayout;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {

        mPager = (ViewPager) popupLayout.findViewById(R.id.assignment_popup_view_pager);
        Log.d(TAG, mPager.toString());
        final LinearLayout pageTrackerLayout = (LinearLayout) popupLayout
                .findViewById(R.id.assignment_popup_page_tracker);
        ViewPager.SimpleOnPageChangeListener viewPagerListener = new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                super.onPageSelected(position);
                String popupTitle = getActivity().getResources().getString(
                        R.string.how_to_attempt_assignment);
                if (position == 0) {
                    popupTitle = getActivity().getResources().getString(
                            R.string.instructions_caps);
                }
                popupLayout.findViewById(R.id.assignment_popup_swipe).setVisibility(View.INVISIBLE);
                popupLayout.findViewById(R.id.assignment_popup_got_it).setVisibility(View.INVISIBLE);
                if (position == 0) {
                    popupLayout.findViewById(R.id.assignment_popup_swipe).setVisibility(View.VISIBLE);
                } else if (position == 2) {
                    popupLayout.findViewById(R.id.assignment_popup_got_it).setVisibility(View.VISIBLE);
                }
                ((TextView) popupLayout.findViewById(R.id.assignment_popup_title)).setText(popupTitle);
                for (int k = 0; k < 3; k++) {
                    View view = pageTrackerLayout.getChildAt(k);
                    int drawableResId = (k == position) ? R.drawable.dot_blue
                            : R.drawable.dot_light_gray;
                    view.setBackgroundResource(drawableResId);
                }
            }
        };

        // int sideMargin = (int) getResources().getDimension(R.dimen.login_side_margin);
        // Log.d(TAG, "Putting a side margin of " + sideMargin);
        // mPager.setPageMargin(sideMargin);

        mPager.setOnPageChangeListener(viewPagerListener);

        AssignmentPopupViewPagerAdapter viewPagerAdapter = new AssignmentPopupViewPagerAdapter(
                getChildFragmentManager(), 3);
        mPager.setAdapter(viewPagerAdapter);
        mPager.setOffscreenPageLimit(3);

        for (int k = 0; k < 3; k++) {
            View view = new View(getActivity());
            int dotDimen = (int) (getResources().getDimension(R.dimen.tracker_dot_dimen));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dotDimen, dotDimen);
            params.setMargins(8, 0, 0, 0);
            view.setLayoutParams(params);
            view.setBackgroundResource(R.drawable.dot_light_gray);
            pageTrackerLayout.addView(view);
        }
        pageTrackerLayout.getChildAt(0).setBackgroundResource(R.drawable.dot_blue);

        View.OnClickListener closeAssignmentPopupListner = new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                dismiss();

            }
        };

        popupLayout.findViewById(R.id.close_assignment_popup).setOnClickListener(closeAssignmentPopupListner);
        popupLayout.findViewById(R.id.assignment_popup_got_it).setOnClickListener(closeAssignmentPopupListner);

        super.onActivityCreated(arg0);

    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        super.onDismiss(dialog);
    }

    class AssignmentPopupViewPagerAdapter extends FragmentStatePagerAdapter {

        int assignmentPopupPageCount;

        public AssignmentPopupViewPagerAdapter(FragmentManager fm, int assignmentPopupPageCount) {

            super(fm);
            this.assignmentPopupPageCount = assignmentPopupPageCount;
        }

        @Override
        public Fragment getItem(int position) {

            Fragment f = AssignmentInstructionsDialogFragment.AssignmentPopupFragment.newInstance(position);
            return f;
        }

        @Override
        public int getCount() {

            return assignmentPopupPageCount;
        }

    }

    public static class AssignmentPopupFragment extends NhanceBaseFragment {

        int          currentPos;
        private View fragmentRootView;

        public static AssignmentInstructionsDialogFragment.AssignmentPopupFragment newInstance(int position) {

            AssignmentInstructionsDialogFragment.AssignmentPopupFragment f = new AssignmentInstructionsDialogFragment.AssignmentPopupFragment();
            Bundle args = new Bundle();
            args.putInt(ConstantGlobal.POSITION, position);
            f.setArguments(args);
            return f;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {

            if (getArguments() != null) {
                currentPos = getArguments().getInt(ConstantGlobal.POSITION);
            }
            super.onCreate(savedInstanceState);
        }

        //correction required
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            if (currentPos == 0) {
                fragmentRootView = inflater.inflate(R.layout.fragment_assignment_popup_instructions,
                        container, false);
            } else {
                fragmentRootView = inflater.inflate(R.layout.fragment_with_image_only, container,
                        false);
                int imageResId = -1;
                switch (currentPos) {
                    case 1:
                        imageResId = R.drawable.how_to_take_test1;
                        break;
                    case 2:
                        imageResId = R.drawable.how_to_take_test2;
                        break;
                    default:
                        break;
                }
                ((ImageView) fragmentRootView).setImageResource(imageResId);
            }
            int sidePadding = getResources().getDimensionPixelSize(R.dimen.test_popup_side_padding);
            fragmentRootView.setPadding(sidePadding, 0, sidePadding, 0);
            return fragmentRootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {

            if (currentPos == 0) {
                ListView listView = (ListView) fragmentRootView
                        .findViewById(R.id.assignment_popup_list_view);

                listView.setAdapter(new AssignmentInstructionListAdapter(getActivity(),
                        getResources().getStringArray(R.array.assignment_instructions)));
            }
            super.onActivityCreated(savedInstanceState);
        }
    }

}
