package com.nhance.android.recentActivities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.nhance.android.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomListAdapterDialog extends BaseAdapter {

private List<Actor> listData;
private Context context;
private LayoutInflater layoutInflater;

public CustomListAdapterDialog(Context context, List<Actor> listData) {
    this.listData = listData;
    this.context = context;
    layoutInflater = LayoutInflater.from(context);
}

@Override
public int getCount() {
    return listData.size();
}

@Override
public Object getItem(int position) {
    return listData.get(position);
}

@Override
public long getItemId(int position) {
    return position;
}

public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
        convertView = layoutInflater.inflate(R.layout.dialog_list_item, null);
        holder = new ViewHolder();
        holder.profileImageView = (ImageView) convertView.findViewById(R.id.actorImageView);
        holder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        convertView.setTag(holder);
    } else {
        holder = (ViewHolder) convertView.getTag();
    }
    Picasso.with(context).load(listData.get(position).thumbnail).into(holder.profileImageView);
    holder.nameTextView.setText(listData.get(position).firstName+" "+listData.get(position).lastName);

    return convertView;
}

static class ViewHolder {
    ImageView profileImageView;
    TextView nameTextView;
}

}