package com.qianchen.sportsbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * {@link RequestAdapter} exposes a list of teams to a {@link android.widget.ListView}.
 *
 * Created by Qian Chen on 7/31/2014.
 */
public class RequestAdapter extends BaseAdapter {

    private Context context;
    private List<Request> requests;

    public RequestAdapter(Context context, List<Request> requests) {
        this.context = context;
        this.requests = requests;
    }

    @Override
    public int getCount() {
        return requests.size();
    }

    @Override
    public Object getItem(int position) {
        return requests.get(position);
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
            view = inflater.inflate(R.layout.list_item_request, null);
        }

        Request request = requests.get(position);
        if (request != null) {
            ((Button) view.findViewById(R.id.button_ignore)).setOnClickListener(new IgnoreListener(position));
            ((Button) view.findViewById(R.id.button_approve)).setOnClickListener(new ApproveListener(position));
            ((TextView) view.findViewById(R.id.text_user_name)).setText(request.getUserName());
            ParseImageView imageView = (ParseImageView) view.findViewById(R.id.image_user_avatar);
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            ParseUser user;
            try {
                user = query.get(request.getUserID());
            } catch (ParseException e) {
                // todo user default avatar
                return view;
            }
            imageView.setParseFile(user.getParseFile("avatar"));
            imageView.loadInBackground();
        }

        return view;
    }

    class ApproveListener implements View.OnClickListener {
        int position;

        public ApproveListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Request request = requests.remove(position);

            try {
                // update team member info
                ParseQuery<Team> teamQuery = ParseQuery.getQuery("Team");
                Team team = teamQuery.get(request.getTeamID());
                team.addMember(request.getUserID());
                team.saveInBackground();

                // upload an ApprovedRequest instance
                ApprovedRequest approvedRequest = new ApprovedRequest();
                approvedRequest.setUserID(request.getUserID());
                approvedRequest.setTeamID(request.getTeamID());
                approvedRequest.saveInBackground();
//                ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
//                ParseUser newMember = userQuery.get(request.getUserID());
//                List<String> teamsId = newMember.getList("teamsJoined");
//                if (teamsId == null) {
//                    teamsId = new ArrayList<String>();
//                }
//                teamsId.add(request.getTeamID());
//                newMember.put("teamsJoined", teamsId);
//                newMember.saveInBackground();
            } catch (ParseException e) {
                //todo
            }

            request.deleteInBackground();

            notifyDataSetChanged();
        }
    }

    class IgnoreListener implements View.OnClickListener {
        int position;

        public IgnoreListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Request request = requests.remove(position);
            request.deleteInBackground();

            notifyDataSetChanged();
        }
    }
}
