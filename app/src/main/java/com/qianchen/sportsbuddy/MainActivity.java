package com.qianchen.sportsbuddy;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Locale;

public class MainActivity extends FragmentActivity implements ActionBar.TabListener,
        EventFragment.OnFragmentInteractionListener, TeamFragment.OnFragmentInteractionListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // restore cached user account if there is one
        ParseUser currentUser = null;
        try {
            currentUser = ParseUser.getCurrentUser().fetch();
        } catch (ParseException e) {
        }
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        // dynamically inflate the options menu
        // TODO
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
                case 4:
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
