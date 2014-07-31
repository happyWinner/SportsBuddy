package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NewTeamActivity extends Activity {

    public static final int UPLOAD_EMBLEM_REQUEST_CODE = 4;
    public static final int CROP_EMBLEM_REQUEST_CODE = 411;
    public static final int EMBLEM_WIDTH = 400;
    public static final int EMBLEM_HEIGHT = 400;

    private NoDefaultSpinner sportsTypeSpinner;
    private EditText editTeamName;
    private Button buttonUploadEmblem;
    private EditText editTeamDescription;
    private ImageView emblemImageView;
    private ParseFile emblem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_team);

        // register Team as the subclass of ParseObject
        ParseObject.registerSubclass(Team.class);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // get references of views
        editTeamName = (EditText) findViewById(R.id.edit_team_name);
        sportsTypeSpinner = (NoDefaultSpinner) findViewById(R.id.spinner_sports_type);
        buttonUploadEmblem = (Button) findViewById(R.id.button_upload_emblem);
        emblemImageView = (ImageView) findViewById(R.id.image_view_emblem);
        editTeamDescription = (EditText) findViewById(R.id.edit_team_description);


        // add listener to convert the team name to uppercase
        editTeamName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null) {
                    editTeamName.removeTextChangedListener(this);
                    editTeamName.setText(s.toString().toUpperCase());
                    editTeamName.setSelection(start + count);
                    editTeamName.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> sportTypeAdapter = ArrayAdapter.createFromResource(this, R.array.sport_type_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        sportTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sportsTypeSpinner.setAdapter(sportTypeAdapter);

        // register a touch listener to the layout
        ((RelativeLayout) findViewById(R.id.relative_layout)).setOnTouchListener(new TouchListener());

        // register a touch listener to the layout
        ((TableLayout) findViewById(R.id.table_layout)).setOnTouchListener(new TouchListener());

        // set "Upload Emblem" button listener
        buttonUploadEmblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent choosePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(choosePictureIntent, UPLOAD_EMBLEM_REQUEST_CODE);
            }
        });

        // set "Cancel" button listener
        ((Button) findViewById(R.id.button_cancel_team)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // set "Create" button listener
        ((Button) findViewById(R.id.button_create_team)).setOnClickListener(new CreateListener());
    }

    private class TouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            // hide the soft keyboard when the background is touched
            hideSoftInput();
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_team, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPLOAD_EMBLEM_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // ask user to crop the image
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(data.getData(), "image/*");
            intent.putExtra("crop", "true");
            // aspectX aspectY
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            // outputX outputY
            intent.putExtra("outputX", EMBLEM_WIDTH);
            intent.putExtra("outputY", EMBLEM_HEIGHT);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, CROP_EMBLEM_REQUEST_CODE);
        }

        if (requestCode == CROP_EMBLEM_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                Drawable drawable = new BitmapDrawable(this.getResources(),photo);
                emblemImageView.setImageDrawable(drawable);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                emblem = new ParseFile("emblem.png", byteArray);
            }
        }
    }

    private void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private class CreateListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // hide the soft keyboard
            hideSoftInput();

            // get team name
            String teamName = editTeamName.getText().toString();
            if (teamName == null || teamName.length() == 0) {
                editTeamName.setError(getString(R.string.error_empty_team_name));
                editTeamName.requestFocus();
                return;
            }

            // get sports type
            if (sportsTypeSpinner.getSelectedItem() == null) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_empty_sport_type), Toast.LENGTH_SHORT).show();
                return;
            }
            String sportsType = sportsTypeSpinner.getSelectedItem().toString();

            // set default emblem if user hasn't upload customized emblem
            if (emblem == null) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_team_emblem).compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                emblem = new ParseFile("emblem.png", byteArray);
            }

            // get team description
            String teamDescription = editTeamDescription.getText().toString();

            // check whether the team name is used
            ParseQuery<Team> query = ParseQuery.getQuery("Team");
            query.whereEqualTo("name", teamName);
            try {
                if (query.getFirst() != null) {
                    editTeamName.setError(getString(R.string.error_team_name_taken));
                    editTeamName.requestFocus();
                    return;
                }
            } catch (ParseException e) {
                if (e.getCode() != ParseException.OBJECT_NOT_FOUND) {
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
            }

            // upload team data to Parse.com
            Team team = new Team();
            team.setName(teamName);
            team.setSportsType(sportsType);
            team.setEmblem(emblem);
            team.setDescription(teamDescription);
            team.setLeaderID(ParseUser.getCurrentUser().getObjectId());
            team.addMember(ParseUser.getCurrentUser().getObjectId());
            team.saveInBackground(new SaveTeamCallback(team));
        }
    }

    private class SaveTeamCallback extends SaveCallback {
        Team team;

        public SaveTeamCallback(Team team) {
            this.team = team;
        }

        @Override
        public void done(ParseException e) {
            Intent intent = new Intent();
            if (e == null) {
                // update user's team information
                ParseUser user = ParseUser.getCurrentUser();
                List<String> teamIdList = user.getList("teamsJoined");
                if (teamIdList == null) {
                    teamIdList = new ArrayList<String>();
                }
                teamIdList.add(team.getObjectId());
                user.put("teamsJoined", teamIdList);
                user.saveInBackground();
                try {
                    ParseUser.getCurrentUser().fetch();
                } catch (ParseException userException) {
                }

                Toast.makeText(getApplicationContext(), getString(R.string.team_created_successfully), Toast.LENGTH_SHORT).show();

                intent.putExtra("teamId", team.getObjectId());
                setResult(RESULT_OK, intent);
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

                setResult(RESULT_CANCELED, intent);
            }
            finish();
        }
    }
}
