package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.google.android.gms.maps.model.LatLng;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * The activity for users to create a new event
 *
 * Created by Qian Chen on 7/28/2014.
 */
public class NewEventActivity extends Activity {

    public static final int REQUEST_CODE = 75;
    public static final int MILLISECONDS_PER_HOUR = 3600000;
    public static final int MILLISECONDS_PER_MINUTE = 60000;

    private NoDefaultSpinner sportTypeSpinner;
    private NoDefaultSpinner visibilitySpinner;
    private CalendarView calendarView;
    private TimePicker timePicker;
    private TextView textMaxPeople;
    private ZoomControls controllerMaxPeople;
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
        textMaxPeople = (TextView) findViewById(R.id.text_max_people);
        controllerMaxPeople = (ZoomControls) findViewById(R.id.control_max_people);
        editNotes = (EditText) findViewById(R.id.edit_notes);

        // set zoom control listener
        controllerMaxPeople.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textMaxPeople.setText(String.valueOf(Integer.parseInt(textMaxPeople.getText().toString()) + 1));
            }
        });
        controllerMaxPeople.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int maxPeople = Integer.parseInt(textMaxPeople.getText().toString()) - 1;
                if (maxPeople < 1) {
                    maxPeople = 1;
                }
                textMaxPeople.setText(String.valueOf(maxPeople));
            }
        });

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
        // get user's team list
        List<String> teams = ParseUser.getCurrentUser().getList("teamsJoined");
        if (teams != null) {
            for (String teamID : teams) {
                ParseQuery<Team> teamQuery = ParseQuery.getQuery("Team");
                teamQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                try {
                    visibilityAdapter.add(teamQuery.get(teamID).getName());
                } catch (ParseException e) {
                }
            }
        }
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
            // hide the soft keyboard
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

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

            // check whether it is a future time
            if (Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis() > dateMilliseconds + hour * MILLISECONDS_PER_HOUR + minute * MILLISECONDS_PER_MINUTE) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_date), Toast.LENGTH_SHORT).show();
                return;
            }

            // get location
            if (latLng == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_empty_location), Toast.LENGTH_SHORT).show();
                return;
            }

            // get max people
            String maxPeople = textMaxPeople.getText().toString();

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
            event.saveInBackground(new SaveEventCallback(event, getApplicationContext()));
        }
    }

    private class SaveEventCallback extends SaveCallback {
        Event event;
        Context context;

        public SaveEventCallback(Event event, Context context) {
            this.event = event;
            this.context = context;
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

                // update team's event information if the event is visible to some team
                String teamName = event.getVisibility();
                ParseQuery<Team> teamQuery = ParseQuery.getQuery("Team");
                teamQuery.whereEqualTo("name", teamName);
                try {
                    Team team = teamQuery.getFirst();
                    // update cache
                    teamQuery.get(team.getObjectId());
                    team.addEvent(event.getObjectId());
                    team.saveInBackground();
                } catch (ParseException e1) {
                }

                Intent intent = new Intent();
                intent.putExtra("eventID", event.getObjectId());
                setResult(RESULT_OK, intent);
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
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    }
}
