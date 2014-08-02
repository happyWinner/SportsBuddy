package com.qianchen.sportsbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ProfileFragment extends Fragment {

    public static final String CALLBACK_URL = "oauth://sportsbuddy";
    private static final String ERROR_MESSAGE = "Fail to authenticate!";
    public static final int UPLOAD_REQUEST_CODE = 124213;
    public static final int CROP_REQUEST_CODE = 1243124;
    public static final int AVATAR_WIDTH = 200;
    public static final int AVATAR_HEIGHT = 200;

    private ListView profileInterestListView;
    private ProfileInterestAdapter profileInterestAdapter;
    private List<ProfileInterest> profileInterestList;
    private TextView profileName;
    private ImageView profileAvatar;
    private Uri outputFileUri;
    private Uri croppedFileUri;

    private OnFragmentInteractionListener mListener;

    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: add parse method, I just hardcoded the interest kinds and star scores
        ProfileInterest test = new ProfileInterest();
        test.setKind("Soccer");
        test.setScore(3.1f);
        ProfileInterest test2 = new ProfileInterest();
        test2.setKind("Soccer");
        test2.setScore(4.3f);
        profileInterestList = new ArrayList<ProfileInterest>();
        profileInterestList.add(test);
        profileInterestList.add(test);
        profileInterestList.add(test);
        profileInterestList.add(test2);
        profileInterestList.add(test2);

        profileInterestAdapter = new ProfileInterestAdapter(getActivity(), profileInterestList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileInterestListView = (ListView) view.findViewById(R.id.profile_interest);
        profileName = (TextView)view.findViewById(R.id.profile_name);
        profileAvatar = (ImageView)view.findViewById(R.id.profile_avatar);
        profileAvatar.setImageResource(R.drawable.ic_launcher);
        //TODO: set to parse
        profileName.setText("DHOOUUPUP");
        profileInterestListView.setAdapter(profileInterestAdapter);

        // set twitter button listener
        ((Button) view.findViewById(R.id.profile_twitter)).setOnClickListener(new TwitterListener());

        // set upload avatar button listener
        ((Button) view.findViewById(R.id.profile_update)).setOnClickListener(new UploadListener());

        return view;
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

    class UploadListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // Determine Uri of camera image to save.
            final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
            root.mkdirs();
            final File sdImageMainDirectory = new File(root, "img_"+ System.currentTimeMillis() + ".jpg");
            outputFileUri = Uri.fromFile(sdImageMainDirectory);

            // Camera.
            final List<Intent> cameraIntents = new ArrayList<Intent>();
            final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            final PackageManager packageManager = getActivity().getPackageManager();
            final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
            for(ResolveInfo res : listCam) {
                final String packageName = res.activityInfo.packageName;
                final Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                cameraIntents.add(intent);
            }

            // Filesystem.
            final Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryIntent.setType("image/*");
            galleryIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            galleryIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputFileUri);

            // Chooser of filesystem options.
            final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

            // Add the camera options.
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));

            startActivityForResult(chooserIntent, UPLOAD_REQUEST_CODE);
        }
    }

    class TwitterListener implements View.OnClickListener {

        private RequestToken requestToken;

        @Override
        public void onClick(View v) {
            if (MainActivity.accessToken == null) {
                // authorize Twitter account
                GetRequestTokenTask getRequestTokenTask = new GetRequestTokenTask(MainActivity.twitter);
                getRequestTokenTask.execute();

                try {
                    requestToken = getRequestTokenTask.get();
                    if (requestToken == null) {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
                    return;
                }

                // start authorization page
                Intent intent = new Intent(getActivity(), TwitterOAuthActivity.class);
                intent.putExtra("requestToken", requestToken);
                startActivity(intent);
            } else {
                // let user choose to unlink with Twitter account
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setMessage(getString(R.string.profile_unlink_twitter_message)).setTitle(getString(R.string.profile_unlink_twitter_title));

                builder.setPositiveButton(getString(R.string.profile_unlink_twitter_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // delete stored Twitter account information
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("token");
                        editor.remove("tokenSecret");
                        editor.commit();
                        MainActivity.accessToken = null;
                        ConfigurationBuilder conf = new ConfigurationBuilder();
                        conf.setDebugEnabled(true)
                                .setOAuthConsumerKey(getString(R.string.consumer_key))
                                .setOAuthConsumerSecret(getString(R.string.consumer_secret));
                        MainActivity.twitter = new TwitterFactory(conf.build()).getInstance();
                        Toast.makeText(getActivity(), getString(R.string.profile_unlink_twitter_successfully), Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(getString(R.string.dialog_button_cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // user cancelled the dialog
                    }
                });

                // show the alert dialog
                builder.create().show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == UPLOAD_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }

                // ask user to crop the avatar
                Intent intent = new Intent("com.android.camera.action.CROP");
                // indicate image type and Uri
                intent.setDataAndType(selectedImageUri, "image/*");
                // set crop properties
                intent.putExtra("crop", "true");
                // indicate aspect of desired crop
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                // indicate output X and Y
                intent.putExtra("outputX", AVATAR_WIDTH);
                intent.putExtra("outputY", AVATAR_HEIGHT);
                // retrieve data on return
                intent.putExtra("return-data", false);

                final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
                root.mkdirs();
                final File sdImageMainDirectory = new File(root, "img_"+ System.currentTimeMillis() + ".jpg");
                croppedFileUri = Uri.fromFile(sdImageMainDirectory);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, croppedFileUri);
                // start the activity - we handle returning in onActivityResult
                startActivityForResult(intent, CROP_REQUEST_CODE);
            }
            if (requestCode == CROP_REQUEST_CODE) {
                Bitmap avatar = null;
                try {
                    avatar = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), croppedFileUri);
                    Drawable drawable = new BitmapDrawable(getResources(), avatar);
                    profileAvatar.setImageDrawable(drawable);
                } catch (IOException e) {
                }
            }
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


    // asynchronous task to get Twitter OAuth request token
    class GetRequestTokenTask extends AsyncTask<Void, Void, RequestToken> {

        private Twitter twitter;

        GetRequestTokenTask(Twitter twitter) {
            this.twitter = twitter;
        }

        @Override
        protected RequestToken doInBackground(Void... params) {
            RequestToken requestToken = null;
            try {
                requestToken = twitter.getOAuthRequestToken(CALLBACK_URL);
            } catch (TwitterException e) {
            }
            return requestToken;
        }

        @Override
        protected void onPostExecute(RequestToken requestToken) {
            super.onPostExecute(requestToken);
            if (requestToken == null) {
                // toast if fail to get request token
                Toast.makeText(getActivity(), ERROR_MESSAGE, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
