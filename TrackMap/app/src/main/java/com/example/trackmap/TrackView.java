package com.example.trackmap;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.trackmap.database.AppDatabase;
import com.example.trackmap.database.TrackDao;
import com.example.trackmap.database.TrackData;
import com.example.trackmap.track.SpeedColor;
import com.example.trackmap.track.Track;
import com.example.trackmap.track.TrackSegment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnMapsSdkInitializedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.trackmap.databinding.ActivityTrackViewBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StyleSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class TrackView extends FragmentActivity implements OnMapReadyCallback, OnMapsSdkInitializedCallback {

    boolean loaded = false;
    Track track;

    private GoogleMap mMap;
    private ActivityTrackViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, this);

        binding = ActivityTrackViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        //Get data
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            int id = extras.getInt("idd");
            AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "TrackMap").allowMainThreadQueries().fallbackToDestructiveMigration().build();
            TrackDao trackDao = db.trackDao();
            TrackData trackData = trackDao.findById(id);
            db.close();

            if(trackData == null) {
                Toast.makeText(getApplicationContext(), "TrackData is NULL" ,Toast.LENGTH_SHORT).show();
                finish();
            }

            track = new Track(trackData.name, trackData.data, Track.StringToTrackData(trackData.data), trackData.time);
            Log.i("LOADED TRACK", "TRACK IS LOADED");
            loaded = true;
        } else {
            Toast.makeText(getApplicationContext(), "ERROR NO ID" ,Toast.LENGTH_SHORT).show();
            finish();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        ShowTrack();
    }

    public void ShowTrack() {
        //To change
        List<SpeedColor> list = new ArrayList<>();
        list.add(new SpeedColor(new StyleSpan(Color.GREEN), 25));
        list.add(new SpeedColor(new StyleSpan(Color.BLUE), 60));
        list.add(new SpeedColor(new StyleSpan(Color.YELLOW), 90));
        list.add(new SpeedColor(new StyleSpan(Color.RED), 999));

        Collections.sort(list, new Comparator<SpeedColor>() {
                    @Override
                    public int compare(SpeedColor o1, SpeedColor o2) {
                        return -Float.compare(o1.getSpeedLimit(), o2.getSpeedLimit());
                    }
                });

        //Create Polyline
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(10);
        polylineOptions.clickable(true);
        Polyline polyline = mMap.addPolyline(polylineOptions);


        //Load path and colors
        List<LatLng> latLngList = new LinkedList<>();
        List<StyleSpan> styleList = new LinkedList<>();

        List<TrackSegment> listSegment = track.getTrack();
        for (int i = 0; i < listSegment.size(); i++) {
            latLngList.add(listSegment.get(i).position);
            styleList.add(SpeedColor.getMatchingColor(list, listSegment.get(i).speed).getStyle());
        }

        polyline.setPoints(latLngList);
        polyline.setSpans(styleList);

        //Add markers
        mMap.addMarker(new MarkerOptions().position(latLngList.get(0)).title("Start"));
        mMap.addMarker(new MarkerOptions().position(latLngList.get(latLngList.size()-1)).title("End"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(0), 17));

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