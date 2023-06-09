package com.example.trackmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.trackmap.adapter.TrackAdapter;
import com.example.trackmap.database.AppDatabase;
import com.example.trackmap.database.TrackDao;
import com.example.trackmap.database.TrackData;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int GPS_PERMISSION_CODE = 60;
    private static final int INTERNET_PERMISSION_CODE = 70;
    private static final int GPS_FINE_PERMISSION_CODE = 80;

    RecyclerView recyclerView;

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hides bar
        getSupportActionBar().hide();

        SetUpDatabase();
        SetUpButtons();
        CheckPermissions();
        PopulateView();
    }

    private void SetUpDatabase() {
       db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "TrackMap").allowMainThreadQueries().fallbackToDestructiveMigration().build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PopulateView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void SetUpButtons() {
        //Add new track btn
        ImageButton btn = findViewById(R.id.btnMap);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose name for track");

                // Set up the input
                final EditText input = new EditText(builder.getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();

                        if(name == null)
                            name = "DEFAULT NAME";

                        Intent intent = new Intent(MainActivity.this, TrackRecord.class);
                        intent.putExtra("name", name);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        ImageButton trackH = findViewById(R.id.trackHighlightBtn);
        trackH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TrackHighlightCustomization.class);
                startActivity(intent);
            }
        });
    }

    void PopulateView() {
        TrackDao trackDao = db.trackDao();
        List<TrackData> tracks = trackDao.getAll();

        //Set up list
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        TrackAdapter trackAdapter = new TrackAdapter(tracks, getApplicationContext());
        recyclerView.setAdapter(trackAdapter);
        db.close();
    }

    //Permissions
    private void CheckPermissions() {
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, GPS_FINE_PERMISSION_CODE);
        checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, GPS_PERMISSION_CODE);
        checkPermission(Manifest.permission.INTERNET, INTERNET_PERMISSION_CODE);
    }

    public void checkPermission(String permission, int requestCode) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == GPS_PERMISSION_CODE || requestCode == GPS_FINE_PERMISSION_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Toast.makeText(MainActivity.this, "GPS Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "GPS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == INTERNET_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "INTERNET Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "INTERNET Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}