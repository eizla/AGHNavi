package com.aghnavi.agh_navi.dmsl.tasks;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.aghnavi.agh_navi.AnyplaceAPI;
import com.aghnavi.agh_navi.dmsl.utils.AnyplaceUtils;
import com.aghnavi.agh_navi.dmsl.utils.NetworkUtils;

import cz.msebera.android.httpclient.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.net.SocketTimeoutException;
import java.sql.SQLOutput;

/**
 * The main task that downloads a radio map for the specified area.
 * TODO - we should check more thoroughly the concurrency issues
 * Time consuming real time calculation of radiomaps
 */
public class DownloadRadioMapTaskBuid extends AsyncTask<Void, Void, String> {

    public interface DownloadRadioMapListener {

        void onPrepareLongExecute();

        void onErrorOrCancel(String result);

        void onSuccess(String result);
    }

    // Allow only one download task (real time creation of radiomap)
    private static volatile Boolean downInProgress = false;

    private DownloadRadioMapListener mListener;
    private Context ctx;
    private String json_req;
    private String mBuildID;
    private String mFloor_number;
    private Boolean mForceDonwload;

    private boolean success = false;

    // Sync/Run PreExecute Listener on UI Thread
    private final Object syncListener = new Object();
    private boolean run = false;

    public DownloadRadioMapTaskBuid(DownloadRadioMapListener mListener, Context ctx, String lat, String lon, String buid, String floor_number, boolean forceDonwload) {
        this(mListener, ctx, buid, floor_number, forceDonwload);
    }

    private DownloadRadioMapTaskBuid(DownloadRadioMapListener mListener, Context ctx, String buid, String floor_number, boolean forceDonwload) {

        try {
            this.mListener = mListener;
            this.ctx = ctx;

            JSONObject j = new JSONObject();

            j.put("username", "username");
            j.put("password", "pass");

            // add the building and floor in order to get only the necessary
            // radio map
            j.put("buid", buid);
            j.put("floor", floor_number);

            this.json_req = j.toString();
            this.mBuildID = buid;
            this.mFloor_number = floor_number;
            this.mForceDonwload = forceDonwload;

        } catch (JSONException ignored) {
        }

    }

    public DownloadRadioMapListener getCallbackInterface() {
        return this.mListener;
    }

    @Override
    protected String doInBackground(Void... params) {

        boolean releaseLock = false;
        try {

            if (json_req == null)
                return "Error creating the request!";
            // check sdcard state
            File root;
            try {
                root = AnyplaceUtils.getRadioMapFolder(ctx, mBuildID, mFloor_number);
            } catch (Exception e) {
                return e.getMessage();
            }

            File okfile = new File(root, "ok.txt");
            if (!mForceDonwload && okfile.exists()) {
                success = true;
                return "Successfully read radio map from cache!";
            }

            // Allow only one download of the radiomap
            synchronized (downInProgress) {
                if (!downInProgress) {
                    downInProgress = true;
                    releaseLock = true;
                } else {
                    return "Already downloading radio map. Please wait...";
                }
            }

            runPreExecuteOnUI();
            okfile.delete();

            // receive only the radio map for the current floor, 0 timeout overrides default timeout
            String response = NetworkUtils.downloadHttpClientJsonPost(AnyplaceAPI.getRadioDownloadBuid(), json_req, 0);
            JSONObject json = new JSONObject(response);

            if (json.getString("status").equalsIgnoreCase("error")) {
                return "Error Message: " + json.getString("message");
            }

            String means = json.getString("map_url_mean");

            // create the credentials JSON in order to send and download the radio map
            JSONObject json_credentials = new JSONObject();
            json_credentials.put("username", "username");
            json_credentials.put("password", "pass");
            String cred_str = json_credentials.toString();

            String ms = NetworkUtils.downloadHttpClientJsonPost("http://" + means, cred_str);

            // check if the files downloaded correctly
            if (ms.contains("error")) {
                json = new JSONObject(response);
                return "Error Message: " + json.getString("message");
            }

            // rename the radiomap according to the floor
            // parameters and weights not used any more (RPF Algorithm Removed)
            String filename_radiomap_download = AnyplaceUtils.getRadioMapFileName(mFloor_number);
            // String rbf_weights_fname = mean_fname.replace(".txt", "-rbf-weights.txt");
            // String parameters_fname = mean_fname.replace(".txt", "-parameters.txt");
            FileWriter out;

            out = new FileWriter(new File(root, filename_radiomap_download));
            out.write(ms);
            out.close();

            out = new FileWriter(okfile);
            out.write("ok;version:0;");
            out.close();

            waitPreExecute();
            success = true;
            return "Successfully saved radio maps!";

        } catch (ConnectTimeoutException e) {
            e.printStackTrace();
            return "Connecting to Anyplace service is taking too long!";
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return "Communication with the server is taking too long!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error downloading radio maps [ " + e.getMessage() + " ]";
        } finally {
            if (releaseLock)
                downInProgress = false;
        }

    }

    @Override
    protected void onPostExecute(String result) {
        if (success) {
            if (mListener != null)
                mListener.onSuccess(result);
        } else {
            // there was an error during the process
            if (mListener != null)
                mListener.onErrorOrCancel(result);
        }
    }

    private void runPreExecuteOnUI() {
        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(ctx.getMainLooper());

        Runnable myRunnable = new Runnable() {

            @Override
            public void run() {
                try {
                    //if (mListener != null)
                    //    mListener.onPrepareLongExecute();
                } finally {
                    synchronized (syncListener) {
                        run = true;
                        syncListener.notifyAll();
                    }
                }
            }
        };

        mainHandler.post(myRunnable);
    }

    private void waitPreExecute() throws InterruptedException {
        synchronized (syncListener) {
            while (!run) {
                syncListener.wait();
            }
        }
    }

    @Override
    protected void onCancelled(String result) {
        if (mListener != null)
            mListener.onErrorOrCancel(result);
    }

    @Override
    protected void onCancelled() {
        if (mListener != null)
            mListener.onErrorOrCancel("Downloading RadioMap was cancelled!");
    }

}// end of radiomap download task


