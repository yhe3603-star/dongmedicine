# Phase 2: Hilt DI + Room Offline Cache — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace manual singletons with Hilt dependency injection, add Room database for offline caching, and change Repository to network-first-with-cache strategy.

**Architecture:** Hilt provides Retrofit/OkHttp/ApiService/DAO/Database/Repository as singletons via `@Module` classes. Room entities reuse the existing model classes with `@Entity` annotations. Repository returns `LiveData<Resource<T>>` backed by Room DAO (auto-updates when cache changes), with async network calls that write to Room on success.

**Tech Stack:** Java 11, Hilt 2.51.1, Room 2.6.1, LiveData, Navigation Hilt support

**Prerequisites:** Phase 1 complete (ViewBinding, Safe Args, Repository refactored)

---

### Task 1: Add Hilt dependencies and plugin

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts` (root)
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add Hilt to version catalog**

Edit `gradle/libs.versions.toml`. Add to `[versions]`:
```toml
hilt = "2.51.1"
hiltNavigationFragment = "1.2.0"
```

Add to `[libraries]`:
```toml
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-fragment = { group = "androidx.hilt", name = "hilt-navigation-fragment", version.ref = "hiltNavigationFragment" }
```

Add to `[plugins]`:
```toml
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

- [ ] **Step 2: Apply Hilt plugin in root build.gradle.kts**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.safeargs) apply false
    alias(libs.plugins.hilt) apply false
}
```

- [ ] **Step 3: Apply Hilt plugin and add dependencies in app/build.gradle.kts**

Add to plugins block:
```kotlin
alias(libs.plugins.hilt)
```

Add to dependencies:
```kotlin
implementation(libs.hilt.android)
annotationProcessor(libs.hilt.compiler)
implementation(libs.hilt.navigation.fragment)
```

- [ ] **Step 4: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (Hilt plugin applied, no `@HiltAndroidApp` yet — that's OK, build succeeds as long as no Hilt annotations are used)

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts app/build.gradle.kts
git commit -m "build: add Hilt dependency injection framework"
```

---

### Task 2: Create Application class + annotate components

**Files:**
- Create: `app/src/main/java/com/dongmedicine/DongmedicineApplication.java`
- Modify: `app/src/main/java/com/dongmedicine/MainActivity.java`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Create DongmedicineApplication.java**

```java
package com.dongmedicine;

import android.app.Application;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class DongmedicineApplication extends Application {
}
```

- [ ] **Step 2: Register Application in AndroidManifest.xml**

Add `android:name=".DongmedicineApplication"` to the `<application>` tag:
```xml
<application
    android:name=".DongmedicineApplication"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    ...
```

- [ ] **Step 3: Annotate MainActivity**

```java
package com.dongmedicine;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
```

- [ ] **Step 4: Annotate all Fragments with @AndroidEntryPoint**

Add `import dagger.hilt.android.AndroidEntryPoint;` and `@AndroidEntryPoint` to:
- `HomeFragment.java`
- `PlantsFragment.java`
- `PlantDetailFragment.java`
- `InheritorsFragment.java`
- `InheritorDetailFragment.java`
- `KnowledgeFragment.java`
- `KnowledgeDetailFragment.java`
- `QaFragment.java`

For each file, add before the class declaration:
```java
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class XxxFragment extends Fragment {
```

- [ ] **Step 5: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/ app/src/main/AndroidManifest.xml
git commit -m "feat: add @HiltAndroidApp Application, @AndroidEntryPoint to Activity and Fragments"
```

---

### Task 3: Create Hilt DI modules

**Files:**
- Create: `app/src/main/java/com/dongmedicine/di/NetworkModule.java`

- [ ] **Step 1: Create NetworkModule.java**

```java
package com.dongmedicine.di;

import com.dongmedicine.BuildConfig;
import com.dongmedicine.data.api.ApiService;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String BASE_URL = "http://10.0.2.2:8080/";

    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);

        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    Retrofit provideRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }
}
```

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/dongmedicine/di/
git commit -m "feat: add Hilt NetworkModule for Retrofit/OkHttp/ApiService"
```

---

