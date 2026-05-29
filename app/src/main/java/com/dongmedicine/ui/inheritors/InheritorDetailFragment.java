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

import com.bumptech.glide.Glide;
import com.dongmedicine.R;
import com.dongmedicine.data.model.Inheritor;
import com.dongmedicine.data.repository.Resource;
import com.dongmedicine.databinding.FragmentInheritorDetailBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InheritorDetailFragment extends Fragment {

    private InheritorDetailViewModel viewModel;
    private FragmentInheritorDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInheritorDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InheritorDetailViewModel.class);

        setupToolbar();
        loadInheritorData();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
    }

    private void loadInheritorData() {
        int inheritorId = InheritorDetailFragmentArgs.fromBundle(requireArguments()).getInheritorId();
        if (inheritorId > 0) {
            viewModel.loadInheritor(inheritorId);
        }
    }

    private void observeData() {
        viewModel.getInheritor().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        // Could show a loading indicator if one exists in layout
                        break;
                    case SUCCESS:
                        if (resource.getData() != null) {
                            displayInheritor(resource.getData());
                        }
                        break;
                    case ERROR:
                        // Could show error state
                        break;
                }
            }
        });
    }

    private void displayInheritor(Inheritor inheritor) {
        binding.inheritorDetailName.setText(inheritor.getName());
        binding.inheritorDetailTitle.setText(inheritor.getTitle());
        binding.inheritorDetailSpecialization.setText(inheritor.getSpecialization());

        if (inheritor.getIntroduction() != null) {
            binding.inheritorDetailIntroduction.setText(inheritor.getIntroduction());
        } else {
            binding.inheritorDetailIntroduction.setText(getString(R.string.no_introduction));
        }

        Glide.with(this)
                .load(inheritor.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(binding.inheritorDetailImage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
