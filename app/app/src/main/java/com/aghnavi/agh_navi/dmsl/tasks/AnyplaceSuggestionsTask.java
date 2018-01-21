package com.aghnavi.agh_navi.dmsl.tasks;


import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;

import com.aghnavi.agh_navi.dmsl.cache.AnyplaceCache;
import com.aghnavi.agh_navi.dmsl.google.api.GooglePlaces;
import com.aghnavi.agh_navi.dmsl.google.api.Place;
import com.aghnavi.agh_navi.dmsl.google.api.PlacesList;
import com.aghnavi.agh_navi.dmsl.nav.AnyPlaceSearchingHelper;
import com.aghnavi.agh_navi.dmsl.nav.IPoisClass;
import com.aghnavi.agh_navi.dmsl.nav.PoisModel;
import com.aghnavi.agh_navi.dmsl.utils.GeoPoint;

import cz.msebera.android.httpclient.conn.ConnectTimeoutException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * The task that provides the Suggestions according to the zoom level and the position.
 */
public class AnyplaceSuggestionsTask extends AsyncTask<Void, Void, String> {

    public interface AnyplaceSuggestionsListener {
        void onSuccess(String result, List<? extends IPoisClass> pois);
        void onUpdateStatus(String string, Cursor cursor);
        void onErrorOrCancel(String result);
    }

    private AnyplaceSuggestionsListener mListener;
    private Context ctx;
    private final Object sync = new Object();
    private boolean run = false;
    private boolean exceptionOccured = false;
    private AnyPlaceSearchingHelper.SearchTypes searchType;
    private GeoPoint position;
    private String query;
    private List<? extends IPoisClass> pois;
    private AnyplaceCache mAnyplaceCache = null;
    private boolean source;

    public AnyplaceSuggestionsTask(AnyplaceSuggestionsListener l, Context ctx, AnyPlaceSearchingHelper.SearchTypes searchType, GeoPoint position, String query, boolean source) {
        this.mListener = l;
        this.searchType = searchType;
        this.position = position;
        this.query = query;
        this.ctx = ctx;
        this.source = source;
        mAnyplaceCache = AnyplaceCache.getInstance(ctx);
    }

    private static boolean matchQueryPoi(String query, String poi) {
        query = query.toLowerCase(Locale.ENGLISH);
        poi = poi.toLowerCase(Locale.ENGLISH);
        String[] segs = poi.split(" ");

        for (String s : segs) {
            if (s.contains(query)) {
                return true;
            }
        }

        return false;
    }

    private List<PoisModel> queryStaticAnyPlacePOI(String query) throws IOException {
        Collection<PoisModel> pois = mAnyplaceCache.getPois();
        List<PoisModel> ianyplace = new ArrayList<PoisModel>();
        for (PoisModel pm : pois) {
            if (matchQueryPoi(query, pm.name) || matchQueryPoi(query, pm.description)) {
                pm.setSource(source);
                ianyplace.add(pm);
            }
        }
        return ianyplace;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            // get the search suggestions
            if (searchType == AnyPlaceSearchingHelper.SearchTypes.INDOOR_MODE) {
                // if we are at a zoom level higher than 19 then we use the
                // AnyPlacePOI API

                // sleep for a while to avoid execution in case another task
                // started and check afterwards if you are cancelled
                Thread.sleep(150);
                if (isCancelled()) {
                    return "Cancelled!";
                }

                // use the 2-step method to get out quickly if this task is
                // cancelled
                List<PoisModel> places = queryStaticAnyPlacePOI(query);
                if (isCancelled()) {
                    return "Cancelled!";
                }

                // create the cursor for the results
                // cursor = AnyPlaceSearchingHelper.prepareSearchViewCursor(places);
                pois = places;

            } else if (searchType == AnyPlaceSearchingHelper.SearchTypes.OUTDOOR_MODE) {
                // at a lower zoom level we use the Google Places API for search
                // in order to allow the user to search more coarsely for
                // locations

                // sleep for a while to avoid execution in case another task
                // started and check afterwards if you are cancelled
                Thread.sleep(500);
                if (isCancelled()) {
                    return "Cancelled!";
                }

                // Get a handler that can be used to post to the main thread
                Handler mainHandler = new Handler(ctx.getMainLooper());

                Runnable myRunnable = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            List<IPoisClass> places = new ArrayList<IPoisClass>(1);
                            PoisModel pm = new PoisModel();
                            pm.name = "Szukaj z Google";
                            places.add(pm);
                            Cursor cursor = AnyPlaceSearchingHelper.prepareSearchViewCursor(places);
                            mListener.onUpdateStatus("Dummy Result", cursor);
                        } finally {
                            synchronized (sync) {
                                run = true;
                                sync.notifyAll();
                            }
                        }
                    }
                };
                mainHandler.post(myRunnable);

                // cursor = AnyplacePOIProvider.queryStatic(query,
                // AnyplacePOIProvider.POI_GOOGLE_PLACES, position);
                PlacesList places = GooglePlaces.queryStaticGoogle(query, position);
                if (isCancelled())
                    return "Cancelled!";

                // create the cursor for the results
                // cursor = AnyPlaceSearchingHelper.prepareSearchViewCursor(places.results);

                for(Place p : places.results) {
                    p.setSource(source);
                }
                pois = places.results;

                synchronized (sync) {
                    while (!run) {
                        sync.wait();
                    }
                }

            }

            if (isCancelled()) {
                return "Cancelled!";
            }

            return "Success!";

        } catch (ConnectTimeoutException e) {
            exceptionOccured = true;
            return "Connecting to the server is taking too long!";
        } catch (SocketTimeoutException e) {
            exceptionOccured = true;
            return "Communication with the server is taking too long!";
        } catch (IOException e) {
            exceptionOccured = true;
            return "Communication error[ " + e.getMessage() + " ]";
        } catch (InterruptedException e) {
            exceptionOccured = true;
            return "Suggestions task interrupted!";
        }

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        // TODO Auto-generated method stub
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        if (exceptionOccured) {
            // call the error listener
            mListener.onErrorOrCancel(result);
        } else {

            // call the success listener
            mListener.onSuccess(result, pois);

        }
    }

    @Override
    protected void onCancelled(String result) {
        // mListener.onErrorOrCancel(result);
    }

    @Override
    protected void onCancelled() { // just for < API 11
        // mListener.onErrorOrCancel("Anyplace Suggestions task cancelled!");
    }
}// end of suggestions task

