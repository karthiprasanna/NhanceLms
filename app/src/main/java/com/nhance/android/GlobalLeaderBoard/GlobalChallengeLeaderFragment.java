package com.nhance.android.GlobalLeaderBoard;



        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentManager;
        import android.support.v4.app.FragmentStatePagerAdapter;
        import android.support.v4.view.MenuItemCompat;
        import android.support.v4.view.ViewPager;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.Menu;
        import android.view.MenuInflater;
        import android.view.MenuItem;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.RelativeLayout;
        import android.widget.Spinner;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.astuetz.PagerSlidingTabStrip;
        import com.nhance.android.ChallengeArena.model.ViewPagerTab;
        import com.nhance.android.R;
        import com.nhance.android.activities.baseclasses.NhanceBaseFragment;
        import com.nhance.android.assignment.StudentPerformance.NetUtils;
        import com.nhance.android.constants.ConstantGlobal;
        import com.nhance.android.db.datamanagers.OrgDataManager;
        import com.nhance.android.db.models.Organization;
        import com.nhance.android.managers.SessionManager;
        import com.nhance.android.pojos.OrgMemberInfo;
        import com.nhance.android.utils.GoogleAnalyticsUtils;
        import com.nhance.android.utils.VedantuWebUtils;

        import org.json.JSONArray;
        import org.json.JSONException;
        import org.json.JSONObject;

        import java.util.ArrayList;
        import java.util.HashMap;


public class GlobalChallengeLeaderFragment extends NhanceBaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private SessionManager session;
    private OrgMemberInfo orgMemberInfo;


    private ArrayAdapter<String> adapter;
    private Spinner spinner;
    private JSONObject result, result2, facet, ac_count, cc_count, yc_count;

    private int ac_count1, cc_count1, yc_count1;

    private JSONArray list;
    private String name;
    private ArrayList<ViewPagerTab> tabsList = new ArrayList<>();





    PagerSlidingTabStrip tabs;
    ViewPager pager;
    private  MainAdapter adapter1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleAnalyticsUtils.setScreenName(ConstantGlobal.GA_SCREEN_PROFILE);
        session = SessionManager.getInstance(getActivity());
        Organization org = new OrgDataManager(m_cObjNhanceBaseActivity)
                .getOrganization(session.getSessionStringValue(ConstantGlobal.ORG_ID),
                        session.getSessionStringValue(ConstantGlobal.CMDS_URL));
        orgMemberInfo = session.getOrgMemberInfo();

        if (org == null || orgMemberInfo == null) {
            Toast.makeText(m_cObjNhanceBaseActivity, "Invalid session",
                    Toast.LENGTH_LONG).show();
            m_cObjNhanceBaseActivity.finish();
            session.logoutUser();
            return;
        }
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_maintabbar_global, container, false);
        tabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs_global);

        pager = (ViewPager) view.findViewById(R.id.pager_global);

        tabsList.add(new ViewPagerTab("Weekly"));
        tabsList.add(new ViewPagerTab("Monthly"));
        tabsList.add(new ViewPagerTab("Overall"));

        adapter1 = new MainAdapter(getActivity().getSupportFragmentManager(), tabsList);
        pager.setAdapter(adapter1);
        tabs.setViewPager(pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {




            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                Log.e("Main Activity", "onPageScrolled"+ position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

                Log.e("Main Activity", "onPageScrollStateChanged"+ state);

            }

        });
        pager.setOffscreenPageLimit(2);


        return view;
    }

    public class MainAdapter extends FragmentStatePagerAdapter
            implements PagerSlidingTabStrip.CustomTabProvider {


        ArrayList<ViewPagerTab> tabs;

        public MainAdapter(FragmentManager fm, ArrayList<ViewPagerTab> tabs) {
            super(fm);
            this.tabs = tabs;
        }

        @Override
        public View getCustomTabView(ViewGroup viewGroup, int i) {
            RelativeLayout tabLayout = (RelativeLayout)
                    LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tab_layoutss, null);


            TextView tabTitle = (TextView) tabLayout.findViewById(R.id.tab_title);
            TextView badge = (TextView) tabLayout.findViewById(R.id.badge);

            ViewPagerTab tab = tabs.get(i);

            tabTitle.setText(tab.title.toUpperCase());
            if (tab.notifications > 0) {
                badge.setVisibility(View.VISIBLE);
                badge.setText(String.valueOf(tab.notifications));
            } else {
                badge.setVisibility(View.GONE);
            }

            return tabLayout;
        }

        @Override
        public void tabSelected(View view) {
            RelativeLayout tabLayout = (RelativeLayout) view;
            TextView badge = (TextView) tabLayout.findViewById(R.id.badge);
            //   badge.setVisibility(View.GONE);


        }


        @Override
        public void tabUnselected(View view) {

        }


        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            Bundle values=new Bundle();
            switch (position) {
                case 0:

                    fragment= new GlobalweeklyLeaderBoardFragment();
                    return  fragment;

                case 1:

                    fragment= new GlobalMonthlyLeaderBoardFragment();
                    return fragment;

                case 2:

                    fragment= new GlobalOverallLeaderBoardFragment();


                    return  fragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabs.size();
        }

        public int getItemPosition(Object item) {

            return POSITION_NONE;
        }

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }




}
