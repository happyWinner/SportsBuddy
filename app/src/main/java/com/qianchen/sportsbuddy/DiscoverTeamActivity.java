package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;

public class DiscoverTeamActivity extends Activity {

    private SearchView searchView;
    private GridView gridView;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover_team);

        // get references of views
        searchView = (SearchView) findViewById(R.id.discover_team_searchview);
        gridView = (GridView) findViewById(R.id.discover_team_gridview);
        listView = (ListView) findViewById(R.id.discover_team_listview);

        
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
}
