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
