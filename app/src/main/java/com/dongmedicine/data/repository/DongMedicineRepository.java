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
                                  MutableLiveData<Resource<T>> liveData) {
        liveData.postValue(Resource.loading(null));
        call.enqueue(new Callback<ApiService.ApiResponse<T>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<T>> call,
                                   Response<ApiService.ApiResponse<T>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    liveData.postValue(Resource.success(response.body().getData()));
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

    public LiveData<Resource<List<Plant>>> getPlants() {
        MutableLiveData<Resource<List<Plant>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getPlants().enqueue(new Callback<ApiService.ApiResponse<List<Plant>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<Plant>>> call,
                                   Response<ApiService.ApiResponse<List<Plant>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Plant> data = response.body().getData();
                    executor.execute(() -> plantDao.insertAll(data));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<Plant>>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Plant>> getPlantById(int id) {
        MutableLiveData<Resource<Plant>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getPlantById(id).enqueue(new Callback<ApiService.ApiResponse<Plant>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Plant>> call,
                                   Response<ApiService.ApiResponse<Plant>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Plant data = response.body().getData();
                    executor.execute(() -> plantDao.insertAll(Collections.singletonList(data)));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Plant>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<List<Inheritor>>> getInheritors() {
        MutableLiveData<Resource<List<Inheritor>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getInheritors().enqueue(new Callback<ApiService.ApiResponse<List<Inheritor>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<Inheritor>>> call,
                                   Response<ApiService.ApiResponse<List<Inheritor>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Inheritor> data = response.body().getData();
                    executor.execute(() -> inheritorDao.insertAll(data));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<Inheritor>>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<Inheritor>> getInheritorById(int id) {
        MutableLiveData<Resource<Inheritor>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getInheritorById(id).enqueue(new Callback<ApiService.ApiResponse<Inheritor>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Inheritor>> call,
                                   Response<ApiService.ApiResponse<Inheritor>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Inheritor data = response.body().getData();
                    executor.execute(() -> inheritorDao.insertAll(Collections.singletonList(data)));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Inheritor>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<List<KnowledgeItem>>> getKnowledgeList() {
        MutableLiveData<Resource<List<KnowledgeItem>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getKnowledgeList().enqueue(new Callback<ApiService.ApiResponse<List<KnowledgeItem>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<KnowledgeItem>>> call,
                                   Response<ApiService.ApiResponse<List<KnowledgeItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<KnowledgeItem> data = response.body().getData();
                    executor.execute(() -> knowledgeDao.insertAll(data));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<KnowledgeItem>>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }

    public LiveData<Resource<KnowledgeItem>> getKnowledgeById(int id) {
        MutableLiveData<Resource<KnowledgeItem>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getKnowledgeById(id).enqueue(new Callback<ApiService.ApiResponse<KnowledgeItem>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<KnowledgeItem>> call,
                                   Response<ApiService.ApiResponse<KnowledgeItem>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    KnowledgeItem data = response.body().getData();
                    executor.execute(() -> knowledgeDao.insertAll(Collections.singletonList(data)));
                    result.postValue(Resource.success(data));
                } else {
                    result.postValue(Resource.error("请求失败: HTTP " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<KnowledgeItem>> call, Throwable t) {
                result.postValue(Resource.error("网络错误: " + t.getMessage(), null));
            }
        });

        return result;
    }
}
