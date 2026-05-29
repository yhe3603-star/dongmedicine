package com.dongmedicine.ui.knowledge;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<List<KnowledgeItem>>> knowledgeList = new MutableLiveData<>();
    private final MutableLiveData<List<KnowledgeItem>> filteredKnowledge = new MutableLiveData<>();
    private String selectedCategory;

    public KnowledgeViewModel() {
        repository = DongMedicineRepository.getInstance();
        selectedCategory = "全部";
        loadKnowledge();
    }

    public LiveData<Resource<List<KnowledgeItem>>> getKnowledgeList() { return knowledgeList; }
    public LiveData<List<KnowledgeItem>> getFilteredKnowledge() { return filteredKnowledge; }

    public void loadKnowledge() {
        repository.getKnowledgeList(knowledgeList);
    }

    public void setSelectedCategory(String category) {
        selectedCategory = category;
        applyFilters();
    }

    public void applyFilters() {
        Resource<List<KnowledgeItem>> resource = knowledgeList.getValue();
        if (resource == null || resource.getData() == null) return;

        List<KnowledgeItem> sourceList = resource.getData();
        List<KnowledgeItem> result = new ArrayList<>();
        String category = selectedCategory != null ? selectedCategory : "全部";

        for (KnowledgeItem item : sourceList) {
            boolean matchesCategory = category.equals("全部") ||
                    (item.getCategory() != null && item.getCategory().equals(category));
            if (matchesCategory) {
                result.add(item);
            }
        }
        filteredKnowledge.setValue(result);
    }

    public String[] getCategories() {
        return new String[]{"全部", "基础知识", "诊断方法", "制药工艺", "治疗方法", "养生保健"};
    }
}
