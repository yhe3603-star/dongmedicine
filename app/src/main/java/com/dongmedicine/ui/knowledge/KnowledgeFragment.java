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
import com.dongmedicine.adapters.SpaceItemDecoration;
import com.dongmedicine.ui.knowledge.KnowledgeDetailFragmentArgs;
import com.dongmedicine.utils.AnimationUtils;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.databinding.FragmentKnowledgeBinding;
import com.google.android.material.chip.Chip;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
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
        int spacing = (int) (8 * getResources().getDisplayMetrics().density);
        binding.recyclerView.addItemDecoration(new SpaceItemDecoration(spacing));
        AnimationUtils.runLayoutAnimation(binding.recyclerView, R.anim.item_fall_down);
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
                binding.recyclerView.scheduleLayoutAnimation();
            }
        });
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }

    private void showData() {
        binding.recyclerView.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        binding.tvError.setVisibility(View.GONE);
    }

    private void showEmpty() {
        binding.recyclerView.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
        binding.tvError.setVisibility(View.GONE);
    }

    private void showError(String message) {
        binding.tvError.setText(message != null ? message : getString(R.string.error_network));
        binding.tvError.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
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
        Bundle args = new KnowledgeDetailFragmentArgs.Builder()
                .setKnowledgeId(item.getId())
                .build()
                .toBundle();
        Navigation.findNavController(requireView())
                .navigate(R.id.action_knowledgeFragment_to_knowledgeDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
