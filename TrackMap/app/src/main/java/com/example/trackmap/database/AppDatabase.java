package com.example.trackmap.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TrackData.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TrackDao trackDao();
}