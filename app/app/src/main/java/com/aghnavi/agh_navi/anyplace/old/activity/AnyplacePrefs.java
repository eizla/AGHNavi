package com.aghnavi.agh_navi.anyplace.old.activity;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.aghnavi.agh_navi.MapNavigationActivity;
import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.dmsl.tasks.DeleteFolderBackgroundTask;
import com.aghnavi.agh_navi.dmsl.utils.AnyplaceUtils;
import com.aghnavi.agh_navi.fabric.FabricInitializer;

import java.io.File;

/**
 * Defines the behavior of the preferences menu.
 *
 * @author KIOS Research Center and Data Management Systems Lab, University of Cyprus
 *
 */
public class AnyplacePrefs extends PreferenceActivity implements FabricInitializer{

    public enum Action {
        REFRESH_BUILDING, REFRESH_MAP
    }

    /**
     * Build preference menu when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initializeFabric(this);

        // Load the appropriate preferences
        getPreferenceManager().setSharedPreferencesName(MapNavigationActivity.SHARED_PREFS_ANYPLACE);

        addPreferencesFromResource(R.xml.anyplace_preferences_anyplace);

        /*
        getPreferenceManager().findPreference("clear_radiomaps").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                File root;
                try {
                    root = AnyplaceUtils.getRadioMapsRootFolder(AnyplacePrefs.this);
                    DeleteFolderBackgroundTask task = new DeleteFolderBackgroundTask(AnyplacePrefs.this);
                    task.setFiles(root);
                    task.execute();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        */

        getPreferenceManager().findPreference("clear_floorplans").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                File root;
                try {
                    root = AnyplaceUtils.getFloorPlansRootFolder(AnyplacePrefs.this);
                    DeleteFolderBackgroundTask task = new DeleteFolderBackgroundTask(AnyplacePrefs.this);
                    task.setFiles(root);
                    task.execute();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        getPreferenceManager().findPreference("refresh_building").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("action", Action.REFRESH_BUILDING);
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
            }
        });
        getPreferenceManager().findPreference("refresh_map").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("action", Action.REFRESH_MAP);
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
            }
        });

        // Customize the description of algorithms
        getPreferenceManager().findPreference("Short_Desc").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                final String[] names = getResources().getStringArray(R.array.AlgorithmsNames);
                final String[] descriptions = getResources().getStringArray(R.array.AlgorithmsDescriptions);

                // TODO Auto-generated method stub
                AlertDialog.Builder builder = new AlertDialog.Builder(AnyplacePrefs.this);

                builder.setNeutralButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Show something if does not exit the app
                        dialog.dismiss();
                    }
                });

                builder.setTitle("Opis algorytmów");
                builder.setSingleChoiceItems(names, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                popup_msg(descriptions[0], names[0], 0);
                                break;
                            case 1:
                                popup_msg(descriptions[1], names[1], 0);
                                break;
                            case 2:
                                popup_msg(descriptions[2], names[2], 0);
                                break;
                            case 3:
                                popup_msg(descriptions[3], names[3], 0);
                                break;
                        }

                    }
                });

                AlertDialog alert = builder.create();

                alert.show();
                return true;

            }
        });
    }

    /**
     * Actions to be taken on Preference menu exit.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            default:
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void popup_msg(String msg, String title, int imageID) {

        AlertDialog.Builder alert_box = new AlertDialog.Builder(this);
        alert_box.setTitle(title);
        alert_box.setMessage(msg);
        alert_box.setIcon(imageID);

        alert_box.setNeutralButton("Ukryj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alert_box.create();
        alert.show();
    }


}
