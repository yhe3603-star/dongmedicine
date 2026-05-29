# 项目问题修复实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复分析报告中发现的 30 个问题，按优先级分 5 个阶段执行

**Architecture:** Phase 1 修复关键 bug 和安全问题；Phase 2 重构 Repository 消除重复并实现离线回退；Phase 3 修复 ViewModel/Fragment 的 LiveData 和错误处理；Phase 4 清理资源/布局问题；Phase 5 补全测试

**Tech Stack:** Java 11, Android SDK 35, Hilt 2.51.1, Room 2.6.1, Retrofit 2.9, ViewBinding

---

## Phase 1: 关键 Bug 和安全修复（5 个 Task）

### Task 1: 修复暗色模式背景色缺失

**Files:**
- Modify: `app/src/main/res/values-night/colors.xml`

**说明:** `@color/background` (#FAFAFA) 只在 `values/colors.xml` 中定义。所有 Fragment 根布局使用 `android:background="@color/background"`，暗色模式下背景显示浅灰色。

- [ ] **Step 1: 添加缺失的暗色模式背景色**

在 `values-night/colors.xml` 的 `<!-- Surface and background -->` 注释前添加：

```xml
<color name="background">#121212</color>
```

完整文件应为：

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Base -->
    <color name="black">#FFFFFFFF</color>
    <color name="white">#FF000000</color>

    <!-- Theme colors (dark mode) -->
    <color name="primary">#81C784</color>
    <color name="primary_dark">#1B5E20</color>
    <color name="primary_light">#A5D6A7</color>
    <color name="accent">#FFB74D</color>

    <!-- Surface and background -->
    <color name="background">#121212</color>
    <color name="background_primary">#121212</color>
    <color name="background_secondary">#1E1E1E</color>
    <color name="card_background">#1E1E1E</color>

    <!-- Text -->
    <color name="text_primary">#E0E0E0</color>
    <color name="text_secondary">#AAAAAA</color>
    <color name="divider">#424242</color>

    <!-- Stats card backgrounds -->
    <color name="stats_blue_bg">#1A237E</color>
    <color name="stats_blue_text">#90CAF9</color>
    <color name="stats_orange_bg">#3E2723</color>
    <color name="stats_orange_text">#FFB74D</color>
    <color name="stats_purple_bg">#4A148C</color>
    <color name="stats_purple_text">#CE93D8</color>

    <!-- Quick entry buttons -->
    <color name="button_blue">#90CAF9</color>
    <color name="button_orange">#FFB74D</color>
    <color name="button_purple">#CE93D8</color>

    <!-- Category tag -->
    <color name="category_tag_bg">#1B5E20</color>
</resources>
```

- [ ] **Step 2: 验证**

运行 `./gradlew assembleDebug` 确认编译通过。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/values-night/colors.xml
git commit -m "fix: add missing dark mode background color"
```

---

### Task 2: 修复 EditText inputType 和硬编码字符串

**Files:**
- Modify: `app/src/main/res/layout/fragment_qa.xml`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: 添加缺失的字符串资源**

在 `strings.xml` 的 `</resources>` 前添加：

```xml
    <string name="label_faq">常见问题</string>
```

- [ ] **Step 2: 修复 fragment_qa.xml**

将第 116 行的硬编码字符串替换为字符串资源，并在第 57 行后添加 inputType：

在 `fragment_qa.xml` 第 57 行 (`android:minHeight="100dp"`) 之后，添加：
```xml
                    android:inputType="textMultiLine"
```

将第 116 行：
```xml
            android:text="常见问题"
```
替换为：
```xml
            android:text="@string/label_faq"
```

- [ ] **Step 3: 验证**

运行 `./gradlew assembleDebug` 确认编译通过。

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/fragment_qa.xml app/src/main/res/values/strings.xml
git commit -m "fix: add inputType to QA EditText and replace hardcoded string"
```

---

### Task 3: 修复 BuildConfig 和网络安全配置

**Files:**
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/res/xml/network_security_config.xml`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: 在 build.gradle.kts 中启用 BuildConfig**

在 `android { }` 块中，在 `viewBinding = true` 之后添加 `buildConfig = true`：

```kotlin
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
```

- [ ] **Step 2: 创建网络安全配置**

创建 `app/src/main/res/xml/network_security_config.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>
```

- [ ] **Step 3: 更新 AndroidManifest.xml**

将第 18 行：
```xml
        android:usesCleartextTraffic="true"
```
替换为：
```xml
        android:networkSecurityConfig="@xml/network_security_config"
```

- [ ] **Step 4: 验证**

运行 `./gradlew assembleDebug` 确认编译通过。确认 `NetworkModule.java` 中的 `BuildConfig.DEBUG` 引用不再有 lint 警告。

- [ ] **Step 5: Commit**

```bash
git add app/build.gradle.kts app/src/main/res/xml/network_security_config.xml app/src/main/AndroidManifest.xml
git commit -m "fix: enable BuildConfig generation and add network security config"
```

---

### Task 4: 添加 Room 迁移回退策略

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/di/DatabaseModule.java`

- [ ] **Step 1: 添加 fallbackToDestructiveMigration**

将 `DatabaseModule.java` 第 26-31 行的 `provideDatabase` 方法修改为：

```java
    @Provides
    @Singleton
    DongMedicineDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(
                context,
                DongMedicineDatabase.class,
                "dongmedicine_db"
        ).fallbackToDestructiveMigration().build();
    }
