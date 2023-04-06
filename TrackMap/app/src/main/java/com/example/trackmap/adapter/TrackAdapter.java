package com.example.trackmap.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackmap.MainActivity;
import com.example.trackmap.R;
import com.example.trackmap.database.TrackData;

import java.util.List;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {
    List<TrackData> itemList;
    Context context;

    public TrackAdapter(List<TrackData> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public TrackAdapter.TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        TrackViewHolder vh = new TrackViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull TrackAdapter.TrackViewHolder holder, int position) {
        holder.textTitle.setText(itemList.get(position).name);
        holder.textTitle.setTag(itemList.get(position).idTrack);
        holder.textDate.setText(itemList.get(position).date);
        ///TODO CHANGE THIS
        holder.textTime.setText(itemList.get(position).date);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class TrackViewHolder extends RecyclerView.ViewHolder {
        public TextView textTitle;
        public TextView textDate;
        public TextView textTime;
        public Button btn;

        public TrackViewHolder(View v) {
            super(v);
            textTitle = v.findViewById(R.id.textTitle);
            textDate = v.findViewById(R.id.textDate);
            textTime = v.findViewById(R.id.textTime);
            btn = v.findViewById(R.id.btnWhole);

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "VOLIM CIVOT", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void clear() {
        itemList.clear();
        notifyDataSetChanged();
    }

    public void add(TrackData item) {
        itemList.add(item);
        notifyDataSetChanged();
    }
}