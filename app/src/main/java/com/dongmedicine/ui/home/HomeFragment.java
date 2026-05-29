package com.dongmedicine.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.dongmedicine.R;

public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        setupStatistics(view);
        setupNavigation(view);
    }

    private void setupStatistics(View view) {
        TextView tvPlantCount = view.findViewById(R.id.tv_plant_count);
        TextView tvInheritorCount = view.findViewById(R.id.tv_inheritor_count);
        TextView tvKnowledgeCount = view.findViewById(R.id.tv_knowledge_count);
        TextView tvUserCount = view.findViewById(R.id.tv_user_count);

        viewModel.getStatistics().observe(getViewLifecycleOwner(), statistics -> {
            if (statistics != null) {
                tvPlantCount.setText(String.valueOf(statistics.getPlantCount()));
                tvInheritorCount.setText(String.valueOf(statistics.getInheritorCount()));
                tvKnowledgeCount.setText(String.valueOf(statistics.getKnowledgeCount()));
                tvUserCount.setText(String.valueOf(statistics.getUserCount()));
            }
        });
    }

    private void setupNavigation(View view) {
        NavController navController = Navigation.findNavController(view);

        view.findViewById(R.id.card_plants).setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_plantsFragment));

        view.findViewById(R.id.card_inheritors).setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_inheritorsFragment));

        view.findViewById(R.id.card_knowledge).setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_knowledgeFragment));

        view.findViewById(R.id.card_qa).setOnClickListener(v ->
                navController.navigate(R.id.action_homeFragment_to_qaFragment));
    }
}
