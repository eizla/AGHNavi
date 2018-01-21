package com.aghnavi.agh_navi.dmsl.tasks;


import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.aghnavi.agh_navi.AnyplaceAPI;
import com.aghnavi.agh_navi.dmsl.nav.FloorModel;
import com.aghnavi.agh_navi.dmsl.utils.NetworkUtils;

//TODO: Later to decide if user should know about this task (alert dialog)
public class FetchFloorsByBuidTask extends AsyncTask<Void, Void, String> {

    public interface FetchFloorsByBuidTaskListener {
        void onErrorOrCancel(String result);
        void onSuccess(String result, List<FloorModel> floors);
    }

    private FetchFloorsByBuidTaskListener mListener;
    private Context ctx;
    private List<FloorModel> floors = new ArrayList<>();
    private String buid;
    private boolean success = false;
    private ProgressDialog dialog;
    private Boolean showDialog = true;

    public FetchFloorsByBuidTask(FetchFloorsByBuidTaskListener fetchFloorsByBuidTaskListener, Context ctx, String buid) {
        this.mListener = fetchFloorsByBuidTaskListener;
        this.ctx = ctx;
        this.buid = buid;
    }

    public FetchFloorsByBuidTask(FetchFloorsByBuidTaskListener fetchFloorsByBuidTaskListener, Context ctx, String buid, Boolean showDialog) {
        this.mListener = fetchFloorsByBuidTaskListener;
        this.ctx = ctx;
        this.buid = buid;
        this.showDialog = showDialog;
    }

    @Override
    protected void onPreExecute() {
        if (showDialog) {
            dialog = new ProgressDialog(ctx);
            dialog.setIndeterminate(true);
            dialog.setTitle("Wczytywanie pięter");
            dialog.setMessage("Proszę poczekać...");
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // finishJob();
                    FetchFloorsByBuidTask.this.cancel(true);
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
            JSONObject j = new JSONObject();
            try {
                j.put("username", "username");
                j.put("password", "pass");
                j.put("buid", this.buid);
            } catch (JSONException e) {
                return "Error requesting the floors for the buildings!";
            }

            //Uses GZIP encoding
            String response = NetworkUtils.downloadHttpClientJsonPost(AnyplaceAPI.getFetchFloorsByBuidUrl(), j.toString());
            JSONObject json = new JSONObject(response);

            if (json.has("status") && json.getString("status").equalsIgnoreCase("error")) {
                return "Error Message: " + json.getString("message");
            }

            // process the buildings received
            FloorModel b;
            JSONArray buids_json = new JSONArray(json.getString("floors"));

            if (buids_json.length() == 0) {
                return "Błąd: brak pięter";
            }

            for (int i = 0, sz = buids_json.length(); i < sz; i++) {
                JSONObject cp = (JSONObject) buids_json.get(i);
                b = new FloorModel();
                b.buid = cp.getString("buid");
                b.floor_name = cp.getString("floor_name");
                b.floor_number = cp.getString("floor_number");
                b.description = cp.getString("description");

                // use optString() because these values might not exist of a
                // floor plan has not been set for this floor
                b.bottom_left_lat = cp.optString("bottom_left_lat");
                b.bottom_left_lng = cp.optString("bottom_left_lng");
                b.top_right_lat = cp.optString("top_right_lat");
                b.top_right_lng = cp.optString("top_right_lng");

                floors.add(b);
            }

            Collections.sort(floors);

            success = true;
            return "Successfully fetched floors";

        } catch (ConnectTimeoutException e) {
            return "Cannot connect to Anyplace service!";
        } catch (SocketTimeoutException e) {
            return "Communication with the server is taking too long!";
        } catch (UnknownHostException e) {
            return "No connection available!";
        } catch (Exception e) {
            // Log.d("Wczytywanie pięter task", e.getMessage());
            return "Error fetching floors. [ " + e.getMessage() + " ]";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        // removes the progress dialog
        if (showDialog)
            dialog.dismiss();

        if (success) {
            mListener.onSuccess(result, floors);
        } else {
            // there was an error during the process
            mListener.onErrorOrCancel(result);
        }

    }

    @Override
    protected void onCancelled(String result) {
        if (showDialog)
            dialog.dismiss();
        mListener.onErrorOrCancel("Floor fetching cancelled...");
    }

    @Override
    protected void onCancelled() { // just for < API 11
        if (showDialog)
            dialog.dismiss();
        mListener.onErrorOrCancel("Floor fetching cancelled...");
    }

}
