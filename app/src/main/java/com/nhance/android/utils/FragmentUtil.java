package com.nhance.android.utils;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class FragmentUtil {

    public static OnItemClickListener getOnItemClickListner(final FragmentManager fm) {

        AdapterView.OnItemClickListener listner = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // TestFullPageSummaryFragment testSummaryFragment = new
                // TestFullPageSummaryFragment();
                // Bundle args = new Bundle();
                // args.putInt(ConstantGlobal.CONTENT_ID, (Integer) view.getTag());
                // testSummaryFragment.setArguments(args);
                // FragmentTransaction ft = fm.beginTransaction();
                // ft.replace(R.id.prog_content_container_fragment, testSummaryFragment);
                // ft.commit();
            }
        };
        return listner;
    }

    public static OnItemClickListener
            getOnItemClickListner(final Context context, final Bundle args) {

        AdapterView.OnItemClickListener listner = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Intent intent = new Intent(context, TestFullPageActivity.class);
                // intent.putExtra(ConstantGlobal.CONTENT_ID, (Integer) view.getTag());
                // intent.putExtras(args);
                // intent.putExtra("previousPage", "Analytics");
                // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // context.startActivity(intent);
            }
        };
        return listner;
    }
}
