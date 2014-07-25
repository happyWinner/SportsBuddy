package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class RegisterActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // authenticates this client to Parse
        Parse.initialize(this, getString(R.string.application_id), getString(R.string.client_key));

        // hide the soft keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // register a touch listener to the layout
        ((LinearLayout) findViewById(R.id.layout_register)).setOnTouchListener(new TouchListener());

        // register a click listener to the button
        ((Button) findViewById(R.id.register_button)).setOnClickListener(new RegisterListener());
    }

    private class TouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            // hide the soft keyboard when the background is touched
            hideSoftInput();
            return true;
        }
    }

    private class RegisterListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            // hide the soft keyboard when the background is touched
            hideSoftInput();

            // validate username
            EditText usernameView = (EditText) findViewById(R.id.register_username);
            String username = usernameView.getText().toString();
            if (TextUtils.isEmpty(username)) {
                usernameView.setError(getString(R.string.error_field_required));
                usernameView.requestFocus();
                return;
            }

            // validate email
            EditText emailView = (EditText) findViewById(R.id.register_email);
            String email = emailView.getText().toString();
            if (TextUtils.isEmpty(email)) {
                emailView.setError(getString(R.string.error_field_required));
                emailView.requestFocus();
                return;
            }
            if (!email.matches("[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\\.[a-zA-Z]{2,4}")) {
                emailView.setError(getString(R.string.error_invalid_email));
                emailView.requestFocus();
                return;
            }

            // validate password
            EditText passwordView = (EditText) findViewById(R.id.register_password);
            String password = passwordView.getText().toString();
            if (TextUtils.isEmpty(password)) {
                passwordView.setError(getString(R.string.error_field_required));
                passwordView.requestFocus();
                return;
            }

            // confirm password
            EditText passwordConfirmView = (EditText) findViewById(R.id.register_confirm_password);
            String passwordConfirm = passwordConfirmView.getText().toString();
            if (TextUtils.isEmpty(passwordConfirm)) {
                passwordConfirmView.setError(getString(R.string.error_field_required));
                passwordConfirmView.requestFocus();
                return;
            }
            if (!password.equals(passwordConfirm)) {
                passwordConfirmView.setError(getString(R.string.error_password_not_match));
                passwordConfirmView.requestFocus();
                return;
            }

            // sign up new account
            ParseUser user = new ParseUser();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.signUpInBackground(new RegisterCallback(usernameView, emailView));
        }

        class RegisterCallback extends SignUpCallback {
            EditText usernameView;
            EditText emailView;

            RegisterCallback(EditText usernameView, EditText emailView) {
                this.usernameView = usernameView;
                this.emailView = emailView;
            }

            @Override
            public void done(ParseException e) {
                if (e == null) {
                    // Hooray! Let them use the app now.
                    //TODO
                    Toast.makeText(getApplicationContext(), "Succeed", Toast.LENGTH_LONG).show();
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    switch (e.getCode()) {
                        case ParseException.INTERNAL_SERVER_ERROR:
                            Toast.makeText(getApplicationContext(), "Server is down! Retry later!", Toast.LENGTH_LONG).show();
                            break;

                        case ParseException.CONNECTION_FAILED:
                            Toast.makeText(getApplicationContext(), "Connection error! Retry later!", Toast.LENGTH_LONG).show();
                            break;

                        case ParseException.TIMEOUT:
                            Toast.makeText(getApplicationContext(), "Timeout! Retry later!", Toast.LENGTH_LONG).show();
                            break;

                        case ParseException.USERNAME_TAKEN:
                            usernameView.setError(getString(R.string.error_username_taken));
                            usernameView.requestFocus();
                            break;

                        case ParseException.EMAIL_TAKEN:
                            emailView.setError(getString(R.string.error_email_taken));
                            emailView.requestFocus();
                            break;

                        default:
                            Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_LONG).show();
                            break;
                    }
                }
            }
        }
    }

    private void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}