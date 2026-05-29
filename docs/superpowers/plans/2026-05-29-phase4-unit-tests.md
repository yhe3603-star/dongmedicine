# Phase 4: Unit Tests — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add unit tests for Repository and all ViewModels to verify data flow, filtering logic, and error handling.

**Architecture:** Tests use Mockito to mock Repository and ApiService. `InstantTaskExecutorRule` synchronizes LiveData. Hilt testing support provides test doubles for DI. Tests run on JVM (not device) via `./gradlew test`.

**Tech Stack:** JUnit 4.13.2, Mockito 5.11.0, AndroidX Core Testing 2.2.0, Hilt Testing 2.51.1, Robolectric 4.12

**Prerequisites:** Phase 2 complete (Hilt DI + Room). Repository and ViewModels use constructor injection.

---

### Task 1: Add test dependencies

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Add test dependencies to version catalog**

Edit `gradle/libs.versions.toml`. Add to `[versions]`:
```toml
mockito = "5.11.0"
coreTesting = "2.2.0"
robolectric = "4.12"
```

Add to `[libraries]`:
```toml
mockito-core = { group = "org.mockito", name = "mockito-core", version.ref = "mockito" }
core-testing = { group = "androidx.arch.core", name = "core-testing", version.ref = "coreTesting" }
hilt-testing = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt" }
robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }
```

- [ ] **Step 2: Add test dependencies to app/build.gradle.kts**

```kotlin
testImplementation(libs.mockito.core)
testImplementation(libs.core.testing)
testImplementation(libs.hilt.testing)
testImplementation(libs.robolectric)
testAnnotationProcessor(libs.hilt.compiler)
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "test: add Mockito, Hilt testing, Robolectric dependencies"
```

---

### Task 2: Write DongMedicineRepositoryTest

**Files:**
- Create: `app/src/test/java/com/dongmedicine/data/repository/DongMedicineRepositoryTest.java`

- [ ] **Step 1: Create DongMedicineRepositoryTest.java**

```java
package com.dongmedicine.data.repository;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.api.ApiService;
import com.dongmedicine.data.local.InheritorDao;
import com.dongmedicine.data.local.KnowledgeDao;
import com.dongmedicine.data.local.PlantDao;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.model.Plant;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DongMedicineRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private ApiService apiService;
    @Mock private PlantDao plantDao;
    @Mock private InheritorDao inheritorDao;
    @Mock private KnowledgeDao knowledgeDao;
    @Mock private Call<ApiService.ApiResponse<List<Plant>>> plantsCall;
    @Mock private Call<ApiService.ApiResponse<List<Inheritor>>> inheritorsCall;
    @Mock private Call<ApiService.ApiResponse<List<KnowledgeItem>>> knowledgeCall;

    private DongMedicineRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new DongMedicineRepository(apiService, plantDao, inheritorDao, knowledgeDao);
    }

    @Test
    public void getPlants_success_emitsSuccessResource() throws InterruptedException {
        // Arrange
        List<Plant> plants = Arrays.asList(
                createPlant(1, "钩藤"),
                createPlant(2, "透骨草")
        );
        ApiService.ApiResponse<List<Plant>> apiResponse = new ApiService.ApiResponse<>();
        apiResponse.setCode(200);
        apiResponse.setData(plants);

        when(apiService.getPlants()).thenReturn(plantsCall);
        doAnswer(invocation -> {
            Callback<ApiService.ApiResponse<List<Plant>>> callback = invocation.getArgument(0);
            callback.onResponse(plantsCall, Response.success(apiResponse));
            return null;
        }).when(plantsCall).enqueue(any());

        // Act
        Resource<List<Plant>> result = getValue(repository.getPlants());

        // Assert
        assertNotNull(result);
        assertEquals(Resource.Status.SUCCESS, result.getStatus());
        assertEquals(2, result.getData().size());
        assertEquals("钩藤", result.getData().get(0).getName());
    }

    @Test
    public void getPlants_failure_emitsErrorResource() throws InterruptedException {
        // Arrange
        when(apiService.getPlants()).thenReturn(plantsCall);
        doAnswer(invocation -> {
            Callback<ApiService.ApiResponse<List<Plant>>> callback = invocation.getArgument(0);
            callback.onFailure(plantsCall, new RuntimeException("Connection failed"));
            return null;
        }).when(plantsCall).enqueue(any());

        // Act
        Resource<List<Plant>> result = getValue(repository.getPlants());

        // Assert
        assertNotNull(result);
        assertEquals(Resource.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage().contains("网络错误"));
    }

    @Test
    public void getPlants_httpError_emitsErrorResource() throws InterruptedException {
        // Arrange
        ApiService.ApiResponse<List<Plant>> apiResponse = new ApiService.ApiResponse<>();
        apiResponse.setCode(500);

        when(apiService.getPlants()).thenReturn(plantsCall);
        doAnswer(invocation -> {
            Callback<ApiService.ApiResponse<List<Plant>>> callback = invocation.getArgument(0);
            callback.onResponse(plantsCall, Response.success(apiResponse));
            return null;
        }).when(plantsCall).enqueue(any());

        // Act
        Resource<List<Plant>> result = getValue(repository.getPlants());

        // Assert
        assertNotNull(result);
        assertEquals(Resource.Status.ERROR, result.getStatus());
    }

    // Helper: extract value from LiveData synchronously (via InstantTaskExecutorRule)
    @SuppressWarnings("unchecked")
    private <T> T getValue(MutableLiveData<T> liveData) throws InterruptedException {
        // Wait briefly for async postValue to complete
        Thread.sleep(100);
        return liveData.getValue();
    }

    // Overload for LiveData wrapper
    private <T> T getValue(androidx.lifecycle.LiveData<T> liveData) throws InterruptedException {
        Thread.sleep(100);
        return liveData.getValue();
    }

    private Plant createPlant(int id, String name) {
        Plant plant = new Plant();
        plant.setId(id);
        plant.setName(name);
        return plant;
    }
}
```

