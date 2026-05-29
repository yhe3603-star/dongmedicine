package com.dongmedicine.ui.qa;

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

import com.dongmedicine.adapters.QaAdapter;
import com.dongmedicine.databinding.FragmentQaBinding;

public class QaFragment extends Fragment {

    private QaViewModel viewModel;
    private FragmentQaBinding binding;
    private QaAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(QaViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        observeData();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp());
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new QaAdapter();
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnSubmit.setOnClickListener(v -> {
            String question = binding.etQuestion.getText().toString().trim();
            if (!question.isEmpty()) {
                viewModel.submitQuestion(question);
            }
        });

        binding.cardAnswer.setOnClickListener(v -> viewModel.toggleAnswerVisibility());
    }

    private void observeData() {
        viewModel.getCurrentAnswer().observe(getViewLifecycleOwner(), answer -> {
            if (answer != null && !answer.isEmpty()) {
                binding.tvAnswer.setText(answer);
                binding.cardAnswer.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsAnswerVisible().observe(getViewLifecycleOwner(), isVisible -> {
            if (isVisible != null) {
                binding.tvAnswer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getQaList().observe(getViewLifecycleOwner(), qaItems -> {
            if (qaItems != null) {
                adapter.submitList(qaItems);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
