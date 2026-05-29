package com.dongmedicine.ui.inheritors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.dongmedicine.R;

public class InheritorDetailFragment extends Fragment {

    private TextView nameText;
    private TextView titleText;
    private TextView specializationText;
    private TextView introductionText;
    private ImageView inheritorImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inheritor_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupToolbar(view);
        loadInheritorData();
    }

    private void initViews(View view) {
        nameText = view.findViewById(R.id.inheritor_detail_name);
        titleText = view.findViewById(R.id.inheritor_detail_title);
        specializationText = view.findViewById(R.id.inheritor_detail_specialization);
        introductionText = view.findViewById(R.id.inheritor_detail_introduction);
        inheritorImage = view.findViewById(R.id.inheritor_detail_image);
    }

    private void setupToolbar(View view) {
        view.findViewById(R.id.toolbar).setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp());
    }

    private void loadInheritorData() {
        Bundle args = getArguments();
        if (args != null) {
            int inheritorId = args.getInt("inheritorId", 0);
            loadSampleData(inheritorId);
        }
    }

    private void loadSampleData(int id) {
        nameText.setText("杨秀华");
        titleText.setText("国家级非遗传承人");
        specializationText.setText("侗医药传统诊疗");
        introductionText.setText("杨秀华，侗族，国家级非物质文化遗产项目侗医药代表性传承人。从事侗医药研究和临床工作50余年，精通侗医诊断方法和传统疗法，培养了大批侗医药传承人才。曾获得多项省级、国家级荣誉，为侗族医药文化的传承和发展做出了重要贡献。");

        Glide.with(this)
                .load("https://example.com/inheritor1.jpg")
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(inheritorImage);
    }
}