Note: The `getValue()` helper with `Thread.sleep(100)` handles the async `postValue()` calls in the Repository. `InstantTaskExecutorRule` handles the `setValue()` calls on the main thread.

- [ ] **Step 2: Run the test**

Run: `./gradlew test --tests "com.dongmedicine.data.repository.DongMedicineRepositoryTest" -v`
Expected: 3 tests PASSED

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/dongmedicine/data/repository/DongMedicineRepositoryTest.java
git commit -m "test: add DongMedicineRepository unit tests (success, network error, HTTP error)"
```

---

### Task 3: Write PlantsViewModelTest

**Files:**
- Create: `app/src/test/java/com/dongmedicine/ui/plants/PlantsViewModelTest.java`

- [ ] **Step 1: Create PlantsViewModelTest.java**

```java
package com.dongmedicine.ui.plants;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class PlantsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private DongMedicineRepository repository;

    private PlantsViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Mock repository to return a LiveData with sample data
        MutableLiveData<Resource<List<Plant>>> plantsLiveData = new MutableLiveData<>();
        List<Plant> plants = Arrays.asList(
                createPlant(1, "钩藤", "清热解毒"),
                createPlant(2, "当归", "补益类"),
                createPlant(3, "红花", "活血化瘀")
        );
        plantsLiveData.setValue(Resource.success(plants));
        when(repository.getPlants()).thenReturn(plantsLiveData);

        viewModel = new PlantsViewModel(repository);
    }

    @Test
    public void loadPlants_emitsSuccessResource() {
        Resource<List<Plant>> resource = viewModel.getPlants().getValue();
        assertNotNull(resource);
        assertEquals(Resource.Status.SUCCESS, resource.getStatus());
        assertEquals(3, resource.getData().size());
    }

    @Test
    public void filterByCategory_returnsMatchingPlants() {
        viewModel.setSelectedCategory("清热解毒");
        List<Plant> filtered = viewModel.getFilteredPlants().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("钩藤", filtered.get(0).getName());
    }

    @Test
    public void filterByCategory_all_returnsAllPlants() {
        viewModel.setSelectedCategory("全部");
        List<Plant> filtered = viewModel.getFilteredPlants().getValue();
        assertNotNull(filtered);
        assertEquals(3, filtered.size());
    }

    @Test
    public void filterBySearchQuery_returnsMatchingPlants() {
        viewModel.setSearchQuery("当");
        List<Plant> filtered = viewModel.getFilteredPlants().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("当归", filtered.get(0).getName());
    }

    @Test
    public void filterBySearchAndCategory_returnsIntersection() {
        viewModel.setSelectedCategory("补益类");
        viewModel.setSearchQuery("当");
        List<Plant> filtered = viewModel.getFilteredPlants().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("当归", filtered.get(0).getName());
    }

    @Test
    public void filterBySearchAndCategory_noMatch_returnsEmpty() {
        viewModel.setSelectedCategory("清热解毒");
        viewModel.setSearchQuery("当");
        List<Plant> filtered = viewModel.getFilteredPlants().getValue();
        assertNotNull(filtered);
        assertEquals(0, filtered.size());
    }

    @Test
    public void getCategories_returnsExpectedCategories() {
        String[] categories = viewModel.getCategories();
        assertEquals(6, categories.length);
        assertEquals("全部", categories[0]);
    }

    private Plant createPlant(int id, String name, String category) {
        Plant plant = new Plant();
        plant.setId(id);
        plant.setName(name);
        plant.setCategory(category);
        return plant;
    }
}
```

- [ ] **Step 2: Run the test**

Run: `./gradlew test --tests "com.dongmedicine.ui.plants.PlantsViewModelTest" -v`
Expected: 7 tests PASSED

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/com/dongmedicine/ui/plants/PlantsViewModelTest.java
git commit -m "test: add PlantsViewModel tests (data loading, filtering, search)"
```