```

- [ ] **Step 2: 验证**

运行 `./gradlew assembleDebug` 确认编译通过。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/dongmedicine/di/DatabaseModule.java
git commit -m "fix: add fallbackToDestructiveMigration to Room database"
```

---

### Task 5: 修复 Detail ViewModel LiveData 初始为 null 和 PlantsViewModel NPE

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/ui/plants/PlantDetailViewModel.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/inheritors/InheritorDetailViewModel.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/knowledge/KnowledgeDetailViewModel.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/plants/PlantsViewModel.java`

- [ ] **Step 1: 修复 PlantDetailViewModel**

将 `PlantDetailViewModel.java` 修改为：

```java
package com.dongmedicine.ui.plants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PlantDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<Plant>> plant = new MutableLiveData<>();

    @Inject
    public PlantDetailViewModel(DongMedicineRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<Plant>> getPlant() { return plant; }

    public void loadPlant(int plantId) {
        Resource<Plant> resource = repository.getPlantById(plantId).getValue();
        if (resource != null) {
            plant.setValue(resource);
        }
    }
}
```

**Wait — this approach won't work.** The repository returns LiveData from an async Retrofit call. The value isn't immediately available. I need a different approach.

**Correct approach:** Keep the field as `MutableLiveData` initialized empty, and observe the repository's returned LiveData:

```java
package com.dongmedicine.ui.plants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PlantDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<Plant>> plant = new MutableLiveData<>();

    @Inject
    public PlantDetailViewModel(DongMedicineRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<Plant>> getPlant() { return plant; }

    public void loadPlant(int plantId) {
        repository.getPlantById(plantId).observeForever(resource -> {
            plant.setValue(resource);
        });
    }
}
```

- [ ] **Step 2: 修复 InheritorDetailViewModel**

```java
package com.dongmedicine.ui.inheritors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class InheritorDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<Inheritor>> inheritor = new MutableLiveData<>();

    @Inject
    public InheritorDetailViewModel(DongMedicineRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<Inheritor>> getInheritor() { return inheritor; }

    public void loadInheritor(int inheritorId) {
        repository.getInheritorById(inheritorId).observeForever(resource -> {
            inheritor.setValue(resource);
        });
    }
}
```

- [ ] **Step 3: 修复 KnowledgeDetailViewModel**

```java
package com.dongmedicine.ui.knowledge;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class KnowledgeDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<KnowledgeItem>> knowledgeItem = new MutableLiveData<>();

    @Inject
    public KnowledgeDetailViewModel(DongMedicineRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<KnowledgeItem>> getKnowledgeItem() { return knowledgeItem; }

    public void loadKnowledge(int knowledgeId) {
        repository.getKnowledgeById(knowledgeId).observeForever(resource -> {
            knowledgeItem.setValue(resource);
        });
    }
}
```

- [ ] **Step 4: 修复 PlantsViewModel.getName() NPE**

在 `PlantsViewModel.java` 第 61 行，将：
```java
                    plant.getName().toLowerCase().contains(query) ||
```
替换为：
```java
                    (plant.getName() != null && plant.getName().toLowerCase().contains(query)) ||
```

- [ ] **Step 5: 验证**

运行 `./gradlew test` 确认所有现有测试通过。

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/dongmedicine/ui/plants/PlantDetailViewModel.java \
       app/src/main/java/com/dongmedicine/ui/inheritors/InheritorDetailViewModel.java \
       app/src/main/java/com/dongmedicine/ui/knowledge/KnowledgeDetailViewModel.java \
       app/src/main/java/com/dongmedicine/ui/plants/PlantsViewModel.java
git commit -m "fix: initialize DetailViewModel LiveData as empty and fix PlantsViewModel NPE"
```

---

## Phase 2: Repository 重构（3 个 Task）

