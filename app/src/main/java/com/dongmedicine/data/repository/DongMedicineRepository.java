package com.dongmedicine.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.api.ApiClient;
import com.dongmedicine.data.api.ApiService;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.model.Plant;

import java.util.ArrayList;
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

    public LiveData<Resource<List<Plant>>> getPlants() {
        MutableLiveData<Resource<List<Plant>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getPlants().enqueue(new Callback<ApiService.ApiResponse<List<Plant>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<Plant>>> call, Response<ApiService.ApiResponse<List<Plant>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("加载失败", null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<Plant>>> call, Throwable t) {
                result.setValue(Resource.error("网络错误: " + t.getMessage(), getSamplePlants()));
            }
        });

        return result;
    }

    public LiveData<Resource<Plant>> getPlantById(int id) {
        MutableLiveData<Resource<Plant>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getPlantById(id).enqueue(new Callback<ApiService.ApiResponse<Plant>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<Plant>> call, Response<ApiService.ApiResponse<Plant>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("加载失败", null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<Plant>> call, Throwable t) {
                result.setValue(Resource.error("网络错误: " + t.getMessage(), getSamplePlantById(id)));
            }
        });

        return result;
    }

    public LiveData<Resource<List<Inheritor>>> getInheritors() {
        MutableLiveData<Resource<List<Inheritor>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getInheritors().enqueue(new Callback<ApiService.ApiResponse<List<Inheritor>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<Inheritor>>> call, Response<ApiService.ApiResponse<List<Inheritor>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("加载失败", null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<Inheritor>>> call, Throwable t) {
                result.setValue(Resource.error("网络错误: " + t.getMessage(), getSampleInheritors()));
            }
        });

        return result;
    }

    public LiveData<Resource<List<KnowledgeItem>>> getKnowledgeList() {
        MutableLiveData<Resource<List<KnowledgeItem>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading(null));

        apiService.getKnowledgeList().enqueue(new Callback<ApiService.ApiResponse<List<KnowledgeItem>>>() {
            @Override
            public void onResponse(Call<ApiService.ApiResponse<List<KnowledgeItem>>> call, Response<ApiService.ApiResponse<List<KnowledgeItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    result.setValue(Resource.success(response.body().getData()));
                } else {
                    result.setValue(Resource.error("加载失败", null));
                }
            }

            @Override
            public void onFailure(Call<ApiService.ApiResponse<List<KnowledgeItem>>> call, Throwable t) {
                result.setValue(Resource.error("网络错误: " + t.getMessage(), getSampleKnowledge()));
            }
        });

        return result;
    }

    private List<Plant> getSamplePlants() {
        List<Plant> plants = new ArrayList<>();
        plants.add(new Plant(1, "钩藤", "Uncaria rhynchophylla", "清热平肝，息风定惊。用于头痛眩晕，感冒夹惊，惊痫抽搐。", "https://img.alicdn.com/imgextra/i4/2200724907121/O1CN01FJwvKB1DOGvBvLhFn_!!2200724907121.jpg"));
        plants.add(new Plant(2, "透骨草", "Phryma leptostachya", "祛风除湿，活血止痛。用于风湿痹痛，筋骨挛缩，跌打损伤。", "https://img.alicdn.com/imgextra/i4/2200724907121/O1CN01FJwvKB1DOGvBvLhFn_!!2200724907121.jpg"));
        plants.add(new Plant(3, "九节茶", "Sarcandra glabra", "清热解毒，祛风活血。用于肺炎，阑尾炎，蜂窝组织炎。", "https://img.alicdn.com/imgextra/i4/2200724907121/O1CN01FJwvKB1DOGvBvLhFn_!!2200724907121.jpg"));
        plants.add(new Plant(4, "金银花", "Lonicera japonica", "清热解毒，疏散风热。用于感冒发热，咽喉肿痛。", "https://img.alicdn.com/imgextra/i4/2200724907121/O1CN01FJwvKB1DOGvBvLhFn_!!2200724907121.jpg"));
        plants.add(new Plant(5, "当归", "Angelica sinensis", "补血活血，调经止痛。用于血虚萎黄，月经不调。", "https://img.alicdn.com/imgextra/i4/2200724907121/O1CN01FJwvKB1DOGvBvLhFn_!!2200724907121.jpg"));
        plants.add(new Plant(6, "人参", "Panax ginseng", "大补元气，复脉固脱。用于体虚欲脱，肢冷脉微。", "https://img.alicdn.com/imgextra/i4/2200724907121/O1CN01FJwvKB1DOGvBvLhFn_!!2200724907121.jpg"));
        return plants;
    }

    private Plant getSamplePlantById(int id) {
        for (Plant plant : getSamplePlants()) {
            if (plant.getId() == id) {
                return plant;
            }
        }
        return null;
    }

    private List<Inheritor> getSampleInheritors() {
        List<Inheritor> inheritors = new ArrayList<>();
        inheritors.add(new Inheritor(1, "杨秀华", "国家级传承人", "侗医药传统诊疗", "侗医药国家级非遗传承人，从事侗医药研究50余年", "https://example.com/inheritor1.jpg"));
        inheritors.add(new Inheritor(2, "吴志明", "省级传承人", "侗药炮制技艺", "省级非遗传承人，擅长侗药传统炮制工艺", "https://example.com/inheritor2.jpg"));
        inheritors.add(new Inheritor(3, "李永珍", "市级传承人", "侗医推拿疗法", "市级非遗传承人，精通侗医推拿按摩技法", "https://example.com/inheritor3.jpg"));
        return inheritors;
    }

    private List<KnowledgeItem> getSampleKnowledge() {
        List<KnowledgeItem> items = new ArrayList<>();
        items.add(new KnowledgeItem(1, "侗医药概述", "侗族传统医药是侗族人民在长期的生产生活中积累的医药知识...", "基础知识", "2024-01-01", "侗医药研究院"));
        items.add(new KnowledgeItem(2, "侗医诊断方法", "侗医诊断注重望、闻、问、切四诊合参...", "诊断方法", "2024-01-02", "侗医药研究院"));
        items.add(new KnowledgeItem(3, "侗药炮制工艺", "侗药炮制是侗族医药的重要组成部分...", "制药工艺", "2024-01-03", "侗医药研究院"));
        items.add(new KnowledgeItem(4, "侗医推拿疗法", "侗医推拿是侗族传统疗法之一...", "治疗方法", "2024-01-04", "侗医药研究院"));
        return items;
    }
}
