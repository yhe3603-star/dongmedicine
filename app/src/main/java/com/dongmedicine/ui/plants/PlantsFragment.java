package com.dongmedicine.ui.plants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dongmedicine.R;
import com.dongmedicine.adapters.PlantAdapter;
import com.dongmedicine.adapters.SpaceItemDecoration;
import com.dongmedicine.data.model.Plant;
import com.dongmedicine.databinding.FragmentPlantsBinding;
import com.google.android.material.chip.Chip;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PlantsFragment extends Fragment implements PlantAdapter.OnItemClickListener {

    private PlantsViewModel viewModel;
    private FragmentPlantsBinding binding;
    private PlantAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PlantsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
        setupSwipeRefresh();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlantAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        int spacing = (int) (8 * getResources().getDisplayMetrics().density);
        binding.recyclerView.addItemDecoration(new SpaceItemDecoration(spacing));
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.setSearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });
    }

    private void setupCategoryChips() {
        String[] categories = viewModel.getCategories();
        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                chip.setChecked(true);
                viewModel.setSelectedCategory(category);
            });
            binding.chipGroupCategory.addView(chip);
        }
        if (binding.chipGroupCategory.getChildCount() > 0) {
            ((Chip) binding.chipGroupCategory.getChildAt(0)).setChecked(true);
        }
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadPlants());
    }

    private void observeData() {
        viewModel.getPlants().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        showLoading();
                        break;
                    case SUCCESS:
                        binding.swipeRefresh.setRefreshing(false);
                        hideLoading();
                        if (resource.getData() != null && !resource.getData().isEmpty()) {
                            showData();
                        } else {
                            showEmpty();
                        }
                        break;
                    case ERROR:
                        binding.swipeRefresh.setRefreshing(false);
                        hideLoading();
                        showError(resource.getMessage());
                        break;
                }
            }
        });

        viewModel.getFilteredPlants().observe(getViewLifecycleOwner(), plants -> {
            if (plants != null) {
                adapter.submitList(plants);
                updateEmptyState(plants);
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }

    private void showData() {
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.tvEmpty.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        binding.tvEmpty.setText(message != null ? message : getString(R.string.error_network));
        binding.tvEmpty.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
    }

    private void updateEmptyState(List<Plant> plants) {
        if (plants == null || plants.isEmpty()) {
            showEmpty();
        } else {
            showData();
        }
    }

    @Override
    public void onItemClick(Plant plant) {
        Navigation.findNavController(requireView())
                .navigate(PlantsFragmentDirections.actionPlantsFragmentToPlantDetailFragment(plant.getId()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
