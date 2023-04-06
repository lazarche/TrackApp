package com.example.trackmap.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TrackData {
    @PrimaryKey(autoGenerate = true)
    public int idTrack;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "data")
    public String data;
}