---

### Task 4: Write remaining ViewModel tests

**Files:**
- Create: `app/src/test/java/com/dongmedicine/ui/inheritors/InheritorsViewModelTest.java`
- Create: `app/src/test/java/com/dongmedicine/ui/knowledge/KnowledgeViewModelTest.java`
- Create: `app/src/test/java/com/dongmedicine/ui/plants/PlantDetailViewModelTest.java`
- Create: `app/src/test/java/com/dongmedicine/ui/home/HomeViewModelTest.java`

- [ ] **Step 1: Create InheritorsViewModelTest.java**

Same pattern as PlantsViewModelTest but for Inheritor filtering by level:

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

import java.util.Arrays;
import java.util.List;

public class InheritorsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private DongMedicineRepository repository;
    private InheritorsViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        MutableLiveData<Resource<List<Inheritor>>> data = new MutableLiveData<>();
        List<Inheritor> inheritors = Arrays.asList(
                createInheritor(1, "杨秀华", "国家级传承人"),
                createInheritor(2, "吴志明", "省级传承人"),
                createInheritor(3, "李永珍", "市级传承人")
        );
        data.setValue(Resource.success(inheritors));
        when(repository.getInheritors()).thenReturn(data);

        viewModel = new InheritorsViewModel(repository);
    }

    @Test
    public void loadInheritors_emitsSuccessResource() {
        assertNotNull(viewModel.getInheritors().getValue());
        assertEquals(Resource.Status.SUCCESS, viewModel.getInheritors().getValue().getStatus());
    }

    @Test
    public void filterByLevel_national_returnsMatching() {
        viewModel.setSelectedLevel("国家级");
        List<Inheritor> filtered = viewModel.getFilteredInheritors().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("杨秀华", filtered.get(0).getName());
    }

    @Test
    public void filterByLevel_all_returnsAll() {
        viewModel.setSelectedLevel("全部");
        List<Inheritor> filtered = viewModel.getFilteredInheritors().getValue();
        assertNotNull(filtered);
        assertEquals(3, filtered.size());
    }

    private Inheritor createInheritor(int id, String name, String title) {
        Inheritor i = new Inheritor();
        i.setId(id);
        i.setName(name);
        i.setTitle(title);
        return i;
    }
}
```

- [ ] **Step 2: Create KnowledgeViewModelTest.java**

Same pattern for KnowledgeItem filtering by category:

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

import java.util.Arrays;
import java.util.List;

public class KnowledgeViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private DongMedicineRepository repository;
    private KnowledgeViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        MutableLiveData<Resource<List<KnowledgeItem>>> data = new MutableLiveData<>();
        List<KnowledgeItem> items = Arrays.asList(
                createItem(1, "侗医药概述", "基础知识"),
                createItem(2, "侗医诊断", "诊断方法"),
                createItem(3, "侗药炮制", "制药工艺")
        );
        data.setValue(Resource.success(items));
        when(repository.getKnowledgeList()).thenReturn(data);

        viewModel = new KnowledgeViewModel(repository);
    }

    @Test
    public void loadKnowledge_emitsSuccessResource() {
        assertNotNull(viewModel.getKnowledgeList().getValue());
        assertEquals(Resource.Status.SUCCESS, viewModel.getKnowledgeList().getValue().getStatus());
    }

    @Test
    public void filterByCategory_basic_returnsMatching() {
        viewModel.setSelectedCategory("基础知识");
        List<KnowledgeItem> filtered = viewModel.getFilteredKnowledge().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("侗医药概述", filtered.get(0).getTitle());
    }

    @Test
    public void filterByCategory_all_returnsAll() {
        viewModel.setSelectedCategory("全部");
        List<KnowledgeItem> filtered = viewModel.getFilteredKnowledge().getValue();
        assertNotNull(filtered);
        assertEquals(3, filtered.size());
    }

    private KnowledgeItem createItem(int id, String title, String category) {
        KnowledgeItem item = new KnowledgeItem();
        item.setId(id);
        item.setTitle(title);
        item.setCategory(category);
        return item;
    }
}
```

