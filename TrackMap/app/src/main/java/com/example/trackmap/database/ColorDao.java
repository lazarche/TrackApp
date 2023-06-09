package com.example.trackmap.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ColorDao {
    @Query("SELECT * FROM colordata")
    List<ColorData> getAll();

    @Query("SELECT * FROM colordata WHERE idColor = :idd")
    ColorData findById(int idd);

    @Insert
    void insertColorData(ColorData colorData);

    @Delete
    void deleteColorData(ColorData data);

    @Query("DELETE FROM colordata")
    void deleteAll();
}
