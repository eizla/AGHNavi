package com.aghnavi.agh_navi.dmsl.utils;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextPaint;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AndroidUtils {

    //TODO: probably to delete
    public static void showWifiSettings(final Activity activity) {
        showWifiSettings(activity, null, null);
    }

    //NOTIFICATION
    // Runnable default value null
    public static void showWifiSettings(final Activity activity, final Runnable yes, final Runnable no) {
        // check for internet connection
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("No Internet Connection");
        alertDialog.setMessage("Would you like to change settings ?");
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                if (yes != null)
                    yes.run();
            }
        });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                if (no != null)
                    no.run();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    //NOTIFICATION
    public static void showGPSSettings(final Activity activity) {
        // check for internet connection
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setTitle("GPS ");
        alertDialog.setMessage("GPS jest wyłączony. Proszę włączyć.");
        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Opcje", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    public static boolean checkExternalStorageState() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (!mExternalStorageWriteable || !mExternalStorageAvailable) {
            // we cannot download the floor plan on the sdcard
            return false;
        }
        return true;
    }

    public static void unzip(String strZipFile) {

        try {
			/*
			 * STEP 1 : Create directory with the name of the zip file
			 *
			 * For e.g. if we are going to extract c:/demo.zip create c:/demo directory where we can extract all the zip entries
			 */
            File fSourceZip = new File(strZipFile);
            String zipPath = strZipFile.substring(0, strZipFile.length() - 4);
            File temp = new File(zipPath);
            temp.mkdir();
            //if(temp.mkdir()) {
                System.out.println(zipPath + " created");
            //}
            //else {
            //    System.out.println("UNZIP Cannot create directory!");
            //    return;
            //}

			/*
			 * STEP 2 : Extract entries while creating required sub-directories
			 */
            ZipFile zipFile = new ZipFile(fSourceZip);
            Enumeration<? extends ZipEntry> e = zipFile.entries();

            /*
            while (e.hasMoreElements()) {

                ZipEntry entry = (ZipEntry) e.nextElement();
                File destinationFilePath = new File(zipPath, entry.getName());

                // create directories if required.
                if(destinationFilePath.getParentFile().mkdirs()) {

                    // if the entry is directory, leave it. Otherwise extract it.
                    if (!entry.isDirectory()) {
                        // System.out.println("Extracting " + destinationFilePath);

                        BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

                        int b;
                        byte buffer[] = new byte[1024];

                        FileOutputStream fos = new FileOutputStream(destinationFilePath);
                        BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);

                        while ((b = bis.read(buffer, 0, 1024)) != -1) {
                            bos.write(buffer, 0, b);
                        }

                        // flush the output stream and close it.
                        bos.flush();
                        bos.close();

                        // close the input stream.
                        bis.close();
                    }
                }
                else {
                    System.out.println("DESTINATION Cannot create directory!");
                    return;
                }

            }
            */
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                File destinationFilePath = new File(zipPath, entry.getName());

                // create directories if required.
                destinationFilePath.getParentFile().mkdirs();

                // if the entry is directory, leave it. Otherwise extract it.
                if (entry.isDirectory()) {
                    continue;
                } else {
                    // System.out.println("Extracting " + destinationFilePath);

					/*
					 * Get the InputStream for current entry of the zip file using
					 *
					 * InputStream getInputStream(Entry entry) method.
					 */
                    BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));

                    int b;
                    byte buffer[] = new byte[1024];

					/*
					 * read the current entry from the zip file, extract it and write the extracted file.
					 */
                    FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);

                    while ((b = bis.read(buffer, 0, 1024)) != -1) {
                        bos.write(buffer, 0, b);
                    }

                    // flush the output stream and close it.
                    bos.flush();
                    bos.close();

                    // close the input stream.
                    bis.close();
                }

            }
            zipFile.close();
        } catch (IOException ioe) {
            System.out.println("IOError :" + ioe);
        }

    }

    //METHOD FOR FILLING POI TEXT BOX
    public static String fillTextBox(TextPaint paint, int fragmentWidth, String source) {
        StringBuilder sb = new StringBuilder();
        final int length = source.length();
        // Display whole words only
        int lastWhiteSpace = 0;

        for (int index = 0; paint.measureText(sb.toString()) < fragmentWidth && index < length; index++) {
            char c = source.charAt(index);
            if (Character.isWhitespace(c))
                lastWhiteSpace = index;
            sb.append(c);
        }
        if (sb.length() != length) {
            // Delete last word part
            sb.delete(lastWhiteSpace, sb.length());
            sb.append("...");
        }
        return sb.toString();

    }

    //METHOD FOR FILLING POI TEXT BOX
    public static String fillTextBox(TextPaint paint, int fragmentWidth, String source, int start) {
        StringBuilder sb = new StringBuilder();
        final int length = source.length();
        int indexLeft = start;
        int indexRight = start + 1;
        int lastWhiteSpaceL = 0;
        int lastWhiteSpaceR = 0;

        while (paint.measureText(sb.toString()) < fragmentWidth && (indexLeft >= 0 || indexRight < length)) {
            if (indexLeft >= 0) {
                char c = source.charAt(indexLeft);
                if (Character.isWhitespace(c))
                    lastWhiteSpaceL = indexLeft;
                sb.insert(0, c);
                indexLeft--;
            }

            if (indexRight < length) {
                char c = source.charAt(indexRight);
                if (Character.isWhitespace(c))
                    lastWhiteSpaceR = indexRight;
                sb.append(c);
                indexRight++;
            }
        }

        if (indexLeft >= 0) {
            // Delete first word part
            sb.delete(0, lastWhiteSpaceL - indexLeft);
            sb.insert(0, "...");
            indexLeft = lastWhiteSpaceL - 3; // Set new index left
        }
        if (indexRight < length) {
            // Delete last word part
            sb.delete(lastWhiteSpaceR - (indexLeft + 1), sb.length());
            sb.append("...");
        }
        return sb.toString();
    }


    //LOCATION BY IP - IT IS FUCKING SUSPICIOUS AS FUCK
    public static GeoPoint getIPLocation() throws Exception {
        // http://ip-api.com/docs/api:json
        String response = NetworkUtils.downloadUrlAsStringHttp("http://ip-api.com/json");

        JSONObject json = new JSONObject(response);

        if (json.getString("status").equalsIgnoreCase("error")) {
            throw new Exception(json.getString("message"));
        }
        String lat = json.getString("lat");
        String lon = json.getString("lon");
        return new GeoPoint(lat, lon);

    }
}
