package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
 */
public class TeamFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private List<Team> teamList;
    private TeamAdapter teamAdapter;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        teamList = new ArrayList<Team>();
        ParseUser user = ParseUser.getCurrentUser();
        System.out.println(user.getEmail());
        List<String> teamIdList = user.getList("teamsJoined");//ParseUser.getCurrentUser().getList("teamsJoined");
        if (teamIdList != null) {
            for (String teamId : teamIdList) {
                ParseQuery<Team> query = ParseQuery.getQuery("Team");
                try {
                    teamList.add(query.get(teamId));
                } catch (ParseException e) {
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
        }
        teamAdapter = new TeamAdapter(getActivity(), teamList);

        // inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_team, container, false);

        // get a reference to the ListView, and attach this adapter to it
        ListView listView = (ListView) rootView.findViewById(R.id.listview_team);
        listView.setAdapter(teamAdapter);

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
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
        // todo
        teamAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
