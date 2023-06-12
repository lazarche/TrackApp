package com.example.trackmap;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.trackmap.database.AppDatabase;
import com.example.trackmap.database.ColorData;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
    final float KMHTOMS = 0.2777778f;

    boolean loaded = false;
    Track track;

    private GoogleMap mMap;
    Polyline polyline;
    private ActivityTrackViewBinding binding;

    Marker clickedMarker = null;

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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng clickCoords) {
                List<LatLng> points =  polyline.getPoints();

                LatLng closest = points.get(0);
                float dist = 100000000000000f;
                int index = 0;
                for (int i = 0; i < points.size() ; i++) {
                    float trenDist = distance((float) points.get(i).latitude, (float) points.get(i).longitude, (float) clickCoords.latitude, (float) clickCoords.longitude);
                    if(trenDist < dist) {
                        dist = trenDist;
                        closest = points.get(i);
                        index = i;
                    }
                }

                if(clickedMarker != null)
                    clickedMarker.remove();


                if(dist < 30) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.racing_flag));
                    markerOptions.position(closest);
                    markerOptions.title("Speed: " + Math.round(track.getTrack().get(index).speed / 0.2777777778f) + " Km/h");

                    clickedMarker = mMap.addMarker(markerOptions);
                    clickedMarker.showInfoWindow();
                }


                Log.e("TAG", "Found @ " + clickCoords.latitude + " " + clickCoords.longitude);
            }
        });

        ShowTrack();
    }

    public float distance (float lat_a, float lng_a, float lat_b, float lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return new Float(distance * meterConversion).floatValue();
    }

    private ArrayList LoadSpeedList() {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "TrackMap").allowMainThreadQueries().fallbackToDestructiveMigration().build();
        List<ColorData> colors = db.colorDao().getAll();
        db.close();


        ArrayList<SpeedColor> list = new ArrayList<SpeedColor>();
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

//        for (int i = 0; i < list.size(); i++)
//            Log.i("COLOR TRACK", i + " " + list.)
        return list;
    }

    public void ShowTrack() {
        //To change
        List<SpeedColor> list = LoadSpeedList();

        //Create Polyline
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.width(10);
        polylineOptions.clickable(true);
        polyline = mMap.addPolyline(polylineOptions);


        //Load path and colors
        List<LatLng> latLngList = new LinkedList<>();
        List<StyleSpan> styleList = new LinkedList<>();

        List<TrackSegment> listSegment = track.getTrack();

        int count50 = 0;
        for (int i = 0; i < listSegment.size(); i++) {
            latLngList.add(listSegment.get(i).position);
            styleList.add(SpeedColor.getMatchingColor(list, listSegment.get(i).speed).getStyle());

        }
        Log.i("SPEEDTEST", count50 + " ");

        polyline.setPoints(latLngList);
        polyline.setSpans(styleList);

        polyline.setClickable(false);

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