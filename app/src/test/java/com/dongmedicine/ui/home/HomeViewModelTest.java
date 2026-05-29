package com.dongmedicine.ui.home;

import static org.junit.Assert.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class HomeViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private HomeViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new HomeViewModel();
    }

    @Test
    public void loadStatistics_emitsNonNullData() {
        HomeViewModel.HomeStatistics stats = viewModel.getStatistics().getValue();
        assertNotNull(stats);
    }

    @Test
    public void loadStatistics_hasExpectedValues() {
        HomeViewModel.HomeStatistics stats = viewModel.getStatistics().getValue();
        assertNotNull(stats);
        assertEquals(156, stats.getPlantCount());
        assertEquals(23, stats.getInheritorCount());
        assertEquals(89, stats.getKnowledgeCount());
        assertEquals(1024, stats.getUserCount());
    }
}
