package com.dongmedicine.ui.knowledge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.dongmedicine.R;

public class KnowledgeDetailFragment extends Fragment {

    private TextView titleText;
    private TextView categoryText;
    private TextView authorText;
    private TextView dateText;
    private TextView contentText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_knowledge_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar(view);
        loadKnowledgeData();
    }

    private void initViews(View view) {
        titleText = view.findViewById(R.id.knowledge_detail_title);
        categoryText = view.findViewById(R.id.knowledge_detail_category);
        authorText = view.findViewById(R.id.knowledge_detail_author);
        dateText = view.findViewById(R.id.knowledge_detail_date);
        contentText = view.findViewById(R.id.knowledge_detail_content);
    }

    private void setupToolbar(View view) {
        view.findViewById(R.id.toolbar).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());
    }

    private void loadKnowledgeData() {
        Bundle args = getArguments();
        if (args != null) {
            int knowledgeId = args.getInt("knowledgeId", 0);
            loadSampleData(knowledgeId);
        }
    }

    private void loadSampleData(int id) {
        titleText.setText("侗医药概述");
        categoryText.setText("基础知识");
        authorText.setText("侗医药研究院");
        dateText.setText("2024-01-01");
        contentText.setText("侗族传统医药是侗族人民在长期的生产生活中积累的医药知识体系，具有悠久的历史和丰富的实践经验。\n\n侗医药具有以下特点：\n\n1. 独特的诊断方法：侗医诊断注重望、闻、问、切四诊合参，同时结合侗族特有的摸骨诊病等方法。\n\n2. 丰富的药物资源：侗族地区药用植物资源丰富，常用药物达数百种，其中不乏珍稀名贵药材。\n\n3. 多样的治疗方法：包括内服药物、外治疗法、推拿按摩、药浴熏蒸等多种治疗手段。\n\n4. 预防保健理念：侗医药强调\"治未病\"，注重养生保健和疾病预防。\n\n侗医药是中华民族医药宝库的重要组成部分，2008年被列入国家级非物质文化遗产名录。");
    }
}
