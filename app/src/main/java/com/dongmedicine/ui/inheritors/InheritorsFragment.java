package com.dongmedicine.ui.inheritors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dongmedicine.R;
import com.dongmedicine.adapters.InheritorAdapter;
import com.dongmedicine.adapters.SpaceItemDecoration;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.databinding.FragmentInheritorsBinding;
import com.google.android.material.chip.Chip;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InheritorsFragment extends Fragment implements InheritorAdapter.OnItemClickListener {

    private InheritorsViewModel viewModel;
    private FragmentInheritorsBinding binding;
    private InheritorAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInheritorsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InheritorsViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupLevelChips();
        setupSwipeRefresh();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InheritorAdapter(this);
        binding.recyclerView.setAdapter(adapter);
        int spacing = (int) (8 * getResources().getDisplayMetrics().density);
        binding.recyclerView.addItemDecoration(new SpaceItemDecoration(spacing));
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
            binding.chipGroupLevel.addView(chip);
        }
        if (binding.chipGroupLevel.getChildCount() > 0) {
            ((Chip) binding.chipGroupLevel.getChildAt(0)).setChecked(true);
        }
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadInheritors());
    }

    private void observeData() {
        viewModel.getInheritors().observe(getViewLifecycleOwner(), resource -> {
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

        viewModel.getFilteredInheritors().observe(getViewLifecycleOwner(), inheritors -> {
            if (inheritors != null) {
                adapter.submitList(inheritors);
                updateEmptyState(inheritors);
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

    private void updateEmptyState(List<Inheritor> inheritors) {
        if (inheritors == null || inheritors.isEmpty()) {
            showEmpty();
        } else {
            showData();
        }
    }

    @Override
    public void onItemClick(Inheritor inheritor) {
        Navigation.findNavController(requireView())
                .navigate(InheritorsFragmentDirections.actionInheritorsFragmentToInheritorDetailFragment(inheritor.getId()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
