package com.dongmedicine.ui.plants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dongmedicine.R;
import com.dongmedicine.adapters.PlantAdapter;
import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.Resource;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class PlantsFragment extends Fragment implements PlantAdapter.OnItemClickListener {

    private PlantsViewModel viewModel;
    private RecyclerView recyclerView;
    private PlantAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private SearchView searchView;
    private ChipGroup chipGroupCategory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PlantsViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupCategoryChips();
        setupSwipeRefresh();
        observeData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        searchView = view.findViewById(R.id.search_view);
        chipGroupCategory = view.findViewById(R.id.chip_group_category);

        view.findViewById(R.id.toolbar).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PlantAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
            chipGroupCategory.addView(chip);
        }
        if (chipGroupCategory.getChildCount() > 0) {
            ((Chip) chipGroupCategory.getChildAt(0)).setChecked(true);
        }
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.loadPlants();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void observeData() {
        viewModel.getPlants().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        showLoading();
                        break;
                    case SUCCESS:
                        hideLoading();
                        if (resource.getData() != null && !resource.getData().isEmpty()) {
                            showData(resource.getData());
                        } else {
                            showEmpty();
                        }
                        break;
                    case ERROR:
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
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showData(List<Plant> plants) {
        recyclerView.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        tvEmpty.setText(message != null ? message : getString(R.string.error_network));
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void updateEmptyState(List<Plant> plants) {
        if (plants == null || plants.isEmpty()) {
            showEmpty();
        } else {
            showData(plants);
        }
    }

    @Override
    public void onItemClick(Plant plant) {
        Bundle bundle = new Bundle();
        bundle.putInt("plantId", plant.getId());
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_plantsFragment_to_plantDetailFragment, bundle);
    }
}
