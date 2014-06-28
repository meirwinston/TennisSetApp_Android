package com.tennissetapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.tennissetapp.Constants;
import com.tennissetapp.form.UpdateTokenForm;
import com.tennissetapp.rest.Client;
import com.tennissetapp.rest.ServiceResponse;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by mwinston on 5/20/2014.
 */
public class GcmUtils {
    private Activity activity;
    private String TAG = getClass().getSimpleName();
    private GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    private String regid;

    public GcmUtils(Activity activity){
        this.activity = activity;
    }
    public void register(){
        Log.d(TAG,"register");
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(activity.getApplicationContext());
            Log.d(TAG,"BEFORE register checkPlayServices is available GCM: " + gcm);
            regid = getRegistrationId();
            Log.d(TAG,"AFTER register got registration ID from device storage: " + regid);
            if (regid.isEmpty()) {
                Log.d(TAG,"register registerInBackground...");
                registerInBackground();
            }
            else{
                Log.d(TAG,"regid was found in device sending to server: " + regid);
                Client.getInstance().updateToken(new UpdateTokenForm("GCM",regid));
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public String getRegistrationId() {
        Log.i(TAG,"getRegistrationId CALLED");
        final SharedPreferences prefs = activity.getSharedPreferences(
                    Constants.AttributeKeys.GCM_REGISTRATION,
                    Context.MODE_PRIVATE);

        String registrationId = prefs.getString(Constants.GCM.REGISTRATION_ID, "");
        Log.i(TAG,"HERE is prefs " + registrationId);

        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
//        // Check if app was updated; if so, it must clear the registration ID
//        // since the existing regID is not guaranteed to work with the new
//        // app version.
//        int registeredVersion = prefs.getInt(Constants.AttributeKeys.APP_VERSION, Integer.MIN_VALUE);
//        int currentVersion = getAppVersion();
//        if (registeredVersion != currentVersion) {
//            Log.i(TAG, "App version changed.");
//            return "";
//        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = activity.getApplicationContext().getPackageManager()
                    .getPackageInfo(activity.getApplicationContext().getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        Constants.GCM.PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    public void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(activity.getApplicationContext());
                    }
                    Log.i(TAG,"REGISTERING WITH GCM " + Constants.GCM.PROJECT_NUMBER);

                    regid = gcm.register(Constants.GCM.PROJECT_NUMBER);
                    Thread.sleep(2000);

                    msg = "Device registered, registration ID=" + regid;
                    Log.i(TAG,"SUCCESS! REGID IS " + regid);
                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    Client.getInstance().updateToken(new UpdateTokenForm("GCM",regid));

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(regid);
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage(),ex);
                    msg = "Error :" + ex.getMessage() + ", sender ID: " + Constants.GCM.PROJECT_NUMBER + ", Project ID: " + Constants.GCM.REGISTRATION_ID;
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.d(TAG, "onPostExecute " + msg + "\n");
            }
        }.execute(null, null, null);
    }


    private void storeRegistrationId(String regId) {
        final SharedPreferences prefs = activity.getApplicationContext().getSharedPreferences(
                Constants.AttributeKeys.GCM_REGISTRATION,
                Context.MODE_PRIVATE);
        int appVersion = getAppVersion();
        Log.i(TAG, "Saving regId (" +regId + ") on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.GCM.REGISTRATION_ID, regId);
        editor.putInt(Constants.AttributeKeys.APP_VERSION, appVersion);
        editor.commit();
    }
}
