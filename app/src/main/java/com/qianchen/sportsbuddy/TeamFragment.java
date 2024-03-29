package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TeamFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TeamFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * Created by Qian Chen on 7/29/2014.
 */
public class TeamFragment extends Fragment {

    public static final int CREATE_TEAM_REQUEST_CODE = 28;
    public static final int DISCOVER_TEAM_REQUEST_CODE = 428;
    public static final int TEAM_INFO_REQUEST_CODE = 117;
    private OnFragmentInteractionListener mListener;
    public static List<Team> teamList;
    private TeamAdapter teamAdapter;
    private ListView listView;

    public static TeamFragment newInstance(String param1, String param2) {
        TeamFragment fragment = new TeamFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public TeamFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // register Team as the subclass of ParseObject
        ParseObject.registerSubclass(Team.class);

        // authenticates this client to Parse
        Parse.initialize(getActivity(), getString(R.string.application_id), getString(R.string.client_key));

        setHasOptionsMenu(true);

        teamList = new ArrayList<Team>();
        List<String> teamIdList = ParseUser.getCurrentUser().getList("teamsJoined");
        if (teamIdList != null) {
            for (String teamId : teamIdList) {
                ParseQuery<Team> query = ParseQuery.getQuery("Team");
                // try to load from the cache; but if that fails, load results from the network
                query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                try {
                    teamList.add(query.get(teamId));
                } catch (ParseException e) {
                    exceptionHandler(e);
                }
            }
        }
        teamAdapter = new TeamAdapter(getActivity(), teamList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);
        // get a reference to the ListView, and attach this adapter to it
        listView = (ListView) view.findViewById(R.id.listview_team);
        listView.setAdapter(teamAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), TeamInfoActivity.class);
                intent.putExtra("teamID", teamList.get(position).getObjectId());
                startActivityForResult(intent, TEAM_INFO_REQUEST_CODE);
            }
        });
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.team_new:
                startActivityForResult(new Intent(getActivity(), NewTeamActivity.class), CREATE_TEAM_REQUEST_CODE);
                break;

            case R.id.team_discover:
                startActivityForResult(new Intent(getActivity(), DiscoverTeamActivity.class), DISCOVER_TEAM_REQUEST_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == CREATE_TEAM_REQUEST_CODE || requestCode == DISCOVER_TEAM_REQUEST_CODE) && resultCode == getActivity().RESULT_OK) {
            String teamId = data.getStringExtra("teamId");
            ParseQuery<Team> query = ParseQuery.getQuery("Team");
            // try to load from the cache; but if that fails, load results from the network
            query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            try {
                teamList.add(query.get(teamId));
            } catch (ParseException e) {
                exceptionHandler(e);
            }
        }
        if (requestCode == TEAM_INFO_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            // delete the team the user just left
            String teamId = data.getStringExtra("teamId");
            for (int i = 0; i < teamList.size(); ++i) {
                if (teamList.get(i).getObjectId().equals(teamId)) {
                    teamList.remove(i);
                    ParseQuery<Team> teamQuery = ParseQuery.getQuery("Team");
                    try {
                        EventFragment.teamsJoined.remove(teamQuery.get(teamId).getName());
                    } catch (ParseException e) {
                    }
                    break;
                }
            }
        }
        teamAdapter.notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    private void exceptionHandler(ParseException e) {
        switch (e.getCode()) {
            case ParseException.INTERNAL_SERVER_ERROR:
                Toast.makeText(getActivity(), getString(R.string.error_internal_server), Toast.LENGTH_LONG).show();
                break;

            case ParseException.CONNECTION_FAILED:
                Toast.makeText(getActivity(), getString(R.string.error_connection_failed), Toast.LENGTH_LONG).show();
                break;

            case ParseException.TIMEOUT:
                Toast.makeText(getActivity(), getString(R.string.error_timeout), Toast.LENGTH_LONG).show();
                break;

            default:
                Toast.makeText(getActivity(), getString(R.string.error_general), Toast.LENGTH_LONG).show();
                break;
        }
    }
}