- [ ] **Step 3: Create PlantDetailViewModelTest.java**

```java
package com.dongmedicine.ui.plants;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PlantDetailViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private DongMedicineRepository repository;
    private PlantDetailViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new PlantDetailViewModel(repository);
    }

    @Test
    public void loadPlant_success_emitsPlant() {
        Plant plant = new Plant();
        plant.setId(1);
        plant.setName("钩藤");

        MutableLiveData<Resource<Plant>> data = new MutableLiveData<>();
        data.setValue(Resource.success(plant));
        when(repository.getPlantById(1)).thenReturn(data);

        viewModel.loadPlant(1);
        Resource<Plant> resource = viewModel.getPlant().getValue();

        assertNotNull(resource);
        assertEquals(Resource.Status.SUCCESS, resource.getStatus());
        assertEquals("钩藤", resource.getData().getName());
    }

    @Test
    public void loadPlant_error_emitsErrorResource() {
        MutableLiveData<Resource<Plant>> data = new MutableLiveData<>();
        data.setValue(Resource.error("Not found", null));
        when(repository.getPlantById(999)).thenReturn(data);

        viewModel.loadPlant(999);
        Resource<Plant> resource = viewModel.getPlant().getValue();

        assertNotNull(resource);
        assertEquals(Resource.Status.ERROR, resource.getStatus());
    }
}
```

- [ ] **Step 4: Create HomeViewModelTest.java**

```java
package com.dongmedicine.ui.home;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HomeViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private HomeViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new HomeViewModel();
    }

    @Test
    public void loadStatistics_emitsNonNullData() {
        HomeViewModel.HomeStatistics stats = viewModel.getStatistics().getValue();
        assertNotNull(stats);
    }

    @Test
    public void loadStatistics_hasExpectedValues() {
        HomeViewModel.HomeStatistics stats = viewModel.getStatistics().getValue();
        assertNotNull(stats);
        assertEquals(156, stats.getPlantCount());
        assertEquals(23, stats.getInheritorCount());
        assertEquals(89, stats.getKnowledgeCount());
        assertEquals(1024, stats.getUserCount());
    }
}
```

- [ ] **Step 5: Run all tests**

Run: `./gradlew test -v`
Expected: All tests PASSED (Repository 3 + PlantsVM 7 + InheritorsVM 3 + KnowledgeVM 3 + PlantDetailVM 2 + HomeVM 2 = 20 tests)

- [ ] **Step 6: Commit**

```bash
git add app/src/test/java/com/dongmedicine/
git rm app/src/test/java/com/dongmedicine/ExampleUnitTest.java
git commit -m "test: add unit tests for all ViewModels and Repository (20 tests)"
```

---

### Task 5: Final verification

- [ ] **Step 1: Clean build + full test suite**

```bash
./gradlew clean test
```
Expected: BUILD SUCCESSFUL, all tests PASSED

- [ ] **Step 2: Verify test count**

```bash
./gradlew test 2>&1 | grep -E "tests|PASSED|FAILED"
```
Expected: All tests passed, ~20 tests total

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "chore: Phase 4 complete — unit tests for Repository and all ViewModels"
```
