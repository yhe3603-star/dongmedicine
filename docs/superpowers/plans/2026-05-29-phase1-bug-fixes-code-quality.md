# Phase 1: Bug Fixes + Code Quality — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix all critical bugs, migrate to ViewBinding + Safe Args, refactor Repository with generic callback, and normalize hardcoded strings/colors.

**Architecture:** Single-module Android app using MVVM (Fragment + ViewModel + LiveData). Phase 1 keeps the existing manual singleton pattern — Hilt/Room are introduced in Phase 2. The Repository gains a generic `executeCall()` helper that all API methods use. ViewModels switch to stable MutableLiveData fields. Fragments migrate from `findViewById` to ViewBinding.

**Tech Stack:** Java 11, Android SDK 35, Retrofit 2.9, OkHttp 4.12, Navigation Component 2.7.7, ViewBinding, Glide 4.16

**Prerequisites:** No git repo exists. Task 1 initializes one.

---

### Task 1: Initialize git repository and commit current state

**Files:**
- (none created — git init only)

- [ ] **Step 1: Initialize git**

```bash
cd /mnt/d/AndroidTool/AndroidProject/dongmedicine
git init
git add -A
git commit -m "chore: initial commit of existing codebase"
```

---

### Task 2: Add Safe Args plugin to build config

**Files:**
- Modify: `build.gradle.kts` (root)
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add Safe Args to version catalog**

Edit `gradle/libs.versions.toml`. Add to `[versions]`:
```toml
safeargs = "2.7.7"
```

Add to `[plugins]`:
```toml
safeargs = { id = "androidx.navigation.safeargs", version.ref = "safeargs" }
```

- [ ] **Step 2: Apply Safe Args plugin in root build.gradle.kts**

Edit `build.gradle.kts` (root) to:
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.safeargs) apply false
}
```

- [ ] **Step 3: Apply Safe Args plugin in app build.gradle.kts**

Edit `app/build.gradle.kts`. Change the plugins block to:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.safeargs)
}
```

- [ ] **Step 4: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL (Safe Args generates direction/args classes from nav_graph.xml)

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts app/build.gradle.kts
git commit -m "build: add Safe Args plugin for type-safe navigation"
```

---

### Task 3: Refactor Repository — generic executeCall + thread safety

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/data/repository/DongMedicineRepository.java`

- [ ] **Step 1: Rewrite DongMedicineRepository.java**

Replace the entire file content with:

```java
package com.dongmedicine.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.api.ApiClient;
import com.dongmedicine.data.api.ApiService;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.model.Plant;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DongMedicineRepository {

    private static DongMedicineRepository instance;
    private final ApiService apiService;

    private DongMedicineRepository() {
        apiService = ApiClient.getApiService();
    }

    public static synchronized DongMedicineRepository getInstance() {
        if (instance == null) {
            instance = new DongMedicineRepository();
        }
        return instance;
    }

    // --- Generic API call handler ---

    private <T> void executeCall(Call<ApiService.ApiResponse<T>> call,
                                  MutableLiveData<Resource<T>> liveData) {
        liveData.postValue(Resource.loading(null));
        call.enqueue(new Callback<ApiService.ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<T>> call,
                                   Response<ApiService.ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    liveData.postValue(Resource.success(response.body().getData()));
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

    // --- Public API methods ---

    public void getPlants(MutableLiveData<Resource<List<Plant>>> liveData) {
        executeCall(apiService.getPlants(), liveData);
    }

    public void getPlantById(int id, MutableLiveData<Resource<Plant>> liveData) {
        executeCall(apiService.getPlantById(id), liveData);
    }

    public void getInheritors(MutableLiveData<Resource<List<Inheritor>>> liveData) {
        executeCall(apiService.getInheritors(), liveData);
    }

    public void getInheritorById(int id, MutableLiveData<Resource<Inheritor>> liveData) {
        executeCall(apiService.getInheritorById(id), liveData);
    }

    public void getKnowledgeList(MutableLiveData<Resource<List<KnowledgeItem>>> liveData) {
        executeCall(apiService.getKnowledgeList(), liveData);
    }

    public void getKnowledgeById(int id, MutableLiveData<Resource<KnowledgeItem>> liveData) {
        executeCall(apiService.getKnowledgeById(id), liveData);
    }
}
```

Key changes:
- All methods now accept `MutableLiveData` as parameter (ViewModel owns the LiveData)
- `executeCall()` uses `postValue()` instead of `setValue()` (thread safety)
- Removed all `getSample*()` methods
- Removed all hardcoded Chinese error strings (will be extracted to strings.xml in Task 9)

