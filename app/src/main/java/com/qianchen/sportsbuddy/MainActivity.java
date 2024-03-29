package com.qianchen.sportsbuddy;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Main activity holding four tabs/fragments.
 *
 * Created by Qian Chen on 7/28/2014.
 */
public class MainActivity extends FragmentActivity implements ActionBar.TabListener,
        EventFragment.OnFragmentInteractionListener, TeamFragment.OnFragmentInteractionListener,
        DiscussionFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener {

    /**
     * Total number of pages.
     */
    final int pageNumber = 4;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    public static Twitter twitter;
    public static AccessToken accessToken;

    private int lastTabPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // register Team as the subclass of ParseObject
        ParseObject.registerSubclass(Team.class);

        // register ApprovedRequest as the subclass of ParseObject
        ParseObject.registerSubclass(ApprovedRequest.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // clear all cached results when the app first launches
        ParseQuery.clearAllCachedResults();

        // restore cached user account if there is one
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            try {
                currentUser = currentUser.fetch();
            } catch (ParseException e) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("email", currentUser.getEmail());
            query.whereEqualTo("emailVerified", true);
            query.getFirstInBackground(new GetCallback<ParseUser>() {
                public void done(ParseUser user, ParseException e) {
                    if (user == null) {
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                }
            });

            // process user's approved requests
            ParseQuery<ApprovedRequest> approvedRequestQuery = ParseQuery.getQuery("ApprovedRequest");
            approvedRequestQuery.whereEqualTo("userID", currentUser.getObjectId());
            try {
                List<String> teamsJoined = currentUser.getList("teamsJoined");
                if (teamsJoined == null) {
                    teamsJoined = new ArrayList<String>();
                }
                List<ApprovedRequest> approvedRequests = approvedRequestQuery.find();
                for (ApprovedRequest approvedRequest : approvedRequests) {
                    teamsJoined.add(approvedRequest.getTeamID());
                    approvedRequest.deleteInBackground();
                }
                currentUser.put("teamsJoined", teamsJoined);
                currentUser.saveInBackground();
            } catch (ParseException e) {
            }
        }

        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setIcon(mSectionsPagerAdapter.getIconID(i))
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }

        if (savedInstanceState != null) {
            lastTabPosition = savedInstanceState.getInt("lastTabPosition");
            onTabSelected(actionBar.getTabAt(lastTabPosition), null);
        }

        // create Twitter4j reference
        ConfigurationBuilder conf = new ConfigurationBuilder();
        conf.setDebugEnabled(true)
                .setOAuthConsumerKey(getString(R.string.consumer_key))
                .setOAuthConsumerSecret(getString(R.string.consumer_secret));
        twitter = new TwitterFactory(conf.build()).getInstance();

        // restore access token if already authorized
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String token = sharedPreferences.getString("token", null);
        String tokenSecret = sharedPreferences.getString("tokenSecret", null);
        if (token != null && tokenSecret != null) {
            accessToken = new AccessToken(token, tokenSecret);
            twitter.setOAuthAccessToken(accessToken);
        } else {
            accessToken = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        // dynamically inflate the options menus
        switch (getActionBar().getSelectedTab().getPosition()) {
            case 0:
                getMenuInflater().inflate(R.menu.event, menu);
                break;

            case 1:
                getMenuInflater().inflate(R.menu.team, menu);
                break;

            case 2:
                getMenuInflater().inflate(R.menu.discussion, menu);
                break;

            case 3:
                getMenuInflater().inflate(R.menu.profile, menu);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // store the last position
        lastTabPosition = tab.getPosition();
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        // refresh the options menu
        invalidateOptionsMenu();
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return pageNumber;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_event).toUpperCase(l);
                case 1:
                    return getString(R.string.title_team).toUpperCase(l);
                case 2:
                    return getString(R.string.title_discussion).toUpperCase(l);
                case 3:
                    return getString(R.string.title_profile).toUpperCase(l);
            }
            return null;
        }

        public int getIconID(int position) {
            switch (position) {
                case 0:
                    return R.drawable.ic_action_event;
                case 1:
                    return R.drawable.ic_action_team;
                case 2:
                    return R.drawable.ic_action_discussion;
                case 3:
                    return R.drawable.ic_action_profile;
            }
            return -1;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static Fragment newInstance(int sectionNumber) {
            Fragment fragment;
            switch (sectionNumber) {
                case 1:
                    fragment = new EventFragment();
                    break;

                case 2:
                    fragment = new TeamFragment();
                break;

                case 3:
                    fragment = new DiscussionFragment();
                    break;

                case 4:
                    fragment = new ProfileFragment();
                    break;

                default:
                    fragment = new PlaceholderFragment();
            }
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}
