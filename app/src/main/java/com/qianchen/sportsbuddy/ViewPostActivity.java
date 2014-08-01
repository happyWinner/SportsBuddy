package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.LinkedList;
import java.util.List;

public class ViewPostActivity extends Activity {

    private ParseImageView postAvatar;
    private TextView postAuthor;
    private TextView postTitle;
    private DiscussionPost post;
    private TextView postContent;
    private ListView replyListView;
    private List<DiscussionReply> replyList;
    private ReplyAdapter replyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        // register DiscussionPost as the subclass of ParseObject
        ParseObject.registerSubclass(DiscussionPost.class);

        // register DiscussionReply as the subclass of ParseObject
        ParseObject.registerSubclass(DiscussionReply.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // get references of views
        postAvatar = (ParseImageView) findViewById(R.id.post_avatar);
        postAuthor = (TextView) findViewById(R.id.post_author);
        postTitle = (TextView) findViewById(R.id.post_title);
        postContent = (TextView) findViewById(R.id.post_content);
        replyListView = (ListView) findViewById(R.id.listview_reply);

        ParseQuery postQuery = ParseQuery.getQuery("DiscussionPost");
        try {
            post = (DiscussionPost) postQuery.get(getIntent().getStringExtra("postID"));
        } catch (ParseException e) {
            exceptionHandler(e);
            finish();
        }

        postAvatar.setParseFile(getAvatarByName(post.getAuthor()));
        postAvatar.loadInBackground();
        postAuthor.setText(post.getAuthor());
        postTitle.setText(post.getTitle());
        postContent.setText(post.getContent());

        replyList = new LinkedList<DiscussionReply>();
        List<String> replyIDs = post.getReplies();
        if (replyIDs != null) {
            for (String replyID : replyIDs) {
                ParseQuery<DiscussionReply> replyQuery = ParseQuery.getQuery("DiscussionReply");
                // try to load from the cache; but if that fails, load results from the network
                replyQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                try {
                    replyList.add(0, replyQuery.get(replyID));
                } catch (ParseException e) {
                    exceptionHandler(e);
                    finish();
                }
            }
        }
        replyAdapter = new ReplyAdapter(this, replyList);
        replyListView.setAdapter(replyAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.view_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    private ParseFile getAvatarByName(String name) {
        ParseQuery query = ParseUser.getQuery();
        query.whereEqualTo("username", name);
        try {
            ParseUser user = (ParseUser) query.getFirst();
            return user.getParseFile("avatar");
        } catch (ParseException e) {
            return null;
        }
    }

    private void exceptionHandler(ParseException e) {
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

            default:
                Toast.makeText(getApplicationContext(), getString(R.string.error_general), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
