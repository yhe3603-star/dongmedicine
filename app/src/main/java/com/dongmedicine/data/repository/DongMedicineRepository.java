package com.dongmedicine.data.repository;

import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.api.ApiClient;
import com.dongmedicine.data.api.ApiService;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.model.Plant;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DongMedicineRepository {

    private static DongMedicineRepository instance;
    private final ApiService apiService;

    private DongMedicineRepository() {
        apiService = ApiClient.getApiService();
    }

    public static synchronized DongMedicineRepository getInstance() {
        if (instance == null) {
            instance = new DongMedicineRepository();
        }
        return instance;
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

    public void getPlants(MutableLiveData<Resource<List<Plant>>> liveData) {
        executeCall(apiService.getPlants(), liveData);
    }

    public void getPlantById(int id, MutableLiveData<Resource<Plant>> liveData) {
        executeCall(apiService.getPlantById(id), liveData);
    }

    public void getInheritors(MutableLiveData<Resource<List<Inheritor>>> liveData) {
        executeCall(apiService.getInheritors(), liveData);
    }

    public void getInheritorById(int id, MutableLiveData<Resource<Inheritor>> liveData) {
        executeCall(apiService.getInheritorById(id), liveData);
    }

    public void getKnowledgeList(MutableLiveData<Resource<List<KnowledgeItem>>> liveData) {
        executeCall(apiService.getKnowledgeList(), liveData);
    }

    public void getKnowledgeById(int id, MutableLiveData<Resource<KnowledgeItem>> liveData) {
        executeCall(apiService.getKnowledgeById(id), liveData);
    }
}
