package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class AddReplyActivity extends Activity {

    private EditText editReply;
    private String postID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reply);

        // register DiscussionPost as the subclass of ParseObject
        ParseObject.registerSubclass(DiscussionPost.class);

        // register DiscussionReply as the subclass of ParseObject
        ParseObject.registerSubclass(DiscussionReply.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // hide the soft keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // register a touch listener to the layout
        ((RelativeLayout) findViewById(R.id.layout_add_reply)).setOnTouchListener(new TouchListener());

        // set "Reply" button listener
        ((Button) findViewById(R.id.button_reply)).setOnClickListener(new ReplyListener());

        // set "Cancel" button listener
        ((Button) findViewById(R.id.button_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        // get references of views
        editReply = (EditText) findViewById(R.id.edit_reply);

        // get post ID
        postID = getIntent().getStringExtra("postID");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_reply, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    class ReplyListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // hide the soft keyboard when the button is clicked
            hideSoftInput();

            // get reply message
            String replyMessage = editReply.getText().toString();
            if (replyMessage == null || replyMessage.length() == 0) {
                // notify user to add reply
                editReply.setError(getString(R.string.error_empty_reply));
                editReply.requestFocus();
                return;
            }

            // upload reply to Parse
            DiscussionReply reply = new DiscussionReply();
            reply.setPostID(postID);
            reply.setUserName(ParseUser.getCurrentUser().getUsername());
            reply.setUserID(ParseUser.getCurrentUser().getObjectId());
            reply.setReplyMessage(replyMessage);
            reply.saveInBackground();

            // update post info
            ParseQuery<DiscussionPost> postQuery = ParseQuery.getQuery("DiscussionPost");
            // try to load from the cache; but if that fails, load results from the network
            postQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            try {
                DiscussionPost post = postQuery.get(postID);
                post.addReply(reply.getObjectId());
                post.saveInBackground();
            } catch (ParseException e) {
            }

            Toast.makeText(getApplicationContext(), getString(R.string.reply_successfully), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("replyID", reply.getObjectId());
            setResult(RESULT_OK, intent);
            finish();
        }
    }


    class TouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            // hide the soft keyboard when the background is touched
            hideSoftInput();
            return true;
        }
    }

    private void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}
