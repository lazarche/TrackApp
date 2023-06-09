package com.example.trackmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trackmap.adapter.ColorAdapter;
import com.example.trackmap.adapter.TrackAdapter;
import com.example.trackmap.database.AppDatabase;
import com.example.trackmap.database.ColorData;

import java.util.ArrayList;
import java.util.List;

public class TrackHighlightCustomization extends AppCompatActivity {

    RecyclerView recyclerView;
    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_highlight_customization);

        //Hides bar
        getSupportActionBar().hide();

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "TrackMap").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        List<ColorData> colors = db.colorDao().getAll();

        //Set up list
        recyclerView = (RecyclerView) findViewById(R.id.colorRecyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        ColorAdapter colorAdapter = new ColorAdapter(colors, getApplicationContext());
        recyclerView.setAdapter(colorAdapter);

        SetUpButtons();
    }

    public void SaveNewData() {
        db.colorDao().deleteAll();
        for (ColorData colorData: ((ColorAdapter) recyclerView.getAdapter()).itemList ) {
            db.colorDao().insertColorData(colorData);
        }
    }

    public void SetUpButtons() {
        ImageButton btnAdd = findViewById(R.id.btn_add_trackcolor);
        btnAdd.setOnClickListener(v -> {
            Dialog dialog = new Dialog(TrackHighlightCustomization.this);
            dialog.setContentView(R.layout.dialog_trackcolor);

            //Binding
            TextView minSpd = dialog.findViewById(R.id.min_track_dialog);
            EditText limitSpd = dialog.findViewById(R.id.num_track_dialog);
            Button btnCancel = dialog.findViewById(R.id.btn_track_dialog_cancel);
            Button btnSave = dialog. findViewById(R.id.btn_track_dialog_save);

            //Setting up
            //View text
            minSpd.setText("Limit: ");

            //Edit text
            limitSpd.setText(0 + "");

            //Cancel button
            btnCancel.setOnClickListener(v1 -> {
                dialog.dismiss();
            });

            //Save button
            btnSave.setOnClickListener(v1 -> {
                int newLimit = Integer.parseInt(limitSpd.getText().toString());

                ColorData colorData = new ColorData();
                colorData.color = "#12c742";
                colorData.limit = newLimit;

                ((ColorAdapter) recyclerView.getAdapter()).add(colorData);
                dialog.dismiss();
            });

            dialog.show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((ColorAdapter) recyclerView.getAdapter()).sortList();
        SaveNewData();
        db.close();
    }
}