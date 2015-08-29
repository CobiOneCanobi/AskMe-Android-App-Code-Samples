package com.cobionecanobi.askme;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.cobionecanobi.askme.dialogs.Accept;
import com.cobionecanobi.askme.dialogs.Tutorial;
import com.cobionecanobi.askme.gcm.QuickstartPreferences;
import com.cobionecanobi.askme.gcm.RegistrationIntentService;
import com.cobionecanobi.askme.model.User;
import com.cobionecanobi.askme.parsers.HttpManager;
import com.cobionecanobi.askme.parsers.UserJSONParser;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Random;

//Code that is used on front page of AskMe-Personal Advice App
//Google Play Store download link: bit.ly/1hTDeXX
//Screenshot of front page is visible on the Google Play Store page

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity" ;
    private User user;
    private SharedPreferences pref;
    private ImageButton options;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private String token;
    private Tracker tracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
        tracker = analytics.newTracker(R.string.analytics_pass); // Send hits to tracker id UA-XXXX-Y
        // All subsequent hits will be send with screen name = "main screen"
        tracker.setScreenName("main screen");

        gcmServices();
        displayOptions();

        pref = getSharedPreferences("AskMe", MODE_PRIVATE);
        String showTutorial = pref.getString("showDisplay", "nil");
        token = pref.getString("gcm_id","nil");
        Log.d(TAG, "this: "+token);

        if (!showTutorial.equals("1")){ //displays the information dialog on first time launching the app. Once the user launches the app once, the info dialog will no longer display on start up
            DialogFragment newFragment = new Accept();
            newFragment.show(getFragmentManager(), "Accept");
        }

            if (isOnline()) {
                requestData(String.valueOf(R.string.url));
            } else {
                Toast.makeText(this, "Network isn't available", Toast.LENGTH_LONG).show();
            }

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
    }


    //Used for registering the user with GCM
    public void gcmServices(){
        //Once GCM receives the user's generated token, this method is called
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d(TAG, "Token retrieved and sent to server! You can now use gcmsender to send downstream messages to this app.");
                } else {
                    Log.d(TAG, "An error occurred while either fetching the InstanceID token,\n" +
                            "        sending the fetched token to the server or subscribing to the PubSub topic. Please try\n" +
                            "        running the sample again");
                }
            }
        };
    }

    //On click of options button displays drop down menu of additional options
    private void displayOptions() {
        options = (ImageButton) findViewById(R.id.options);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, options);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.menu_options, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.feedback) {
                            Intent send = new Intent(Intent.ACTION_SENDTO);
                            String uriText = "mailto:" + Uri.encode("getaskmeadvice@gmail.com") +
                                    "?subject=" + Uri.encode("Feedback - ") +
                                    "&body=" + Uri.encode("");
                            Uri uri = Uri.parse(uriText);

                            send.setData(uri);
                            startActivity(Intent.createChooser(send, "Send mail..."));
                        }
                        if (item.getItemId() == R.id.testimonial) {
                            Intent send = new Intent(Intent.ACTION_SENDTO);
                            String uriText = "mailto:" + Uri.encode("getaskmeadvice@gmail.com") +
                                    "?subject=" + Uri.encode("Testimonial - ") +
                                    "&body=" + Uri.encode("");
                            Uri uri = Uri.parse(uriText);

                            send.setData(uri);
                            startActivity(Intent.createChooser(send, "Send mail..."));
                        }
                        if (item.getItemId() == R.id.informed) {
                            Intent intent = null;
                            try {
                                // get the Twitter app if possible
                                getPackageManager().getPackageInfo("com.twitter.android", 0);
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=3398176444"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            } catch (Exception e) {
                                // no Twitter app, revert to browser
                                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/3398176444"));
                            }
                            startActivity(intent);
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Launches email app so user can submit problem
    public void openEmail(View view) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory("UX")
                .setAction("click")
                .setLabel("submitAdvice")
                .build());
        String userId =pref.getString("uid", "nil");
        Random r = new Random();
        int adviceId = r.nextInt(10000 - 1000) + 1000;
        Intent send = new Intent(Intent.ACTION_SENDTO);
        String uriText = "mailto:" + Uri.encode("getaskmeadvice@gmail.com") +
                "?subject=" + Uri.encode("") +
                "&body=" + Uri.encode("Advice ID Number: "+userId+"0-"+String.valueOf(adviceId)+" (DO NOT DELETE, ENTER SUBMISSION ABOVE OR BELOW)");
        Uri uri = Uri.parse(uriText);

        send.setData(uri);
        startActivity(Intent.createChooser(send, "Send mail..."));
    }

    //Opens up the tutorial dialog window
    public void showTutorial(View view) {
        DialogFragment newFragment = new Tutorial();
        newFragment.show(getFragmentManager(), "tutorial");
    }

    //Opens up the Share dialog window
    public void showShare(View view) {
        Intent i = new Intent(this, ShareActivity.class);
        startActivity(i);
    }


    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    //Gets the request package ready to be sent to the url and then launches the AsyncTask
    private void requestData(String uri) {
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        RequestPackage p = new RequestPackage();
        p.setMethod("POST");
        p.setUri(uri);
        p.setParam("tag", "user");
        p.setParam("username", androidId);
        Log.d(TAG,token);
        p.setParam("gcm_id", token);

        getUser task = new getUser();
        task.execute(p);
    }

    public void viewAdvice(View view) {
        String userId =pref.getString("uid", "nil");
        Intent intent = new Intent(this, AdviceListActivity.class);
       intent.putExtra("userId", userId); //send user id to AdviceListActivity class
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }


    //Background process to create or get user from database
    private class getUser extends AsyncTask<RequestPackage, String, User> {

        @Override
        protected User doInBackground(RequestPackage... params) {
            String content = HttpManager.getData(params[0]); //gets user info from web database
            user = UserJSONParser.parseFeed(content); //parses received json formatted String and stores in user object
            return user;
        }

        @Override
        protected void onPostExecute(User user) {
            if(user == null){ //if there is a web service problem
                Toast.makeText(MainActivity.this, "User could not be found or added",Toast.LENGTH_LONG).show();
            }else{//addes values to SharedPreferences so on next app launch for easy access across app
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("uid", String.valueOf(user.getId()));
                editor.putString("username",user.getUsername());;
                editor.commit();
            }
        }
    }
}
