package com.dongmedicine.ui.knowledge;

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
import com.dongmedicine.adapters.KnowledgeAdapter;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.databinding.FragmentKnowledgeBinding;
import com.google.android.material.chip.Chip;

import java.util.List;

public class KnowledgeFragment extends Fragment implements KnowledgeAdapter.OnItemClickListener {

    private KnowledgeViewModel viewModel;
    private FragmentKnowledgeBinding binding;
    private KnowledgeAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentKnowledgeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(KnowledgeViewModel.class);

        setupToolbar();
        setupRecyclerView();
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
        adapter = new KnowledgeAdapter(this);
        binding.recyclerView.setAdapter(adapter);
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
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadKnowledge());
    }

    private void observeData() {
        viewModel.getKnowledgeList().observe(getViewLifecycleOwner(), resource -> {
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

        viewModel.getFilteredKnowledge().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.submitList(items);
                updateEmptyState(items);
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

    private void updateEmptyState(List<KnowledgeItem> items) {
        if (items == null || items.isEmpty()) {
            showEmpty();
        } else {
            showData();
        }
    }

    @Override
    public void onItemClick(KnowledgeItem item) {
        Navigation.findNavController(requireView())
                .navigate(KnowledgeFragmentDirections.actionKnowledgeFragmentToKnowledgeDetailFragment(item.getId()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
