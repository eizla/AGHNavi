package com.aghnavi.agh_navi.dmsl.utils;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

public class NetworkUtils {

    private static String encodeURL(String urlStr) throws URISyntaxException, MalformedURLException {
        URL url = new URL(urlStr);
        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
        url = uri.toURL();
        return url.toString();
    }

    private static String readInputStream(InputStream stream) throws IOException {
        int n;
        char[] buffer = new char[1024 * 4];
        StringWriter writer = new StringWriter();
        try (InputStreamReader reader = new InputStreamReader(stream, "UTF8")) {
            while (-1 != (n = reader.read(buffer)))
                writer.write(buffer, 0, n);

            return writer.toString();
        }
    }

    // <HTTP Get>
    private static InputStream downloadHttps(String urlS) throws IOException, URISyntaxException {
        InputStream is;

        URL url = new URL(encodeURL(urlS));

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        conn.connect();

        int response = conn.getResponseCode();
        if (response == 200) {
            is = conn.getInputStream();
        } else {
            throw new RuntimeException("Server Error Code: " + conn.getResponseCode());
        }


        return is;
    }

    private static InputStream downloadHttp(String urlS) throws URISyntaxException, IOException {
        InputStream is;

        URL url = new URL(encodeURL(urlS));

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        conn.connect();

        int response = conn.getResponseCode();
        if (response == 200) {
            is = conn.getInputStream();
        } else {
            throw new RuntimeException("Server Error Code: " + conn.getResponseCode());
        }

        return is;
    }

    static String downloadUrlAsStringHttp(String urlS) throws URISyntaxException, IOException {
        InputStream is = null;
        try {
            is = downloadHttp(urlS);
            return readInputStream(is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static String downloadUrlAsStringHttps(String urlS) throws IOException, URISyntaxException {
        InputStream is = null;
        try {
            is = downloadHttps(urlS);
            return readInputStream(is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
    // </HTTP Get>

    /* HTTP Post Json (InputStream) */
    private static InputStream ISdownloadHttpClientJsonPostHelp(String url, String json, int timeout) throws URISyntaxException, IOException {

        InputStream is;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setConnectTimeout(timeout);
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Accept-Encoding", "gzip");
        con.setRequestProperty("Content-type", "application/json");
        con.setDoOutput(true);
        con.setDoInput(true);
        con.connect();

        OutputStream os = con.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(json);
        writer.flush();
        writer.close();
        os.close();

        String encoding = con.getContentEncoding();

        int response = con.getResponseCode();
        if (response == HttpsURLConnection.HTTP_OK) {
            if (encoding != null && encoding.equals("gzip")) {
                is = new GZIPInputStream(con.getInputStream());
            } else {
                is = con.getInputStream();
            }
        } else {
            throw new RuntimeException("Service Error: " + con.getResponseMessage());
        }

        return is;
    }

    public static String downloadHttpClientJsonPost(String url, String json, int timeout) throws URISyntaxException, IOException {
        return readInputStream(ISdownloadHttpClientJsonPostHelp(url, json, timeout));
    }

    public static String downloadHttpClientJsonPost(String url, String json) throws URISyntaxException, IOException {
        return downloadHttpClientJsonPost(url, json, 20000);
    }

    public static InputStream downloadHttpClientJsonPostStream(String url, String json) throws IllegalStateException, IOException, URISyntaxException {
        return ISdownloadHttpClientJsonPostHelp(url, json, 20000);
    }
    //</HTTP Post Json>

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    //NEVER USED BUT WHATEVER
    public static boolean isOnlineWiFiOrMobile(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            boolean isWifiConn = networkInfo.isConnected();
            networkInfo = connMgr.getActiveNetworkInfo();
            boolean isMobileConn = networkInfo.isConnected();
            return isMobileConn || isWifiConn;
        }
        else {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean isWifiConn = networkInfo.isConnected();
            networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            boolean isMobileConn = networkInfo.isConnected();
            return isMobileConn || isWifiConn;
        }
    }

    //NEVER USED BUT WHATEVER
    public static boolean haveNetworkConnection(Activity activity) {

        boolean haveWifi = false;
        boolean haveMobile = false;

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = cm.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network mNetwork : networks) {
                networkInfo = cm.getNetworkInfo(mNetwork);
                    if (networkInfo != null && networkInfo.getTypeName().equalsIgnoreCase("WIFI"))
                        if (networkInfo.isConnectedOrConnecting())
                            haveWifi = true;
                    if (networkInfo != null && networkInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                        if (networkInfo.isConnectedOrConnecting())
                            haveMobile = true;

            }
        }
        else {
            NetworkInfo[] netInfo = cm.getAllNetworkInfo();
            for (NetworkInfo ni : netInfo) {
                if (ni != null && ni.getTypeName().equalsIgnoreCase("WIFI"))
                    if (ni.isConnectedOrConnecting())
                        haveWifi = true;
                if (ni != null && ni.getTypeName().equalsIgnoreCase("MOBILE"))
                    if (ni.isConnectedOrConnecting())
                        haveMobile = true;
            }
        }
        return haveMobile || haveWifi;
    }

}

