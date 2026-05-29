package com.dongmedicine.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "knowledge_items")
public class KnowledgeItem {
    @PrimaryKey
    private int id;
    private String title;
    private String content;
    private String category;
    private String publishDate;
    private String author;

    public KnowledgeItem() {}

    public KnowledgeItem(int id, String title, String content, String category, String publishDate, String author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.publishDate = publishDate;
        this.author = author;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
}
