package com.dongmedicine.data.model;

public class QaItem {
    private int id;
    private String question;
    private String answer;
    private String category;

    public QaItem() {}

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
