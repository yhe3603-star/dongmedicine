package com.dongmedicine.ui.knowledge;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class KnowledgeDetailViewModel extends ViewModel {

    private final DongMedicineRepository repository;
    private LiveData<Resource<KnowledgeItem>> knowledgeItem;

    @Inject
    public KnowledgeDetailViewModel(DongMedicineRepository repository) {
        this.repository = repository;
    }

    public LiveData<Resource<KnowledgeItem>> getKnowledgeItem() { return knowledgeItem; }

    public void loadKnowledge(int knowledgeId) {
        knowledgeItem = repository.getKnowledgeById(knowledgeId);
    }
}
