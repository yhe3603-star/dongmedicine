package com.dongmedicine.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inheritors")
public class Inheritor {
    @PrimaryKey
    private int id;
    private String name;
    private String title;
    private String specialization;
    private String introduction;
    private String imageUrl;

    public Inheritor() {}

    public Inheritor(int id, String name, String title, String specialization, String introduction, String imageUrl) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.specialization = specialization;
        this.introduction = introduction;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getIntroduction() { return introduction; }
    public void setIntroduction(String introduction) { this.introduction = introduction; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