### Task 4: Add Room entities, DAO, and Database

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/data/model/Plant.java`
- Modify: `app/src/main/java/com/dongmedicine/data/model/Inheritor.java`
- Modify: `app/src/main/java/com/dongmedicine/data/model/KnowledgeItem.java`
- Create: `app/src/main/java/com/dongmedicine/data/local/PlantDao.java`
- Create: `app/src/main/java/com/dongmedicine/data/local/InheritorDao.java`
- Create: `app/src/main/java/com/dongmedicine/data/local/KnowledgeDao.java`
- Create: `app/src/main/java/com/dongmedicine/data/local/DongMedicineDatabase.java`

- [ ] **Step 1: Add Room annotations to Plant.java**

```java
package com.dongmedicine.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plants")
public class Plant {
    @PrimaryKey
    private int id;
    private String name;
    private String scientificName;
    private String description;
    private String imageUrl;
    private String effects;
    private String distribution;
    private String category;
    private String nameDong;

    public Plant() {}

    public Plant(int id, String name, String scientificName, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.scientificName = scientificName;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // All getters and setters remain unchanged
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getEffects() { return effects; }
    public void setEffects(String effects) { this.effects = effects; }
    public String getDistribution() { return distribution; }
    public void setDistribution(String distribution) { this.distribution = distribution; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getNameDong() { return nameDong; }
    public void setNameDong(String nameDong) { this.nameDong = nameDong; }
}
```

- [ ] **Step 2: Add Room annotations to Inheritor.java**

```java
package com.dongmedicine.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inheritors")
public class Inheritor {
    @PrimaryKey
    private int id;
    private String name;
    private String title;
    private String specialization;
    private String introduction;
    private String imageUrl;

    public Inheritor() {}

    public Inheritor(int id, String name, String title, String specialization, String introduction, String imageUrl) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.specialization = specialization;
        this.introduction = introduction;
        this.imageUrl = imageUrl;
    }

    // All getters and setters remain unchanged
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
```

- [ ] **Step 3: Add Room annotations to KnowledgeItem.java**

```java
package com.dongmedicine.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "knowledge_items")
public class KnowledgeItem {
    @PrimaryKey
    private int id;
    private String title;
    private String content;
    private String category;
    private String publishDate;
    private String author;

    public KnowledgeItem() {}

    public KnowledgeItem(int id, String title, String content, String category, String publishDate, String author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.publishDate = publishDate;
        this.author = author;
    }

    // All getters and setters remain unchanged
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
```

- [ ] **Step 4: Create PlantDao.java**

```java
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

    @Query("SELECT * FROM plants WHERE category = :category")
    LiveData<List<Plant>> getPlantsByCategory(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Plant> plants);

    @Query("DELETE FROM plants")
    void deleteAll();
}
```

- [ ] **Step 5: Create InheritorDao.java**

```java
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
```

- [ ] **Step 6: Create KnowledgeDao.java**

```java
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

    @Query("SELECT * FROM knowledge_items WHERE category = :category")
    LiveData<List<KnowledgeItem>> getKnowledgeByCategory(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<KnowledgeItem> items);

    @Query("DELETE FROM knowledge_items")
    void deleteAll();
}
```

- [ ] **Step 7: Create DongMedicineDatabase.java**

```java
package com.dongmedicine.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.model.Plant;

@Database(entities = {Plant.class, Inheritor.class, KnowledgeItem.class}, version = 1, exportSchema = false)
public abstract class DongMedicineDatabase extends RoomDatabase {
    public abstract PlantDao plantDao();
    public abstract InheritorDao inheritorDao();
    public abstract KnowledgeDao knowledgeDao();
}
```

- [ ] **Step 8: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: Commit**

```bash
git add app/src/main/java/com/dongmedicine/data/
git commit -m "feat: add Room entities, DAOs, and database for offline caching"
```

---

### Task 5: Create DatabaseModule for Hilt

**Files:**
- Create: `app/src/main/java/com/dongmedicine/di/DatabaseModule.java`

- [ ] **Step 1: Create DatabaseModule.java**

```java
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
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/com/dongmedicine/di/DatabaseModule.java
git commit -m "feat: add Hilt DatabaseModule for Room database and DAOs"
```

---

### Task 6: Convert Repository to Hilt @Singleton with Room cache

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/data/repository/DongMedicineRepository.java`

- [ ] **Step 1: Rewrite DongMedicineRepository.java**

