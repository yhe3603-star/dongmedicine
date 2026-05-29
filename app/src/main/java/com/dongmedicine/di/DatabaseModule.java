package com.dongmedicine.di;

import android.content.Context;

import androidx.room.Room;

import com.dongmedicine.data.local.DongMedicineDatabase;
import com.dongmedicine.data.local.InheritorDao;
import com.dongmedicine.data.local.KnowledgeDao;
import com.dongmedicine.data.local.PlantDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    DongMedicineDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                DongMedicineDatabase.class,
                "dongmedicine_db"
        ).build();
    }

    @Provides
    PlantDao providePlantDao(DongMedicineDatabase database) {
        return database.plantDao();
    }

    @Provides
    InheritorDao provideInheritorDao(DongMedicineDatabase database) {
        return database.inheritorDao();
    }

    @Provides
    KnowledgeDao provideKnowledgeDao(DongMedicineDatabase database) {
        return database.knowledgeDao();
    }
}
