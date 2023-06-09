package com.example.trackmap.database;

import android.graphics.Color;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ColorData {
    @PrimaryKey(autoGenerate = true)
    public int idColor;

    @ColumnInfo(name = "limit")
    public int limit;

    @ColumnInfo(name = "color")
    public String color;

}
