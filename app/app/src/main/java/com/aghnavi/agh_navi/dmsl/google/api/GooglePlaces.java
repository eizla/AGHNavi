package com.aghnavi.agh_navi.dmsl.google.api;


import android.util.Log;

import com.aghnavi.agh_navi.dmsl.utils.GeoPoint;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import cz.msebera.android.httpclient.client.HttpResponseException;

import java.io.IOException;

//TODO: apparently Google Places Java Library for SERVERS is used here
public class GooglePlaces {

    /** Global instance of the HTTP transport. */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    // Google API Key
    //TODO: need to change API_KEY for ours
    private static final String API_KEY = "AIzaSyBlxWZczCyfXQMcZKrb-EDFvLKP-PoAk8w";

    // Google Places serach url's
    //TODO: old place_holders, need to be updated
    private static final String PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";
    private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";

    /**
     * Searching places
     *
     * @param latitude - latitude of place
     * @param longitude - longitude of place
     * @param radius - radius of anyplace_searchable area
     * @param query_text - type of place to search
     * @return list of places
     * @throws IOException -
     * */
    public static PlacesList autocomplete(double latitude, double longitude, double radius, String query_text) throws IOException {
        try {
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_AUTOCOMPLETE_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("location", latitude + "," + longitude);
            request.getUrl().put("radius", radius); // in meters
            request.getUrl().put("input", query_text);
            return request.execute().parseAs(PlacesList.class);
        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            return null;
        }

    }

    public static PlacesList queryStaticGoogle(String query, GeoPoint position) throws IOException {
        return GooglePlaces.search(position.dlat, position.dlon, -1, query);
    }

    /**
     * Searching places
     *
     * @param latitude - latitude of place
     * @param longitude - longitude of place
     * @param radius - radius of anyplace_searchable area
     * @param query_text - type of place to search
     * @return list of places
     * @throws IOException -
     * */
    private static PlacesList search(double latitude, double longitude, double radius, String query_text) throws IOException {
        try {
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("location", latitude + "," + longitude);
            request.getUrl().put("rankby", "distance"); // in meters
            //request.getUrl().put("sensor", "false");
            request.getUrl().put("keyword", query_text);

            Log.d("Places status", "url: " + request.getUrl().toString());

            PlacesList list = request.execute().parseAs(PlacesList.class);
            // Check log cat for places response status
            Log.d("Places Status", "" + list.status + " size: " + list.results.size());
            return list;
        } catch (HttpResponseException e) {
            Log.e("Error:", e.getMessage());
            return null;
        }

    }

    /**
     * Searching single place full details
     *
     * @param reference - reference id of place - which you will get in search api equest
     * */
    public static PlaceDetails getPlaceDetails(String reference) throws Exception {
        try {
            HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
            HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
            request.getUrl().put("key", API_KEY);
            request.getUrl().put("reference", reference);
            request.getUrl().put("sensor", "false");
            return request.execute().parseAs(PlaceDetails.class);
        } catch (HttpResponseException e) {
            Log.e("PLACES DETAILS ERROR", e.getMessage());
            throw e;
        }
    }

    /**
     * Creating http request Factory
     * */
    private static HttpRequestFactory createRequestFactory(final HttpTransport transport) {
        return transport.createRequestFactory(new HttpRequestInitializer() {
            public void initialize(HttpRequest request) {
                HttpHeaders headers = new HttpHeaders();
                request.setHeaders(headers);
                JsonObjectParser parser = new JsonObjectParser(new JacksonFactory());
                request.setParser(parser);
            }
        });
    }


}
