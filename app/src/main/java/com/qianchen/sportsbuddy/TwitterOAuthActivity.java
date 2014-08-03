package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

public class TwitterOAuthActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        RequestToken requestToken = (RequestToken) getIntent().getSerializableExtra("requestToken");
        if (requestToken == null) {
            // already got Twitter OAuth
            // parse Twitter OAuth verification
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(ProfileFragment.CALLBACK_URL)) {
                String verifier = uri.getQueryParameter("oauth_verifier");
                try {
                    TwitterException exception = new GetAccessTokenTask().execute(verifier).get();
                    if (exception != null) {
                        throw exception;
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Fail to authenticate!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // set Twitter OAuth access token
                MainActivity.twitter.setOAuthAccessToken(MainActivity.accessToken);

                // save Twitter OAuth access token
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("token", MainActivity.accessToken.getToken());
                editor.putString("tokenSecret", MainActivity.accessToken.getTokenSecret());
                editor.commit();

                Toast.makeText(this, getString(R.string.successfully_link_twitter), Toast.LENGTH_LONG).show();

                // change the "Link Twitter" button to "Unlink Twitter"
                ProfileFragment.twitterButton.setText(getString(R.string.profile_unlink_twitter));

                startActivity(new Intent(this, MainActivity.class));
            }
        } else {
            // need to get Twitter OAuth
            WebView webView = new WebView(this);
            setContentView(webView);
            webView.loadUrl(requestToken.getAuthorizationURL());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.twitter_oauth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    // asynchronous task to get Twitter OAuth access token
    class GetAccessTokenTask extends AsyncTask<String, Void, TwitterException> {

        private final String ERROR_MESSAGE = "Fail to authenticate!";

        @Override
        protected TwitterException doInBackground(String... params) {
            TwitterException exception = null;
            try {
                MainActivity.accessToken = MainActivity.twitter.getOAuthAccessToken(params[0]);
            } catch (TwitterException e) {
                exception = e;
            }
            return exception;
        }
    }
}
