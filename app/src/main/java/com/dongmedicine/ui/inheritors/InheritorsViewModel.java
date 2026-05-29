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

    public LiveData<Resource<List<Inheritor>>> getInheritors() { return inheritors; }
    public LiveData<List<Inheritor>> getFilteredInheritors() { return filteredInheritors; }

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
