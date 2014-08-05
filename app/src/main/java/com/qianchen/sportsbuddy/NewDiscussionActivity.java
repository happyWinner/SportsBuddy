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
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * The activity for users to write a new post on discussion board
 *
 * Created by Qian Chen on 7/30/2014.
 */
public class NewDiscussionActivity extends Activity {

    private EditText editPostTitle;
    private EditText editPostContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_discussion);

        // register DiscussionPost as the subclass of ParseObject
        ParseObject.registerSubclass(DiscussionPost.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // hide the soft keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // get references of all views
        editPostTitle = (EditText) findViewById(R.id.edit_post_title);
        editPostContent = (EditText) findViewById(R.id.edit_post_content);

        // register a touch listener to the layout
        ((RelativeLayout) findViewById(R.id.layout_new_discussion)).setOnTouchListener(new TouchListener());

        // set "Post" button listener
        ((Button) findViewById(R.id.button_post)).setOnClickListener(new PostListener());

        // set "Cancel" button listener
        ((Button) findViewById(R.id.button_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_discussion, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    class PostListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String postTitle = editPostTitle.getText().toString();
            if (postTitle == null || postTitle.length() == 0) {
                editPostTitle.setError(getString(R.string.error_empty_title));
                editPostTitle.requestFocus();
                return;
            }

            String postContent = editPostContent.getText().toString();
            if (postContent == null || postContent.length() == 0) {
                editPostContent.setError(getString(R.string.error_empty_content));
                editPostContent.requestFocus();
                return;
            }

            DiscussionPost post = new DiscussionPost();
            post.setAuthor(ParseUser.getCurrentUser().getUsername());
            post.setAuthorID(ParseUser.getCurrentUser().getObjectId());
            post.setTitle(postTitle);
            post.setContent(postContent);
            post.setReplies(new ArrayList<String>());
            try {
                post.save();
            } catch (ParseException e) {
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
                }
                return;
            }

            Toast.makeText(getApplicationContext(), getString(R.string.successfully_post), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("postID", post.getObjectId());
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
