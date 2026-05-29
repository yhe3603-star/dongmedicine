package com.dongmedicine.ui.plants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

public class PlantDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private LiveData<Resource<Plant>> plant;

    public PlantDetailViewModel() {
        repository = DongMedicineRepository.getInstance();
    }

    public LiveData<Resource<Plant>> getPlant() {
        return plant;
    }

    public void loadPlant(int plantId) {
        plant = repository.getPlantById(plantId);
    }
}
