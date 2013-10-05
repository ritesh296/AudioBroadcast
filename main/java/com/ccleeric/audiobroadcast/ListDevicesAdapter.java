package com.ccleeric.audiobroadcast;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ccleeric on 13/9/26.
 */
public class ListDevicesAdapter extends ArrayAdapter<String> {
    private LayoutInflater mInflater;
    private List<String> mList;

    public ListDevicesAdapter(Context context, int textViewResourceId, List<String> list) {
        super(context, textViewResourceId, list);
        this.mInflater = LayoutInflater.from(context);
        this.mList = list;
    }

    private class ListItem {
        public TextView title;
        public CheckBox checkbox;
        public TextView address;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ListItem item = null;

        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            item = new ListItem();

            item.title = (TextView) convertView.findViewById(R.id.title);
            item.checkbox = (CheckBox) convertView.findViewById((R.id.checkbox));
            item.address = (TextView) convertView.findViewById(R.id.mac_address);

            convertView.setTag(item);
        } else {
            item = (ListItem) convertView.getTag();
        }

        item.title.setText(mList.get(position).split(",")[0]);
        item.address.setText(mList.get(position).split(",")[1]);
        item.checkbox.setChecked(item.checkbox.isChecked());

        return convertView;
    }
}
