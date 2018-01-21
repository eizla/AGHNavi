package com.aghnavi.agh_navi.Settings;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.fabric.FabricInitializer;

import butterknife.BindView;


public class RelatedAccountsActivity extends AppCompatActivity implements FabricInitializer {


    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFabric(this);
        setContentView(R.layout.activity_related_accounts);

        mToolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        mToolbar.setTitle("Powiązane konta");
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
