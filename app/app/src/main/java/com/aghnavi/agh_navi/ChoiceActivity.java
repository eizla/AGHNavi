package com.aghnavi.agh_navi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChoiceActivity extends AppCompatActivity {

    @BindView(R.id.outdoor_button)
    Button mOutdoorButton;
    @BindView(R.id.indoor_button)
    Button mIndoorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        ButterKnife.bind(this);

    }

    @OnClick(R.id.outdoor_button)
    public void onOutdoorButtonClicked(Button button){
        Intent intent = new Intent(ChoiceActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.indoor_button)
    public void onIndoorButtonClick(){

        Intent intent = new Intent(ChoiceActivity.this, MapNavigationActivity.class);
        startActivity(intent);

    }
}
