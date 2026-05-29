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

import java.util.Arrays;
import java.util.List;

public class KnowledgeViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private DongMedicineRepository repository;
    private KnowledgeViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        MutableLiveData<Resource<List<KnowledgeItem>>> data = new MutableLiveData<>();
        List<KnowledgeItem> items = Arrays.asList(
                createItem(1, "侗医药概述", "基础知识"),
                createItem(2, "侗医诊断", "诊断方法"),
                createItem(3, "侗药炮制", "制药工艺")
        );
        data.setValue(Resource.success(items));
        when(repository.getKnowledgeList()).thenReturn(data);

        viewModel = new KnowledgeViewModel(repository);
    }

    @Test
    public void loadKnowledge_emitsSuccessResource() {
        assertNotNull(viewModel.getKnowledgeList().getValue());
        assertEquals(Resource.Status.SUCCESS, viewModel.getKnowledgeList().getValue().getStatus());
    }

    @Test
    public void filterByCategory_basic_returnsMatching() {
        viewModel.setSelectedCategory("基础知识");
        List<KnowledgeItem> filtered = viewModel.getFilteredKnowledge().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("侗医药概述", filtered.get(0).getTitle());
    }

    @Test
    public void filterByCategory_all_returnsAll() {
        viewModel.setSelectedCategory("全部");
        List<KnowledgeItem> filtered = viewModel.getFilteredKnowledge().getValue();
        assertNotNull(filtered);
        assertEquals(3, filtered.size());
    }

    private KnowledgeItem createItem(int id, String title, String category) {
        KnowledgeItem item = new KnowledgeItem();
        item.setId(id);
        item.setTitle(title);
        item.setCategory(category);
        return item;
    }
}
