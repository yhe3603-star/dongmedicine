package com.dongmedicine.ui.qa;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class QaViewModel extends ViewModel {

    private final MutableLiveData<List<QaItem>> qaList;
    private final MutableLiveData<String> currentQuestion;
    private final MutableLiveData<String> currentAnswer;
    private final MutableLiveData<Boolean> isAnswerVisible;

    @Inject
    public QaViewModel() {
        qaList = new MutableLiveData<>();
        currentQuestion = new MutableLiveData<>();
        currentAnswer = new MutableLiveData<>();
        isAnswerVisible = new MutableLiveData<>(false);
        loadSampleData();
    }

    public LiveData<List<QaItem>> getQaList() {
        return qaList;
    }

    public LiveData<String> getCurrentQuestion() {
        return currentQuestion;
    }

    public LiveData<String> getCurrentAnswer() {
        return currentAnswer;
    }

    public LiveData<Boolean> getIsAnswerVisible() {
        return isAnswerVisible;
    }

    public void submitQuestion(String question) {
        currentQuestion.setValue(question);
        isAnswerVisible.setValue(false);

        String answer = generateAnswer(question);
        currentAnswer.setValue(answer);
    }

    public void toggleAnswerVisibility() {
        Boolean visible = isAnswerVisible.getValue();
        isAnswerVisible.setValue(visible == null || !visible);
    }

    private String generateAnswer(String question) {
        if (question.contains("钩藤")) {
            return "钩藤是侗族传统药用植物，具有清热平肝、息风定惊的功效。主要用于治疗头痛眩晕、感冒夹惊、惊痫抽搐等症状。侗族地区常用钩藤配伍其他药物治疗小儿惊风。";
        } else if (question.contains("透骨草")) {
            return "透骨草在侗医药中应用广泛，具有祛风除湿、活血止痛的功效。常用于治疗风湿痹痛、筋骨挛缩、跌打损伤等。侗族民间常用透骨草煎汤外洗治疗关节疼痛。";
        } else if (question.contains("九节茶")) {
            return "九节茶是侗族地区常用的清热解毒药，具有清热解毒、祛风活血的功效。主要用于治疗肺炎、阑尾炎、蜂窝组织炎等炎症性疾病。";
        } else if (question.contains("侗医")) {
            return "侗族传统医药是侗族人民在长期的生产生活中积累的医药知识体系，具有独特的诊断方法和治疗手段。侗医注重整体观念，强调人与自然的和谐统一。";
        } else {
            return "感谢您的提问！侗族传统医药是中华民族医药宝库的重要组成部分，有着悠久的历史和丰富的实践经验。如有具体问题，欢迎继续咨询。";
        }
    }

    private void loadSampleData() {
        List<QaItem> items = new ArrayList<>();
        items.add(new QaItem(1, "钩藤有什么功效？", "清热平肝，息风定惊。用于头痛眩晕，感冒夹惊，惊痫抽搐。", "药用植物"));
        items.add(new QaItem(2, "侗医有哪些诊断方法？", "侗医诊断注重望、闻、问、切四诊合参，同时结合侗族特有的摸骨诊病等方法。", "诊断方法"));
        items.add(new QaItem(3, "透骨草怎么使用？", "可内服煎汤，也可外用煎水熏洗。常用于治疗风湿痹痛、跌打损伤。", "用药指导"));
        qaList.setValue(items);
    }

    public static class QaItem {
        private int id;
        private String question;
        private String answer;
        private String category;

        public QaItem(int id, String question, String answer, String category) {
            this.id = id;
            this.question = question;
            this.answer = answer;
            this.category = category;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }

        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
}
