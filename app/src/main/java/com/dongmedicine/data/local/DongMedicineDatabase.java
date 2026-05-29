package com.dongmedicine.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.model.Plant;

@Database(entities = {Plant.class, Inheritor.class, KnowledgeItem.class}, version = 1, exportSchema = true)
public abstract class DongMedicineDatabase extends RoomDatabase {
    public abstract PlantDao plantDao();
    public abstract InheritorDao inheritorDao();
    public abstract KnowledgeDao knowledgeDao();
}
