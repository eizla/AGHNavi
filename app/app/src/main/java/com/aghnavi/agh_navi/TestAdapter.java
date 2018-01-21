package com.aghnavi.agh_navi;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aghnavi.agh_navi.Outdoor.OutdoorNavigationActivity;
import com.aghnavi.agh_navi.Places.PlaceSerializable;

import java.util.ArrayList;

public class TestAdapter extends RecyclerView.Adapter {

    private ArrayList<PlaceSerializable> items;
    private Context context;

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
            mCardView = itemView.findViewById(R.id.card_view_place);
            mPlaceIcon = (ImageView) itemView.findViewById(R.id.placeIcon);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.distanceTextView);
            mPlaceNameTextView = (TextView) itemView.findViewById(R.id.placeNameTextView);
            mPlaceAddressTextView = (TextView) itemView.findViewById(R.id.placeAddressTextView);
            mStarImageView = (ImageView) itemView.findViewById(R.id.starImageView);
            mRatingTextView = (TextView) itemView.findViewById(R.id.ratingTextView);
        }
    }

    public TestAdapter(ArrayList<PlaceSerializable> items, OutdoorNavigationActivity activity){

        this.items = items;
        this.context = activity.getApplicationContext();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_layout_2, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        PlaceSerializable placeSerializable = items.get(position);
//        ((MyViewHolder) viewHolder).mPlacePhoto.setImageResource(R.drawable.splash_background);
     //   ((MyViewHolder) holder).mDistanceTextView.setText(countDistance(placeSerializable.getLongitude(), placeSerializable.getLatitude()));
        ((MyViewHolder) holder).mPlaceIcon.setImageDrawable(getIcon(placeSerializable));
        ((MyViewHolder) holder).mPlaceNameTextView.setText(placeSerializable.getName());
        ((MyViewHolder) holder).mPlaceAddressTextView.setText(placeSerializable.getAddress());

        if(placeSerializable.getRating() < 0){
            ((MyViewHolder) holder).mStarImageView.setVisibility(View.INVISIBLE);
            ((MyViewHolder) holder).mRatingTextView.setVisibility(View.INVISIBLE);
        }
        else{
            ((MyViewHolder) holder).mRatingTextView.setText(Float.toString(placeSerializable.getRating()));
        }


    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    private Drawable getIcon(PlaceSerializable p){

        int id;
        if(p.getPlaceTypes() != null){
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
        }else {
            id = R.drawable.ic_room_white_24dp;
        }


        Drawable result = ContextCompat.getDrawable(context, id);
        return result;
    }
}
