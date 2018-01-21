package com.aghnavi.agh_navi.Places;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.aghnavi.agh_navi.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlacesInNeighbourhoodAdapter extends RecyclerView.Adapter implements Filterable {

    private RecyclerView mRecyclerView;
    private Set<PlaceSerializable> mPlaceSerializableSet;
    private List<PlaceSerializable> mPlacesListTemp;
    private List<PlaceSerializable> mPlaceListFiltered;
    private LatLng mCurrentDeviceLatLang;
    private PlacesInNeighbourhoodActivity mActivity;

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String search = constraint.toString();
                if(search.isEmpty()){
                    mPlaceListFiltered = mPlacesListTemp;
                }
                else {
                    List<PlaceSerializable> temp = new ArrayList<>();
                    for(PlaceSerializable p : mPlacesListTemp){
                        List<String> placeListTypes = getPlaceTypesList(p);
                        if(p.getName().toUpperCase().contains(search.toUpperCase())
                            || placeListTypes.contains(search.toUpperCase()))
                        {
                            temp.add(p);
                        }
                    }
                    mPlaceListFiltered = temp;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mPlaceListFiltered;
                filterResults.count = mPlaceListFiltered.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mPlaceListFiltered = ((ArrayList<PlaceSerializable> )results.values);
                notifyDataSetChanged();

            }
        };
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
            mCardView = itemView.findViewById(R.id.card_view_place);
            mPlaceIcon = (ImageView) itemView.findViewById(R.id.placeIcon);
            mDistanceTextView = (TextView) itemView.findViewById(R.id.distanceTextView);
            mPlaceNameTextView = (TextView) itemView.findViewById(R.id.placeNameTextView);
            mPlaceAddressTextView = (TextView) itemView.findViewById(R.id.placeAddressTextView);
            mStarImageView = (ImageView) itemView.findViewById(R.id.starImageView);
            mRatingTextView = (TextView) itemView.findViewById(R.id.ratingTextView);
        }
    }

    public PlacesInNeighbourhoodAdapter(RecyclerView mRecyclerView, Set<PlaceSerializable> placeSerializableSet, LatLng currentDeviceLatLang, PlacesInNeighbourhoodActivity activity) {

        this.mRecyclerView = mRecyclerView;
        this.mPlaceSerializableSet = placeSerializableSet;
        this.mPlacesListTemp = new ArrayList<>(mPlaceSerializableSet);
        this.mPlaceListFiltered = new ArrayList<>(mPlaceSerializableSet);
        this.mCurrentDeviceLatLang = currentDeviceLatLang;
        this.mActivity = activity;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.place_layout_2, viewGroup, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int positionClicked = mRecyclerView.getChildAdapterPosition(v);
                mActivity.showDirectionsToPlace(mPlaceListFiltered.get(positionClicked));
            }
        });

        return new MyViewHolder(view);
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
            distance /= 1000;
            unit = "km";
        }
        String result = Integer.toString(distance) + " " + unit;
        return  result;
    }

    private List<String> getPlaceTypesList(PlaceSerializable p){
        List<String> temp = new ArrayList<>();

//        for(int i : placeTypes){
//            for(EPlaceType e : EPlaceType.values()){
//                if( i == e.getNum()){
//                    temp.add(e.getType());
//                }
//            }
//        }

        for(int type : p.getPlaceTypes()){
            Log.d("Adapter:", "place type int: " + type);
            switch (type) {
                case 9:
                    //BAR
                    temp.add("BAR");
                    break;
                case 14:
                    //BUS STATION
                    temp.add("PRZYSTANEK AUTOBUSOWY");
                    break;
                case 15:
                    //CAFE
                    temp.add("KAWIARNIA");
                    break;
                case 88:
                    //STORE
                    temp.add("SKLEP");
                    break;
                case 79:
                    //RESTAIRANT
                    temp.add("RESTAURACJA");
                    break;
            }
        }

        return temp;
    }

    public void filterByType(List<String> types){

        List<PlaceSerializable> temp = new ArrayList<PlaceSerializable>();
        for(PlaceSerializable p : mPlacesListTemp){
            List<String> placeListTypes = getPlaceTypesList(p);
            Log.d("items in types list:", "" +placeListTypes.size() + placeListTypes.get(0));
            for(String type : types){
                Log.d("type", type);
                for(String placeType : placeListTypes){
                    if(placeType.equalsIgnoreCase(type)){
                        temp.add(p);
                    }
                }
            }
        }
        Log.d("items in list:", "" +temp.size());
        mPlaceListFiltered = temp;
        notifyDataSetChanged();
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
}