```java
package com.dongmedicine.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.api.ApiService;
import com.dongmedicine.data.local.InheritorDao;
import com.dongmedicine.data.local.KnowledgeDao;
import com.dongmedicine.data.local.PlantDao;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.model.Plant;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class DongMedicineRepository {

    private final ApiService apiService;
    private final PlantDao plantDao;
    private final InheritorDao inheritorDao;
    private final KnowledgeDao knowledgeDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public DongMedicineRepository(ApiService apiService,
                                   PlantDao plantDao,
                                   InheritorDao inheritorDao,
                                   KnowledgeDao knowledgeDao) {
        this.apiService = apiService;
        this.plantDao = plantDao;
        this.inheritorDao = inheritorDao;
        this.knowledgeDao = knowledgeDao;
    }

    // --- Plants ---

    public LiveData<Resource<List<Plant>>> getPlants() {
        MutableLiveData<Resource<List<Plant>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        // Fetch from network and update cache
        apiService.getPlants().enqueue(new Callback<ApiService.ApiResponse<List<Plant>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<Plant>>> call,
                                   Response<ApiService.ApiResponse<List<Plant>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Plant> data = response.body().getData();
                    executor.execute(() -> plantDao.insertAll(data));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<Plant>>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Plant>> getPlantById(int id) {
        MutableLiveData<Resource<Plant>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getPlantById(id).enqueue(new Callback<ApiService.ApiResponse<Plant>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Plant>> call,
                                   Response<ApiService.ApiResponse<Plant>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Plant data = response.body().getData();
                    executor.execute(() -> plantDao.insertAll(java.util.Collections.singletonList(data)));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Plant>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }

    // --- Inheritors ---

    public LiveData<Resource<List<Inheritor>>> getInheritors() {
        MutableLiveData<Resource<List<Inheritor>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getInheritors().enqueue(new Callback<ApiService.ApiResponse<List<Inheritor>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<Inheritor>>> call,
                                   Response<ApiService.ApiResponse<List<Inheritor>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Inheritor> data = response.body().getData();
                    executor.execute(() -> inheritorDao.insertAll(data));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<Inheritor>>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Inheritor>> getInheritorById(int id) {
        MutableLiveData<Resource<Inheritor>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getInheritorById(id).enqueue(new Callback<ApiService.ApiResponse<Inheritor>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Inheritor>> call,
                                   Response<ApiService.ApiResponse<Inheritor>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Inheritor data = response.body().getData();
                    executor.execute(() -> inheritorDao.insertAll(java.util.Collections.singletonList(data)));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Inheritor>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }

    // --- Knowledge ---

    public LiveData<Resource<List<KnowledgeItem>>> getKnowledgeList() {
        MutableLiveData<Resource<List<KnowledgeItem>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getKnowledgeList().enqueue(new Callback<ApiService.ApiResponse<List<KnowledgeItem>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<KnowledgeItem>>> call,
                                   Response<ApiService.ApiResponse<List<KnowledgeItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<KnowledgeItem> data = response.body().getData();
                    executor.execute(() -> knowledgeDao.insertAll(data));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<KnowledgeItem>>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<KnowledgeItem>> getKnowledgeById(int id) {
        MutableLiveData<Resource<KnowledgeItem>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getKnowledgeById(id).enqueue(new Callback<ApiService.ApiResponse<KnowledgeItem>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<KnowledgeItem>> call,
                                   Response<ApiService.ApiResponse<KnowledgeItem>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    KnowledgeItem data = response.body().getData();
                    executor.execute(() -> knowledgeDao.insertAll(java.util.Collections.singletonList(data)));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<KnowledgeItem>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }
}
```

Key changes:
- `@Singleton` + `@Inject constructor` (no more `getInstance()`)
- DAO dependencies injected
- Network results written to Room cache via `executor.execute()`
- Returns `LiveData<Resource<T>>` (same signature as Phase 1 — ViewModels need no changes)

- [ ] **Step 2: Remove getInstance() calls from all ViewModels**

Since Repository is now injected by Hilt, ViewModels need `@HiltViewModel` + `@Inject constructor`. Update:

