package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NewEventActivity extends Activity {

    public static final int REQUEST_CODE = 75;
    public static final int OFFSET_MILLIS = 1000;

    private NoDefaultSpinner sportTypeSpinner;
    private NoDefaultSpinner visibilitySpinner;
    private CalendarView calendarView;
    private TimePicker timePicker;
    private EditText editMaxPeople;
    private EditText editNotes;
    private LatLng latLng;
    private String addressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        // register Event as the subclass of ParseObject
        ParseObject.registerSubclass(Event.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // hide the soft keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // get references of views
        calendarView = (CalendarView) findViewById(R.id.calendar_view);
        timePicker = (TimePicker) findViewById(R.id.time_picker);
        editMaxPeople = (EditText) findViewById(R.id.edit_max_people);
        editNotes = (EditText) findViewById(R.id.edit_notes);

        // set the minimum date as today in the calendar view
        calendarView.setMinDate(Calendar.getInstance().getTime().getTime() - OFFSET_MILLIS);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Log.e("zzz", dateFormat.format(Calendar.getInstance().getTime().getTime() - OFFSET_MILLIS));
        // Workaround for CalendarView bug relating to setMinDate():
        // https://code.google.com/p/android/issues/detail?id=42750
        // Set then reset the date on the calendar so that it properly
        // shows today's date. The choice of 24 months is arbitrary.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (calendarView != null) {
                Calendar date = Calendar.getInstance();
                date.add(Calendar.MONTH, 24);
                calendarView.setDate(date.getTimeInMillis(), false, true);
                date.add(Calendar.MONTH, -24);
                calendarView.setDate(date.getTimeInMillis(), false, true);
            }
        }


        sportTypeSpinner = (NoDefaultSpinner) findViewById(R.id.spinner_sport_type);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> sportTypeAdapter = ArrayAdapter.createFromResource(this, R.array.sport_type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        sportTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sportTypeSpinner.setAdapter(sportTypeAdapter);

        visibilitySpinner = (NoDefaultSpinner) findViewById(R.id.spinner_visibility);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> visibilityAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item);
        visibilityAdapter.add("Public");
        // Specify the layout to use when the list of choices appears
        visibilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        visibilitySpinner.setAdapter(visibilityAdapter);

        // register a touch listener to the layout
        ((RelativeLayout) findViewById(R.id.relative_layout)).setOnTouchListener(new TouchListener());

        // set "Choose Location" button listener
        ((Button) findViewById(R.id.button_location)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), NewEventMapActivity.class), REQUEST_CODE);
            }
        });

        // set "Cancel" button listener
        ((Button) findViewById(R.id.button_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // set "Confirm" button listener
        ((Button) findViewById(R.id.button_confirm)).setOnClickListener(new ConfirmListener());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            latLng = data.getParcelableExtra("locationLatLng");
            addressText = data.getStringExtra("addressText");
            ((Button) findViewById(R.id.button_location)).setText(addressText);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    private class TouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            // hide the soft keypad when the background is touched
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            return true;
        }
    }

    private class ConfirmListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // get sport type
            if (sportTypeSpinner.getSelectedItem() == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_empty_sport_type), Toast.LENGTH_SHORT).show();
                return;
            }
            String sportType = sportTypeSpinner.getSelectedItem().toString();

            // get date
            long dateMilliseconds = calendarView.getDate();

            // get time
            int hour = timePicker.getCurrentHour();
            int minute = timePicker.getCurrentMinute();

            // get location
            if (latLng == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_empty_location), Toast.LENGTH_SHORT).show();
                return;
            }

            // get max people
            String maxPeople = editMaxPeople.getText().toString();
            if (maxPeople == null || maxPeople.length() == 0) {
                editMaxPeople.setError(getString(R.string.error_empty_max_people));
                editMaxPeople.requestFocus();
                return;
            }

            // get visibility
            if (visibilitySpinner.getSelectedItem() == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_empty_visibility), Toast.LENGTH_SHORT).show();
                return;
            }
            String visibility = visibilitySpinner.getSelectedItem().toString();

            // get notes
            String notes = editNotes.getText().toString();

            // upload event data to Parse.com
            Event event = new Event();
            event.setSportType(sportType);
            event.setDateMilliseconds(dateMilliseconds);
            event.setHour(hour);
            event.setMinute(minute);
            event.setLatitude(latLng.latitude);
            event.setLongitude(latLng.longitude);
            event.setAddressText(addressText);
            event.setMaxPeople(Integer.parseInt(maxPeople));
            event.setCurrentPeople(1);
            event.setVisibility(visibility);
            event.setNotes(notes);
            event.addParticipant(ParseUser.getCurrentUser().getObjectId());
            event.saveInBackground(new SaveEventCallback(event));
        }
    }

    private class SaveEventCallback extends SaveCallback {
        Event event;

        public SaveEventCallback(Event event) {
            this.event = event;
        }

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

                Toast.makeText(getApplicationContext(), getString(R.string.event_created_successfully), Toast.LENGTH_SHORT).show();
            } else {
                switch (e.getCode()) {
                    case ParseException.INTERNAL_SERVER_ERROR:
                        Toast.makeText(getApplicationContext(), getString(R.string.error_internal_server), Toast.LENGTH_LONG).show();
                        break;

                    case ParseException.CONNECTION_FAILED:
                        Toast.makeText(getApplicationContext(), getString(R.string.error_connection_failed), Toast.LENGTH_LONG).show();
                        break;

                    case ParseException.TIMEOUT:
                        Toast.makeText(getApplicationContext(), getString(R.string.error_timeout), Toast.LENGTH_LONG).show();
                        break;

                    default:
                        Toast.makeText(getApplicationContext(), getString(R.string.error_general), Toast.LENGTH_LONG).show();
                        break;
                }
            }
            finish();
        }
    }
}
