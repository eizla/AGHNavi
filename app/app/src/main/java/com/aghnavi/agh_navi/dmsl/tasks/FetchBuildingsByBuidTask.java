package com.aghnavi.agh_navi.dmsl.tasks;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.aghnavi.agh_navi.AnyplaceAPI;
import com.aghnavi.agh_navi.dmsl.nav.BuildingModel;
import com.aghnavi.agh_navi.dmsl.utils.NetworkUtils;

import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class FetchBuildingsByBuidTask extends AsyncTask<Void, Void, String> {

    public interface FetchBuildingsByBuidTaskListener {
        void onErrorOrCancel(String result);

        void onSuccess(String result, BuildingModel building);
    }

    private FetchBuildingsByBuidTaskListener mListener;
    private Context ctx;

    private BuildingModel building;
    private boolean success = false;
    private ProgressDialog dialog;
    private Boolean showDialog = true;
    private String json_req;

    public FetchBuildingsByBuidTask(FetchBuildingsByBuidTaskListener fetchBuildingsTaskListener, Context ctx, String buid) {
        this.mListener = fetchBuildingsTaskListener;
        this.ctx = ctx;

        // create the JSON object for the navigation API call
        JSONObject j = new JSONObject();
        try {
            j.put("username", "username");
            j.put("password", "pass");
            // insert the destination POI and the user's coordinates
            j.put("buid", buid);
            this.json_req = j.toString();
        } catch (JSONException ignored) {
        }
    }

    public FetchBuildingsByBuidTask(FetchBuildingsByBuidTaskListener fetchBuildingsTaskListener, Context ctx, String buid, Boolean showDialog) {
        this(fetchBuildingsTaskListener, ctx, buid);
        this.showDialog = showDialog;
    }

    @Override
    protected void onPreExecute() {
        if (showDialog) {
            dialog = new ProgressDialog(ctx);
            dialog.setIndeterminate(true);
            dialog.setTitle("Wczytywanie budynku");
            dialog.setMessage("Proszę poczekać...");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    FetchBuildingsByBuidTask.this.cancel(true);
                }
            });
            dialog.show();
        }

    }

    @Override
    protected String doInBackground(Void... params) {
        if (!NetworkUtils.isOnline(ctx)) {
            return "No connection available!";
        }

        try {
            if (json_req == null) {
                return "Error creating the request!";
            }

            String response = NetworkUtils.downloadHttpClientJsonPost(AnyplaceAPI.getFetchBuildingsByBuidUrl(), json_req);
            JSONObject json = new JSONObject(response);

            if (json.has("status") && json.getString("status").equalsIgnoreCase("error")) {
                return "Error Message: " + json.getString("message");
            }

            // process the buildings received
            BuildingModel b;
            b = new BuildingModel(ctx);
            b.setPosition(json.getString("coordinates_lat"), json.getString("coordinates_lon"));
            b.buid = json.getString("buid");
            // b.address = json.getString("address");
            // b.description = json.getString("description");
            b.name = json.getString("name");
            // b.url = json.getString("url");

            building = b;

            success = true;
            return "Successfully fetched buildings";

        } catch (ConnectTimeoutException e) {
            return "Cannot connect to Anyplace service!";
        } catch (SocketTimeoutException e) {
            return "Communication with the server is taking too long!";
        } catch (UnknownHostException e) {
            return "No connection available!";
        } catch (Exception e) {
            return "Error fetching buildings. [ " + e.getMessage() + " ]";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (showDialog)
            dialog.dismiss();
        if (success) {
            mListener.onSuccess(result, building);
        } else {
            // there was an error during the process
            mListener.onErrorOrCancel(result);
        }

    }

    @Override
    protected void onCancelled(String result) {
        if (showDialog)
            dialog.dismiss();
        mListener.onErrorOrCancel("Buildings Fetch cancelled...");
    }

    @Override
    protected void onCancelled() { // just for < API 11
        if (showDialog)
            dialog.dismiss();
        mListener.onErrorOrCancel("Buildings Fetch cancelled...");
    }

}