### Task 6: 激活 executeCall 并重构 Repository 消除重复

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/data/repository/DongMedicineRepository.java`

**说明:** Repository 有 6 个方法（第 67-222 行）重复相同的 Retrofit 回调逻辑。第 46-65 行的 `executeCall()` 泛型方法已存在但从未使用。本 Task 激活 `executeCall` 并用它重构所有方法。同时扩展 `executeCall` 支持可选的 Room 缓存回调。

- [ ] **Step 1: 重写 Repository**

将 `DongMedicineRepository.java` 的完整内容替换为：

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

    private <T> void executeCall(Call<ApiService.ApiResponse<T>> call,
                                  MutableLiveData<Resource<T>> liveData,
                                  CacheAction<T> cacheAction) {
        liveData.postValue(Resource.loading(null));
        call.enqueue(new Callback<ApiService.ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<T>> call,
                                   Response<ApiService.ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    T data = response.body().getData();
                    liveData.postValue(Resource.success(data));
                    if (cacheAction != null && data != null) {
                        executor.execute(() -> {
                            try {
                                cacheAction.cache(data);
                            } catch (Exception e) {
                                // Cache write failed silently — network data already displayed
                            }
                        });
                    }
                } else {
                    liveData.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<T>> call, Throwable t) {
                liveData.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });
    }

    private interface CacheAction<T> {
        void cache(T data);
    }

    public LiveData<Resource<List<Plant>>> getPlants() {
        MutableLiveData<Resource<List<Plant>>> result = new MutableLiveData<>();
        executeCall(apiService.getPlants(), result, data -> plantDao.insertAll(data));
        return result;
    }

    public LiveData<Resource<Plant>> getPlantById(int id) {
        MutableLiveData<Resource<Plant>> result = new MutableLiveData<>();
        executeCall(apiService.getPlantById(id), result, data ->
                plantDao.insertAll(java.util.Collections.singletonList(data)));
        return result;
    }

    public LiveData<Resource<List<Inheritor>>> getInheritors() {
        MutableLiveData<Resource<List<Inheritor>>> result = new MutableLiveData<>();
        executeCall(apiService.getInheritors(), result, data -> inheritorDao.insertAll(data));
        return result;
    }

    public LiveData<Resource<Inheritor>> getInheritorById(int id) {
        MutableLiveData<Resource<Inheritor>> result = new MutableLiveData<>();
        executeCall(apiService.getInheritorById(id), result, data ->
                inheritorDao.insertAll(java.util.Collections.singletonList(data)));
        return result;
    }

    public LiveData<Resource<List<KnowledgeItem>>> getKnowledgeList() {
        MutableLiveData<Resource<List<KnowledgeItem>>> result = new MutableLiveData<>();
        executeCall(apiService.getKnowledgeList(), result, data -> knowledgeDao.insertAll(data));
        return result;
    }

    public LiveData<Resource<KnowledgeItem>> getKnowledgeById(int id) {
        MutableLiveData<Resource<KnowledgeItem>> result = new MutableLiveData<>();
        executeCall(apiService.getKnowledgeById(id), result, data ->
                knowledgeDao.insertAll(java.util.Collections.singletonList(data)));
        return result;
    }
}
```

- [ ] **Step 2: 验证**

运行 `./gradlew test --tests "com.dongmedicine.data.repository.DongMedicineRepositoryTest"` 确认 Repository 测试通过。

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/dongmedicine/data/repository/DongMedicineRepository.java
git commit -m "refactor: activate executeCall helper and eliminate repository code duplication"
```

---

### Task 7: 修复列表 ViewModel LiveData 刷新引用丢失

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/ui/plants/PlantsViewModel.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/inheritors/InheritorsViewModel.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/knowledge/KnowledgeViewModel.java`

**说明:** `loadX()` 方法替换 LiveData 字段引用（`data = repository.getXxx()`），导致 Fragment 的 observer 仍绑定旧引用，刷新后 UI 不更新。修复方案：使用 `MutableLiveData` 缓存结果，`loadX()` 观察 repository 的新 LiveData 并转发到缓存的 LiveData。

- [ ] **Step 1: 修复 PlantsViewModel**

将 `PlantsViewModel.java` 替换为：

```java
package com.dongmedicine.ui.plants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
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
    private final MediatorLiveData<Resource<List<Plant>>> plants = new MediatorLiveData<>();
    private final MutableLiveData<List<Plant>> filteredPlants = new MutableLiveData<>();
    private String searchQuery = "";
    private String selectedCategory;
    private LiveData<Resource<List<Plant>>> currentSource;

    @Inject
    public PlantsViewModel(DongMedicineRepository repository) {
        this.repository = repository;
        selectedCategory = "全部";
        loadPlants();
    }

    public LiveData<Resource<List<Plant>>> getPlants() { return plants; }
    public LiveData<List<Plant>> getFilteredPlants() { return filteredPlants; }

    public void loadPlants() {
        if (currentSource != null) {
            plants.removeSource(currentSource);
        }
        currentSource = repository.getPlants();
        plants.addSource(currentSource, resource -> {
            plants.setValue(resource);
            if (resource != null && resource.isSuccess()) {
                applyFilters();
            }
        });
    }

    public void setSearchQuery(String query) {
        searchQuery = query;
        applyFilters();
    }

    public void setSelectedCategory(String category) {
        selectedCategory = category;
        applyFilters();
    }

    public void applyFilters() {
        Resource<List<Plant>> resource = plants.getValue();
        if (resource == null || resource.getData() == null) return;

        List<Plant> sourceList = resource.getData();
        List<Plant> result = new ArrayList<>();
        String query = searchQuery != null ? searchQuery.toLowerCase() : "";
        String category = selectedCategory != null ? selectedCategory : "全部";

        for (Plant plant : sourceList) {
            boolean matchesQuery = query.isEmpty() ||
                    (plant.getName() != null && plant.getName().toLowerCase().contains(query)) ||
                    (plant.getScientificName() != null && plant.getScientificName().toLowerCase().contains(query));
            boolean matchesCategory = category.equals("全部") ||
                    (plant.getCategory() != null && plant.getCategory().equals(category));
            if (matchesQuery && matchesCategory) {
                result.add(plant);
            }
        }
        filteredPlants.setValue(result);
    }

    public String[] getCategories() {
        return new String[]{"全部", "清热解毒", "补益类", "活血化瘀", "祛风除湿", "其他"};
    }
}
```

