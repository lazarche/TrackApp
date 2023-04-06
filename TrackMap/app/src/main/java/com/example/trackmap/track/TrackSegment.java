package com.example.trackmap.track;

import com.google.android.gms.maps.model.LatLng;

public class TrackSegment {
    public LatLng position;
    public float speed;

    public TrackSegment(LatLng position, float speed) {
        this.position = position;
        this.speed = speed;
    }
}
