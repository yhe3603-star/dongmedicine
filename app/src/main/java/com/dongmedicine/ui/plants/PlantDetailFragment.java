package com.dongmedicine.ui.plants;

import android.graphics.Color;
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
import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.Resource;
import com.dongmedicine.databinding.FragmentPlantDetailBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PlantDetailFragment extends Fragment {

    private PlantDetailViewModel viewModel;
    private FragmentPlantDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlantDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PlantDetailViewModel.class);

        setupToolbar();
        loadPlantData();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
        binding.collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        binding.collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
    }

    private void loadPlantData() {
        int plantId = PlantDetailFragmentArgs.fromBundle(requireArguments()).getPlantId();
        if (plantId > 0) {
            viewModel.loadPlant(plantId);
        }
    }

    private void observeData() {
        viewModel.getPlant().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.tvError.setVisibility(View.GONE);
                        break;
                    case SUCCESS:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvError.setVisibility(View.GONE);
                        if (resource.getData() != null) {
                            displayPlant(resource.getData());
                        }
                        break;
                    case ERROR:
                        binding.progressBar.setVisibility(View.GONE);
                        binding.tvError.setText(resource.getMessage() != null ?
                                resource.getMessage() : getString(R.string.error_network));
                        binding.tvError.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    private void displayPlant(Plant plant) {
        binding.collapsingToolbar.setTitle(plant.getName());
        binding.plantDetailName.setText(plant.getName());
        binding.plantDetailScientificName.setText(plant.getScientificName());

        if (plant.getNameDong() != null && !plant.getNameDong().isEmpty()) {
            binding.plantDetailNameDong.setVisibility(View.VISIBLE);
            binding.plantDetailNameDong.setText(getString(R.string.dong_name_prefix, plant.getNameDong()));
        } else {
            binding.plantDetailNameDong.setVisibility(View.GONE);
        }

        if (plant.getDescription() != null) {
            binding.plantDetailDescription.setText(plant.getDescription());
        } else {
            binding.plantDetailDescription.setText(getString(R.string.no_description));
        }

        if (plant.getEffects() != null) {
            binding.plantDetailEffects.setText(plant.getEffects());
        } else {
            binding.plantDetailEffects.setText(getString(R.string.no_effects));
        }

        if (plant.getDistribution() != null) {
            binding.plantDetailDistribution.setText(plant.getDistribution());
        } else {
            binding.plantDetailDistribution.setText(getString(R.string.no_distribution));
        }

        Glide.with(this)
                .load(plant.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(binding.plantDetailImage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
