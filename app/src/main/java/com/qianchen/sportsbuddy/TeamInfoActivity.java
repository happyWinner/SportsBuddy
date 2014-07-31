package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class TeamInfoActivity extends Activity {

    public static final String CAPTAIN_SUFFIX = " (C)";
    private Team team;
    private List<ParseUser> members;
    private ParseImageView teamEmblem;
    private TextView teamName;
    private TextView memberName;
    private ParseImageView memberAvartar;
    private LinearLayout linearLayout;
    private List<String> membersIDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_info);

        // register Team as the subclass of ParseObject
        ParseObject.registerSubclass(Team.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // get references of all the views
        teamEmblem = (ParseImageView) findViewById(R.id.team_info_emblem);
        teamName = (TextView) findViewById(R.id.team_info_team_name);
        linearLayout =(LinearLayout)findViewById(R.id.team_info_linear);

        ParseQuery<Team> query = ParseQuery.getQuery("Team");
        // try to load from the cache; but if that fails, load results from the network
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        try {
            team = query.get(getIntent().getStringExtra("teamID"));
        } catch (ParseException e) {
            exceptionHandler(e);
            finish();
        }

        // show the team emblem and team name
        ParseFile emblem = team.getEmblem();
        teamEmblem.setParseFile(emblem);
        teamEmblem.loadInBackground();
        teamName.setText(team.getName());

        // get members list
        membersIDs = team.getMembers();
        members = new ArrayList<ParseUser>();
        for (String memberID : membersIDs) {
            ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
            // try to load from the cache; but if that fails, load results from the network
            userQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            try {
                members.add(userQuery.get(memberID));
            } catch (ParseException e) {
                exceptionHandler(e);
                finish();
            }
        }

        // show member avartar and name
        for (int index = 0; index < members.size(); ++index) {
            linearLayout.addView(addMemberInfo(index));
        }

        // set "Leave Team" button listener
        ((Button) findViewById(R.id.button_leave)).setOnClickListener(new LeaveListener());
    }

    private View addMemberInfo(int index) {
        LayoutInflater inflater;
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.list_item_member, null);

        // show member avartar
        memberAvartar = (ParseImageView) layout.findViewById(R.id.team_info_member_avartar);
        // todo: change back to user avartar
        memberAvartar.setParseFile(team.getEmblem());
        memberAvartar.loadInBackground();

        // show member name
        memberName = (TextView) layout.findViewById(R.id.team_info_member_name);
        if (index == 0) {
            // team captain
            memberName.setText(members.get(index).getUsername() + CAPTAIN_SUFFIX);
        } else {
            // team member
            memberName.setText(members.get(index).getUsername());
        }

        return layout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.team_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    class LeaveListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // update user's team info
            List<String> teamsJoined = ParseUser.getCurrentUser().getList("teamsJoined");
            teamsJoined.remove(team.getObjectId());
            ParseUser.getCurrentUser().put("teamsJoined", teamsJoined);
            ParseUser.getCurrentUser().saveInBackground();
            try {
                ParseUser.getCurrentUser().fetch();
            } catch (ParseException userException) {
            }

            // update team's member info
            membersIDs.remove(ParseUser.getCurrentUser().getObjectId());
            if (membersIDs.size() == 0) {
                // delete team if no members
                team.deleteInBackground();
            } else {
                if (team.getLeaderID() == ParseUser.getCurrentUser().getObjectId()) {
                    // appoint a new captain
                    team.setLeaderID(membersIDs.get(0));
                }
                team.setMembers(membersIDs);
            }
            team.saveInBackground();

            Toast.makeText(getApplicationContext(), getString(R.string.successfully_leave_team), Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }

    private void exceptionHandler(ParseException e) {
        switch (e.getCode()) {
            case ParseException.INTERNAL_SERVER_ERROR:
                Toast.makeText(this, getString(R.string.error_internal_server), Toast.LENGTH_LONG).show();
                break;

            case ParseException.CONNECTION_FAILED:
                Toast.makeText(this, getString(R.string.error_connection_failed), Toast.LENGTH_LONG).show();
                break;

            case ParseException.TIMEOUT:
                Toast.makeText(this, getString(R.string.error_timeout), Toast.LENGTH_LONG).show();
                break;

            default:
                Toast.makeText(this, getString(R.string.error_general), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
