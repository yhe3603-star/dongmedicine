package com.dongmedicine.ui.home;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.dongmedicine.R;
import com.dongmedicine.databinding.FragmentHomeBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private HomeViewModel viewModel;
    private FragmentHomeBinding binding;
    private boolean statsAnimated = false;

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
        animateNavCardsEntrance();
    }

    private void setupStatistics() {
        viewModel.getStatistics().observe(getViewLifecycleOwner(), statistics -> {
            if (statistics != null) {
                binding.tvPlantCount.setText(String.valueOf(statistics.getPlantCount()));
                binding.tvInheritorCount.setText(String.valueOf(statistics.getInheritorCount()));
                binding.tvKnowledgeCount.setText(String.valueOf(statistics.getKnowledgeCount()));
                binding.tvUserCount.setText(String.valueOf(statistics.getUserCount()));

                if (!statsAnimated) {
                    statsAnimated = true;
                    animateStatsOnLoad(statistics);
                }
            }
        });
    }

    private void animateStatsOnLoad(HomeViewModel.HomeStatistics statistics) {
        animateCounter(binding.tvPlantCount, statistics.getPlantCount(), 0);
        animateCounter(binding.tvInheritorCount, statistics.getInheritorCount(), 150);
        animateCounter(binding.tvKnowledgeCount, statistics.getKnowledgeCount(), 300);
        animateCounter(binding.tvUserCount, statistics.getUserCount(), 450);
    }

    private void animateCounter(TextView textView, int targetValue, long delay) {
        textView.postDelayed(() -> {
            ValueAnimator animator = ValueAnimator.ofInt(0, targetValue);
            animator.setDuration(1000);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                int value = (int) animation.getAnimatedValue();
                textView.setText(String.valueOf(value));
            });
            animator.start();
        }, delay);
    }

    private void animateNavCardsEntrance() {
        View[] cards = {
                binding.cardPlants,
                binding.cardInheritors,
                binding.cardKnowledge,
                binding.cardQa
        };

        for (int i = 0; i < cards.length; i++) {
            final View card = cards[i];
            final long delay = i * 100L;

            card.setAlpha(0f);
            card.setTranslationY(50f);
            card.postDelayed(() -> {
                card.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setInterpolator(new OvershootInterpolator(1.0f))
                        .start();
            }, delay);
        }
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
