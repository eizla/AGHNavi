package com.aghnavi.agh_navi;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aghnavi.agh_navi.Places.EPlaceType;
import com.aghnavi.agh_navi.Places.PlaceSerializable;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlacesPanelAdapter extends RecyclerView.Adapter {

    private RecyclerView mRecyclerView;
    private Set<PlaceSerializable> mPlaceSerializableSet;
    private List<PlaceSerializable> mPlacesListTemp;
    private List<PlaceSerializable> mPlaceListFiltered;
    private LatLng mCurrentDeviceLatLang;
    private MapsActivity mActivity;

    public List<PlaceSerializable> getPlaceSerializableSet() {
        return mPlaceListFiltered;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{

        public CardView mCardView;
        public ImageView mPlaceIcon;
        public TextView mDistanceTextView;
        public TextView mPlaceNameTextView;
        public TextView mPlaceAddressTextView;
        public ImageView mStarImageView;
        public TextView mRatingTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mCardView = (CardView) itemView.findViewById(R.id.card_view_place);
            mPlaceIcon = (ImageView) itemView.findViewById(R.id.placeIcon);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.distanceTextView);
            mPlaceNameTextView = (TextView) itemView.findViewById(R.id.placeNameTextView);
            mPlaceAddressTextView = (TextView) itemView.findViewById(R.id.placeAddressTextView);
            mStarImageView = (ImageView) itemView.findViewById(R.id.starImageView);
            mRatingTextView = (TextView) itemView.findViewById(R.id.ratingTextView);
        }
    }

    public PlacesPanelAdapter(RecyclerView mRecyclerView, Set<PlaceSerializable> placeSerializableSet, LatLng currentDeviceLatLang, MapsActivity activity)
    {
        this.mRecyclerView = mRecyclerView;
        this.mPlaceSerializableSet = placeSerializableSet;
        this.mPlacesListTemp = new ArrayList<>(mPlaceSerializableSet);
        this.mPlaceListFiltered = new ArrayList<>(mPlaceSerializableSet);
        this.mCurrentDeviceLatLang = currentDeviceLatLang;
        this.mActivity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_layout_2, viewGroup, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int positionClicked = mRecyclerView.getChildAdapterPosition(v);
                mActivity.showDirectionsToPlace(mPlaceListFiltered.get(positionClicked));
            }
        });

        return new PlacesPanelAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        PlaceSerializable placeSerializable = mPlaceListFiltered.get(position);
        ((MyViewHolder) viewHolder).mPlaceIcon.setImageDrawable(getIcon(placeSerializable));
        ((MyViewHolder) viewHolder).mDistanceTextView.setText(countDistance(placeSerializable.getLongitude(), placeSerializable.getLatitude()));
        ((MyViewHolder) viewHolder).mPlaceNameTextView.setText(placeSerializable.getName());
        ((MyViewHolder) viewHolder).mPlaceAddressTextView.setText(placeSerializable.getAddress());

        if(placeSerializable.getRating() < 0){
            ((MyViewHolder) viewHolder).mStarImageView.setVisibility(View.INVISIBLE);
            ((MyViewHolder) viewHolder).mRatingTextView.setVisibility(View.INVISIBLE);
        }
        else{
            ((MyViewHolder) viewHolder).mRatingTextView.setText(Float.toString(placeSerializable.getRating()));
        }
    }

    @Override
    public int getItemCount() {
        return mPlaceListFiltered.size();
    }

    private String countDistance(double longitude, double latitude){

        if(mCurrentDeviceLatLang == null){
            return "";
        }else {
            Location loc1 = new Location("");
            loc1.setLatitude(mCurrentDeviceLatLang.latitude);
            loc1.setLongitude(mCurrentDeviceLatLang.longitude);

            Location loc2 = new Location("");
            loc2.setLatitude(latitude);
            loc2.setLongitude(longitude);

            int distance = Math.round(loc1.distanceTo(loc2));
            String unit;
            if(distance < 1000){
                unit = "m";
            }
            else{
                unit = "km";
            }
            String result = Integer.toString(distance) + " " + unit;
            return  result;
        }

    }

    private Drawable getIcon(PlaceSerializable p){

        int id;

        switch (p.getPlaceTypes().get(0)){
            case 9:
                //BAR
                id = R.drawable.ic_local_bar_white_24dp;
                break;
            case 14:
                //BUS STATION
                id = R.drawable.ic_directions_bus_white_24dp;
                break;
            case 15:
                //CAFE
                id = R.drawable.ic_local_cafe_white_24dp;
                break;
            case 88:
                //STORE
                id = R.drawable.ic_local_grocery_store_white_24dp;
                break;
            case 79:
                //RESTAIRANT
                id = R.drawable.ic_restaurant_white_24dp;
                break;
            default:
                id = R.drawable.ic_room_white_24dp;
        }

        Drawable result = ContextCompat.getDrawable(mActivity.getApplicationContext(), id);
        return result;
    }

    public void updateData(List<PlaceSerializable> eventSerializableList){
        mPlaceListFiltered = eventSerializableList;
        this.notifyDataSetChanged();
    }


}
