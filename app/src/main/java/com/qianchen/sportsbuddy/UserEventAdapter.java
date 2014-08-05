package com.qianchen.sportsbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * {@link UserEventAdapter} exposes a list of teams to a {@link android.widget.ListView}.
 *
 * Created by Qian Chen on 8/2/2014.
 */
public class UserEventAdapter extends BaseAdapter {

    public static final int MILLISECONDS_PER_HOUR = 3600000;
    public static final int MILLISECONDS_PER_MINUTE = 60000;

    private Context context;
    private List<Event> events;
    private SimpleDateFormat simpleDateFormat;

    public UserEventAdapter(Context context, List<Event> events) {
        this.context = context;
        this.events = events;
        simpleDateFormat = new SimpleDateFormat("MMM dd HH:mm");
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int position) {
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_user_event, null);
        }

        Event event = events.get(position);
        if (event != null) {
            ((TextView) view.findViewById(R.id.event_type)).setText(event.getSportType());
            long dateMilliseconds = event.getDateMilliseconds() + event.getHour() * MILLISECONDS_PER_HOUR + event.getMinute() * MILLISECONDS_PER_MINUTE;
            ((TextView) view.findViewById(R.id.event_time)).setText(simpleDateFormat.format(new Date(dateMilliseconds)));
            ((TextView) view.findViewById(R.id.event_location)).setText(event.getAddressText().split(",")[0]);
        }

        return view;
    }
}