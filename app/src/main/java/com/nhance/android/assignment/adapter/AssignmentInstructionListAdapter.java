package com.nhance.android.assignment.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhance.android.R;

/**
 * Created by Himank Shah on 11/30/2016.
 */

public class AssignmentInstructionListAdapter extends ArrayAdapter<String> {

    String[] values;

    public AssignmentInstructionListAdapter(Context context, String[] values) {

        super(context, R.layout.list_item_view_take_assigment_instructions, values);
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_view_take_assigment_instructions, parent, false);
        }

        TextView textView = (TextView) view.findViewById(R.id.list_item_text);
        textView.setText(Html.fromHtml(values[position]));

        return view;
    }

}
