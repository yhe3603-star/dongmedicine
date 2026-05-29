package com.dongmedicine.ui.qa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dongmedicine.R;
import com.dongmedicine.adapters.QaAdapter;
import com.google.android.material.button.MaterialButton;

public class QaFragment extends Fragment {

    private QaViewModel viewModel;
    private EditText etQuestion;
    private TextView tvAnswer;
    private CardView cardAnswer;
    private MaterialButton btnSubmit;
    private RecyclerView recyclerView;
    private QaAdapter adapter;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(QaViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeData();
    }

    private void initViews(View view) {
        etQuestion = view.findViewById(R.id.et_question);
        tvAnswer = view.findViewById(R.id.tv_answer);
        cardAnswer = view.findViewById(R.id.card_answer);
        btnSubmit = view.findViewById(R.id.btn_submit);
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);

        view.findViewById(R.id.toolbar).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new QaAdapter();
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            if (!question.isEmpty()) {
                viewModel.submitQuestion(question);
            }
        });

        cardAnswer.setOnClickListener(v -> viewModel.toggleAnswerVisibility());
    }

    private void observeData() {
        viewModel.getCurrentAnswer().observe(getViewLifecycleOwner(), answer -> {
            if (answer != null && !answer.isEmpty()) {
                tvAnswer.setText(answer);
                cardAnswer.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getIsAnswerVisible().observe(getViewLifecycleOwner(), isVisible -> {
            if (isVisible != null) {
                tvAnswer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getQaList().observe(getViewLifecycleOwner(), qaItems -> {
            if (qaItems != null) {
                adapter.submitList(qaItems);
            }
        });
    }
}
