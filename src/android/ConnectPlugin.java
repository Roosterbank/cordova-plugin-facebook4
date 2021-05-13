package org.apache.cordova.facebook;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.ads.AdSettings;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class ConnectPlugin extends CordovaPlugin {

    private final String TAG = "ConnectPlugin";

    private CallbackManager callbackManager;
    private AppEventsLogger logger;
    private Boolean isChild;
    private Boolean sdkInitialised;

    @Override
    protected void pluginInitialize() {

        //Set user as a child until we know otherwise.
        setUserIsChild(true);
        sdkInitialised = false;
        // create callbackManager
        callbackManager = CallbackManager.Factory.create();

        // create AppEventsLogger
        logger = AppEventsLogger.newLogger(cordova.getActivity().getApplicationContext());

        // augment web view to enable hybrid app events
        enableHybridAppEvents();

        // Set up the activity result callback to this class
        cordova.setActivityResultCallback(this);

    }

    @Override
    public void onResume(boolean multitasking) {
        super.onResume(multitasking);
        // Developers can observe how frequently users activate their app by logging an app activation event.
        AppEventsLogger.activateApp(cordova.getActivity().getApplication());
    }

    @Override
    public void onPause(boolean multitasking) {
        super.onPause(multitasking);
        AppEventsLogger.deactivateApp(cordova.getActivity().getApplication());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "activity result in plugin: requestCode(" + requestCode + "), resultCode(" + resultCode + ")");
        callbackManager.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("logEvent")) {
            executeLogEvent(args, callbackContext);
            return true;
        } else if (action.equals("activateApp")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    AppEventsLogger.activateApp(cordova.getActivity().getApplication());
                }
            });

            return true;
        } else if (action.equals("userIsChild")) {
            executeUserIsChild(args, callbackContext);
            return true;
        } else if (action.equals("setAdvertiserTracking")) {
            // android does not support this function
            return true;
        }
        return false;
    }

    private void executeUserIsChild(JSONArray args, CallbackContext callbackContext) {
        cordova.getThreadPool().execute(new Runnable() {
            public void run() {
                Boolean value;
                try {
                    value = args.getBoolean(0);
                } catch (JSONException e) {
                    value = true;
                }
                setUserIsChild(value);
            }
        });
    }

    private void setUserIsChild(Boolean value) {
        isChild = value;
        AdSettings.setMixedAudience(isChild);
        FacebookSdk.setAutoLogAppEventsEnabled(!isChild);
        FacebookSdk.setAdvertiserIDCollectionEnabled(!isChild);
        if (!isChild && !sdkInitialised) {
            FacebookSdk.setAutoInitEnabled(true);
            FacebookSdk.fullyInitialize();
            sdkInitialised = true;
        } else {
            FacebookSdk.setAutoInitEnabled(!isChild);
        }
    }

    private void executeLogEvent(JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (args.length() == 0) {
            // Not enough parameters
            callbackContext.error("Invalid arguments");
            return;
        }

        if(isChild) {
            callbackContext.error("Feature disabled for a CHILD user.");
            return;
        }

        String eventName = args.getString(0);
        if (args.length() == 1) {
            logger.logEvent(eventName);
            callbackContext.success();
            return;
        }

        // Arguments is greater than 1
        JSONObject params = args.getJSONObject(1);
        Bundle parameters = new Bundle();
        Iterator<String> iter = params.keys();

        while (iter.hasNext()) {
            String key = iter.next();
            try {
                // Try get a String
                String value = params.getString(key);
                parameters.putString(key, value);
            } catch (JSONException e) {
                // Maybe it was an int
                Log.w(TAG, "Type in AppEvent parameters was not String for key: " + key);
                try {
                    int value = params.getInt(key);
                    parameters.putInt(key, value);
                } catch (JSONException e2) {
                    // Nope
                    Log.e(TAG, "Unsupported type in AppEvent parameters for key: " + key);
                }
            }
        }

        if (args.length() == 2) {
            logger.logEvent(eventName, parameters);
            callbackContext.success();
        }

        if (args.length() == 3) {
            double value = args.getDouble(2);
            logger.logEvent(eventName, value, parameters);
            callbackContext.success();
        }
    }

    private void enableHybridAppEvents() {
        try {
            Context appContext = cordova.getActivity().getApplicationContext();
            Resources res = appContext.getResources();
            int enableHybridAppEventsId = res.getIdentifier("fb_hybrid_app_events", "bool", appContext.getPackageName());
            boolean enableHybridAppEvents = enableHybridAppEventsId != 0 && res.getBoolean(enableHybridAppEventsId);
            if (enableHybridAppEvents) {
                AppEventsLogger.augmentWebView((WebView) this.webView.getView(), appContext);
                Log.d(TAG, "FB Hybrid app events are enabled");
            } else {
                Log.d(TAG, "FB Hybrid app events are not enabled");
            }
        } catch (Exception e) {
            Log.d(TAG, "FB Hybrid app events cannot be enabled");
        }
    }

}
