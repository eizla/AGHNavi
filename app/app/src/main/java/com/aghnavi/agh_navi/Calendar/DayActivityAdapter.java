package com.aghnavi.agh_navi.Calendar;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aghnavi.agh_navi.Event.EventSerializable;
import com.aghnavi.agh_navi.R;

import java.util.ArrayList;
import java.util.List;

public class DayActivityAdapter  extends RecyclerView.Adapter implements Filterable {

    private RecyclerView mRecyclerView;
    private List<EventSerializable> mEventsSerializableList;
    private List<EventSerializable> mEventsSerializableListFiltered;
    private Context mContext;
    private DayActivity mActivity;

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String search = constraint.toString();
                if(search.isEmpty()){
                    mEventsSerializableListFiltered = mEventsSerializableList;
                }
                else {
                    List<EventSerializable> temp = new ArrayList<>();
                    for(EventSerializable e : mEventsSerializableList){
                        if(e.getTitle().toLowerCase().contains(search) || e.getType().toLowerCase().contains(search)){
                            temp.add(e);
                        }
                    }
                    mEventsSerializableListFiltered = temp;
                }


                FilterResults filterResults = new FilterResults();
                filterResults.values = mEventsSerializableListFiltered;
                filterResults.count = mEventsSerializableListFiltered.size();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mEventsSerializableListFiltered = ((ArrayList<EventSerializable> )results.values);
                notifyDataSetChanged();

            }
        };
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{

        public CardView mCardView;
        public RelativeLayout mRelativeLayout;
        public TextView mEventTimeTextView;
        public Button mOptionsButton;
        public TextView mEventNameTextView;
        public TextView mEventPlaceTextView;
        public TextView mEventTypeTextView;

        public MyViewHolder(View itemView) {
            super(itemView);

            mCardView = (CardView) itemView.findViewById(R.id.eventCardView);
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);
            mEventTimeTextView = (TextView) itemView.findViewById(R.id.eventTimeTextView);
            mOptionsButton = (Button) itemView.findViewById(R.id.optionsButton);
            mEventNameTextView = (TextView) itemView.findViewById(R.id.eventNameTextView);
            mEventPlaceTextView = (TextView) itemView.findViewById(R.id.eventPlaceTextView);
            mEventTypeTextView = (TextView) itemView.findViewById(R.id.eventTypeTextView);

        }
    }

    public DayActivityAdapter(RecyclerView recyclerView, List<EventSerializable> events, Context context, DayActivity activity) {

        this.mRecyclerView = recyclerView;
        this.mEventsSerializableList = events;
        this.mContext = context;
        this.mEventsSerializableListFiltered = events;
        this.mActivity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.event_layout, viewGroup, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int positionClicked = mRecyclerView.getChildAdapterPosition(v);
                //todo: handle item clicked

            }
        });

        return new DayActivityAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        final EventSerializable eventSerializable = mEventsSerializableListFiltered.get(position);
        ((MyViewHolder) viewHolder).mEventTimeTextView.setText(eventSerializable.getDurationString());
        ((MyViewHolder) viewHolder).mEventNameTextView.setText(eventSerializable.getTitle());
        ((MyViewHolder) viewHolder).mEventPlaceTextView.setText(eventSerializable.getLocation());
        ((MyViewHolder) viewHolder).mEventTypeTextView.setText(eventSerializable.getType());

        int color = getColorForEventType(eventSerializable.getType());
        ((MyViewHolder) viewHolder).mCardView.setCardBackgroundColor(color);
        ((MyViewHolder) viewHolder).mRelativeLayout.setBackgroundColor(color);

        ((MyViewHolder) viewHolder).mOptionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popup = new PopupMenu(mContext, ((MyViewHolder) viewHolder).mOptionsButton);
                popup.inflate(R.menu.event_options_menu);

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.edit:
                                mActivity.editEvent(eventSerializable);
                                break;
                            case R.id.delete:
                                mActivity.deleteEvent(eventSerializable);
                                break;
                        }
                        return true;
                    }
                });

                popup.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if(mEventsSerializableListFiltered != null){
            return mEventsSerializableListFiltered.size();
        }
        return 0;
    }

    private int getColorForEventType(String evenType){
        int backgroundColor;

        switch (evenType){
            case "WYKŁAD":
                backgroundColor = ContextCompat.getColor(mContext, R.color.eventTypeWYKŁAD);
                break;
            case "LABORATORIUM":
                backgroundColor = ContextCompat.getColor(mContext, R.color.eventTypeLABORATORIUM);
                break;
            case "ĆWICZENIA":
                backgroundColor = ContextCompat.getColor(mContext, R.color.eventTypeĆWICZENIA);
                break;
            case "EGZAMIN":
                backgroundColor = ContextCompat.getColor(mContext, R.color.eventTypeEGZAMIN);
                break;
            case "KOLOKWIUM":
                backgroundColor = ContextCompat.getColor(mContext, R.color.eventTypeKOLOKWIUM);
                break;
            case "INNE":
                backgroundColor = ContextCompat.getColor(mContext, R.color.eventTypeINNE);
                break;
            default:
                backgroundColor = ContextCompat.getColor(mContext, R.color.eventTypeINNE);
                break;
        }
        return backgroundColor;
    }

    public void updateData(List<EventSerializable> eventSerializableList){
        mEventsSerializableListFiltered = eventSerializableList;
        Log.d("ADAPTER", "data null");
        notifyDataSetChanged();
    }

    public void updateData(EventSerializable eventSerializableList){
        mEventsSerializableListFiltered.add(eventSerializableList);
        Log.d("ADAPTER", "data null");
        notifyDataSetChanged();
    }
}
