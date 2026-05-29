package com.dongmedicine.ui.inheritors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dongmedicine.R;
import com.dongmedicine.adapters.InheritorAdapter;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.repository.Resource;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class InheritorsFragment extends Fragment implements InheritorAdapter.OnItemClickListener {

    private InheritorsViewModel viewModel;
    private RecyclerView recyclerView;
    private InheritorAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private ChipGroup chipGroupLevel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inheritors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InheritorsViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupLevelChips();
        setupSwipeRefresh();
        observeData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        chipGroupLevel = view.findViewById(R.id.chip_group_level);

        view.findViewById(R.id.toolbar).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InheritorAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupLevelChips() {
        String[] levels = viewModel.getLevels();
        for (String level : levels) {
            Chip chip = new Chip(requireContext());
            chip.setText(level);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                chip.setChecked(true);
                viewModel.setSelectedLevel(level);
            });
            chipGroupLevel.addView(chip);
        }
        if (chipGroupLevel.getChildCount() > 0) {
            ((Chip) chipGroupLevel.getChildAt(0)).setChecked(true);
        }
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.loadInheritors();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void observeData() {
        viewModel.getInheritors().observe(getViewLifecycleOwner(), resource -> {
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

        viewModel.getFilteredInheritors().observe(getViewLifecycleOwner(), inheritors -> {
            if (inheritors != null) {
                adapter.submitList(inheritors);
                updateEmptyState(inheritors);
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

    private void showData(List<Inheritor> inheritors) {
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

    private void updateEmptyState(List<Inheritor> inheritors) {
        if (inheritors == null || inheritors.isEmpty()) {
            showEmpty();
        } else {
            showData(inheritors);
        }
    }

    @Override
    public void onItemClick(Inheritor inheritor) {
        Bundle bundle = new Bundle();
        bundle.putInt("inheritorId", inheritor.getId());
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_inheritorsFragment_to_inheritorDetailFragment, bundle);
    }
}
