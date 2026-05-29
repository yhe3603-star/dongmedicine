package com.dongmedicine.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dongmedicine.data.model.Inheritor;

import java.util.List;

@Dao
public interface InheritorDao {
    @Query("SELECT * FROM inheritors")
    LiveData<List<Inheritor>> getAllInheritors();

    @Query("SELECT * FROM inheritors WHERE id = :id")
    LiveData<Inheritor> getInheritorById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Inheritor> inheritors);

    @Query("DELETE FROM inheritors")
    void deleteAll();
}
