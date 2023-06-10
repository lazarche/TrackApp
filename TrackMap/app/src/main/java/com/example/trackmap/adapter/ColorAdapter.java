package com.example.trackmap.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trackmap.R;
import com.example.trackmap.TrackHighlightCustomization;
import com.example.trackmap.database.ColorData;
import com.example.trackmap.database.TrackData;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import org.w3c.dom.Text;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder>{
    public List<ColorData> itemList;
    Context context;

    public ColorAdapter(List<ColorData> data, Context context) {
        itemList = data;
        this.context = context;

        sortList();
    }

    @NonNull
    @Override
    public ColorAdapter.ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.color_track_item, parent, false);
        ColorViewHolder ch = new ColorViewHolder(v);
        return  ch;
    }

    @Override
    public void onBindViewHolder(@NonNull ColorAdapter.ColorViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String limitText = "";
        if(position == 0)
            limitText = "0 - ";
        else
            limitText = itemList.get(position-1).limit + " - ";

        limitText = limitText + itemList.get(position).limit + " km/h";
        holder.textSpeed.setText(limitText);
        int color = Color.parseColor(itemList.get(position).color);
        holder.colorBtn.setBackgroundColor(color);

        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return  itemList.size();
    }

    public class ColorViewHolder extends RecyclerView.ViewHolder {
        public TextView textSpeed;
        public Button colorBtn;
        public int position;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            textSpeed = itemView.findViewById(R.id.speedText);
            colorBtn = itemView.findViewById(R.id.colorBtn);

            textSpeed.setOnClickListener(v -> {
                Dialog dialog = new Dialog(itemView.getContext());
                dialog.setContentView(R.layout.dialog_trackcolor);

                //Binding
                TextView minSpd = dialog.findViewById(R.id.min_track_dialog);
                EditText limitSpd = dialog.findViewById(R.id.num_track_dialog);
                Button btnCancel = dialog.findViewById(R.id.btn_track_dialog_cancel);
                Button btnSave = dialog. findViewById(R.id.btn_track_dialog_save);

                //Setting up
                //View text
                String limitTxt = "";
                if(position == 0)
                    limitTxt = "0 - ";
                else
                    limitTxt = itemList.get(position-1).limit + " - ";
                minSpd.setText(limitTxt);

                //Edit text
                limitSpd.setText(itemList.get(position).limit + "");

                //Cancel button
                btnCancel.setOnClickListener(v1 -> {
                    dialog.dismiss();
                });

                //Save button
                btnSave.setOnClickListener(v1 -> {
                    int newLimit = Integer.parseInt(limitSpd.getText().toString());

                    if(itemList.size() > 1 && position != itemList.size()-1)
                        if(itemList.get(position+1).limit < newLimit) {
                            Toast toast = new Toast(context);
                            toast.setText("Can't set limit higher than next track color");
                            toast.show();
                            dialog.dismiss();
                            return;
                        }

                    itemList.get(position).limit = newLimit;
                    notifyDataSetChanged();
                    dialog.dismiss();
                });

                dialog.show();
            });

            colorBtn.setOnClickListener(v -> {
                new ColorPickerDialog.Builder(itemView.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                        .setTitle("ColorPicker Dialog")
                        .setPreferenceName("MyColorPickerDialog")
                        .setPositiveButton("Confirm",
                                new ColorEnvelopeListener() {
                                    @Override
                                    public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                                        colorBtn.setBackgroundColor(envelope.getColor());

                                        String hexColor = String.format("#%06X", (0xFFFFFF & envelope.getColor()));

                                        itemList.get(position).color = hexColor;
                                    }
                                })
                        .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .attachAlphaSlideBar(false) // default is true. If false, do not show the AlphaSlideBar.
                        .attachBrightnessSlideBar(false)  // default is true. If false, do not show the BrightnessSlideBar.
                        .show();
            });
        }
    }

    public void clear() {
        itemList.clear();
        notifyDataSetChanged();
    }

    public void add(ColorData item) {
        itemList.add(item);

        sortList();

        notifyDataSetChanged();
    }

    public void sortList() {
        Collections.sort(itemList, new Comparator<ColorData>() {
            public int compare(ColorData o1, ColorData o2) {
                return o1.limit - o2.limit;
            }
        });
    }

}
