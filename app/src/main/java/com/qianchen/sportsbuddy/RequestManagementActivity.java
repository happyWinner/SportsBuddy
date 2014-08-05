package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * The activity for team captain to manage applications
 *
 * Created by Qian Chen on 7/29/2014.
 */
public class RequestManagementActivity extends Activity {

    private List<Request> requestList;
    private RequestAdapter requestsAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_management);

        // register Team as the subclass of ParseObject
        ParseObject.registerSubclass(Team.class);

        // register Request as the subclass of ParseObject
        ParseObject.registerSubclass(Request.class);

        // register ApprovedRequest as the subclass of ParseObject
        ParseObject.registerSubclass(ApprovedRequest.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // get requests
        ParseQuery<Request> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("teamID", getIntent().getStringExtra("teamId"));
        try {
            requestList = query.find();
        } catch (ParseException e) {
            requestList = new ArrayList<Request>();
        }
        requestsAdapter = new RequestAdapter(this, requestList);

        // get a reference to the ListView, and attach this adapter to it
        listView = (ListView) findViewById(R.id.listview_request);
        listView.setAdapter(requestsAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.request_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId() == R.id.manage_requests_done) {
            setResult(RESULT_OK);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