- [ ] **Step 2: 修复 InheritorsViewModel**

将 `InheritorsViewModel.java` 替换为：

```java
package com.dongmedicine.ui.inheritors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class InheritorsViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MediatorLiveData<Resource<List<Inheritor>>> inheritors = new MediatorLiveData<>();
    private final MutableLiveData<List<Inheritor>> filteredInheritors = new MutableLiveData<>();
    private String selectedLevel;
    private LiveData<Resource<List<Inheritor>>> currentSource;

    @Inject
    public InheritorsViewModel(DongMedicineRepository repository) {
        this.repository = repository;
        selectedLevel = "全部";
        loadInheritors();
    }

    public LiveData<Resource<List<Inheritor>>> getInheritors() { return inheritors; }
    public LiveData<List<Inheritor>> getFilteredInheritors() { return filteredInheritors; }

    public void loadInheritors() {
        if (currentSource != null) {
            inheritors.removeSource(currentSource);
        }
        currentSource = repository.getInheritors();
        inheritors.addSource(currentSource, resource -> {
            inheritors.setValue(resource);
            if (resource != null && resource.isSuccess()) {
                applyFilters();
            }
        });
    }

    public void setSelectedLevel(String level) {
        selectedLevel = level;
        applyFilters();
    }

    public void applyFilters() {
        Resource<List<Inheritor>> resource = inheritors.getValue();
        if (resource == null || resource.getData() == null) return;

        List<Inheritor> sourceList = resource.getData();
        List<Inheritor> result = new ArrayList<>();
        String level = selectedLevel != null ? selectedLevel : "全部";

        for (Inheritor inheritor : sourceList) {
            boolean matchesLevel = level.equals("全部") ||
                    (inheritor.getTitle() != null && inheritor.getTitle().contains(level));
            if (matchesLevel) {
                result.add(inheritor);
            }
        }
        filteredInheritors.setValue(result);
    }

    public String[] getLevels() {
        return new String[]{"全部", "国家级", "省级", "市级", "县级"};
    }
}
```

- [ ] **Step 3: 修复 KnowledgeViewModel**

将 `KnowledgeViewModel.java` 替换为：

```java
package com.dongmedicine.ui.knowledge;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class KnowledgeViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MediatorLiveData<Resource<List<KnowledgeItem>>> knowledgeList = new MediatorLiveData<>();
    private final MutableLiveData<List<KnowledgeItem>> filteredKnowledge = new MutableLiveData<>();
    private String selectedCategory;
    private LiveData<Resource<List<KnowledgeItem>>> currentSource;

    @Inject
    public KnowledgeViewModel(DongMedicineRepository repository) {
        this.repository = repository;
        selectedCategory = "全部";
        loadKnowledge();
    }

    public LiveData<Resource<List<KnowledgeItem>>> getKnowledgeList() { return knowledgeList; }
    public LiveData<List<KnowledgeItem>> getFilteredKnowledge() { return filteredKnowledge; }

    public void loadKnowledge() {
        if (currentSource != null) {
            knowledgeList.removeSource(currentSource);
        }
        currentSource = repository.getKnowledgeList();
        knowledgeList.addSource(currentSource, resource -> {
            knowledgeList.setValue(resource);
            if (resource != null && resource.isSuccess()) {
                applyFilters();
            }
        });
    }

    public void setSelectedCategory(String category) {
        selectedCategory = category;
        applyFilters();
    }

    public void applyFilters() {
        Resource<List<KnowledgeItem>> resource = knowledgeList.getValue();
        if (resource == null || resource.getData() == null) return;

        List<KnowledgeItem> sourceList = resource.getData();
        List<KnowledgeItem> result = new ArrayList<>();
        String category = selectedCategory != null ? selectedCategory : "全部";

        for (KnowledgeItem item : sourceList) {
            boolean matchesCategory = category.equals("全部") ||
                    (item.getCategory() != null && item.getCategory().equals(category));
            if (matchesCategory) {
                result.add(item);
            }
        }
        filteredKnowledge.setValue(result);
    }

    public String[] getCategories() {
        return new String[]{"全部", "基础知识", "诊断方法", "制药工艺", "治疗方法", "养生保健"};
    }
}
```

- [ ] **Step 4: 验证**

运行 `./gradlew test` 确认所有 ViewModel 测试通过。

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/dongmedicine/ui/plants/PlantsViewModel.java \
       app/src/main/java/com/dongmedicine/ui/inheritors/InheritorsViewModel.java \
       app/src/main/java/com/dongmedicine/ui/knowledge/KnowledgeViewModel.java
