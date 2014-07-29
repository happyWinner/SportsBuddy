package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


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

    private static View view;

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

        // authenticates this client to Parse
        Parse.initialize(getActivity(), getString(R.string.application_id), getString(R.string.client_key));

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
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
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                            if (!eventIDHashSet.contains(event.getObjectId()) && event.getMaxPeople() > event.getCurrentPeople()) {
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
            event.increment("currentPeople");
            event.addParticipant(ParseUser.getCurrentUser());
            event.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(getActivity(), getString(R.string.event_join_successfully), Toast.LENGTH_LONG).show();

                        // clear the mark on the map if event has enough participants
                        if (event.getCurrentPeople() == event.getMaxPeople()) {
                            eventIDHashSet.remove(event.getObjectId());
                            markerEventHashMap.remove(marker);
                            marker.remove();
                        }

                        // dismiss the popup window
                        popupWindow.dismiss();
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

}
