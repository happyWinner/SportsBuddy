package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class TeamInfoActivity extends Activity {

    private Team team;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_info);

        // register Team as the subclass of ParseObject
        ParseObject.registerSubclass(Team.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        ParseQuery<Team> query = ParseQuery.getQuery("Team");
        // try to load from the cache; but if that fails, load results from the network
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        try {
            team = query.get(getIntent().getStringExtra("teamID"));
        } catch (ParseException e) {
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
            finish();
        }
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
}