- [ ] **Step 2: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: FAIL — ViewModels still call the old `repository.getPlants()` signature. This is expected; we fix them in the next task.

---

### Task 4: Fix all ViewModels — stable LiveData + new Repository API

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/ui/plants/PlantsViewModel.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/plants/PlantDetailViewModel.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/inheritors/InheritorsViewModel.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/knowledge/KnowledgeViewModel.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/home/HomeViewModel.java`

- [ ] **Step 1: Rewrite PlantsViewModel.java**

```java
package com.dongmedicine.ui.plants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.R;
import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import java.util.ArrayList;
import java.util.List;

public class PlantsViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<List<Plant>>> plants = new MutableLiveData<>();
    private final MutableLiveData<List<Plant>> filteredPlants = new MutableLiveData<>();
    private String searchQuery = "";
    private String selectedCategory;

    public PlantsViewModel() {
        repository = DongMedicineRepository.getInstance();
        selectedCategory = "全部";
        loadPlants();
    }

    public LiveData<Resource<List<Plant>>> getPlants() {
        return plants;
    }

    public LiveData<List<Plant>> getFilteredPlants() {
        return filteredPlants;
    }

    public void loadPlants() {
        repository.getPlants(plants);
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
                    plant.getName().toLowerCase().contains(query) ||
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

Key changes:
- `plants` is now `final MutableLiveData` (stable reference, observers not lost)
- `searchQuery` and `selectedCategory` are plain fields (no LiveData overhead for internal state)
- `loadPlants()` updates the existing LiveData via repository

- [ ] **Step 2: Rewrite PlantDetailViewModel.java**

```java
package com.dongmedicine.ui.plants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

public class PlantDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<Plant>> plant = new MutableLiveData<>();

    public PlantDetailViewModel() {
        repository = DongMedicineRepository.getInstance();
    }

    public LiveData<Resource<Plant>> getPlant() {
        return plant;
    }

    public void loadPlant(int plantId) {
        repository.getPlantById(plantId, plant);
    }
}
```

- [ ] **Step 3: Rewrite InheritorsViewModel.java**

```java
package com.dongmedicine.ui.inheritors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import java.util.ArrayList;
import java.util.List;

public class InheritorsViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<List<Inheritor>>> inheritors = new MutableLiveData<>();
    private final MutableLiveData<List<Inheritor>> filteredInheritors = new MutableLiveData<>();
    private String selectedLevel;

    public InheritorsViewModel() {
        repository = DongMedicineRepository.getInstance();
        selectedLevel = "全部";
        loadInheritors();
    }

    public LiveData<Resource<List<Inheritor>>> getInheritors() {
        return inheritors;
    }

    public LiveData<List<Inheritor>> getFilteredInheritors() {
        return filteredInheritors;
    }

    public void loadInheritors() {
        repository.getInheritors(inheritors);
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

- [ ] **Step 4: Rewrite KnowledgeViewModel.java**

```java
package com.dongmedicine.ui.knowledge;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<List<KnowledgeItem>>> knowledgeList = new MutableLiveData<>();
    private final MutableLiveData<List<KnowledgeItem>> filteredKnowledge = new MutableLiveData<>();
    private String selectedCategory;

    public KnowledgeViewModel() {
        repository = DongMedicineRepository.getInstance();
        selectedCategory = "全部";
        loadKnowledge();
    }

    public LiveData<Resource<List<KnowledgeItem>>> getKnowledgeList() {
        return knowledgeList;
    }

    public LiveData<List<KnowledgeItem>> getFilteredKnowledge() {
        return filteredKnowledge;
    }

    public void loadKnowledge() {
        repository.getKnowledgeList(knowledgeList);
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

- [ ] **Step 5: Create InheritorDetailViewModel.java**

New file: `app/src/main/java/com/dongmedicine/ui/inheritors/InheritorDetailViewModel.java`

```java
package com.dongmedicine.ui.inheritors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

public class InheritorDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<Inheritor>> inheritor = new MutableLiveData<>();

    public InheritorDetailViewModel() {
        repository = DongMedicineRepository.getInstance();
    }

    public LiveData<Resource<Inheritor>> getInheritor() {
        return inheritor;
    }

    public void loadInheritor(int inheritorId) {
        repository.getInheritorById(inheritorId, inheritor);
    }
}
```

- [ ] **Step 6: Create KnowledgeDetailViewModel.java**

New file: `app/src/main/java/com/dongmedicine/ui/knowledge/KnowledgeDetailViewModel.java`

```java
package com.dongmedicine.ui.knowledge;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

public class KnowledgeDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<KnowledgeItem>> knowledgeItem = new MutableLiveData<>();

    public KnowledgeDetailViewModel() {
        repository = DongMedicineRepository.getInstance();
    }

    public LiveData<Resource<KnowledgeItem>> getKnowledgeItem() {
        return knowledgeItem;
    }

    public void loadKnowledge(int knowledgeId) {
        repository.getKnowledgeById(knowledgeId, knowledgeItem);
    }
}
```

- [ ] **Step 7: Verify build compiles**

Run: `./gradlew assembleDebug`
Expected: FAIL — Fragments still call old Repository/ViewModel APIs. Fixed in next task.

---

### Task 5: Migrate all Fragments to ViewBinding + Safe Args

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/ui/plants/PlantsFragment.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/plants/PlantDetailFragment.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/inheritors/InheritorsFragment.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/inheritors/InheritorDetailFragment.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/knowledge/KnowledgeFragment.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/knowledge/KnowledgeDetailFragment.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/home/HomeFragment.java`
- Modify: `app/src/main/java/com/dongmedicine/ui/qa/QaFragment.java`

- [ ] **Step 1: Rewrite PlantsFragment.java**

```java
package com.dongmedicine.ui.plants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dongmedicine.R;
import com.dongmedicine.adapters.PlantAdapter;
import com.dongmedicine.data.model.Plant;
import com.dongmedicine.databinding.FragmentPlantsBinding;
import com.google.android.material.chip.Chip;

import java.util.List;

public class PlantsFragment extends Fragment implements PlantAdapter.OnItemClickListener {

    private FragmentPlantsBinding binding;
    private PlantsViewModel viewModel;
    private PlantAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PlantsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
        setupSwipeRefresh();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlantAdapter(this);
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });
    }

    private void setupCategoryChips() {
        String[] categories = viewModel.getCategories();
        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                chip.setChecked(true);
                viewModel.setSelectedCategory(category);
            });
            binding.chipGroupCategory.addView(chip);
        }
        if (binding.chipGroupCategory.getChildCount() > 0) {
            ((Chip) binding.chipGroupCategory.getChildAt(0)).setChecked(true);
        }
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.loadPlants();
        });
    }

    private void observeData() {
        viewModel.getPlants().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        binding.tvEmpty.setVisibility(View.GONE);
                    } else {
                        binding.recyclerView.setVisibility(View.GONE);
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    }
                    // Apply filters after data loads
                    viewModel.applyFilters();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.tvEmpty.setText(resource.getMessage() != null
                            ? resource.getMessage() : getString(R.string.error_network));
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                    break;
            }
        });

        viewModel.getFilteredPlants().observe(getViewLifecycleOwner(), plants -> {
            if (plants != null) {
                adapter.submitList(plants);
                if (plants.isEmpty()) {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onItemClick(Plant plant) {
        Navigation.findNavController(requireView())
                .navigate(PlantsFragmentDirections
                        .actionPlantsFragmentToPlantDetailFragment(plant.getId()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

Key changes: ViewBinding, Safe Args Directions class, `setNavigationOnClickListener()` instead of `setOnClickListener()`, swipeRefresh spinner persists until data loads, `onDestroyView()` cleanup.

- [ ] **Step 2: Rewrite PlantDetailFragment.java**

```java
package com.dongmedicine.ui.plants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.dongmedicine.R;
import com.dongmedicine.data.model.Plant;
import com.dongmedicine.databinding.FragmentPlantDetailBinding;

public class PlantDetailFragment extends Fragment {

    private FragmentPlantDetailBinding binding;
    private PlantDetailViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlantDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PlantDetailViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        int plantId = PlantDetailFragmentArgs.fromBundle(requireArguments()).getPlantId();
        if (plantId > 0) {
            viewModel.loadPlant(plantId);
        }

        observeData();
    }

    private void observeData() {
        viewModel.getPlant().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        displayPlant(resource.getData());
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void displayPlant(Plant plant) {
        binding.plantDetailName.setText(plant.getName());
        binding.plantDetailScientificName.setText(plant.getScientificName());

        if (plant.getNameDong() != null && !plant.getNameDong().isEmpty()) {
            binding.plantDetailNameDong.setVisibility(View.VISIBLE);
            binding.plantDetailNameDong.setText(getString(R.string.dong_name_prefix, plant.getNameDong()));
        } else {
            binding.plantDetailNameDong.setVisibility(View.GONE);
        }

        binding.plantDetailDescription.setText(
                plant.getDescription() != null ? plant.getDescription() : getString(R.string.no_description));
        binding.plantDetailEffects.setText(
                plant.getEffects() != null ? plant.getEffects() : getString(R.string.no_effects));
        binding.plantDetailDistribution.setText(
                plant.getDistribution() != null ? plant.getDistribution() : getString(R.string.no_distribution));

        Glide.with(this)
                .load(plant.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.plantDetailImage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

- [ ] **Step 3: Rewrite InheritorsFragment.java**

Read the current file first, then apply the same ViewBinding + Safe Args + `setNavigationOnClickListener()` pattern. The structure mirrors PlantsFragment but uses `InheritorAdapter`, level chips instead of category chips, and `InheritorsFragmentDirections`.

```java
package com.dongmedicine.ui.inheritors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dongmedicine.R;
import com.dongmedicine.adapters.InheritorAdapter;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.databinding.FragmentInheritorsBinding;
import com.google.android.material.chip.Chip;

import java.util.List;

public class InheritorsFragment extends Fragment implements InheritorAdapter.OnItemClickListener {

    private FragmentInheritorsBinding binding;
    private InheritorsViewModel viewModel;
    private InheritorAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInheritorsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(InheritorsViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InheritorAdapter(this);
        binding.recyclerView.setAdapter(adapter);

        setupLevelChips();
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadInheritors());

        observeData();
    }

    private void setupLevelChips() {
        for (String level : viewModel.getLevels()) {
            Chip chip = new Chip(requireContext());
            chip.setText(level);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                chip.setChecked(true);
                viewModel.setSelectedLevel(level);
            });
            binding.chipGroupCategory.addView(chip);
        }
        if (binding.chipGroupCategory.getChildCount() > 0) {
            ((Chip) binding.chipGroupCategory.getChildAt(0)).setChecked(true);
        }
    }

    private void observeData() {
        viewModel.getInheritors().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        binding.tvEmpty.setVisibility(View.GONE);
                    } else {
                        binding.recyclerView.setVisibility(View.GONE);
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    }
                    viewModel.applyFilters();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.tvEmpty.setText(resource.getMessage() != null
                            ? resource.getMessage() : getString(R.string.error_network));
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                    break;
            }
        });

        viewModel.getFilteredInheritors().observe(getViewLifecycleOwner(), inheritors -> {
            if (inheritors != null) {
                adapter.submitList(inheritors);
                if (inheritors.isEmpty()) {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onItemClick(Inheritor inheritor) {
        Navigation.findNavController(requireView())
                .navigate(InheritorsFragmentDirections
                        .actionInheritorsFragmentToInheritorDetailFragment(inheritor.getId()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

- [ ] **Step 4: Rewrite InheritorDetailFragment.java — use ViewModel + real API call**

```java
package com.dongmedicine.ui.inheritors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.dongmedicine.R;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.databinding.FragmentInheritorDetailBinding;

public class InheritorDetailFragment extends Fragment {

    private FragmentInheritorDetailBinding binding;
    private InheritorDetailViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInheritorDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(InheritorDetailViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        int inheritorId = InheritorDetailFragmentArgs.fromBundle(requireArguments()).getInheritorId();
        if (inheritorId > 0) {
            viewModel.loadInheritor(inheritorId);
        }

        observeData();
    }

    private void observeData() {
        viewModel.getInheritor().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        displayInheritor(resource.getData());
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void displayInheritor(Inheritor inheritor) {
        binding.inheritorDetailName.setText(inheritor.getName());
        binding.inheritorDetailTitle.setText(inheritor.getTitle());
        binding.inheritorDetailSpecialization.setText(inheritor.getSpecialization());
        binding.inheritorDetailIntroduction.setText(inheritor.getIntroduction());

        Glide.with(this)
                .load(inheritor.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.inheritorDetailImage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

- [ ] **Step 5: Rewrite KnowledgeFragment.java**

Same ViewBinding + Safe Args pattern as InheritorsFragment, using `KnowledgeAdapter`, category chips, and `KnowledgeFragmentDirections`.

```java
package com.dongmedicine.ui.knowledge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dongmedicine.R;
import com.dongmedicine.adapters.KnowledgeAdapter;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.databinding.FragmentKnowledgeBinding;
import com.google.android.material.chip.Chip;

import java.util.List;

public class KnowledgeFragment extends Fragment implements KnowledgeAdapter.OnItemClickListener {

    private FragmentKnowledgeBinding binding;
    private KnowledgeViewModel viewModel;
    private KnowledgeAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentKnowledgeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(KnowledgeViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new KnowledgeAdapter(this);
        binding.recyclerView.setAdapter(adapter);

        setupCategoryChips();
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadKnowledge());

        observeData();
    }

    private void setupCategoryChips() {
        for (String category : viewModel.getCategories()) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                chip.setChecked(true);
                viewModel.setSelectedCategory(category);
            });
            binding.chipGroupCategory.addView(chip);
        }
        if (binding.chipGroupCategory.getChildCount() > 0) {
            ((Chip) binding.chipGroupCategory.getChildAt(0)).setChecked(true);
        }
    }

    private void observeData() {
        viewModel.getKnowledgeList().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    if (resource.getData() != null && !resource.getData().isEmpty()) {
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        binding.tvEmpty.setVisibility(View.GONE);
                    } else {
                        binding.recyclerView.setVisibility(View.GONE);
                        binding.tvEmpty.setVisibility(View.VISIBLE);
                    }
                    viewModel.applyFilters();
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    binding.swipeRefresh.setRefreshing(false);
                    binding.tvEmpty.setText(resource.getMessage() != null
                            ? resource.getMessage() : getString(R.string.error_network));
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                    break;
            }
        });

        viewModel.getFilteredKnowledge().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.submitList(items);
                if (items.isEmpty()) {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.tvEmpty.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onItemClick(KnowledgeItem item) {
        Navigation.findNavController(requireView())
                .navigate(KnowledgeFragmentDirections
                        .actionKnowledgeFragmentToKnowledgeDetailFragment(item.getId()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

- [ ] **Step 6: Rewrite KnowledgeDetailFragment.java — use ViewModel + real API call**

```java
package com.dongmedicine.ui.knowledge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.databinding.FragmentKnowledgeDetailBinding;

public class KnowledgeDetailFragment extends Fragment {

    private FragmentKnowledgeDetailBinding binding;
    private KnowledgeDetailViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentKnowledgeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(KnowledgeDetailViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());

        int knowledgeId = KnowledgeDetailFragmentArgs.fromBundle(requireArguments()).getKnowledgeId();
        if (knowledgeId > 0) {
            viewModel.loadKnowledge(knowledgeId);
        }

        observeData();
    }

    private void observeData() {
        viewModel.getKnowledgeItem().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.getStatus()) {
                case LOADING:
                    binding.progressBar.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    binding.progressBar.setVisibility(View.GONE);
                    if (resource.getData() != null) {
                        displayKnowledge(resource.getData());
                    }
                    break;
                case ERROR:
                    binding.progressBar.setVisibility(View.GONE);
                    break;
            }
        });
    }

    private void displayKnowledge(KnowledgeItem item) {
        binding.knowledgeDetailTitle.setText(item.getTitle());
        binding.knowledgeDetailCategory.setText(item.getCategory());
        binding.knowledgeDetailAuthor.setText(item.getAuthor());
        binding.knowledgeDetailDate.setText(item.getPublishDate());
        binding.knowledgeDetailContent.setText(item.getContent());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

- [ ] **Step 7: Read and rewrite HomeFragment.java with ViewBinding**

Read current `HomeFragment.java` first, then apply ViewBinding migration. The HomeFragment is the dashboard — it uses MPAndroidChart. Apply the same pattern: `FragmentHomeBinding`, `setNavigationOnClickListener()`, `onDestroyView()`.

- [ ] **Step 8: Read and rewrite QaFragment.java with ViewBinding**

Read current `QaFragment.java` first, then apply ViewBinding migration.

- [ ] **Step 9: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 10: Commit**

```bash
git add app/src/main/java/com/dongmedicine/
git commit -m "refactor: ViewBinding + Safe Args migration, fix ViewModel LiveData observer loss

- All Fragments migrated from findViewById to ViewBinding
- Navigation uses Safe Args Directions classes instead of raw Bundles
- ViewModels use stable MutableLiveData fields (observers not lost on refresh)
- InheritorDetailFragment and KnowledgeDetailFragment now call real API
- Repository refactored with generic executeCall() helper
- All setValue() changed to postValue() for thread safety
- Removed hardcoded sample data from Repository
- Toolbar uses setNavigationOnClickListener()"
```

---

### Task 6: Fix adapters — DiffUtil, static ViewHolder, final listener

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/adapters/PlantAdapter.java`
- Modify: `app/src/main/java/com/dongmedicine/adapters/InheritorAdapter.java`
- Modify: `app/src/main/java/com/dongmedicine/adapters/KnowledgeAdapter.java`
- Modify: `app/src/main/java/com/dongmedicine/adapters/QaAdapter.java`

- [ ] **Step 1: Rewrite PlantAdapter.java**

```java
package com.dongmedicine.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dongmedicine.R;
import com.dongmedicine.data.model.Plant;

import java.util.Objects;

public class PlantAdapter extends ListAdapter<Plant, PlantAdapter.PlantViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Plant plant);
    }

    public PlantAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<Plant>() {
            @Override
            public boolean areItemsTheSame(@NonNull Plant oldItem, @NonNull Plant newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Plant oldItem, @NonNull Plant newItem) {
                return Objects.equals(oldItem.getName(), newItem.getName())
                        && Objects.equals(oldItem.getScientificName(), newItem.getScientificName())
                        && Objects.equals(oldItem.getDescription(), newItem.getDescription())
                        && Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl())
                        && Objects.equals(oldItem.getEffects(), newItem.getEffects());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        holder.bind(getCurrentList().get(position));
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView scientificNameText;
        private final TextView descriptionText;
        private final ImageView plantImage;

        PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.plant_name);
            scientificNameText = itemView.findViewById(R.id.plant_scientific_name);
            descriptionText = itemView.findViewById(R.id.plant_description);
            plantImage = itemView.findViewById(R.id.plant_image);
        }

        void bind(Plant plant) {
            nameText.setText(plant.getName());
            scientificNameText.setText(plant.getScientificName());

            if (plant.getDescription() != null && !plant.getDescription().isEmpty()) {
                descriptionText.setText(plant.getDescription());
            } else if (plant.getEffects() != null && !plant.getEffects().isEmpty()) {
                descriptionText.setText(plant.getEffects());
            } else {
                descriptionText.setText(itemView.getContext().getString(R.string.no_description));
            }

            if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(plant.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(plantImage);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(plant);
                }
            });
        }
    }
}
```

Wait — the `listener` field is `final` but the static ViewHolder needs access to it. Since ViewHolder is static, it can't access the adapter's instance field. Fix: pass the listener through `onBindViewHolder` or keep ViewHolder non-static. The simplest fix: keep ViewHolder non-static (it's already the pattern in this codebase) but make the field `final`. Actually, let's keep the current non-static pattern but fix the other issues.

Corrected PlantAdapter — keep non-static ViewHolder, fix DiffUtil and make listener final:

```java
package com.dongmedicine.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dongmedicine.R;
import com.dongmedicine.data.model.Plant;

import java.util.Objects;

public class PlantAdapter extends ListAdapter<Plant, PlantAdapter.PlantViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Plant plant);
    }

    public PlantAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<Plant>() {
            @Override
            public boolean areItemsTheSame(@NonNull Plant oldItem, @NonNull Plant newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull Plant oldItem, @NonNull Plant newItem) {
                return Objects.equals(oldItem.getName(), newItem.getName())
                        && Objects.equals(oldItem.getScientificName(), newItem.getScientificName())
                        && Objects.equals(oldItem.getDescription(), newItem.getDescription())
                        && Objects.equals(oldItem.getImageUrl(), newItem.getImageUrl())
                        && Objects.equals(oldItem.getEffects(), newItem.getEffects());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        holder.bind(getCurrentList().get(position));
    }

    class PlantViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView scientificNameText;
        private final TextView descriptionText;
        private final ImageView plantImage;

        PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.plant_name);
            scientificNameText = itemView.findViewById(R.id.plant_scientific_name);
            descriptionText = itemView.findViewById(R.id.plant_description);
            plantImage = itemView.findViewById(R.id.plant_image);
        }

        void bind(Plant plant) {
            nameText.setText(plant.getName());
            scientificNameText.setText(plant.getScientificName());

            if (plant.getDescription() != null && !plant.getDescription().isEmpty()) {
                descriptionText.setText(plant.getDescription());
            } else if (plant.getEffects() != null && !plant.getEffects().isEmpty()) {
                descriptionText.setText(plant.getEffects());
            } else {
                descriptionText.setText(itemView.getContext().getString(R.string.no_description));
            }

            if (plant.getImageUrl() != null && !plant.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(plant.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(plantImage);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(plant);
                }
            });
        }
    }
}
```

- [ ] **Step 2: Apply same fixes to InheritorAdapter.java**

Apply: `final` listener, `Objects.equals()` for `areContentsTheSame` comparing name + title + specialization + imageUrl, `static` ViewHolder fields as `final`.

- [ ] **Step 3: Apply same fixes to KnowledgeAdapter.java**

Apply: `final` listener, `Objects.equals()` for `areContentsTheSame` comparing title + category + content + author + publishDate, `static` ViewHolder fields as `final`.

- [ ] **Step 4: Fix QaAdapter.java DiffUtil**

Apply: `Objects.equals()` for `areContentsTheSame` comparing question + answer + category.

- [ ] **Step 5: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/dongmedicine/adapters/
git commit -m "fix: improve adapter DiffUtil, use Objects.equals for null safety

- areContentsTheSame compares all relevant fields
- Objects.equals prevents NPE in DiffUtil callbacks
- Click listener fields declared final"
```

---

### Task 7: Update nav_graph.xml — add default values, extract labels to strings

**Files:**
- Modify: `app/src/main/res/navigation/nav_graph.xml`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Add missing strings to strings.xml**

```xml
<string name="title_plant_detail">植物详情</string>
<string name="title_inheritor_detail">传承人详情</string>
<string name="title_knowledge_detail">知识详情</string>
```

- [ ] **Step 2: Update nav_graph.xml**

Change hardcoded labels to string references and add default values:

```xml
<argument
    android:name="plantId"
    app:argType="integer"
    android:defaultValue="0" />
```

```xml
<argument
    android:name="inheritorId"
    app:argType="integer"
    android:defaultValue="0" />
```

```xml
<argument
    android:name="knowledgeId"
    app:argType="integer"
    android:defaultValue="0" />
```

Change labels:
```xml
android:label="@string/title_plant_detail"
android:label="@string/title_inheritor_detail"
android:label="@string/title_knowledge_detail"
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/res/navigation/nav_graph.xml app/src/main/res/values/strings.xml
git commit -m "fix: add Safe Args default values, extract nav labels to strings.xml"
```

---

### Task 8: Extract hardcoded strings to strings.xml

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/java/com/dongmedicine/data/repository/DongMedicineRepository.java` (if any remain)
- Modify: all layout XML files with hardcoded `android:text`

- [ ] **Step 1: Add all missing string resources to strings.xml**

Add these entries to `strings.xml`:

```xml
<!-- Error messages -->
<string name="error_request_failed">请求失败: HTTP %d</string>
<string name="error_network_prefix">网络错误: %s</string>

<!-- Detail placeholders -->
<string name="dong_name_prefix">侗语名称: %s</string>
<string name="no_description">暂无详细描述</string>
<string name="no_effects">暂无功效信息</string>
<string name="no_distribution">暂无分布信息</string>

<!-- Plant detail section headers -->
<string name="label_effects">功效作用</string>
<string name="label_description">详细描述</string>
<string name="label_distribution">分布区域</string>

<!-- Inheritor detail section headers -->
<string name="label_specialization">擅长领域</string>
<string name="label_introduction">个人简介</string>

<!-- Default placeholder text for items -->
<string name="placeholder_plant_name">植物名称</string>
<string name="placeholder_scientific_name">学名</string>
<string name="placeholder_effects">功效描述</string>
<string name="placeholder_inheritor_name">传承人姓名</string>
<string name="placeholder_title_level">国家级传承人</string>
<string name="placeholder_specialization">擅长领域</string>
<string name="placeholder_knowledge_title">知识标题</string>
<string name="placeholder_category">分类</string>
<string name="placeholder_author">作者</string>
<string name="placeholder_date">2024-01-01</string>
<string name="placeholder_question">问题</string>
<string name="placeholder_answer">回答</string>

<!-- QA -->
<string name="label_faq">常见问题</string>

<!-- Home stats defaults -->
<string name="default_plant_count">156</string>
<string name="default_inheritor_count">23</string>
<string name="default_knowledge_count">89</string>
<string name="default_user_count">1024</string>

<!-- Accessibility -->
<string name="desc_plant_image">药用植物图片</string>
<string name="desc_inheritor_image">传承人头像</string>
<string name="desc_home_plants">查看药用植物</string>
<string name="desc_home_inheritors">查看传承人</string>
<string name="desc_home_knowledge">查看知识库</string>
<string name="desc_home_qa">进入问答</string>
```

- [ ] **Step 2: Update layout XMLs — replace hardcoded android:text with @string references**

For each layout file that has hardcoded `android:text` values (not `tools:text`), replace with the corresponding `@string/` reference. The `tools:text` values can stay as-is (they're design-time only).

Files to update:
- `fragment_plant_detail.xml` — section headers
- `fragment_inheritor_detail.xml` — section headers + toolbar title
- `fragment_knowledge_detail.xml` — toolbar title
- `fragment_qa.xml` — "常见问题"
- `fragment_home.xml` — default stat numbers
- `item_plant.xml` — placeholder text
- `item_inheritor.xml` — placeholder text
- `item_knowledge.xml` — placeholder text
- `item_qa.xml` — placeholder text

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/
git commit -m "i18n: extract all hardcoded Chinese strings to strings.xml"
```

---

### Task 9: Extract hardcoded colors to colors.xml + fix tint attributes

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `fragment_home.xml`
- Modify: `item_inheritor.xml`
- Modify: `item_knowledge.xml`
- Modify: `drawable/category_tag_bg.xml`

- [ ] **Step 1: Add new color entries to colors.xml**

```xml
<!-- Stats card backgrounds -->
<color name="stats_blue_bg">#E3F2FD</color>
<color name="stats_blue_text">#1565C0</color>
<color name="stats_orange_bg">#FFF3E0</color>
<color name="stats_orange_text">#E65100</color>
<color name="stats_purple_bg">#F3E5F5</color>
<color name="stats_purple_text">#7B1FA2</color>

<!-- Quick entry buttons -->
<color name="button_blue">#1976D2</color>
<color name="button_orange">#F57C00</color>
<color name="button_purple">#7B1FA2</color>

<!-- Category tag -->
<color name="category_tag_bg">#E8F5E9</color>
```

- [ ] **Step 2: Replace hardcoded colors in fragment_home.xml**

Replace all hex color values with `@color/` references:
- `#E3F2FD` → `@color/stats_blue_bg`
- `#1565C0` → `@color/stats_blue_text`
- `#FFF3E0` → `@color/stats_orange_bg`
- `#E65100` → `@color/stats_orange_text`
- `#F3E5F5` → `@color/stats_purple_bg`
- `#7B1FA2` → `@color/stats_purple_text`
- `#1976D2` → `@color/button_blue`
- `#F57C00` → `@color/button_orange`
- `#7B1FA2` → `@color/button_purple`

Also replace `android:tint` with `app:tint` on ImageViews.

- [ ] **Step 3: Fix item layout colors**

In `item_inheritor.xml` and `item_knowledge.xml`, replace:
- `@android:color/black` → `@color/text_primary`
- `@android:color/darker_gray` → `@color/text_secondary`

- [ ] **Step 4: Fix drawable colors**

In `drawable/category_tag_bg.xml`, replace hardcoded `#E8F5E9` with `@color/category_tag_bg`.

- [ ] **Step 5: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/values/colors.xml app/src/main/res/layout/ app/src/main/res/drawable/
git commit -m "style: extract hardcoded colors to colors.xml, fix tint attributes"
```

---

### Task 10: Replace deprecated `<fragment>` with FragmentContainerView

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/java/com/dongmedicine/MainActivity.java`

- [ ] **Step 1: Update activity_main.xml**

Replace the `<fragment>` tag with `<FragmentContainerView>`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <androidx.fragment.app.FragmentContainerView
        android:id="@+id/nav_host_fragment"
        android:name="androidx.navigation.fragment.NavHostFragment"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:defaultNavHost="true"
        app:navGraph="@navigation/nav_graph"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

- [ ] **Step 2: Update MainActivity.java to find NavController via FragmentContainerView**

```java
package com.dongmedicine;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();
    }
}
```

- [ ] **Step 3: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/activity_main.xml app/src/main/java/com/dongmedicine/MainActivity.java
git commit -m "fix: replace deprecated <fragment> with FragmentContainerView"
```

---

### Task 11: Fix ApiClient thread safety + conditional logging

**Files:**
- Modify: `app/src/main/java/com/dongmedicine/data/api/ApiClient.java`

- [ ] **Step 1: Rewrite ApiClient.java**

```java
package com.dongmedicine.data.api;

import com.dongmedicine.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static volatile Retrofit retrofit;
    private static volatile ApiService apiService;

    public static synchronized Retrofit getRetrofit() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(BuildConfig.DEBUG
                    ? HttpLoggingInterceptor.Level.BODY
                    : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService.class);
        }
        return apiService;
    }
}
```

Key changes:
- `synchronized` on both methods
- `volatile` on static fields for double-checked locking safety
- Logging level tied to `BuildConfig.DEBUG`
- Removed `setBaseUrl()` (unused, was a thread-safety hazard)
- `BASE_URL` is now `final`

- [ ] **Step 2: Verify build**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/com/dongmedicine/data/api/ApiClient.java
git commit -m "fix: ApiClient thread safety, conditional logging for debug builds"
```

---

### Task 12: Final verification — clean build + full check

- [ ] **Step 1: Clean build**

```bash
./gradlew clean assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Run unit tests**

```bash
./gradlew test
```
Expected: All tests pass (only default ExampleUnitTest exists)

- [ ] **Step 3: Verify no hardcoded strings remain in Java source**

```bash
grep -rn '"加载失败\|"网络错误\|"暂无\|"侗医药' app/src/main/java/ || echo "No hardcoded strings found"
```
Expected: No hardcoded strings found

- [ ] **Step 4: Final commit if any fixes were needed**

```bash
git add -A
git commit -m "chore: Phase 1 complete — bug fixes, ViewBinding, Safe Args, resource normalization"
```
