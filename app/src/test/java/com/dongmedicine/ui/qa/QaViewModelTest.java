package com.dongmedicine.ui.qa;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.dongmedicine.data.model.QaItem;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

public class QaViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private QaViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new QaViewModel();
    }

    @Test
    public void submitQuestion_withKeyword_returnsAnswer() {
        viewModel.submitQuestion("钩藤有什么功效？");
        String answer = viewModel.getCurrentAnswer().getValue();
        assertNotNull(answer);
        assertTrue(answer.contains("钩藤"));
    }

    @Test
    public void submitQuestion_withNoKeyword_returnsDefaultAnswer() {
        viewModel.submitQuestion("什么是感冒？");
        String answer = viewModel.getCurrentAnswer().getValue();
        assertNotNull(answer);
        assertTrue(answer.contains("侗医"));
    }

    @Test
    public void loadSampleData_populatesQaList() {
        List<QaItem> items = viewModel.getQaList().getValue();
        assertNotNull(items);
        assertFalse(items.isEmpty());
    }
}
