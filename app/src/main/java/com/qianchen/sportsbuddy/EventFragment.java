package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import twitter4j.TwitterException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EventFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EventFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class EventFragment extends Fragment {

    public static final int MILLISECONDS_PER_HOUR = 3600000;
    public static final int MILLISECONDS_PER_MINUTE = 60000;
    public static final String TWEET_PREFIX = "Join me to play ";
    public static final String TWEET_SUFFIX = " via SportsBuddy";
    public static final int NEW_EVENT_REQUEST_CODE = 1234;

    private static View view;
    public static HashSet<String> teamsJoined;

    private GoogleMap map;
    private LocationManager locationManager;
    private OnFragmentInteractionListener mListener;
    private CameraPosition lastPosition;
    private HashMap<Marker, Event> markerEventHashMap;
    private HashSet<String> eventIDHashSet;
    private SimpleDateFormat simpleDateFormat;

    public static EventFragment newInstance(String param1, String param2) {
        EventFragment fragment = new EventFragment();
        return fragment;
    }

    public EventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // register Event as the subclass of ParseObject
        ParseObject.registerSubclass(Event.class);

        // register Team as the subclass of ParseObject
        ParseObject.registerSubclass(Team.class);

        // authenticates this client to Parse
        Parse.initialize(getActivity(), getString(R.string.application_id), getString(R.string.client_key));

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        try {
            // inflate the layout for this fragment
            view = inflater.inflate(R.layout.fragment_event, container, false);
        } catch (InflateException e) {
            // map is already there, just return view as it is
        }

        // set Google Map parameters
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.setOnCameraChangeListener(new CameraListener());
        map.setOnInfoWindowClickListener(new InfoWindowListener());

        // restore previous data
        if (savedInstanceState != null) {
            lastPosition = savedInstanceState.getParcelable("lastPosition");
        } else {
            lastPosition = null;
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        map.clear();
        markerEventHashMap = new HashMap<Marker, Event>();
        eventIDHashSet = new HashSet<String>();
        simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd EEE HH:mm");

        if (lastPosition == null) {
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
            if (location == null) {
                Toast.makeText(getActivity(), getString(R.string.error_fail_to_get_location), Toast.LENGTH_SHORT).show();
            } else {
                // set the centre of the map to the user's position
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(16).build();
                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        } else {
            // move camera to the last position
            map.moveCamera(CameraUpdateFactory.newCameraPosition(lastPosition));
        }

        new CameraListener().onCameraChange(map.getCameraPosition());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("lastPosition", lastPosition);
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

        teamsJoined = new HashSet<String>();
        List<String> teamIDs = ParseUser.getCurrentUser().getList("teamsJoined");
        if (teamIDs != null) {
            for (String teamID : teamIDs) {
                ParseQuery<Team> teamQuery = ParseQuery.getQuery("Team");
                try {
                    teamsJoined.add(teamQuery.get(teamID).getName());
                } catch (ParseException e) {
                }
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==  R.id.event_new) {
            startActivityForResult(new Intent(getActivity(), NewEventActivity.class), NEW_EVENT_REQUEST_CODE);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_EVENT_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            ParseQuery<Event> eventQuery = ParseQuery.getQuery("Event");
            eventQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            try {
                shareOnTwitter(eventQuery.get(data.getStringExtra("eventID")), getActivity());
            } catch (ParseException e) {
            }
        }
    }

    class CameraListener implements GoogleMap.OnCameraChangeListener {

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            // save position every time camera changes
            lastPosition = cameraPosition;

            // query all events in the visible region
            VisibleRegion visibleRegion = map.getProjection().getVisibleRegion();
            double left = visibleRegion.latLngBounds.southwest.longitude;
            double top = visibleRegion.latLngBounds.northeast.latitude;
            double right = visibleRegion.latLngBounds.northeast.longitude;
            double bottom = visibleRegion.latLngBounds.southwest.latitude;

            // show the events on Google Map
            ParseQuery<Event> query = ParseQuery.getQuery(Event.class.getSimpleName());
            query.whereLessThan("latitude", top);
            query.whereGreaterThan("latitude", bottom);
            query.whereLessThan("longitude", right);
            query.whereGreaterThan("longitude", left);
            query.findInBackground(new FindCallback<Event>() {
                @Override
                public void done(List<Event> events, ParseException e) {
                    if (e == null) {
                        for (Event event : events) {
                            if (!eventIDHashSet.contains(event.getObjectId()) && event.getMaxPeople() > event.getCurrentPeople()
                                    && (teamsJoined.contains(event.getVisibility()) || event.getVisibility().equals("Public"))) {
                                eventIDHashSet.add(event.getObjectId());
                                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(event.getLatitude(), event.getLongitude()));
                                long dateMilliseconds = event.getDateMilliseconds() + event.getHour() * MILLISECONDS_PER_HOUR + event.getMinute() * MILLISECONDS_PER_MINUTE;
                                markerOptions.title(event.getSportType()).snippet(simpleDateFormat.format(new Date(dateMilliseconds)));
                                markerEventHashMap.put(map.addMarker(markerOptions), event);
                            }
                        }
                    }
                }
            });
        }
    }

    class InfoWindowListener implements GoogleMap.OnInfoWindowClickListener {

        public static final int POPUP_WINDOW_HEIGHT = 800;

        @Override
        public void onInfoWindowClick(Marker marker) {
            Event event = markerEventHashMap.get(marker);
            View popupView = ((LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_event, null, false);
            PopupWindow popupWindow = new PopupWindow(popupView);
            ((TextView) popupView.findViewById(R.id.value_sport_type)).setText(event.getSportType());
            long dateMilliseconds = event.getDateMilliseconds() + event.getHour() * MILLISECONDS_PER_HOUR + event.getMinute() * MILLISECONDS_PER_MINUTE;
            ((TextView) popupView.findViewById(R.id.value_time)).setText(simpleDateFormat.format(new Date(dateMilliseconds)));
            ((TextView) popupView.findViewById(R.id.value_location)).setText(event.getAddressText());
            ((TextView) popupView.findViewById(R.id.value_max_people)).setText(String.valueOf(event.getMaxPeople()));
            ((TextView) popupView.findViewById(R.id.value_current_people)).setText(String.valueOf(event.getCurrentPeople()));
            ((TextView) popupView.findViewById(R.id.value_visibility)).setText(event.getVisibility());
            ((TextView) popupView.findViewById(R.id.value_notes)).setText(event.getNotes());
            ((Button) popupView.findViewById(R.id.button_join)).setOnClickListener(new JoinListener(marker, event, popupWindow));

            popupWindow.setFocusable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(R.color.black));
            popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
            popupWindow.setHeight(POPUP_WINDOW_HEIGHT);
            popupWindow.showAtLocation(getActivity().findViewById(R.id.frame_layout), Gravity.BOTTOM, 0, 0);
        }
    }

    class JoinListener implements View.OnClickListener {
        Marker marker;
        Event event;
        PopupWindow popupWindow;

        JoinListener(Marker marker, Event event, PopupWindow popupWindow) {
            this.marker = marker;
            this.event = event;
            this.popupWindow = popupWindow;
        }

        @Override
        public void onClick(View v) {
            ParseUser user = ParseUser.getCurrentUser();
            List<String> eventList = user.getList("eventsJoined");
            if (eventList != null && eventList.contains(event.getObjectId())) {
                // notify user if s/he has already joined the event
                Toast.makeText(getActivity(), getString(R.string.error_already_joined_event), Toast.LENGTH_LONG).show();
                return;
            }

            event.increment("currentPeople");
            event.addParticipant(ParseUser.getCurrentUser().getObjectId());
            event.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        // update user's event information
                        ParseUser user = ParseUser.getCurrentUser();
                        List<String> eventList = user.getList("eventsJoined");
                        if (eventList == null) {
                            eventList = new ArrayList<String>();
                        }
                        eventList.add(event.getObjectId());
                        user.put("eventsJoined", eventList);
                        user.saveInBackground();
                        try {
                            ParseUser.getCurrentUser().fetch();
                        } catch (ParseException userException) {
                        }

                        Toast.makeText(getActivity(), getString(R.string.event_join_successfully), Toast.LENGTH_LONG).show();

                        // clear the mark on the map if event has enough participants
                        if (event.getCurrentPeople() == event.getMaxPeople()) {
                            eventIDHashSet.remove(event.getObjectId());
                            markerEventHashMap.remove(marker);
                            marker.remove();
                        }

                        // dismiss the popup window
                        popupWindow.dismiss();

                        // todo: update event participants
                        // ask user whether to share the event on Twitter
                        if (MainActivity.accessToken != null) {
                            shareOnTwitter(event, getActivity());
                        }

                    } else {
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
            });
        }
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


    public void shareOnTwitter(final Event event, final Context context) {
        if (MainActivity.accessToken == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(context.getString(R.string.dialog_share_message)).setTitle(context.getString(R.string.dialog_share_title));

        builder.setPositiveButton(context.getString(R.string.dialog_share_button), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm, MMM dd");
                StringBuffer message = new StringBuffer();

                message.append(TWEET_PREFIX);
                message.append(event.getSportType());
                message.append(" in ");
                message.append(event.getAddressText().split(",")[0]);
                message.append(" at ");
                long dateMilliseconds = event.getDateMilliseconds() + event.getHour() * MILLISECONDS_PER_HOUR + event.getMinute() * MILLISECONDS_PER_MINUTE;
                message.append(simpleDateFormat.format(new Date(dateMilliseconds)));
                message.append(TWEET_SUFFIX);

                // start tweet
                new TweetTask(context).execute(message.toString());
            }
        });
        builder.setNegativeButton(context.getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // show the alert dialog
        builder.create().show();
    }

    // asynchronous task to tweet message
    class TweetTask extends AsyncTask<String, Void, Exception> {

        private final String SUCCEED_MESSAGE = "Shared on Twitter!";
        private final String ERROR_MESSAGE = "Failed to share on Twitter!";

        private Context context;

        public TweetTask(Context context) {
            this.context = context;
        }

        @Override
        protected TwitterException doInBackground(String... tweetMessage) {
            try {
                MainActivity.twitter.updateStatus(tweetMessage[0]);
            } catch (TwitterException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);
            if (e == null) {
                Toast.makeText(context, SUCCEED_MESSAGE, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
            }
        }
    }
}