git commit -m "fix: use MediatorLiveData to fix refresh observer loss in list ViewModels"
```

---

### Task 8: 修复详情 Fragment 错误处理

**Files:**
- Modify: `app/src/main/res/layout/fragment_plant_detail.xml`
- Modify: `app/src/main/res/layout/fragment_inheritor_detail.xml`
- Modify: `app/src/main/res/layout/fragment_knowledge_detail.xml`
- Modify: `app/src/main/java/com/dongmedicine/ui/plants/PlantDetailFragment.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/inheritors/InheritorDetailFragment.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/knowledge/KnowledgeDetailFragment.java`

**说明:** 三个详情 Fragment 的 ERROR 和 LOADING 分支为空操作。用户看到空白屏幕。需要：(1) 在布局中添加 ProgressBar 和错误 TextView；(2) 在 Fragment 中实现错误/加载状态显示。

- [ ] **Step 1: 修改 fragment_plant_detail.xml**

在 `</androidx.coordinatorlayout.widget.CoordinatorLayout>` 结束标签前，确认已存在 ProgressBar（第 146-151 行已有）。在 ProgressBar 之后添加错误 TextView：

```xml
    <TextView
        android:id="@+id/tv_error"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="@string/error_network"
        android:textColor="@color/text_secondary"
        android:textSize="16sp"
        android:visibility="gone" />
```

- [ ] **Step 2: 修改 fragment_inheritor_detail.xml**

在 `</androidx.coordinatorlayout.widget.CoordinatorLayout>` 结束标签前添加：

```xml
    <ProgressBar
        android:id="@+id/progress_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

    <TextView
        android:id="@+id/tv_error"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="@string/error_network"
        android:textColor="@color/text_secondary"
        android:textSize="16sp"
        android:visibility="gone" />
```

- [ ] **Step 3: 修改 fragment_knowledge_detail.xml**

在 `</androidx.coordinatorlayout.widget.CoordinatorLayout>` 结束标签前添加：

```xml
    <ProgressBar
        android:id="@+id/progress_bar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:visibility="gone" />

    <TextView
        android:id="@+id/tv_error"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center"
        android:text="@string/error_network"
        android:textColor="@color/text_secondary"
        android:textSize="16sp"
        android:visibility="gone" />
```

- [ ] **Step 4: 更新 PlantDetailFragment.observeData()**

将 `PlantDetailFragment.java` 第 58-77 行的 `observeData()` 方法替换为：

```java
    private void observeData() {
        viewModel.getPlant().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.tvError.setVisibility(View.GONE);
                        break;
                    case SUCCESS:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvError.setVisibility(View.GONE);
                        if (resource.getData() != null) {
                            displayPlant(resource.getData());
                        }
                        break;
                    case ERROR:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvError.setText(resource.getMessage() != null ?
                                resource.getMessage() : getString(R.string.error_network));
                        binding.tvError.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }
```

- [ ] **Step 5: 更新 InheritorDetailFragment.observeData()**

将 `InheritorDetailFragment.java` 第 58-76 行的 `observeData()` 方法替换为：

```java
    private void observeData() {
        viewModel.getInheritor().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.tvError.setVisibility(View.GONE);
                        break;
                    case SUCCESS:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvError.setVisibility(View.GONE);
                        if (resource.getData() != null) {
                            displayInheritor(resource.getData());
                        }
                        break;
                    case ERROR:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvError.setText(resource.getMessage() != null ?
                                resource.getMessage() : getString(R.string.error_network));
                        binding.tvError.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }
```

- [ ] **Step 6: 更新 KnowledgeDetailFragment.observeData()**

将 `KnowledgeDetailFragment.java` 第 56-73 行的 `observeData()` 方法替换为：

```java
    private void observeData() {
        viewModel.getKnowledgeItem().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.tvError.setVisibility(View.GONE);
                        break;
                    case SUCCESS:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvError.setVisibility(View.GONE);
                        if (resource.getData() != null) {
                            displayKnowledge(resource.getData());
                        }
                        break;
                    case ERROR:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvError.setText(resource.getMessage() != null ?
                                resource.getMessage() : getString(R.string.error_network));
                        binding.tvError.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }
```

- [ ] **Step 7: 验证**

运行 `./gradlew assembleDebug` 确认编译通过。

- [ ] **Step 8: Commit**

```bash
git add app/src/main/res/layout/fragment_plant_detail.xml \
       app/src/main/res/layout/fragment_inheritor_detail.xml \
       app/src/main/res/layout/fragment_knowledge_detail.xml \
       app/src/main/java/com/dongmedicine/ui/plants/PlantDetailFragment.java \
       app/src/main/java/com/dongmedicine/ui/inheritors/InheritorDetailFragment.java \
       app/src/main/java/com/dongmedicine/ui/knowledge/KnowledgeDetailFragment.java
