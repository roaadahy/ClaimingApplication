package com.example.claimapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.List;

public class HistoryListAdapter extends ArrayAdapter<HistoryData> {

    public HistoryListAdapter(Context context, int resource, List<HistoryData> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_history_item, parent, false);
        }

        HistoryData historyData = getItem(position);

        TextView address = convertView.findViewById(R.id.address_txt);
        address.setText(historyData.getAddress());

        TextView claimDateTime = convertView.findViewById(R.id.claim_date_time_txt);
        claimDateTime.setText(historyData.getClaimDateTime());

        TextView status = convertView.findViewById(R.id.status_txt);
        status.setText(historyData.getStatus());

        TextView authentic = convertView.findViewById(R.id.authentic_txt);
        authentic.setText(String.valueOf(historyData.getAuthentic()));

        return convertView;
    }
}
