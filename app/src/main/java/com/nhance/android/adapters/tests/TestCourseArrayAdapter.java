package com.nhance.android.adapters.tests;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.R;

public class TestCourseArrayAdapter extends ArrayAdapter<TestMetadata> {

    final private String TAG = "TestStudentAnswersFragment";

    public TestCourseArrayAdapter(Context context, int resource, List<TestMetadata> objects) {

        super(context, resource, objects);
        // TODO Auto-generated constructor stub
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView view = (TextView) convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = (TextView)inflater.inflate(R.layout.list_item_spinner_course, parent, false);
        }
        TestMetadata metadata = getItem(position);        
        String name = metadata.name;
        String id = metadata.id;
        view.setText(name);
        view.setTag(id);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
    
}
