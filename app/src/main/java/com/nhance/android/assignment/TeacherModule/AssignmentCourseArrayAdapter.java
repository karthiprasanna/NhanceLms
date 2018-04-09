package com.nhance.android.assignment.TeacherModule;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhance.android.R;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;

import java.util.List;

/**
 * Created by karthi on 1/3/17.
 */


public class AssignmentCourseArrayAdapter extends ArrayAdapter<AssignmentMetadata> {

    final private String TAG = "AssignmentStudentAnswersFragment";

    public AssignmentCourseArrayAdapter(Context context, int resource, List<AssignmentMetadata> objects) {

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
        AssignmentMetadata metadata = getItem(position);
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
