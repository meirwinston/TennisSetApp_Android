package com.tennissetapp.activities;

import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tennissetapp.R;
import com.tennissetapp.utils.GcmUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


public class TestActivity extends Activity {
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "tennissetapp2014";
    private static final String PROPERTY_APP_VERSION = "1";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "637067041525";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Demo";

    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;

    String regid;
    GcmUtils u = new GcmUtils(this);
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        mDisplay = (TextView) findViewById(R.id.display);
////        u.register();
//        context = getApplicationContext();
//        Log.d(TAG,"BEFORE");
//        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
//        if (u.checkPlayServices()) {
//            Log.d(TAG,"AFTER CHECK");
//            gcm = GoogleCloudMessaging.getInstance(this);
//            regid = u.getRegistrationId();
//            regid = ""; //TEST
//            Log.d(TAG,"GCM " + gcm +", REGID " + regid);
//            if (regid.isEmpty()) {
//                Log.d(TAG,"REGISTER IN THE BACKGROUND");
//                u.registerInBackground();
//            }
//        } else {
//            Log.i(TAG, "No valid Google Play Services APK found.");
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check device for Play Services APK.
        u.checkPlayServices();
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

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    Log.i(TAG,"REGISTERING WITH GCM " + SENDER_ID);
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    Log.i(TAG,"SUCCESS! REGID IS " + regid);
                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    Log.e(TAG, ex.getMessage(),ex);
                    msg = "Error :" + ex.getMessage() + ", sender ID: " + SENDER_ID + ", Project ID: " + PROPERTY_REG_ID;
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    // Send an upstream message.
    public void onClick(final View view) {

//        if (view == findViewById(R.id.send)) {
//            new AsyncTask<Void, Void, String>() {
//                @Override
//                protected String doInBackground(Void... params) {
//                    String msg = "";
//                    try {
//                        Bundle data = new Bundle();
//                        data.putString("my_message", "Hello World");
//                        data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
//                        String id = Integer.toString(msgId.incrementAndGet());
//                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
//                        msg = "Sent message";
//                    } catch (IOException ex) {
//                        msg = "Error :" + ex.getMessage();
//                    }
//                    return msg;
//                }
//
//                @Override
//                protected void onPostExecute(String msg) {
//                    mDisplay.append(msg + "\n");
//                }
//            }.execute(null, null, null);
//        } else if (view == findViewById(R.id.clear)) {
//            mDisplay.setText("");
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(TestActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    //-----------------------------
}









//package com.tennissetapp.activities;
//
//import android.support.v7.app.ActionBarActivity;
//import android.os.Bundle;
//import com.google.android.gms.common.ConnectionResult;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.android.gms.gcm.GoogleCloudMessaging;
//import com.tennissetapp.R;
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager.NameNotFoundException;
//import android.os.AsyncTask;
//import android.util.Log;
//import android.view.View;
//import android.widget.TextView;
//import java.io.IOException;
//import java.util.concurrent.atomic.AtomicInteger;
//
//
//public class TestActivity extends ActionBarActivity {
//    public static final String EXTRA_MESSAGE = "message";
//    public static final String PROPERTY_REG_ID = "tennissetapp2014";
//    private static final String PROPERTY_APP_VERSION = "1";
//    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
//
//    /**
//     * Substitute you own sender ID here. This is the project number you got
//     * from the API Console, as described in "Getting Started."
//     */
//    String PROJECT_NUMBER = "637067041525";
//
//    /**
//     * Tag used on log messages.
//     */
//    static final String TAG = "GCM Demo";
//
//    TextView mDisplay;
//    GoogleCloudMessaging gcm;
//    AtomicInteger msgId = new AtomicInteger();
//    Context context;
//
//    String regid;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_test);
//
//        mDisplay = (TextView) findViewById(R.id.display);
//
//        context = getApplicationContext();
//        Log.d(TAG,"BEFORE");
//        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
//        if (checkPlayServices()) {
//            Log.d(TAG,"AFTER CHECK");
//            gcm = GoogleCloudMessaging.getInstance(this);
//            regid = getRegistrationId(context);
//            regid = ""; //TEST
//            Log.d(TAG,"GCM " + gcm +", REGID " + regid);
//            if (regid.isEmpty()) {
//                Log.d(TAG,"REGISTER IN THE BACKGROUND");
//                registerInBackground();
//            }
//        } else {
//            Log.i(TAG, "No valid Google Play Services APK found.");
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        // Check device for Play Services APK.
//        checkPlayServices();
//    }
//
//    /**
//     * Check the device to make sure it has the Google Play Services APK. If
//     * it doesn't, display a dialog that allows users to download the APK from
//     * the Google Play Store or enable it in the device's system settings.
//     */
//    private boolean checkPlayServices() {
//        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
//        if (resultCode != ConnectionResult.SUCCESS) {
//            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
//                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
//                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
//            } else {
//                Log.i(TAG, "This device is not supported.");
//                finish();
//            }
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * Stores the registration ID and the app versionCode in the application's
//     * {@code SharedPreferences}.
//     *
//     * @param context application's context.
//     * @param regId registration ID
//     */
//    private void storeRegistrationId(Context context, String regId) {
//        final SharedPreferences prefs = getGcmPreferences(context);
//        int appVersion = getAppVersion(context);
//        Log.i(TAG, "Saving regId on app version " + appVersion);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(PROPERTY_REG_ID, regId);
//        editor.putInt(PROPERTY_APP_VERSION, appVersion);
//        editor.commit();
//    }
//
//    /**
//     * Gets the current registration ID for application on GCM service, if there is one.
//     * <p>
//     * If result is empty, the app needs to register.
//     *
//     * @return registration ID, or empty string if there is no existing
//     *         registration ID.
//     */
//    private String getRegistrationId(Context context) {
//        final SharedPreferences prefs = getGcmPreferences(context);
//        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
//        if (registrationId.isEmpty()) {
//            Log.i(TAG, "Registration not found.");
//            return "";
//        }
//        // Check if app was updated; if so, it must clear the registration ID
//        // since the existing regID is not guaranteed to work with the new
//        // app version.
//        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
//        int currentVersion = getAppVersion(context);
//        if (registeredVersion != currentVersion) {
//            Log.i(TAG, "App version changed.");
//            return "";
//        }
//        return registrationId;
//    }
//
//    /**
//     * Registers the application with GCM servers asynchronously.
//     * <p>
//     * Stores the registration ID and the app versionCode in the application's
//     * shared preferences.
//     */
//    private void registerInBackground() {
//        new AsyncTask<Void, Void, String>() {
//            @Override
//            protected String doInBackground(Void... params) {
//                String msg = "";
//                try {
//                    if (gcm == null) {
//                        gcm = GoogleCloudMessaging.getInstance(context);
//                    }
//                    Log.i(TAG,"REGISTERING WITH GCM " + PROJECT_NUMBER);
//                    regid = gcm.register(PROJECT_NUMBER);
//                    msg = "Device registered, registration ID=" + regid;
//                    Log.i(TAG,"SUCCESS! REGID IS " + regid);
//                    // You should send the registration ID to your server over HTTP, so it
//                    // can use GCM/HTTP or CCS to send messages to your app.
//                    sendRegistrationIdToBackend();
//
//                    // For this demo: we don't need to send it because the device will send
//                    // upstream messages to a server that echo back the message using the
//                    // 'from' address in the message.
//
//                    // Persist the regID - no need to register again.
//                    storeRegistrationId(context, regid);
//                } catch (IOException ex) {
//                    Log.e(TAG, ex.getMessage(),ex);
//                    msg = "Error :" + ex.getMessage() + ", sender ID: " + PROJECT_NUMBER + ", Project ID: " + PROPERTY_REG_ID;
//                    // If there is an error, don't just keep trying to register.
//                    // Require the user to click a button again, or perform
//                    // exponential back-off.
//                }
//                return msg;
//            }
//
//            @Override
//            protected void onPostExecute(String msg) {
//                mDisplay.append(msg + "\n");
//            }
//        }.execute(null, null, null);
//    }
//
//    // Send an upstream message.
//    public void onClick(final View view) {
//
//        if (view == findViewById(R.id.send)) {
//            new AsyncTask<Void, Void, String>() {
//                @Override
//                protected String doInBackground(Void... params) {
//                    String msg = "";
//                    try {
//                        Bundle data = new Bundle();
//                        data.putString("my_message", "Hello World");
//                        data.putString("my_action", "com.google.android.gcm.demo.app.ECHO_NOW");
//                        String id = Integer.toString(msgId.incrementAndGet());
//                        gcm.send(PROJECT_NUMBER + "@gcm.googleapis.com", id, data);
//                        msg = "Sent message";
//                    } catch (IOException ex) {
//                        msg = "Error :" + ex.getMessage();
//                    }
//                    return msg;
//                }
//
//                @Override
//                protected void onPostExecute(String msg) {
//                    mDisplay.append(msg + "\n");
//                }
//            }.execute(null, null, null);
//        } else if (view == findViewById(R.id.clear)) {
//            mDisplay.setText("");
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//    }
//
//    /**
//     * @return Application's version code from the {@code PackageManager}.
//     */
//    private static int getAppVersion(Context context) {
//        try {
//            PackageInfo packageInfo = context.getPackageManager()
//                    .getPackageInfo(context.getPackageName(), 0);
//            return packageInfo.versionCode;
//        } catch (NameNotFoundException e) {
//            // should never happen
//            throw new RuntimeException("Could not get package name: " + e);
//        }
//    }
//
//    /**
//     * @return Application's {@code SharedPreferences}.
//     */
//    private SharedPreferences getGcmPreferences(Context context) {
//        // This sample app persists the registration ID in shared preferences, but
//        // how you store the regID in your app is up to you.
//        return getSharedPreferences(TestActivity.class.getSimpleName(),
//                Context.MODE_PRIVATE);
//    }
//    /**
//     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
//     * messages to your app. Not needed for this demo since the device sends upstream messages
//     * to a server that echoes back the message using the 'from' address in the message.
//     */
//    private void sendRegistrationIdToBackend() {
//        // Your implementation here.
//    }
//
//    //-----------------------------
////    @Override
////    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_test);
////    }
////
////
////    @Override
////    public boolean onCreateOptionsMenu(Menu menu) {
////
////        // Inflate the menu; this adds items to the action bar if it is present.
////        getMenuInflater().inflate(R.menu.env, menu);
////        return true;
////    }
////
////    @Override
////    public boolean onOptionsItemSelected(MenuItem item) {
////        // Handle action bar item clicks here. The action bar will
////        // automatically handle clicks on the Home/Up button, so long
////        // as you specify a parent activity in AndroidManifest.xml.
////        int id = item.getItemId();
////        if (id == R.id.action_settings) {
////            return true;
////        }
////        return super.onOptionsItemSelected(item);
////    }
//
//}
