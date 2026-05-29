package com.dongmedicine.ui.plants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.dongmedicine.R;
import com.dongmedicine.data.model.Plant;
import com.dongmedicine.data.repository.Resource;

public class PlantDetailFragment extends Fragment {

    private PlantDetailViewModel viewModel;
    private TextView nameText;
    private TextView scientificNameText;
    private TextView nameDongText;
    private TextView descriptionText;
    private TextView effectsText;
    private TextView distributionText;
    private ImageView plantImage;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_plant_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PlantDetailViewModel.class);

        initViews(view);
        setupToolbar(view);
        loadPlantData();
        observeData();
    }

    private void initViews(View view) {
        nameText = view.findViewById(R.id.plant_detail_name);
        scientificNameText = view.findViewById(R.id.plant_detail_scientific_name);
        nameDongText = view.findViewById(R.id.plant_detail_name_dong);
        descriptionText = view.findViewById(R.id.plant_detail_description);
        effectsText = view.findViewById(R.id.plant_detail_effects);
        distributionText = view.findViewById(R.id.plant_detail_distribution);
        plantImage = view.findViewById(R.id.plant_detail_image);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupToolbar(View view) {
        view.findViewById(R.id.toolbar).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());
    }

    private void loadPlantData() {
        Bundle args = getArguments();
        if (args != null) {
            int plantId = args.getInt("plantId", 0);
            if (plantId > 0) {
                viewModel.loadPlant(plantId);
            }
        }
    }

    private void observeData() {
        viewModel.getPlant().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.getStatus()) {
                    case LOADING:
                        showLoading();
                        break;
                    case SUCCESS:
                        hideLoading();
                        if (resource.getData() != null) {
                            displayPlant(resource.getData());
                        }
                        break;
                    case ERROR:
                        hideLoading();
                        break;
                }
            }
        });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void displayPlant(Plant plant) {
        nameText.setText(plant.getName());
        scientificNameText.setText(plant.getScientificName());

        if (plant.getNameDong() != null && !plant.getNameDong().isEmpty()) {
            nameDongText.setVisibility(View.VISIBLE);
            nameDongText.setText("侗语名称: " + plant.getNameDong());
        } else {
            nameDongText.setVisibility(View.GONE);
        }

        if (plant.getDescription() != null) {
            descriptionText.setText(plant.getDescription());
        } else {
            descriptionText.setText("暂无详细描述");
        }

        if (plant.getEffects() != null) {
            effectsText.setText(plant.getEffects());
        } else {
            effectsText.setText("暂无功效信息");
        }

        if (plant.getDistribution() != null) {
            distributionText.setText(plant.getDistribution());
        } else {
            distributionText.setText("暂无分布信息");
        }

        Glide.with(this)
                .load(plant.getImageUrl())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(plantImage);
    }
}
