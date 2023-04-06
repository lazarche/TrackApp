package com.example.trackmap.track;


import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

public class Track {
    public String name;
    public String date;
    public float time;

    List<TrackSegment> track;

    public Track(String name, String date) {
        this.name = name;
        this.date = date;
        track = new LinkedList<>();
    }

    public Track(String name, String date, List<TrackSegment> track, float time) {
        this.name = name;
        this.date = date;
        this.track = track;
        this.time = time;
    }

    public List<TrackSegment> getTrack() {
        return track;
    }
    public void AddSegment(TrackSegment segment) {
        track.add(segment);
    }

    public static String TrackDataToString(Track data) {
        if(data == null)
            return  "";

        StringBuilder sb = new StringBuilder();
        List<TrackSegment> trackSegmentList = data.getTrack();
        DecimalFormat df = new DecimalFormat("0.00");

        for (int i = 0; i < trackSegmentList.size(); i++) {
            LatLng pos = trackSegmentList.get(i).position;
            float spd = trackSegmentList.get(i).speed;
            float time = trackSegmentList.get(i).time;
            String temp = "-" + pos.latitude + "," + pos.longitude + "," + df.format(spd) + "," + df.format(time);
            sb.append(temp);
        }

        return  sb.toString();
    }

    public static List<TrackSegment> StringToTrackData(String data) {
        if(data == null)
            return  null;

        List<TrackSegment> list = new LinkedList<>();
        String[] splitedPerSegments = data.split("-");

        for (int i = 1; i < splitedPerSegments.length; i++) {
            String[] splitedSegment = splitedPerSegments[i].split(",");
            double lat = Double.parseDouble(splitedSegment[0]);
            double lon = Double.parseDouble(splitedSegment[1]);
            float speed = Float.parseFloat(splitedSegment[2]);
            float time = Float.parseFloat(splitedSegment[3]);

            list.add(new TrackSegment(new LatLng(lat,lon), speed, time));
        }

        return list;
    }

 }
