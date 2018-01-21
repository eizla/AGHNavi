package com.aghnavi.agh_navi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aghnavi.agh_navi.fabric.FabricInitializer;

public class SplashActivity extends AppCompatActivity implements FabricInitializer{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFabric(this);


        //TODO: later StartActivity->MapNavigationActivity (eventually some Login check)
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);

        this.finish();
    }
}
