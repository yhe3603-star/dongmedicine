package com.dongmedicine.data.api;

import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.model.Plant;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/plants/list")
    Call<ApiResponse<List<Plant>>> getPlants();

    @GET("api/plants/{id}")
    Call<ApiResponse<Plant>> getPlantById(@Path("id") int id);

    @GET("api/plants/list")
    Call<ApiResponse<List<Plant>>> getPlantsByCategory(@Query("category") String category);

    @GET("api/inheritors/list")
    Call<ApiResponse<List<Inheritor>>> getInheritors();

    @GET("api/inheritors/{id}")
    Call<ApiResponse<Inheritor>> getInheritorById(@Path("id") int id);

    @GET("api/knowledge/list")
    Call<ApiResponse<List<KnowledgeItem>>> getKnowledgeList();

    @GET("api/knowledge/{id}")
    Call<ApiResponse<KnowledgeItem>> getKnowledgeById(@Path("id") int id);

    @GET("api/knowledge/list")
    Call<ApiResponse<List<KnowledgeItem>>> getKnowledgeByCategory(@Query("category") String category);

    class ApiResponse<T> {
        private int code;
        private String message;
        private T data;

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        public boolean isSuccess() {
            return code == 200;
        }
    }
}
