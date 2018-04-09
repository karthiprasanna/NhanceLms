package com.nhance.android.adapters.tests;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.nhance.android.managers.LocalManager;
import com.nhance.android.pojos.tests.UserEntityRank;
import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;

public class RankListAdapter extends ArrayAdapter<UserEntityRank> {

    private int      totalMarks;
    private Drawable defaultPic;

    public RankListAdapter(Context context, int textViewResourceId, List<UserEntityRank> objects,
            int totalMarks) {

        super(context, textViewResourceId, objects);
        this.totalMarks = totalMarks;
        this.defaultPic = getContext().getResources().getDrawable(R.drawable.default_profile_pic);
    }

    @Override
    /**
     * layout to be used --> {@link R.layout.list_item_view_test_rank} 
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_view_test_rank, parent, false);
        }

        UserEntityRank item = getItem(position);
        ViewUtils.setTextViewValue(view, R.id.rank, item.rank);
        ViewUtils.setTextViewValue(view, R.id.user_fname,
                StringUtils.upperCase(item.user.firstName));
        ViewUtils
                .setTextViewValue(view, R.id.user_lname, StringUtils.upperCase(item.user.lastName));

        ImageView userThum = (ImageView) view.findViewById(R.id.user_thum);
        userThum.setImageDrawable(defaultPic);
        LocalManager.downloadImage(item.user.thumbnail, userThum);

        ViewUtils.setTextViewValue(view, R.id.time_taken,
                LocalManager.getDurationMinString((int) (item.measures.timeTaken / 1000)));

        ViewUtils.setTextViewValue(view, R.id.total_marks, totalMarks, null, getContext()
                .getResources().getString(R.string.seperator));

        ViewUtils.setTextViewValue(view, R.id.scored_marks, item.measures.score);
        view.setTag(position);
        return view;
    }
}
