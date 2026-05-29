package com.dongmedicine.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dongmedicine.data.model.KnowledgeItem;

import java.util.List;

@Dao
public interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_items")
    LiveData<List<KnowledgeItem>> getAllKnowledgeItems();

    @Query("SELECT * FROM knowledge_items WHERE id = :id")
    LiveData<KnowledgeItem> getKnowledgeById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<KnowledgeItem> items);

    @Query("DELETE FROM knowledge_items")
    void deleteAll();
}