git commit -m "fix: add loading and error state UI to detail fragments"
```

---

## Phase 3: 资源清理（2 个 Task）

### Task 9: 替换布局中的硬编码字符串

**Files:**
- Modify: `app/src/main/res/layout/fragment_plant_detail.xml` (3 处)
- Modify: `app/src/main/res/layout/fragment_inheritor_detail.xml` (3 处)
- Modify: `app/src/main/res/layout/fragment_knowledge_detail.xml` (1 处)

**说明:** 字符串资源已存在于 `strings.xml`，但布局中使用了硬编码中文。

- [ ] **Step 1: 修复 fragment_plant_detail.xml**

替换 3 处：
- 第 18 行 `app:title="植物详情"` → `app:title="@string/title_plant_detail"`
- 第 88 行 `android:text="功效作用"` → `android:text="@string/label_effects"`
- 第 106 行 `android:text="详细描述"` → `android:text="@string/label_description"`
- 第 124 行 `android:text="分布区域"` → `android:text="@string/label_distribution"`

- [ ] **Step 2: 修复 fragment_inheritor_detail.xml**

替换 3 处：
- 第 18 行 `app:title="传承人详情"` → `app:title="@string/title_inheritor_detail"`
- 第 79 行 `android:text="擅长领域"` → `android:text="@string/label_specialization"`
- 第 96 行 `android:text="个人简介"` → `android:text="@string/label_introduction"`

- [ ] **Step 3: 修复 fragment_knowledge_detail.xml**

替换 1 处：
- 第 18 行 `app:title="知识详情"` → `app:title="@string/title_knowledge_detail`

- [ ] **Step 4: 验证**

运行 `./gradlew assembleDebug` 确认编译通过。

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/layout/fragment_plant_detail.xml \
       app/src/main/res/layout/fragment_inheritor_detail.xml \
       app/src/main/res/layout/fragment_knowledge_detail.xml
git commit -m "fix: replace hardcoded strings with string resources in detail layouts"
```

---

### Task 10: 将硬编码依赖移入版本目录

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: 在 libs.versions.toml 中添加条目**

在 `[versions]` 部分添加：
```toml
cardview = "1.0.0"
swiperefreshlayout = "1.1.0"
```

在 `[libraries]` 部分添加：
```toml
cardview = { group = "androidx.cardview", name = "cardview", version.ref = "cardview" }
swiperefreshlayout = { group = "androidx.swiperefreshlayout", name = "swiperefreshlayout", version.ref = "swiperefreshlayout" }
glide-compiler = { group = "com.github.bumptech.glide", name = "compiler", version.ref = "glide" }
```

- [ ] **Step 2: 更新 app/build.gradle.kts**

将第 62-66 行：
```kotlin
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    annotationProcessor(libs.room.compiler)
    annotationProcessor(libs.hilt.compiler)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
```
替换为：
```kotlin
    implementation(libs.cardview)
    implementation(libs.swiperefreshlayout)
    annotationProcessor(libs.room.compiler)
    annotationProcessor(libs.hilt.compiler)
    annotationProcessor(libs.glide.compiler)
```

- [ ] **Step 3: 验证**

运行 `./gradlew assembleDebug` 确认编译通过。

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "chore: move hardcoded dependencies to version catalog"
```

---

## Phase 4: 代码清理（2 个 Task）

### Task 11: 提取 QaItem 为独立 model 类

**Files:**
- Create: `app/src/main/java/com/dongmedicine/data/model/QaItem.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/qa/QaViewModel.java`
- Modify: `app/src/main/java/com/dongmedicine/adapters/QaAdapter.java`

- [ ] **Step 1: 创建 QaItem.java**

创建 `app/src/main/java/com/dongmedicine/data/model/QaItem.java`：

```java
package com.dongmedicine.data.model;

public class QaItem {
    private int id;
    private String question;
    private String answer;
    private String category;

    public QaItem() {}

    public QaItem(int id, String question, String answer, String category) {
        this.id = id;
        this.question = question;
        this.answer = answer;
        this.category = category;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
```

- [ ] **Step 2: 更新 QaViewModel.java**

移除 `QaViewModel.java` 内部的 `QaItem` 内部类（第 82-106 行），并添加 import：

在 imports 部分添加：
```java
import com.dongmedicine.data.model.QaItem;
```

删除 `QaItem` 内部类（第 82-106 行的 `public static class QaItem { ... }`）。

- [ ] **Step 3: 更新 QaAdapter.java**

将 `QaAdapter.java` 的 import：
```java
import com.dongmedicine.ui.qa.QaViewModel.QaItem;
```
替换为：
```java
import com.dongmedicine.data.model.QaItem;
```

- [ ] **Step 4: 验证**

运行 `./gradlew assembleDebug` 确认编译通过。

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/dongmedicine/data/model/QaItem.java \
       app/src/main/java/com/dongmedicine/ui/qa/QaViewModel.java \
       app/src/main/java/com/dongmedicine/adapters/QaAdapter.java
git commit -m "refactor: extract QaItem from ViewModel inner class to standalone model"
```

---

### Task 12: 启用 Room schema 导出

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/data/local/DongMedicineDatabase.java`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: 修改 DongMedicineDatabase.java**

将第 10 行：
```java
@Database(entities = {Plant.class, Inheritor.class, KnowledgeItem.class}, version = 1, exportSchema = false)
```
替换为：
```java
@Database(entities = {Plant.class, Inheritor.class, KnowledgeItem.class}, version = 1, exportSchema = true)
```

