package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiscoverTeamActivity extends Activity {

    private SearchView searchView;
    private GridView gridView;
    private ListView listView;
    private List<Team> teamList;
    private TeamDiscoverAdapter teamDiscoverAdapter;
    private String[] teamTypes;
    private HashMap<String, List<Team>> teamTypeMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_team);

        // register Team as the subclass of ParseObject
        ParseObject.registerSubclass(Team.class);

        // register Request as the subclass of ParseObject
        ParseObject.registerSubclass(Request.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // get references of views
        searchView = (SearchView) findViewById(R.id.discover_team_searchview);
        gridView = (GridView) findViewById(R.id.discover_team_gridview);
        listView = (ListView) findViewById(R.id.discover_team_listview);

        // get team info from Parse
        ParseQuery<Team> teamQuery = ParseQuery.getQuery("Team");
        try {
            teamList = teamQuery.find();
        } catch (ParseException e) {
            exceptionHandler(e, "");
            finish();
        }

        teamDiscoverAdapter = new TeamDiscoverAdapter(this, teamList);
        listView.setAdapter(teamDiscoverAdapter);
        listView.setOnItemClickListener(new ListItemClickListener());

        teamTypes = getResources().getStringArray(R.array.discover_team_array);
        ArrayAdapter<String> teamTypeAdapter = new ArrayAdapter<String>(this, R.layout.grid_item_team_type, teamTypes);
        gridView.setAdapter(teamTypeAdapter);
        gridView.setOnItemClickListener(new GridItemClickListener());

        // classify teams by type
        teamTypeMap = new HashMap<String, List<Team>>();
        for (String teamType : teamTypes) {
            teamTypeMap.put(teamType, new ArrayList<Team>());
        }
        for (Team team : teamList) {
            String teamType = team.getSportsType();
            if (teamTypeMap.containsKey(teamType)) {
                teamTypeMap.get(teamType).add(team);
            }
        }
        teamTypeMap.put("All", new ArrayList<Team>(teamList));

        teamList = teamTypeMap.get(teamTypes[teamTypes.length - 1]);
        teamDiscoverAdapter = new TeamDiscoverAdapter(getApplication(), teamList);
        listView.setAdapter(teamDiscoverAdapter);

        // set search view listener
        searchView.setOnQueryTextListener(new SearchListener());
    }

    class ListItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            sendRequest(teamList.get(position));
        }
    }

    class GridItemClickListener implements GridView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            for (int i = 0; i < teamTypes.length; ++i) {
                if (i == position) {
                    gridView.getChildAt(i).setBackground(getResources().getDrawable(R.drawable.grid_textview_selected));
                    ((TextView) gridView.getChildAt(i)).setTextColor(getResources().getColor(R.color.white));
                } else {
                    gridView.getChildAt(i).setBackground(getResources().getDrawable(R.drawable.grid_textview_unselected));
                    ((TextView) gridView.getChildAt(i)).setTextColor(getResources().getColor(R.color.textview_boarder));
                }
            }
            teamList = teamTypeMap.get(teamTypes[position]);
            teamDiscoverAdapter = new TeamDiscoverAdapter(getApplication(), teamList);
            listView.setAdapter(teamDiscoverAdapter);
        }
    }

    class SearchListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            // hide keyboard
            searchView.clearFocus();

            if (query == null || query.length() == 0) {
                return false;
            }

            ParseQuery<Team> parseQuery = new ParseQuery<Team>("Team");
            parseQuery.whereEqualTo("name", query.toUpperCase());
            Team team = null;
            try {
                team = parseQuery.getFirst();
            } catch (ParseException e) {
                exceptionHandler(e, getString(R.string.error_team_not_found));
                return false;
            }

            sendRequest(team);

            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }

    private void sendRequest(Team team) {
        if (ParseUser.getCurrentUser().getList("teamsJoined").contains(team.getObjectId())) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_team_already_joined), Toast.LENGTH_SHORT).show();
            return;
        }

        if (team != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage(getString(R.string.dialog_message)).setTitle(getString(R.string.dialog_title));

            final Team finalTeam = team;
            builder.setPositiveButton(getString(R.string.dialog_button_confirm), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ParseUser user = ParseUser.getCurrentUser();
                    Request request = new Request();
                    request.setUserName(user.getUsername());
                    request.setUserID(user.getObjectId());
                    request.setTeamID(finalTeam.getObjectId());
                    request.saveInBackground();
                    Toast.makeText(getApplicationContext(), getString(R.string.request_sent), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            // show the alert dialog
            builder.create().show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.discover_team, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    private void exceptionHandler(ParseException e, String objNotFoundMessage) {
        switch (e.getCode()) {
            case ParseException.INTERNAL_SERVER_ERROR:
                Toast.makeText(getApplicationContext(), getString(R.string.error_internal_server), Toast.LENGTH_LONG).show();
                break;

            case ParseException.CONNECTION_FAILED:
                Toast.makeText(getApplicationContext(), getString(R.string.error_connection_failed), Toast.LENGTH_LONG).show();
                break;

            case ParseException.TIMEOUT:
                Toast.makeText(getApplicationContext(), getString(R.string.error_timeout), Toast.LENGTH_LONG).show();
                break;

            case ParseException.OBJECT_NOT_FOUND:
                Toast.makeText(getApplicationContext(), objNotFoundMessage, Toast.LENGTH_LONG).show();
                break;

            default:
                Toast.makeText(getApplicationContext(), getString(R.string.error_general), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
