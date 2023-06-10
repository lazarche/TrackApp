package com.example.trackmap.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.trackmap.R;
import com.example.trackmap.TrackView;
import com.example.trackmap.database.AppDatabase;
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
        holder.textDate.setText("Date: " + itemList.get(position).date);
        holder.textTime.setText("Time: " + itemList.get(position).time/1000);
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
                    Intent intent = new Intent(context, TrackView.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("idd",Integer.parseInt(textTitle.getTag().toString()));
                    context.startActivity(intent);

                }
            });

            btn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Dialog dialogEdit = new Dialog((v.getContext()));
                    dialogEdit.setContentView(R.layout.dialog_track_edit);

                    //Binding
                    EditText trackName = dialogEdit.findViewById(R.id.edit_dialog_track);
                    Button btnCancel = dialogEdit.findViewById(R.id.btn_dialog_track_cancel);
                    Button btnDelete = dialogEdit.findViewById(R.id.btn_dialog_track_delete);
                    Button btnRename = dialogEdit.findViewById(R.id.btn_dialog_track_rename);

                    //Setup
                    trackName.setText(textTitle.getText().toString());

                    btnCancel.setOnClickListener(v1 -> {
                        dialogEdit.dismiss();
                    });

                    btnDelete.setOnClickListener(v1 -> {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        delete(Integer.parseInt(textTitle.getTag().toString()));
                                        dialogEdit.dismiss();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(dialogEdit.getContext());
                        builder.setMessage("Are you sure?").setPositiveButton("Delete track", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    });

                    btnRename.setOnClickListener(v1 -> {
                        rename(Integer.parseInt(textTitle.getTag().toString()), trackName.getText().toString());
                        dialogEdit.dismiss();
                    });

                    dialogEdit.show();
                    return true;
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

    public void delete(int id) {
       AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "TrackMap").allowMainThreadQueries().fallbackToDestructiveMigration().build();

       TrackData td = itemList.stream().filter(trackData -> trackData.idTrack == id).findFirst().orElse(null);

       if(td != null) {
           itemList.remove(td);
           db.trackDao().deleteTrackData(td);
       }
       notifyDataSetChanged();
       db.close();
    }

    public void rename(int id, String newName) {
        AppDatabase db = Room.databaseBuilder(context, AppDatabase.class, "TrackMap").allowMainThreadQueries().fallbackToDestructiveMigration().build();

        TrackData td = itemList.stream().filter(trackData -> trackData.idTrack == id).findFirst().orElse(null);

        if(td != null) {
            td.name = newName;
            db.trackDao().renameTrack(id, newName);
        }
        notifyDataSetChanged();
        db.close();
    }
}
