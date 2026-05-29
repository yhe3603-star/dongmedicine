package com.dongmedicine.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.dongmedicine.R;
import com.dongmedicine.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupStatistics();
        setupNavigation();
    }

    private void setupStatistics() {
        viewModel.getStatistics().observe(getViewLifecycleOwner(), statistics -> {
            if (statistics != null) {
                binding.tvPlantCount.setText(String.valueOf(statistics.getPlantCount()));
                binding.tvInheritorCount.setText(String.valueOf(statistics.getInheritorCount()));
                binding.tvKnowledgeCount.setText(String.valueOf(statistics.getKnowledgeCount()));
                binding.tvUserCount.setText(String.valueOf(statistics.getUserCount()));
            }
        });
    }

    private void setupNavigation() {
        NavController navController = Navigation.findNavController(requireView());

        binding.cardPlants.setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_plantsFragment));

        binding.cardInheritors.setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_inheritorsFragment));

        binding.cardKnowledge.setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_knowledgeFragment));

        binding.cardQa.setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_qaFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
