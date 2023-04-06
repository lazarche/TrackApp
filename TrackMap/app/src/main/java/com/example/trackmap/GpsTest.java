package com.example.trackmap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


import com.example.trackmap.track.SpeedColor;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.maps.model.StrokeStyle;
import com.google.android.gms.maps.model.StyleSpan;
import com.google.android.gms.tasks.OnSuccessListener;


import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Random;

public class GpsTest extends AppCompatActivity implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    private static final int DEFAULT_UPDATE_INTERVAL = 2500;
    private static final int FAST_UPDATE_INTERVAL = 5;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private static final int DEFAULT_ZOOM = 17;

    //Google api
    FusedLocationProviderClient fusedLocationProviderClient;
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    Polyline track;
    Location lastLoc;

    //Camera
    boolean following = true;

    //UI
    ImageButton followButton;
    ImageButton recordButton;
    ImageButton cancelButton;

    //Recording
    boolean recording = false;

    List<SpeedColor> list;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);

        setContentView(R.layout.activity_gps_test);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        supportMapFragment.getMapAsync(this);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        list = new ArrayList<>();
        list.add(new SpeedColor(new StyleSpan(Color.GREEN), 25));
        list.add(new SpeedColor(new StyleSpan(Color.BLUE), 60));
        list.add(new SpeedColor(new StyleSpan(Color.YELLOW), 90));
        list.add(new SpeedColor(new StyleSpan(Color.RED), 999));
        SetUpUi();
    }

    /* /region */

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
        recordButton = (ImageButton) findViewById(R.id.btn_record);
        recordButton.setOnClickListener(v -> StartRecording());
        recordButton.setOnLongClickListener(v -> StopRecording());
        cancelButton = (ImageButton) findViewById(R.id.btn_cancel_record);
        cancelButton.setOnClickListener(v -> CancelRecording());
        cancelButton.setOnLongClickListener(v -> DiscardRecording());
    }

    private boolean DiscardRecording() {
        if(!recording) {
            Toast.makeText(GpsTest.this, "Recording must be in progress", Toast.LENGTH_SHORT).show();
            return false;
        }

        Toast.makeText(GpsTest.this, "Recording is discarded", Toast.LENGTH_SHORT).show();
        List<LatLng> empty = new ArrayList<LatLng>();
        List<StyleSpan> emptySpan = new ArrayList<StyleSpan>();
        track.setPoints(empty);
        track.setSpans(emptySpan);

        recording = false;
        recordButton.setImageResource(R.drawable.record_start);

        return true;
    }

    private boolean StopRecording() {
        Toast.makeText(GpsTest.this, "Recording is saved", Toast.LENGTH_SHORT).show();
        recording = false;
        recordButton.setImageResource(R.drawable.record_start);

        //Save recording
        return  true;
    }

    private void CancelRecording() {
        if(!recording) {
            Toast.makeText(GpsTest.this, "Recording must be in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(GpsTest.this, "Tap and hold to discard recording", Toast.LENGTH_SHORT).show();
    }

    private void StartRecording() {
        Log.i("RECORDING STATUS", recording + " ");
        if(!recording) {
            recordButton.setImageResource(R.drawable.record_stop);
            Log.i("RECORDING STATUS", "Snimanje krenulo");
            recording = true;
        } else {
            Log.i("KOJI KURAC", "KOJI KURAC");
            Toast.makeText(GpsTest.this, "Tap and hold stop button to stop", Toast.LENGTH_SHORT).show();
        }
    }


    protected void startLocationUpdates() {

        LocationRequest.Builder builder = new LocationRequest.Builder(DEFAULT_UPDATE_INTERVAL);
        builder.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        LocationRequest locationRequest = builder.build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //No gps no play
            Toast.makeText(GpsTest.this, "Relaunch app and give location permission", Toast.LENGTH_SHORT).show();
            finish();
        }

        LocationCallback fusedTrackerCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if(location != null) {
                    Log.i("LOCATION REQUEST", "Got location : " + location);
                    updateTrack(location);
                    lastLoc = location;
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, fusedTrackerCallback, Looper.getMainLooper());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMyLocationButtonEnabled(false);


        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLUE);
        polylineOptions.width(10);
        track = map.addPolyline(polylineOptions);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //No gps no play
            Toast.makeText(GpsTest.this, "Relaunch app and give location permission", Toast.LENGTH_SHORT).show();
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

    public void updateTrack(Location location) {
        //Location
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        List<LatLng> points = track.getPoints();
        points.add(point);
        track.setPoints(points);

        Random r = new Random();
        //Color
        StyleSpan ss = list.get(r.nextInt(list.size())).getStyle(); //SpeedColor.getMatchingColor(list, location.getSpeed()).getStyle();
        List<StyleSpan> ssPoints = track.getSpans();
        ssPoints.add(ss);
        track.setSpans(ssPoints);

        Log.d("SPEED", location.getSpeed() + "");

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
        else
            super.onBackPressed();
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
}