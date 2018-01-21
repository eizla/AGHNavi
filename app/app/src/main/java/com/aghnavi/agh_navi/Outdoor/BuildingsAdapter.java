package com.aghnavi.agh_navi.Outdoor;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aghnavi.agh_navi.R;

import java.util.ArrayList;


public class BuildingsAdapter extends RecyclerView.Adapter{

    private ArrayList<BuildingTemp> mBuildings;
    private RecyclerView mRecyclerView;
    private FindDirectionsActivity mActivity;

    private class MyViewHolder extends RecyclerView.ViewHolder{

        public TextView mBuildingNameTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mBuildingNameTextView = itemView.findViewById(R.id.building_name_text_view);

        }
    }

    public BuildingsAdapter(ArrayList<BuildingTemp> buildings, RecyclerView recyclerView, FindDirectionsActivity activity){

        this.mBuildings = buildings;
        this.mRecyclerView = recyclerView;
        this.mActivity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.building_row_layout, parent, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int positionClicked = mRecyclerView.getChildAdapterPosition(v);
                mActivity.pickPlace(mBuildings.get(positionClicked));
            }
        });


        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        BuildingTemp clicked = mBuildings.get(position);
        ((MyViewHolder) holder).mBuildingNameTextView.setText(clicked.getName());

    }

    @Override
    public int getItemCount() {
        return mBuildings.size();
    }
}
