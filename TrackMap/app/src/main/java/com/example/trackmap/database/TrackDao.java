package com.example.trackmap.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.trackmap.database.TrackData;

import java.util.List;

@Dao
public interface TrackDao {
    @Query("SELECT * FROM trackdata")
    List<TrackData> getAll();

    @Query("SELECT * FROM trackdata WHERE idTrack = :idd")
    TrackData findById(int idd);

    @Query("UPDATE trackdata SET name = :newName WHERE idTrack = :idd")
    void renameTrack(int idd, String newName);

    @Insert
    void insertTrackData(TrackData trackData);

    @Delete
    void deleteTrackData(TrackData data);

}