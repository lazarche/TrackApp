package com.example.trackmap.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.trackmap.database.TrackData;

import java.util.List;

@Dao
public interface TrackDao {
    @Query("SELECT * FROM trackdata")
    List<TrackData> getAll();

    @Query("SELECT * FROM trackdata WHERE idTrack = :idd")
    List<TrackData> findById(int idd);

    @Insert
    void insertTrackData(TrackData trackData);

    @Delete
    void deleteTrackData(TrackData data);
}