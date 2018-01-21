package com.aghnavi.agh_navi.dmsl.tasks;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.aghnavi.agh_navi.AnyplaceAPI;
import com.aghnavi.agh_navi.dmsl.nav.PoisModel;
import com.aghnavi.agh_navi.dmsl.utils.NetworkUtils;

import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;

/**
 * Returns the POIs according to a given Building and Floor
 */
public class FetchPoiByPuidTask extends AsyncTask<Void, Void, String> {

    public interface FetchPoiListener {
        void onErrorOrCancel(String result);
        void onSuccess(String result, PoisModel poi);
    }

    private FetchPoiListener mListener;
    private Context mCtx;

    private String puid;
    private PoisModel poi;

    private ProgressDialog dialog;
    private boolean success = false;

    public FetchPoiByPuidTask(FetchPoiListener l, Context ctx, String puid) {
        this.mCtx = ctx;
        this.mListener = l;
        this.puid = puid;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(mCtx);
        dialog.setIndeterminate(true);
        dialog.setTitle("Wczytywanie POI");
        dialog.setMessage("Proszę poczekać...");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                FetchPoiByPuidTask.this.cancel(true);
            }
        });
        dialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        if (!NetworkUtils.isOnline(mCtx)) {
            return "No connection available!";
        }

        try {
            JSONObject j = new JSONObject();
            try {
                j.put("username", "username");
                j.put("password", "pass");
                j.put("pois", this.puid);
            } catch (JSONException e) {
                return "Error Message: Could not create the request for the POIs!";
            }

            // fetch the pois of this floor
            String response = NetworkUtils.downloadHttpClientJsonPost(AnyplaceAPI.getFetchPoisByPuidUrl(), j.toString());
            // fetch the pois for the whole building
            // String response = NetworkUtils.downloadHttpClientJsonPost(
            // AnyplaceAPI.getFetchPoisByBuidUrl(),j.toString());

            JSONObject json = new JSONObject(response);
            if (json.has("status") && json.getString("status").equalsIgnoreCase("error")) {
                return "Error Message: " + json.getString("message");
            }

            // process the buildings received
            poi = new PoisModel();
            poi.lat = json.getString("coordinates_lat");
            poi.lng = json.getString("coordinates_lon");
            poi.buid = json.getString("buid");
            poi.floor_name = json.getString("floor_name");
            poi.floor_number = json.getString("floor_number");
            poi.description = json.getString("description");
            poi.name = json.getString("name");
            poi.pois_type = json.getString("pois_type");
            poi.puid = json.getString("puid");
            poi.is_building_entrance = json.getBoolean("is_building_entrance");

            success = true;
            return "Successfully fetched Points of Interest";

        } catch (ConnectTimeoutException e) {
            return "Connecting to Anyplace service is taking too long!";
        } catch (SocketTimeoutException e) {
            return "Communication with the server is taking too long!";
        } catch (JSONException e) {
            return "Not valid response from the server! Contact the admin.";
        } catch (Exception e) {
            return "Error fetching Point of Interest. Exception[ " + e.getMessage() + " ]";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        dialog.dismiss();
        if (success) {
            mListener.onSuccess(result, poi);
        } else {
            mListener.onErrorOrCancel(result);
        }
    }

    @Override
    protected void onCancelled(String result) {
        dialog.dismiss();
        mListener.onErrorOrCancel(result);
    }

    @Override
    protected void onCancelled() {
        dialog.dismiss();
        mListener.onErrorOrCancel("Fetching POI was cancelled!");
    }

} // end of fetch POIS by building and floor