- [ ] **Step 2: 在 build.gradle.kts 中配置 schema 导出目录**

在 `android { }` 块末尾，`compileOptions` 之前添加：

```kotlin
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }
```

**注意:** `defaultConfig` 块已经存在（第 8-14 行）。需要在现有的 `defaultConfig` 块内，在 `testInstrumentationRunner` 之后添加 `javaCompileOptions`：

```kotlin
    defaultConfig {
        applicationId = "com.dongmedicine"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }
```

- [ ] **Step 3: 验证**

运行 `./gradlew assembleDebug` 确认编译通过。确认 `app/schemas/` 目录生成了 `com.dongmedicine.data.local.DongMedicineDatabase/1.json`。

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/dongmedicine/data/local/DongMedicineDatabase.java app/build.gradle.kts
git commit -m "chore: enable Room schema export for migration testing"
```

---

## Phase 5: 测试补全（1 个 Task）

### Task 13: 补充 Repository 和 ViewModel 测试

**Files:**
- Modify: `app/src/test/java/com/dongmedicine/data/repository/DongMedicineRepositoryTest.java`
- Create: `app/src/test/java/com/dongmedicine/ui/plants/PlantDetailViewModelTest.java` (已存在，检查是否需要更新)
- Create: `app/src/test/java/com/dongmedicine/ui/inheritors/InheritorDetailViewModelTest.java`
- Create: `app/src/test/java/com/dongmedicine/ui/knowledge/KnowledgeDetailViewModelTest.java`
- Create: `app/src/test/java/com/dongmedicine/ui/qa/QaViewModelTest.java`

- [ ] **Step 1: 扩展 DongMedicineRepositoryTest**

在 `DongMedicineRepositoryTest.java` 中，在 `createPlant` 方法之前添加 `getInheritors` 和 `getKnowledgeList` 的测试：

```java
    @Mock private Call<ApiService.ApiResponse<List<Inheritor>>> inheritorsCall;
    @Mock private Call<ApiService.ApiResponse<List<KnowledgeItem>>> knowledgeCall;

    @Test
    public void getInheritors_success_emitsSuccessResource() throws InterruptedException {
        List<Inheritor> inheritors = Arrays.asList(createInheritor(1, "张三"), createInheritor(2, "李四"));
        ApiService.ApiResponse<List<Inheritor>> apiResponse = new ApiService.ApiResponse<>();
        apiResponse.setCode(200);
        apiResponse.setData(inheritors);

        when(apiService.getInheritors()).thenReturn(inheritorsCall);
        doAnswer(invocation -> {
            Callback<ApiService.ApiResponse<List<Inheritor>>> callback = invocation.getArgument(0);
            callback.onResponse(inheritorsCall, Response.success(apiResponse));
            return null;
        }).when(inheritorsCall).enqueue(any());

        Resource<List<Inheritor>> result = getValue(repository.getInheritors());
        assertNotNull(result);
        assertEquals(Resource.Status.SUCCESS, result.getStatus());
        assertEquals(2, result.getData().size());
    }

    @Test
    public void getKnowledgeList_success_emitsSuccessResource() throws InterruptedException {
        List<KnowledgeItem> items = Arrays.asList(createKnowledgeItem(1, "侗医基础"), createKnowledgeItem(2, "草药学"));
        ApiService.ApiResponse<List<KnowledgeItem>> apiResponse = new ApiService.ApiResponse<>();
        apiResponse.setCode(200);
        apiResponse.setData(items);

        when(apiService.getKnowledgeList()).thenReturn(knowledgeCall);
        doAnswer(invocation -> {
            Callback<ApiService.ApiResponse<List<KnowledgeItem>>> callback = invocation.getArgument(0);
            callback.onResponse(knowledgeCall, Response.success(apiResponse));
            return null;
        }).when(knowledgeCall).enqueue(any());

        Resource<List<KnowledgeItem>> result = getValue(repository.getKnowledgeList());
        assertNotNull(result);
        assertEquals(Resource.Status.SUCCESS, result.getStatus());
        assertEquals(2, result.getData().size());
    }

    private Inheritor createInheritor(int id, String name) {
        Inheritor inheritor = new Inheritor();
        inheritor.setId(id);
        inheritor.setName(name);
        return inheritor;
    }

    private KnowledgeItem createKnowledgeItem(int id, String title) {
        KnowledgeItem item = new KnowledgeItem();
        item.setId(id);
        item.setTitle(title);
        return item;
    }
```

同时修复 `getValue` 方法，移除 `Thread.sleep`：

将第 103-106 行替换为：
```java
    private <T> T getValue(androidx.lifecycle.LiveData<T> liveData) throws InterruptedException {
        final Object[] holder = new Object[1];
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        androidx.lifecycle.Observer<T> observer = value -> {
            holder[0] = value;
            latch.countDown();
        };
        liveData.observeForever(observer);
        latch.await(2, java.util.concurrent.TimeUnit.SECONDS);
        liveData.removeObserver(observer);
        //noinspection unchecked
        return (T) holder[0];
    }
