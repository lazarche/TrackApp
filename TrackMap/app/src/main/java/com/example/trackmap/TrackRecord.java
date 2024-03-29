package com.example.trackmap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.example.trackmap.database.AppDatabase;
import com.example.trackmap.database.ColorData;
import com.example.trackmap.database.TrackDao;
import com.example.trackmap.database.TrackData;
import com.example.trackmap.track.SpeedColor;
import com.example.trackmap.track.Track;
import com.example.trackmap.track.TrackSegment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StyleSpan;
import com.google.android.gms.tasks.OnSuccessListener;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TrackRecord extends AppCompatActivity implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    final float KMHTOMS = 0.2777778f;

    private static final int DEFAULT_UPDATE_INTERVAL = 800;
    private static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private static final int DEFAULT_ZOOM = 17;

    //Google api
    FusedLocationProviderClient fusedLocationProviderClient;
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    Polyline track;
    Location lastLoc;
    LocationCallback fusedTrackerCallback;
    boolean mapReady = false;

    //Camera
    boolean following = true;

    //UI
    ImageButton followButton;
    ImageButton recordButton;
    ImageButton cancelButton;
    LinearLayout buttonLayout;
    FrameLayout parentLayout;

    //Recording
    boolean recording = false;
    Instant start;
    String name;

    //Trackdata
    List<SpeedColor> list;
    Track trackData;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);
        setContentView(R.layout.activity_track_record);

        //Hides bar
        getSupportActionBar().hide();

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        supportMapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        LoadSpeedList();

        //Get extas
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            name = extras.getString("name");
        }

        SetUpUi();
    }

    private void LoadSpeedList() {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "TrackMap").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        List<ColorData> colors = db.colorDao().getAll();
        db.close();


        list = new ArrayList<SpeedColor>();
        for (ColorData colorData : colors) {
            float floatLimit = (float)colorData.limit;
            list.add(new SpeedColor(new StyleSpan(Color.parseColor(colorData.color)),floatLimit * KMHTOMS));
        }

        if(list.size() == 0)
            list.add(new SpeedColor(new StyleSpan(Color.GREEN),200));

        Collections.sort(list, new Comparator<SpeedColor>() {
            @Override
            public int compare(SpeedColor o1, SpeedColor o2) {
                return -Float.compare(o1.getSpeedLimit(), o2.getSpeedLimit());
            }
        });

        Log.i("SPEEDCOLOR LIST", "List loaded: " + list.size());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void SetUpUi() {
        followButton = (ImageButton) findViewById(R.id.btn_follow);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                following = !following;
                if(following) {
                    followButton.setImageResource(R.drawable.follow_user);
                    if(lastLoc != null)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude()), DEFAULT_ZOOM));
                } else {
                    followButton.setImageResource(R.drawable.no_follow_user);
                    map.stopAnimation();
                }
            }
        });

        parentLayout = findViewById(R.id.fragmentMap);
        buttonLayout = findViewById(R.id.llButtonHolder);
        recordButton = (ImageButton) findViewById(R.id.btn_record);
        recordButton.setOnClickListener(v -> StartRecording());
        recordButton.setOnLongClickListener(v -> StopRecording());
        cancelButton = (ImageButton) findViewById(R.id.btn_cancel_record);
        cancelButton.setOnClickListener(v -> CancelRecording());
        cancelButton.setOnLongClickListener(v -> DiscardRecording());
    }

    private void RestartPolyline() {
        if(!mapReady)
            return;

        if(track != null)
            track.remove();

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(10);
        track = map.addPolyline(polylineOptions);
    }

    private boolean DiscardRecording() {
        if(!recording) {
            Toast.makeText(TrackRecord.this, "Recording must be in progress", Toast.LENGTH_SHORT).show();
            return false;
        }

        Toast.makeText(TrackRecord.this, "Recording is discarded", Toast.LENGTH_SHORT).show();

        //Reset track
        RestartPolyline();

        recording = false;
        recordButton.setImageResource(R.drawable.record_start);

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean StopRecording() {
        if(!recording)
            return false;

        Toast.makeText(TrackRecord.this, "Recording is saved", Toast.LENGTH_SHORT).show();
        recording = false;
        recordButton.setImageResource(R.drawable.record_start);

        float timeElapsed =  Duration.between(start, Instant.now()).toMillis();

        TrackData data = new TrackData();
        data.name = trackData.name;
        data.date = trackData.date;
        data.time = timeElapsed;
        data.data = Track.TrackDataToString(trackData);

        //Save
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "TrackMap").allowMainThreadQueries().build();
        TrackDao trackDao = db.trackDao();
        trackDao.insertTrackData(data);
        db.close();

        Log.i("RECORDING STATUS", "Recording is saved");
        return  true;
    }

    private void CancelRecording() {
        if(!recording) {
            Toast.makeText(TrackRecord.this, "Recording must be in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(TrackRecord.this, "Tap and hold to discard recording", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void StartRecording() {
        Log.i("RECORDING STATUS", recording + " ");
        if(!recording) {
            recordButton.setImageResource(R.drawable.record_stop);
            Log.i("RECORDING STATUS", "Snimanje krenulo");
            RestartPolyline();
            recording = true;
            start = Instant.now();

            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            String strDate = dateFormat.format(date);

            trackData = new Track(name, strDate);
        } else {
            Toast.makeText(TrackRecord.this, "Tap and hold stop button to stop", Toast.LENGTH_SHORT).show();
        }
    }

    protected void startLocationUpdates() {

        LocationRequest.Builder builder = new LocationRequest.Builder(0);
        builder.setIntervalMillis(0);
        builder.setMinUpdateIntervalMillis(0);
        builder.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        LocationRequest locationRequest = builder.build();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //No gps no play
            Toast.makeText(TrackRecord.this, "Relaunch app and give location permission", Toast.LENGTH_SHORT).show();
            finish();
        }

         fusedTrackerCallback = new LocationCallback() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if(location != null) {
                    Log.i("LOCATION REQUEST", "Got location : " + location);
                    updateTrack(location);
                    lastLoc = location;
                }
            }

             @Override
             public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                 super.onLocationAvailability(locationAvailability);
                 Log.i("LOCATION AVAILABLE", "Got location");
             }
         };
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, fusedTrackerCallback, Looper.getMainLooper());


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);

        mapReady = true;

        RestartPolyline();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //No gps no play
            Toast.makeText(TrackRecord.this, "Relaunch app and give location permission", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        map.setMyLocationEnabled(true);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(loc, DEFAULT_ZOOM);
                map.moveCamera(cu);
                lastLoc = location;
            }
        });

        startLocationUpdates();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void updateTrack(Location location) {
        if(!recording)
            return;

        //Location
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        List<LatLng> points = track.getPoints();
        points.add(point);
        track.setPoints(points);

        //Color
        StyleSpan ss = SpeedColor.getMatchingColor(list, location.getSpeed()).getStyle();
        List<StyleSpan> ssPoints = track.getSpans();
        ssPoints.add(ss);
        track.setSpans(ssPoints);

        //Time
        float timeElapsed =  Duration.between(start, Instant.now()).toMillis();

        //Trackdata
        trackData.AddSegment(new TrackSegment(point, location.getSpeed(), timeElapsed));

        //Move Camera
        if(following) {
            CameraUpdate cu = CameraUpdateFactory.newLatLng(point);
            map.animateCamera(cu);
        }
    }

    @Override
    public void onBackPressed() {
        if(recording)
            Toast.makeText(getApplicationContext(), "Stop the recording first!", Toast.LENGTH_SHORT).show();
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapsSdkInitialized(@NonNull MapsInitializer.Renderer renderer) {
        switch (renderer) {
            case LATEST:
                Log.d("MapsDemo", "The latest version of the renderer is used.");
                break;
            case LEGACY:
                Log.d("MapsDemo", "The legacy version of the renderer is used.");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(fusedTrackerCallback);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i("AAAAAAA", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;

        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
            FrameLayout.LayoutParams newLayout = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) (105 * scale + 0.5f));
            newLayout.gravity = Gravity.BOTTOM;
            buttonLayout.setLayoutParams(newLayout);
        } else {
            buttonLayout.setOrientation(LinearLayout.VERTICAL);
            buttonLayout.setLayoutParams(new FrameLayout.LayoutParams((int) (105 * scale + 0.5f), FrameLayout.LayoutParams.MATCH_PARENT));
        }
        buttonLayout.requestLayout();
        buttonLayout.forceLayout();
        parentLayout.forceLayout();
        parentLayout.requestLayout();
        parentLayout.invalidate();
    }
}