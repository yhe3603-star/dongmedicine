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

import com.dongmedicine.data.model.KnowledgeItem;
import com.dongmedicine.data.repository.Resource;
import com.dongmedicine.databinding.FragmentKnowledgeDetailBinding;

public class KnowledgeDetailFragment extends Fragment {

    private KnowledgeDetailViewModel viewModel;
    private FragmentKnowledgeDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentKnowledgeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(KnowledgeDetailViewModel.class);

        setupToolbar();
        loadKnowledgeData();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
    }

    private void loadKnowledgeData() {
        int knowledgeId = KnowledgeDetailFragmentArgs.fromBundle(requireArguments()).getKnowledgeId();
        if (knowledgeId > 0) {
            viewModel.loadKnowledge(knowledgeId);
        }
    }

    private void observeData() {
        viewModel.getKnowledgeItem().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        // Could show a loading indicator if one exists in layout
                        break;
                    case SUCCESS:
                        if (resource.getData() != null) {
                            displayKnowledge(resource.getData());
                        }
                        break;
                    case ERROR:
                        // Could show error state
                        break;
                }
            }
        });
    }

    private void displayKnowledge(KnowledgeItem item) {
        binding.knowledgeDetailTitle.setText(item.getTitle());
        binding.knowledgeDetailCategory.setText(item.getCategory());
        binding.knowledgeDetailAuthor.setText(item.getAuthor());
        binding.knowledgeDetailDate.setText(item.getPublishDate());

        if (item.getContent() != null) {
            binding.knowledgeDetailContent.setText(item.getContent());
        } else {
            binding.knowledgeDetailContent.setText(getString(R.string.no_content));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
