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

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DiscussionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DiscussionFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class DiscussionFragment extends Fragment {

    public static final int REQUEST_CODE = 963;
    private ListView listView;
    private DiscussionAdapter discussionAdapter;
    private List<DiscussionPost> discussionPostList;

    private OnFragmentInteractionListener mListener;

    public static DiscussionFragment newInstance(String param1, String param2) {
        DiscussionFragment fragment = new DiscussionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public DiscussionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // register DiscussionPost as the subclass of ParseObject
        ParseObject.registerSubclass(DiscussionPost.class);

        // authenticates this client to Parse
        Parse.initialize(getActivity(), getString(R.string.application_id), getString(R.string.client_key));

        setHasOptionsMenu(true);

        ParseQuery<DiscussionPost> postQuery = ParseQuery.getQuery("DiscussionPost");
        // try to load from the cache; but if that fails, load results from the network
        postQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        postQuery.addDescendingOrder("createdAt");
        try {
            discussionPostList = postQuery.find();
        } catch (com.parse.ParseException e) {
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
            }
            return;
        }
        discussionAdapter = new DiscussionAdapter(getActivity(), discussionPostList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discussion, container, false);
        listView = (ListView) view.findViewById(R.id.listview_discussion);
        listView.setAdapter(discussionAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), ViewPostActivity.class);
                intent.putExtra("postID", discussionPostList.get(position).getObjectId());
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.menu.new_discussion) {
            //todo update discussion list!
            startActivity(new Intent(getActivity(), NewDiscussionActivity.class));
        }
        return super.onOptionsItemSelected(item);
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