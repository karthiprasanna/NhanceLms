package com.nhance.android.fragments;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class UserGuideDialogFragment extends DialogFragment {

    private static final String TAG = "UserGuideDialogFragment";
    private View                popupLayout;
    ViewPager                   mPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setStyle(STYLE_NO_TITLE, R.style.DialogFragmentStyle);
        setCancelable(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {

        super.onStart();
        int height = ViewUtils.getMeasuredHeightInPx(getActivity()) * 90 / 100;
        getDialog().getWindow().setLayout(
                ViewUtils.getOrientationSpecificWidth(getActivity(), 90, 65), height);
        Log.d(TAG, "Setting the height of user guide pop as: " + height);
        getDialog().setCanceledOnTouchOutside(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "Creating user guide view");
        popupLayout = inflater.inflate(R.layout.user_guide_popup, container, false);
        return popupLayout;
    }

    @Override
    public void onActivityCreated(Bundle arg0) {

        Log.d(TAG, "Creating user guide to the main activity");
        final int pageCount = 4;
        mPager = (ViewPager) popupLayout.findViewById(R.id.user_guide_popup_view_pager);
        final LinearLayout pageTrackerLayout = (LinearLayout) popupLayout
                .findViewById(R.id.user_guide_popup_page_tracker);
        ViewPager.SimpleOnPageChangeListener viewPagerListener = new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {

                super.onPageSelected(position);
                for (int k = 0; k < pageCount; k++) {
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

        UserGuidePopupViewPagerAdapter viewPagerAdapter = new UserGuidePopupViewPagerAdapter(
                getChildFragmentManager(), pageCount);
        mPager.setAdapter(viewPagerAdapter);
        mPager.setOffscreenPageLimit(pageCount);

        for (int k = 0; k < pageCount; k++) {
            View view = new View(getActivity());
            int dotDimen = (int) (getResources().getDimension(R.dimen.tracker_dot_dimen));
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    dotDimen, dotDimen);
            params.setMargins(8, 0, 0, 0);
            view.setLayoutParams(params);
            view.setBackgroundResource(R.drawable.dot_light_gray);
            pageTrackerLayout.addView(view);
        }
        pageTrackerLayout.getChildAt(0).setBackgroundResource(R.drawable.dot_blue);

        OnClickListener closeTestPopupListner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        };

        popupLayout.findViewById(R.id.close_user_guide_popup).setOnClickListener(
                closeTestPopupListner);

        super.onActivityCreated(arg0);

    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        super.onDismiss(dialog);
    }

    class UserGuidePopupViewPagerAdapter extends FragmentStatePagerAdapter {

        int userGuidePopupPageCount;

        public UserGuidePopupViewPagerAdapter(FragmentManager fm, int testPopupPageCount) {

            super(fm);
            this.userGuidePopupPageCount = testPopupPageCount;
        }

        @Override
        public Fragment getItem(int position) {

            Fragment f = UserGuidePopupFragment.newInstance(position);
            return f;
        }

        @Override
        public int getCount() {

            return userGuidePopupPageCount;
        }

    }

    public static class UserGuidePopupFragment extends NhanceBaseFragment {

        int          currentPos;
        private View fragmentRootView;

        public static UserGuidePopupFragment newInstance(int position) {

            UserGuidePopupFragment f = new UserGuidePopupFragment();
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            if (currentPos == 0) {
                fragmentRootView = inflater.inflate(R.layout.fragment_user_guide_first_page,
                        container, false);
            } else {
                fragmentRootView = inflater.inflate(R.layout.fragment_with_image_only, container,
                        false);
                int imageResId = -1;
                switch (currentPos) {
                case 1:
                    imageResId = R.drawable.how_to_use_app1;
                    break;
                case 2:
                    imageResId = R.drawable.how_to_use_app2;
                    break;
                case 3:
                    imageResId = R.drawable.how_to_use_app3;
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
    }

}