**PlantsViewModel.java:**
```java
package com.dongmedicine.ui.plants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PlantsViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<List<Plant>>> plants = new MutableLiveData<>();
    private final MutableLiveData<List<Plant>> filteredPlants = new MutableLiveData<>();
    private String searchQuery = "";
    private String selectedCategory;

    @Inject
    public PlantsViewModel(DongMedicineRepository repository) {
        this.repository = repository;
        selectedCategory = "全部";
        loadPlants();
    }

    // All other methods remain unchanged from Phase 1
    public LiveData<Resource<List<Plant>>> getPlants() { return plants; }
    public LiveData<List<Plant>> getFilteredPlants() { return filteredPlants; }
    public void loadPlants() { repository.getPlants(plants); }  // WAIT — this won't work anymore
    // ...
}
```

**IMPORTANT: Repository API transition.** Phase 1's Repository methods accepted `MutableLiveData` as parameter and returned void. Phase 2 changes the API to return `LiveData<Resource<T>>` (standard Android pattern). This means:

1. Repository methods change from `void getPlants(MutableLiveData<...> liveData)` to `LiveData<Resource<List<Plant>>> getPlants()`
2. ViewModels must adapt: instead of passing their LiveData to the Repository, they receive a new LiveData from it
3. Since ViewModel holds a stable `MutableLiveData` field (Phase 1 fix), it needs to observe the Repository-returned LiveData and relay values to its own field

**Corrected approach for ViewModels:** Use a helper to relay Repository LiveData to the ViewModel's stable MutableLiveData:

```java
@HiltViewModel
public class PlantsViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<List<Plant>>> plants = new MutableLiveData<>();
    private final MutableLiveData<List<Plant>> filteredPlants = new MutableLiveData<>();
    private String searchQuery = "";
    private String selectedCategory;

    @Inject
    public PlantsViewModel(DongMedicineRepository repository) {
        this.repository = repository;
        selectedCategory = "全部";
        loadPlants();
    }

    public LiveData<Resource<List<Plant>>> getPlants() { return plants; }
    public LiveData<List<Plant>> getFilteredPlants() { return filteredPlants; }

    public void loadPlants() {
        // Repository returns LiveData — observe it and relay to our stable MutableLiveData
        LiveData<Resource<List<Plant>>> repoData = repository.getPlants();
        repoData.observeForever(resource -> {
            plants.setValue(resource);
            if (resource != null && resource.getStatus() == Resource.Status.SUCCESS
                    && resource.getData() != null) {
                applyFilters();
            }
        });
    }

    // setSearchQuery, setSelectedCategory, applyFilters, getCategories unchanged from Phase 1
}
```

> **Note:** `observeForever` is used because we're in a ViewModel (not a Fragment). The observer lives as long as the ViewModel. This is acceptable here because the Repository's LiveData is a single-shot network call, not a continuous stream.

Apply the same `@HiltViewModel` + `@Inject constructor(DongMedicineRepository repository)` pattern to:
- `PlantDetailViewModel`
- `InheritorsViewModel`
- `InheritorDetailViewModel`
- `KnowledgeViewModel`
- `KnowledgeDetailViewModel`
- `HomeViewModel`

For each: add `@HiltViewModel`, add `@Inject` to constructor, accept `DongMedicineRepository` parameter, remove `DongMedicineRepository.getInstance()` call.

- [ ] **Step 3: Delete ApiClient.java**

The `ApiClient` singleton is no longer needed — Hilt's `NetworkModule` provides Retrofit/OkHttp/ApiService. Delete:
```
app/src/main/java/com/dongmedicine/data/api/ApiClient.java
```

- [ ] **Step 4: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/dongmedicine/
git rm app/src/main/java/com/dongmedicine/data/api/ApiClient.java
git commit -m "feat: Hilt DI for Repository + ViewModels, Room cache on network fetch

- Repository is @Singleton with @Inject constructor
- All ViewModels use @HiltViewModel with injected Repository
- Network results cached to Room database
- Deleted ApiClient singleton (replaced by NetworkModule)
- Removed getInstance() pattern"
```

---

### Task 7: Final Phase 2 verification

- [ ] **Step 1: Clean build**

```bash
./gradlew clean assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run tests**

```bash
./gradlew test
```
Expected: All tests pass

- [ ] **Step 3: Verify no manual singleton patterns remain**

```bash
grep -rn "getInstance()" app/src/main/java/ || echo "No getInstance() found"
```
Expected: No getInstance() found

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "chore: Phase 2 complete — Hilt DI + Room offline cache"
```
