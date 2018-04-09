package com.nhance.android.adapters.tests;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhance.android.utils.ViewUtils;
import com.nhance.android.R;
/**
 * remove this class
 * @author shankar
 *
 */
public class TestCourseAnalyticsListAdapter extends ArrayAdapter<String> {

    public TestCourseAnalyticsListAdapter(Context context, int resource, List<String> objects) {

        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_view_test_course_analytics, parent, false);
        }
        String item = getItem(position);
        ViewUtils.setTextViewValue(view, R.id.test_course_name, item);
        ViewGroup topicAnalyticsContainer = (ViewGroup) view
                .findViewById(R.id.test_course_topic_analytics_table);
        addTopicAnalyticsView(topicAnalyticsContainer);
        return view;
    }

    private void addTopicAnalyticsView(ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        String[] topics = new String[] { "Automic Number", "Differential Equation",
                "Laws of Motion" };

        for (String topic : topics) {
            View topicRow = inflater.inflate(R.layout.list_item_view_test_course_topic_analytics,
                    parent, false);
            TextView tNameView = (TextView) topicRow.findViewById(R.id.test_course_topic_name);
            tNameView.setText(topic);
            parent.addView(topicRow);
        }
    }
}
