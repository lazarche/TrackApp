package com.example.trackmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;



import java.util.List;

public class GpsTest extends AppCompatActivity implements OnMapReadyCallback {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_test);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentMap);
        supportMapFragment.getMapAsync(this);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SetUpUi();
    }

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
        cancelButton = (ImageButton) findViewById(R.id.btn_cancel_record);
        cancelButton.setOnClickListener(v -> CancelRecording());
    }

    private void CancelRecording() {
        recording = false;
    }

    private void StartRecording() {
        recording = true;
    }

    protected void startLocationUpdates() {

        LocationRequest.Builder builder = new LocationRequest.Builder(DEFAULT_UPDATE_INTERVAL);
        builder.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        LocationRequest locationRequest = builder.build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
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
        polylineOptions.width(4);
        track = map.addPolyline(polylineOptions);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        List<LatLng> points = track.getPoints();
        points.add(point);
        track.setPoints(points);

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
}