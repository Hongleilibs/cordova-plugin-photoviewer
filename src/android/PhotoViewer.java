package com.Hongleilibs.PhotoViewer;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Class to Open PhotoViewer with the Required Parameters from Cordova
 *
 * - URL
 * - Title
 */
public class PhotoViewer extends CordovaPlugin {

    public static final int PERMISSION_DENIED_ERROR = 20;

    public static final String WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ = Manifest.permission.READ_EXTERNAL_STORAGE;

    public static final int REQ_CODE = 0;

    protected JSONArray args;
    protected CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("showMultiple")) {
            this.args = args;
            this.callbackContext = callbackContext;

            boolean requiresExternalPermission = true;
            try {
                JSONObject options = this.args.optJSONObject(2);
                requiresExternalPermission = options.getBoolean("share");
            } catch (JSONException exception) {
            }

            if (!requiresExternalPermission || (cordova.hasPermission(READ) && cordova.hasPermission(WRITE))) {
                this.launchMultiActivity();
            } else {
                this.getPermission();
            }
            return true;
        }
        return false;
    }

    protected void getPermission() {
        cordova.requestPermissions(this, REQ_CODE, new String[]{WRITE, READ});
    }

    protected void launchMultiActivity() throws JSONException {
        Intent i = new Intent(this.cordova.getActivity(), PhotoMultipleActivity.class);

        i.putExtra("title", this.args.getString(1));
        i.putExtra("options", this.args.optJSONObject(2).toString());

        this.cordova.getActivity().startActivity(i);
        this.callbackContext.success("");
    }
}
