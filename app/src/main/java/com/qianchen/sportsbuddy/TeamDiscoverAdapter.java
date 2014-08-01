package com.qianchen.sportsbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * {@link TeamDiscoverAdapter} exposes a list of teams to a {@link android.widget.ListView}.
 *
 * Created by Qian Chen on 7/31/2014.
 */
public class TeamDiscoverAdapter extends BaseAdapter {

    private Context context;
    private List<Team> teams;

    public TeamDiscoverAdapter(Context context, List<Team> teams) {
        this.context = context;
        this.teams = teams;
    }

    @Override
    public int getCount() {
        return teams.size();
    }

    @Override
    public Object getItem(int position) {
        return teams.get(position);
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
            view = inflater.inflate(R.layout.list_item_discover_team, null);
        }

        Team team = teams.get(position);
        if (team != null) {
            ((TextView) view.findViewById(R.id.text_team_name)).setText(team.getName());
            ((TextView) view.findViewById(R.id.text_team_type)).setText(team.getSportsType());
        }

        return view;
    }
}
