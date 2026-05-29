package com.dongmedicine.ui.knowledge;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class KnowledgeDetailViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private DongMedicineRepository repository;
    private KnowledgeDetailViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new KnowledgeDetailViewModel(repository);
    }

    @Test
    public void loadKnowledge_success_emitsSuccessResource() {
        MutableLiveData<Resource<KnowledgeItem>> liveData = new MutableLiveData<>();
        KnowledgeItem item = new KnowledgeItem();
        item.setId(1);
        item.setTitle("侗医基础");
        liveData.setValue(Resource.success(item));
        when(repository.getKnowledgeById(1)).thenReturn(liveData);

        viewModel.loadKnowledge(1);
        Resource<KnowledgeItem> resource = viewModel.getKnowledgeItem().getValue();
        assertNotNull(resource);
        assertEquals(Resource.Status.SUCCESS, resource.getStatus());
        assertEquals("侗医基础", resource.getData().getTitle());
    }

    @Test
    public void loadKnowledge_error_emitsErrorResource() {
        MutableLiveData<Resource<KnowledgeItem>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.error("网络错误", null));
        when(repository.getKnowledgeById(1)).thenReturn(liveData);

        viewModel.loadKnowledge(1);
        Resource<KnowledgeItem> resource = viewModel.getKnowledgeItem().getValue();
        assertNotNull(resource);
        assertEquals(Resource.Status.ERROR, resource.getStatus());
    }
}
