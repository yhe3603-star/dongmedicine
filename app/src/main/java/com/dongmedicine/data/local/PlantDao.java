package com.dongmedicine.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dongmedicine.data.model.Plant;

import java.util.List;

@Dao
public interface PlantDao {
    @Query("SELECT * FROM plants")
    LiveData<List<Plant>> getAllPlants();

    @Query("SELECT * FROM plants WHERE id = :id")
    LiveData<Plant> getPlantById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Plant> plants);

    @Query("DELETE FROM plants")
    void deleteAll();
}
