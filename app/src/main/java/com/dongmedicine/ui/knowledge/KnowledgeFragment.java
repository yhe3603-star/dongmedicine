package com.dongmedicine.ui.knowledge;

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
import com.dongmedicine.adapters.KnowledgeAdapter;
import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.Resource;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class KnowledgeFragment extends Fragment implements KnowledgeAdapter.OnItemClickListener {

    private KnowledgeViewModel viewModel;
    private RecyclerView recyclerView;
    private KnowledgeAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private ChipGroup chipGroupCategory;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_knowledge, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(KnowledgeViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupCategoryChips();
        setupSwipeRefresh();
        observeData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        chipGroupCategory = view.findViewById(R.id.chip_group_category);

        view.findViewById(R.id.toolbar).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new KnowledgeAdapter(this);
        recyclerView.setAdapter(adapter);
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
            viewModel.loadKnowledge();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void observeData() {
        viewModel.getKnowledgeList().observe(getViewLifecycleOwner(), resource -> {
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

        viewModel.getFilteredKnowledge().observe(getViewLifecycleOwner(), items -> {
            if (items != null) {
                adapter.submitList(items);
                updateEmptyState(items);
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

    private void showData(List<KnowledgeItem> items) {
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

    private void updateEmptyState(List<KnowledgeItem> items) {
        if (items == null || items.isEmpty()) {
            showEmpty();
        } else {
            showData(items);
        }
    }

    @Override
    public void onItemClick(KnowledgeItem item) {
        Bundle bundle = new Bundle();
        bundle.putInt("knowledgeId", item.getId());
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_knowledgeFragment_to_knowledgeDetailFragment, bundle);
    }
}
