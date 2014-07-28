package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static View view;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private GoogleMap map;
    private LocationManager locationManager;
    private OnFragmentInteractionListener mListener;
    private CameraPosition lastPosition;

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
                            map.addMarker(new MarkerOptions().position(new LatLng(event.getLatitude(), event.getLongitude()))).showInfoWindow();
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
