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

    private DongMedicineRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new DongMedicineRepository(apiService, plantDao, inheritorDao, knowledgeDao);
    }

    @Test
    public void getPlants_success_emitsSuccessResource() throws InterruptedException {
        List<Plant> plants = Arrays.asList(createPlant(1, "钩藤"), createPlant(2, "透骨草"));
        ApiService.ApiResponse<List<Plant>> apiResponse = new ApiService.ApiResponse<>();
        apiResponse.setCode(200);
        apiResponse.setData(plants);

        when(apiService.getPlants()).thenReturn(plantsCall);
        doAnswer(invocation -> {
            Callback<ApiService.ApiResponse<List<Plant>>> callback = invocation.getArgument(0);
            callback.onResponse(plantsCall, Response.success(apiResponse));
            return null;
        }).when(plantsCall).enqueue(any());

        Resource<List<Plant>> result = getValue(repository.getPlants());
        assertNotNull(result);
        assertEquals(Resource.Status.SUCCESS, result.getStatus());
        assertEquals(2, result.getData().size());
        assertEquals("钩藤", result.getData().get(0).getName());
    }

    @Test
    public void getPlants_failure_emitsErrorResource() throws InterruptedException {
        when(apiService.getPlants()).thenReturn(plantsCall);
        doAnswer(invocation -> {
            Callback<ApiService.ApiResponse<List<Plant>>> callback = invocation.getArgument(0);
            callback.onFailure(plantsCall, new RuntimeException("Connection failed"));
            return null;
        }).when(plantsCall).enqueue(any());

        Resource<List<Plant>> result = getValue(repository.getPlants());
        assertNotNull(result);
        assertEquals(Resource.Status.ERROR, result.getStatus());
        assertTrue(result.getMessage().contains("网络错误"));
    }

    @Test
    public void getPlants_httpError_emitsErrorResource() throws InterruptedException {
        ApiService.ApiResponse<List<Plant>> apiResponse = new ApiService.ApiResponse<>();
        apiResponse.setCode(500);

        when(apiService.getPlants()).thenReturn(plantsCall);
        doAnswer(invocation -> {
            Callback<ApiService.ApiResponse<List<Plant>>> callback = invocation.getArgument(0);
            callback.onResponse(plantsCall, Response.success(apiResponse));
            return null;
        }).when(plantsCall).enqueue(any());

        Resource<List<Plant>> result = getValue(repository.getPlants());
        assertNotNull(result);
        assertEquals(Resource.Status.ERROR, result.getStatus());
    }

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
