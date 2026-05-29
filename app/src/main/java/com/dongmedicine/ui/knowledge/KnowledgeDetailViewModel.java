package com.dongmedicine.ui.knowledge;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

public class KnowledgeDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private final MutableLiveData<Resource<KnowledgeItem>> knowledgeItem = new MutableLiveData<>();

    public KnowledgeDetailViewModel() {
        repository = DongMedicineRepository.getInstance();
    }

    public LiveData<Resource<KnowledgeItem>> getKnowledgeItem() { return knowledgeItem; }

    public void loadKnowledge(int knowledgeId) {
        repository.getKnowledgeById(knowledgeId, knowledgeItem);
    }
}
