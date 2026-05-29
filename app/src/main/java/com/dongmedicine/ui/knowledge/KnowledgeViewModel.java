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
    private LiveData<Resource<List<KnowledgeItem>>> knowledgeList;
    private final MutableLiveData<List<KnowledgeItem>> filteredKnowledge;
    private final MutableLiveData<String> selectedCategory;

    public KnowledgeViewModel() {
        repository = DongMedicineRepository.getInstance();
        filteredKnowledge = new MutableLiveData<>();
        selectedCategory = new MutableLiveData<>("全部");
        loadKnowledge();
    }

    public LiveData<Resource<List<KnowledgeItem>>> getKnowledgeList() {
        return knowledgeList;
    }

    public LiveData<List<KnowledgeItem>> getFilteredKnowledge() {
        return filteredKnowledge;
    }

    public void loadKnowledge() {
        knowledgeList = repository.getKnowledgeList();
    }

    public void setSelectedCategory(String category) {
        selectedCategory.setValue(category);
        applyFilters();
    }

    public void applyFilters() {
        if (knowledgeList.getValue() != null && knowledgeList.getValue().getData() != null) {
            List<KnowledgeItem> sourceList = knowledgeList.getValue().getData();
            List<KnowledgeItem> result = new ArrayList<>();
            String category = selectedCategory.getValue() != null ? selectedCategory.getValue() : "全部";

            for (KnowledgeItem item : sourceList) {
                boolean matchesCategory = category.equals("全部") ||
                        (item.getCategory() != null && item.getCategory().equals(category));

                if (matchesCategory) {
                    result.add(item);
                }
            }
            filteredKnowledge.setValue(result);
        }
    }

    public String[] getCategories() {
        return new String[]{"全部", "基础知识", "诊断方法", "制药工艺", "治疗方法", "养生保健"};
    }
}
