package com.dongmedicine.ui.plants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import java.util.ArrayList;
import java.util.List;

public class PlantsViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private LiveData<Resource<List<Plant>>> plants;
    private final MutableLiveData<List<Plant>> filteredPlants;
    private final MutableLiveData<String> searchQuery;
    private final MutableLiveData<String> selectedCategory;

    public PlantsViewModel() {
        repository = DongMedicineRepository.getInstance();
        filteredPlants = new MutableLiveData<>();
        searchQuery = new MutableLiveData<>("");
        selectedCategory = new MutableLiveData<>("全部");
        loadPlants();
    }

    public LiveData<Resource<List<Plant>>> getPlants() {
        return plants;
    }

    public LiveData<List<Plant>> getFilteredPlants() {
        return filteredPlants;
    }

    public void loadPlants() {
        plants = repository.getPlants();
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
        applyFilters();
    }

    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
        applyFilters();
    }

    public void applyFilters() {
        if (plants.getValue() != null && plants.getValue().getData() != null) {
            List<Plant> sourceList = plants.getValue().getData();
            List<Plant> result = new ArrayList<>();
            String query = searchQuery.getValue() != null ? searchQuery.getValue().toLowerCase() : "";
            String category = selectedCategory.getValue() != null ? selectedCategory.getValue() : "全部";

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
    }

    public String[] getCategories() {
        return new String[]{"全部", "清热解毒", "补益类", "活血化瘀", "祛风除湿", "其他"};
    }
}
