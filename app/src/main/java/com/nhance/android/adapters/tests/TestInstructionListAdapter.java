package com.nhance.android.adapters.tests;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nhance.android.R;

public class TestInstructionListAdapter extends ArrayAdapter<String> {

    String[] values;

    public TestInstructionListAdapter(Context context, String[] values) {

        super(context, R.layout.list_item_view_take_test_instructions, values);
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_view_take_test_instructions, parent, false);
        }

        TextView textView = (TextView) view.findViewById(R.id.list_item_text);
        textView.setText(Html.fromHtml(values[position]));

        return view;
    }

}
