package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DiscoverTeamActivity extends Activity {

    private SearchView searchView;
    private GridView gridView;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_team);

        // register Team as the subclass of ParseObject
        ParseObject.registerSubclass(Team.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // get references of views
        searchView = (SearchView) findViewById(R.id.discover_team_searchview);
        gridView = (GridView) findViewById(R.id.discover_team_gridview);
        listView = (ListView) findViewById(R.id.discover_team_listview);

        // get team info from Parse

        // set search view listener
        searchView.setOnQueryTextListener(new SearchListener());

    }

    private class SearchListener implements SearchView.OnQueryTextListener {
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
                if (ParseUser.getCurrentUser().getList("teamsJoined").contains(team.getObjectId())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_team_already_joined), Toast.LENGTH_SHORT).show();
                    return false;
                }
            } catch (ParseException e) {
                exceptionHandler(e, getString(R.string.error_team_not_found));
                return false;
            }
            if (team != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DiscoverTeamActivity.this);

// 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage("Join " + team.getName() + " ?").setTitle("Join");

                final Team finalTeam = team;
                builder.setPositiveButton("join", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        ParseUser user = ParseUser.getCurrentUser();
                        finalTeam.addMember(user.getObjectId());
                        finalTeam.saveInBackground();
                        List<String> teamsJoined = user.getList("teamsJoined");
                        if (teamsJoined == null) {
                            teamsJoined = new ArrayList<String>();
                        }
                        teamsJoined.add(finalTeam.getObjectId());
                        user.put("teamsJoined", teamsJoined);
                        user.saveInBackground();
                        try {
                            user.fetch();
                        } catch (ParseException e1) {
                        }
                        Intent intent = new Intent();
                        intent.putExtra("teamId", finalTeam.getObjectId());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

// 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
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
