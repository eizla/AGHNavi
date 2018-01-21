package com.aghnavi.agh_navi.anyplace.old.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.fabric.FabricInitializer;

/**
 * This Activity displays information about AnyPlace and has logos that
 * link to the corresponding websites.
 * The version of the current AnyPlace installation is also displayed.
 *
 * @author Lambros Petrou
 *
 */
public class AnyplaceAboutActivity extends Activity implements FabricInitializer {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFabric(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.anyplace_activity_about);

        // set the version name in the textview
        PackageInfo pinfo;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pinfo.versionName;
            ((TextView)findViewById(R.id.versionBody)).setText(versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("anyplace about", "Cannot get version name and code!");
        }

        // make the logo images clickable and send the user to the clicked website
        ImageView img = (ImageView)findViewById(R.id.viewLogoAnyplace);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://anyplace.cs.ucy.ac.cy/"));
                startActivity(browserIntent);
            }
        });

        img = (ImageView)findViewById(R.id.viewLogoDMSL);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://dmsl.cs.ucy.ac.cy/"));
                startActivity(browserIntent);
            }
        });

        img = (ImageView)findViewById(R.id.viewLogoKIOS);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kios.ucy.ac.cy/"));
                startActivity(browserIntent);
            }
        });

        img = (ImageView)findViewById(R.id.viewLogoUCY);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.ucy.ac.cy/"));
                startActivity(browserIntent);
            }
        });

    }

}

