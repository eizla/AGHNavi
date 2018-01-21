package com.aghnavi.agh_navi.dmsl.tasks;


import android.content.Context;
import android.os.AsyncTask;

import com.aghnavi.agh_navi.AnyplaceAPI;
import com.aghnavi.agh_navi.dmsl.nav.PoisNav;
import com.aghnavi.agh_navi.dmsl.utils.GeoPoint;
import com.aghnavi.agh_navi.dmsl.utils.NetworkUtils;

import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class NavIndoorTask extends AsyncTask<Void, Void, String> {

    public interface NavRouteXYListener {
        void onNavRouteErrorOrCancel(String result);
        void onNavRouteSuccess(String result, List<PoisNav> points);
    }

    private NavRouteXYListener mListener;

    private String json_req;
    private List<PoisNav> mPuids = new ArrayList<>();
    private boolean success = false;

    public NavIndoorTask(NavRouteXYListener l, Context ctx, String poid, GeoPoint pos, String floor) {
        this.mListener = l;

        // create the JSON object for the navigation API call
        JSONObject j = new JSONObject();
        try {
            j.put("username", "username");
            j.put("password", "pass");
            // insert the destination POI and the user's coordinates
            j.put("pois_to", poid);
            j.put("coordinates_lat", pos.lat);
            j.put("coordinates_lon", pos.lng);
            j.put("floor_number", floor);
            this.json_req = j.toString();
        } catch (JSONException ignored) {
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            if (json_req == null)
                return "Error creating the request!";

            // changed to the coordnates function
            String response = NetworkUtils.downloadHttpClientJsonPost(AnyplaceAPI.getNavRouteXYUrl(), json_req);
            JSONObject json = new JSONObject(response);

            if (json.has("status") && json.getString("status").equalsIgnoreCase("error")) {
                return "Error Message: " + json.getString("message");
            }

            int num_of_pois = Integer.parseInt(json.getString("num_of_pois"));
            // If the list is empty it means that no navigation is possible
            if (0 == num_of_pois) {
                return "No valid path exists from your position to the POI selected!";
            }

            // convert the PUIDS received into NavPoints
            JSONArray pois = new JSONArray(json.getString("pois"));
            for (int i = 0; i < num_of_pois; i++) {
                JSONObject cp = (JSONObject) pois.get(i);
                PoisNav navp = new PoisNav();
                navp.lat = cp.getString("lat");
                navp.lon = cp.getString("lon");
                navp.puid = cp.getString("puid");
                navp.buid = cp.getString("buid");
                navp.floor_number = cp.getString("floor_number");
                mPuids.add(navp);
            }
            success = true;
            return "Successfully plotted navigation route!";

        } catch (ConnectTimeoutException e) {
            return "Connecting to the server is taking too long!";
        } catch (SocketTimeoutException e) {
            return "Communication with the server is taking too long!";
        } catch (JSONException e) {
            return "Not valid response from the server! Contact the admin.";
        } catch (Exception e) {
            return "Error plotting navigation route. Exception[ " + e.getMessage() + " ]";
        }

    }

    @Override
    protected void onPostExecute(String result) {
        if (success) {
            // call the success listener
            mListener.onNavRouteSuccess(result, mPuids);
        } else {
            // call the error listener
            mListener.onNavRouteErrorOrCancel(result);
        }
    }

    @Override
    protected void onCancelled(String result) {
        mListener.onNavRouteErrorOrCancel("Navigation task cancelled!");
    }

    @Override
    protected void onCancelled() { // just for < API 11
        mListener.onNavRouteErrorOrCancel("Navigation task cancelled!");
    }
}// end of navroute task

