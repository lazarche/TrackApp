package com.example.trackmap.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TrackData.class, ColorData.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TrackDao trackDao();
    public abstract ColorDao colorDao();
}
