package com.qianchen.sportsbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

/**
 * {@link ProfileInterestAdapter} exposes a list of teams to a {@link android.widget.ListView}.
 *
 * Created by Qian Chen on 8/2/2014.
 */
public class ProfileInterestAdapter extends BaseAdapter {

    private Context context;
    private List<ProfileInterest> profileInterestList;

    public ProfileInterestAdapter(Context context, List<ProfileInterest> profileInterestList){
        this.context= context;
        this.profileInterestList = profileInterestList;
    }

    @Override
    public int getCount() {
        return profileInterestList.size();
    }

    @Override
    public Object getItem(int position) {
        return profileInterestList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.list_item_profile_interest, null);
        }

        ProfileInterest profileInterest = profileInterestList.get(position);
        ((TextView) view.findViewById(R.id.profile_interest_kind)).setText(profileInterest.getType());
        ((RatingBar) view.findViewById(R.id.profile_rating_bar)).setRating(profileInterest.getScore());
        return view;
    }
}