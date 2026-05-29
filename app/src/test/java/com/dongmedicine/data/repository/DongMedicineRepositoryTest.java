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
    @Mock private Call<ApiService.ApiResponse<List<Inheritor>>> inheritorsCall;
    @Mock private Call<ApiService.ApiResponse<List<KnowledgeItem>>> knowledgeCall;

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

    @Test
    public void getInheritors_success_emitsSuccessResource() throws InterruptedException {
        List<Inheritor> inheritors = Arrays.asList(createInheritor(1, "张三"), createInheritor(2, "李四"));
        ApiService.ApiResponse<List<Inheritor>> apiResponse = new ApiService.ApiResponse<>();
        apiResponse.setCode(200);
        apiResponse.setData(inheritors);

        when(apiService.getInheritors()).thenReturn(inheritorsCall);
        doAnswer(invocation -> {
            Callback<ApiService.ApiResponse<List<Inheritor>>> callback = invocation.getArgument(0);
            callback.onResponse(inheritorsCall, Response.success(apiResponse));
            return null;
        }).when(inheritorsCall).enqueue(any());

        Resource<List<Inheritor>> result = getValue(repository.getInheritors());
        assertNotNull(result);
        assertEquals(Resource.Status.SUCCESS, result.getStatus());
        assertEquals(2, result.getData().size());
    }

    @Test
    public void getKnowledgeList_success_emitsSuccessResource() throws InterruptedException {
        List<KnowledgeItem> items = Arrays.asList(createKnowledgeItem(1, "侗医基础"), createKnowledgeItem(2, "草药学"));
        ApiService.ApiResponse<List<KnowledgeItem>> apiResponse = new ApiService.ApiResponse<>();
        apiResponse.setCode(200);
        apiResponse.setData(items);

        when(apiService.getKnowledgeList()).thenReturn(knowledgeCall);
        doAnswer(invocation -> {
            Callback<ApiService.ApiResponse<List<KnowledgeItem>>> callback = invocation.getArgument(0);
            callback.onResponse(knowledgeCall, Response.success(apiResponse));
            return null;
        }).when(knowledgeCall).enqueue(any());

        Resource<List<KnowledgeItem>> result = getValue(repository.getKnowledgeList());
        assertNotNull(result);
        assertEquals(Resource.Status.SUCCESS, result.getStatus());
        assertEquals(2, result.getData().size());
    }

    private Inheritor createInheritor(int id, String name) {
        Inheritor inheritor = new Inheritor();
        inheritor.setId(id);
        inheritor.setName(name);
        return inheritor;
    }

    private KnowledgeItem createKnowledgeItem(int id, String title) {
        KnowledgeItem item = new KnowledgeItem();
        item.setId(id);
        item.setTitle(title);
        return item;
    }

    @SuppressWarnings("unchecked")
    private <T> T getValue(androidx.lifecycle.LiveData<T> liveData) throws InterruptedException {
        final Object[] holder = new Object[1];
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        androidx.lifecycle.Observer<T> observer = value -> {
            holder[0] = value;
            latch.countDown();
        };
        liveData.observeForever(observer);
        latch.await(2, java.util.concurrent.TimeUnit.SECONDS);
        liveData.removeObserver(observer);
        return (T) holder[0];
    }

    private Plant createPlant(int id, String name) {
        Plant plant = new Plant();
        plant.setId(id);
        plant.setName(name);
        return plant;
    }
}
