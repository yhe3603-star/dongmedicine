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
