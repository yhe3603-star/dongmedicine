package com.dongmedicine.ui.inheritors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.repository.DongMedicineRepository;
import com.dongmedicine.data.repository.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

public class InheritorsViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private DongMedicineRepository repository;
    private InheritorsViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        MutableLiveData<Resource<List<Inheritor>>> data = new MutableLiveData<>();
        List<Inheritor> inheritors = Arrays.asList(
                createInheritor(1, "杨秀华", "国家级传承人"),
                createInheritor(2, "吴志明", "省级传承人"),
                createInheritor(3, "李永珍", "市级传承人")
        );
        data.setValue(Resource.success(inheritors));
        when(repository.getInheritors()).thenReturn(data);

        viewModel = new InheritorsViewModel(repository);
    }

    @Test
    public void loadInheritors_emitsSuccessResource() {
        assertNotNull(viewModel.getInheritors().getValue());
        assertEquals(Resource.Status.SUCCESS, viewModel.getInheritors().getValue().getStatus());
    }

    @Test
    public void filterByLevel_national_returnsMatching() {
        viewModel.setSelectedLevel("国家级");
        List<Inheritor> filtered = viewModel.getFilteredInheritors().getValue();
        assertNotNull(filtered);
        assertEquals(1, filtered.size());
        assertEquals("杨秀华", filtered.get(0).getName());
    }

    @Test
    public void filterByLevel_all_returnsAll() {
        viewModel.setSelectedLevel("全部");
        List<Inheritor> filtered = viewModel.getFilteredInheritors().getValue();
        assertNotNull(filtered);
        assertEquals(3, filtered.size());
    }

    private Inheritor createInheritor(int id, String name, String title) {
        Inheritor i = new Inheritor();
        i.setId(id);
        i.setName(name);
        i.setTitle(title);
        return i;
    }
}