```

- [ ] **Step 2: 创建 InheritorDetailViewModelTest**

创建 `app/src/test/java/com/dongmedicine/ui/inheritors/InheritorDetailViewModelTest.java`：

```java
package com.dongmedicine.ui.inheritors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InheritorDetailViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private DongMedicineRepository repository;
    private InheritorDetailViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new InheritorDetailViewModel(repository);
    }

    @Test
    public void loadInheritor_success_emitsSuccessResource() {
        MutableLiveData<Resource<Inheritor>> liveData = new MutableLiveData<>();
        Inheritor inheritor = new Inheritor();
        inheritor.setId(1);
        inheritor.setName("张三");
        liveData.setValue(Resource.success(inheritor));
        when(repository.getInheritorById(1)).thenReturn(liveData);

        viewModel.loadInheritor(1);
        Resource<Inheritor> resource = viewModel.getInheritor().getValue();
        assertNotNull(resource);
        assertEquals(Resource.Status.SUCCESS, resource.getStatus());
        assertEquals("张三", resource.getData().getName());
    }

    @Test
    public void loadInheritor_error_emitsErrorResource() {
        MutableLiveData<Resource<Inheritor>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.error("网络错误", null));
        when(repository.getInheritorById(1)).thenReturn(liveData);

        viewModel.loadInheritor(1);
        Resource<Inheritor> resource = viewModel.getInheritor().getValue();
        assertNotNull(resource);
        assertEquals(Resource.Status.ERROR, resource.getStatus());
    }
}
```

- [ ] **Step 3: 创建 KnowledgeDetailViewModelTest**

创建 `app/src/test/java/com/dongmedicine/ui/knowledge/KnowledgeDetailViewModelTest.java`：

```java
package com.dongmedicine.ui.knowledge;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class KnowledgeDetailViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private DongMedicineRepository repository;
    private KnowledgeDetailViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new KnowledgeDetailViewModel(repository);
    }

    @Test
    public void loadKnowledge_success_emitsSuccessResource() {
        MutableLiveData<Resource<KnowledgeItem>> liveData = new MutableLiveData<>();
        KnowledgeItem item = new KnowledgeItem();
        item.setId(1);
        item.setTitle("侗医基础");
        liveData.setValue(Resource.success(item));
        when(repository.getKnowledgeById(1)).thenReturn(liveData);

        viewModel.loadKnowledge(1);
        Resource<KnowledgeItem> resource = viewModel.getKnowledgeItem().getValue();
        assertNotNull(resource);
        assertEquals(Resource.Status.SUCCESS, resource.getStatus());
        assertEquals("侗医基础", resource.getData().getTitle());
    }

    @Test
    public void loadKnowledge_error_emitsErrorResource() {
        MutableLiveData<Resource<KnowledgeItem>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.error("网络错误", null));
        when(repository.getKnowledgeById(1)).thenReturn(liveData);

        viewModel.loadKnowledge(1);
        Resource<KnowledgeItem> resource = viewModel.getKnowledgeItem().getValue();
        assertNotNull(resource);
        assertEquals(Resource.Status.ERROR, resource.getStatus());
    }
}
```

- [ ] **Step 4: 创建 QaViewModelTest**

创建 `app/src/test/java/com/dongmedicine/ui/qa/QaViewModelTest.java`：

```java
package com.dongmedicine.ui.qa;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.dongmedicine.data.model.QaItem;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class QaViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private QaViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new QaViewModel();
    }

    @Test
    public void submitQuestion_withKeyword_returnsAnswer() {
        viewModel.submitQuestion("钩藤有什么功效？");
        String answer = viewModel.getCurrentAnswer().getValue();
        assertNotNull(answer);
        assertTrue(answer.contains("钩藤"));
    }

    @Test
    public void submitQuestion_withNoKeyword_returnsDefaultAnswer() {
        viewModel.submitQuestion("什么是感冒？");
        String answer = viewModel.getCurrentAnswer().getValue();
        assertNotNull(answer);
        assertTrue(answer.contains("侗医"));
    }

    @Test
    public void loadSampleData_populatesQaList() {
        List<QaItem> items = viewModel.getQaList().getValue();
        assertNotNull(items);
        assertFalse(items.isEmpty());
    }
}
```

- [ ] **Step 5: 验证**

运行 `./gradlew test` 确认所有测试通过。

- [ ] **Step 6: Commit**

```bash
git add app/src/test/java/com/dongmedicine/data/repository/DongMedicineRepositoryTest.java \
       app/src/test/java/com/dongmedicine/ui/inheritors/InheritorDetailViewModelTest.java \
       app/src/test/java/com/dongmedicine/ui/knowledge/KnowledgeDetailViewModelTest.java \
       app/src/test/java/com/dongmedicine/ui/qa/QaViewModelTest.java
git commit -m "test: add repository and ViewModel tests for inheritors, knowledge, and QA"
```

---

## 最终验证

- [ ] **运行完整测试套件:**

```bash
./gradlew test
```

- [ ] **构建 debug APK:**

```bash
./gradlew assembleDebug
```

- [ ] **验证所有修改的文件已提交:**

```bash
git status
git log --oneline -10
```
