package com.dongmedicine.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.api.ApiService;
import com.dongmedicine.data.local.InheritorDao;
import com.dongmedicine.data.local.KnowledgeDao;
import com.dongmedicine.data.local.PlantDao;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.model.Plant;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class DongMedicineRepository {

    private final ApiService apiService;
    private final PlantDao plantDao;
    private final InheritorDao inheritorDao;
    private final KnowledgeDao knowledgeDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public DongMedicineRepository(ApiService apiService,
                                   PlantDao plantDao,
                                   InheritorDao inheritorDao,
                                   KnowledgeDao knowledgeDao) {
        this.apiService = apiService;
        this.plantDao = plantDao;
        this.inheritorDao = inheritorDao;
        this.knowledgeDao = knowledgeDao;
    }

    private <T> void executeCall(Call<ApiService.ApiResponse<T>> call,
                                  MutableLiveData<Resource<T>> liveData,
                                  CacheAction<T> cacheAction) {
        liveData.postValue(Resource.loading(null));
        call.enqueue(new Callback<ApiService.ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<T>> call,
                                   Response<ApiService.ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    T data = response.body().getData();
                    liveData.postValue(Resource.success(data));
                    if (cacheAction != null && data != null) {
                        executor.execute(() -> {
                            try {
                                cacheAction.cache(data);
                            } catch (Exception e) {
                                // Cache write failed — network data already displayed
                            }
                        });
                    }
                } else {
                    liveData.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<T>> call, Throwable t) {
                liveData.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });
    }

    private interface CacheAction<T> {
        void cache(T data);
    }

    public LiveData<Resource<List<Plant>>> getPlants() {
        MutableLiveData<Resource<List<Plant>>> result = new MutableLiveData<>();
        executeCall(apiService.getPlants(), result, data -> plantDao.insertAll(data));
        return result;
    }

    public LiveData<Resource<Plant>> getPlantById(int id) {
        MutableLiveData<Resource<Plant>> result = new MutableLiveData<>();
        executeCall(apiService.getPlantById(id), result, data ->
                plantDao.insertAll(Collections.singletonList(data)));
        return result;
    }

    public LiveData<Resource<List<Inheritor>>> getInheritors() {
        MutableLiveData<Resource<List<Inheritor>>> result = new MutableLiveData<>();
        executeCall(apiService.getInheritors(), result, data -> inheritorDao.insertAll(data));
        return result;
    }

    public LiveData<Resource<Inheritor>> getInheritorById(int id) {
        MutableLiveData<Resource<Inheritor>> result = new MutableLiveData<>();
        executeCall(apiService.getInheritorById(id), result, data ->
                inheritorDao.insertAll(Collections.singletonList(data)));
        return result;
    }

    public LiveData<Resource<List<KnowledgeItem>>> getKnowledgeList() {
        MutableLiveData<Resource<List<KnowledgeItem>>> result = new MutableLiveData<>();
        executeCall(apiService.getKnowledgeList(), result, data -> knowledgeDao.insertAll(data));
        return result;
    }

    public LiveData<Resource<KnowledgeItem>> getKnowledgeById(int id) {
        MutableLiveData<Resource<KnowledgeItem>> result = new MutableLiveData<>();
        executeCall(apiService.getKnowledgeById(id), result, data ->
                knowledgeDao.insertAll(Collections.singletonList(data)));
        return result;
    }
}
