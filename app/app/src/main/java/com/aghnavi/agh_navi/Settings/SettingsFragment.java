package com.aghnavi.agh_navi.Settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.fabric.FabricInitializer;

public class SettingsFragment extends PreferenceFragment implements FabricInitializer{

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        initializeFabric(getActivity());
        addPreferencesFromResource(R.xml.settings);


    }
}
