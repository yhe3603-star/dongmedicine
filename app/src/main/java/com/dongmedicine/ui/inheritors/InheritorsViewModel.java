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
    private LiveData<Resource<List<Inheritor>>> inheritors;
    private final MutableLiveData<List<Inheritor>> filteredInheritors;
    private final MutableLiveData<String> selectedLevel;

    public InheritorsViewModel() {
        repository = DongMedicineRepository.getInstance();
        filteredInheritors = new MutableLiveData<>();
        selectedLevel = new MutableLiveData<>("全部");
        loadInheritors();
    }

    public LiveData<Resource<List<Inheritor>>> getInheritors() {
        return inheritors;
    }

    public LiveData<List<Inheritor>> getFilteredInheritors() {
        return filteredInheritors;
    }

    public void loadInheritors() {
        inheritors = repository.getInheritors();
    }

    public void setSelectedLevel(String level) {
        selectedLevel.setValue(level);
        applyFilters();
    }

    public void applyFilters() {
        if (inheritors.getValue() != null && inheritors.getValue().getData() != null) {
            List<Inheritor> sourceList = inheritors.getValue().getData();
            List<Inheritor> result = new ArrayList<>();
            String level = selectedLevel.getValue() != null ? selectedLevel.getValue() : "全部";

            for (Inheritor inheritor : sourceList) {
                boolean matchesLevel = level.equals("全部") ||
                        (inheritor.getTitle() != null && inheritor.getTitle().contains(level));

                if (matchesLevel) {
                    result.add(inheritor);
                }
            }
            filteredInheritors.setValue(result);
        }
    }

    public String[] getLevels() {
        return new String[]{"全部", "国家级", "省级", "市级", "县级"};
    }
}
