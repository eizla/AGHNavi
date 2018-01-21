package com.aghnavi.agh_navi.Settings;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.fabric.FabricInitializer;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InformationActivity extends AppCompatActivity implements View.OnClickListener, FabricInitializer {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindString(R.string.informationTitle)
    String mToolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeFabric(this);
        setContentView(R.layout.activity_information);
        ButterKnife.bind(this);
        setUpToolbar();
    }

    private void setUpToolbar(){

        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace_white_24dp);
        mToolbar.setTitle(mToolbarTitle);
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(this);

    }


    @Override
    public void onClick(View view) {
        this.finish();
    }



}
