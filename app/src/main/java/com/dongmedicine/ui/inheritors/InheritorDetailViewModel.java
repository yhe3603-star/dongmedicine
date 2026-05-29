package com.dongmedicine.ui.inheritors;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class InheritorDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private LiveData<Resource<Inheritor>> inheritor;

    @Inject
    public InheritorDetailViewModel(DongMedicineRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<Inheritor>> getInheritor() { return inheritor; }

    public void loadInheritor(int inheritorId) {
        inheritor = repository.getInheritorById(inheritorId);
    }
}
