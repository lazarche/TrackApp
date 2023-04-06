package com.example.trackmap.track;

import com.google.android.gms.maps.model.LatLng;

public class TrackSegment {
    public LatLng position;
    public float speed;
    public float time;

    public TrackSegment(LatLng position, float speed, float time) {
        this.position = position;
        this.speed = speed;
        this.time = time;
    }
}
