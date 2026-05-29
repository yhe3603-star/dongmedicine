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

public class InheritorDetailViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Mock private DongMedicineRepository repository;
    private InheritorDetailViewModel viewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        viewModel = new InheritorDetailViewModel(repository);
    }

    @Test
    public void loadInheritor_success_emitsSuccessResource() {
        MutableLiveData<Resource<Inheritor>> liveData = new MutableLiveData<>();
        Inheritor inheritor = new Inheritor();
        inheritor.setId(1);
        inheritor.setName("张三");
        liveData.setValue(Resource.success(inheritor));
        when(repository.getInheritorById(1)).thenReturn(liveData);

        viewModel.loadInheritor(1);
        Resource<Inheritor> resource = viewModel.getInheritor().getValue();
        assertNotNull(resource);
        assertEquals(Resource.Status.SUCCESS, resource.getStatus());
        assertEquals("张三", resource.getData().getName());
    }

    @Test
    public void loadInheritor_error_emitsErrorResource() {
        MutableLiveData<Resource<Inheritor>> liveData = new MutableLiveData<>();
        liveData.setValue(Resource.error("网络错误", null));
        when(repository.getInheritorById(1)).thenReturn(liveData);

        viewModel.loadInheritor(1);
        Resource<Inheritor> resource = viewModel.getInheritor().getValue();
        assertNotNull(resource);
        assertEquals(Resource.Status.ERROR, resource.getStatus());
    }
}
