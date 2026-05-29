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
