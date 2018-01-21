package com.aghnavi.agh_navi.dmsl.google.maps;


import android.content.Context;

import com.aghnavi.agh_navi.R;
import com.aghnavi.agh_navi.dmsl.nav.BuildingModel;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class MyBuildingsRenderer extends DefaultClusterRenderer<BuildingModel> {

    public MyBuildingsRenderer(Context context, GoogleMap map, ClusterManager<BuildingModel> clusterManager) {
        super(context, map, clusterManager);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onBeforeClusterItemRendered(BuildingModel bm, MarkerOptions markerOptions) {

        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.place_pin));
    }

}
