package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class NewEventMapActivity extends Activity {

    public static final String LOG_TAG = NewEventMapActivity.class.getSimpleName();

    private SearchView searchView;
    private LocationManager locationManager;
    private GoogleMap map;
    private LatLng locationLatLng;
    private String addressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event_map);

        // get Google Map reference
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMyLocationEnabled(true);
        map.setOnMapClickListener(new MapClickListener());

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // set "Cancel" button listener
        ((Button) findViewById(R.id.button_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // set "Confirm" button listener
        ((Button) findViewById(R.id.button_confirm)).setOnClickListener(new ConfirmListener());

        // set search view listener
        searchView = ((SearchView) findViewById(R.id.search_view));
        searchView.setOnQueryTextListener(new SearchListener());
    }

    private class SearchListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            // hide keyboard
            searchView.clearFocus();

            if (query == null || query.length() == 0) {
                return false;
            }
            new GeocoderTask().execute(query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }

    private class ConfirmListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (locationLatLng == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_location_selected), Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent();
                intent.putExtra("locationLatLng", locationLatLng);
                intent.putExtra("addressText", addressText);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
        if (location == null) {
            Toast.makeText(this, getString(R.string.error_fail_to_get_location), Toast.LENGTH_SHORT).show();
        } else {
            // set the centre of the map to the user's position
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(16).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_event_map, menu);
        return true;
    }

    class MapClickListener implements GoogleMap.OnMapClickListener {
        @Override
        public void onMapClick(LatLng latLng) {
            // clears all the existing markers on the map
            map.clear();

            Geocoder geocoder = new Geocoder(getApplication());
            Address address = null;
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses == null || addresses.size() == 0) {
                    throw new IOException();
                }
                address = addresses.get(0);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            addressText = String.format("%s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getAdminArea() != null ? address.getAdminArea() : address.getCountryName());

            locationLatLng = latLng;

            map.addMarker(new MarkerOptions().position(latLng).title(addressText)).showInfoWindow();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {
        @Override
        protected List<Address> doInBackground(String... locationName) {
            // creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if(addresses == null || addresses.size() == 0){
                Toast.makeText(getApplicationContext(), getString(R.string.error_no_location_found), Toast.LENGTH_SHORT).show();
            }

            // clears all the existing markers on the map
            map.clear();

            // adding Markers on Google Map for each matching address
            for(int i = 0; i < addresses.size(); ++i) {
                Address address = (Address) addresses.get(i);

                // creating an instance of GeoPoint, to display in Google Map
                locationLatLng = new LatLng(address.getLatitude(), address.getLongitude());

                addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getAdminArea() != null ? address.getAdminArea() : address.getCountryName());

                map.addMarker(new MarkerOptions().position(locationLatLng).title(addressText)).showInfoWindow();

                // locate the first location
                if (i == 0) {
                    map.animateCamera(CameraUpdateFactory.newLatLng(locationLatLng));
                }
            }
        }
    }
}
