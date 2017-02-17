package com.pychen0918.wheretoeat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.pychen0918.wheretoeat.common.IndexedHashMap;

/**
 * Created by pychen0918 on 2016/12/28.
 */

public class IndexedHashMapAdapter extends BaseAdapter implements SpinnerAdapter {
    private final Context context;
    private final int listViewResourceId;
    private final int dropdownViewResourceId;
    private final int textViewId;
    private IndexedHashMap data;

    // Constructor
    public IndexedHashMapAdapter(Context context, int listViewResourceId, int dropdownViewResourceId, IndexedHashMap data) {
        this.context = context;
        this.listViewResourceId = listViewResourceId;
        this.dropdownViewResourceId = dropdownViewResourceId;
        this.textViewId = android.R.id.text1;
        this.data = data;
    }

    // Updater
    public void update(IndexedHashMap data){
        // TODO: thread safe
        this.data = data;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int i) {
        try {
            return data.get(i);
        } catch (RuntimeException e){
            return null;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public String getItemKey(int i){
        try {
            return data.getKey(i);
        } catch (RuntimeException e){
            return null;
        }
    }

    private static class ViewHolder{
        final TextView textView;

        ViewHolder(TextView textView) {
            this.textView = textView;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(listViewResourceId, parent, false);
            viewHolder = new ViewHolder((TextView) convertView.findViewById(textViewId));
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textView.setText(getItem(position));

        return convertView;
    }

    @Override
    public View getDropDownView (int position, View convertView, ViewGroup parent){
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(dropdownViewResourceId, parent, false);
            viewHolder = new ViewHolder((TextView) convertView.findViewById(textViewId));
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textView.setText(getItem(position));

        return convertView;
    }
}
