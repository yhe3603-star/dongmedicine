package com.dongmedicine.data.model;

public class Plant {
    private int id;
    private String name;
    private String scientificName;
    private String description;
    private String imageUrl;
    private String effects;
    private String distribution;
    private String category;
    private String nameDong;

    public Plant() {}

    public Plant(int id, String name, String scientificName, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.scientificName = scientificName;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScientificName() { return scientificName; }
    public void setScientificName(String scientificName) { this.scientificName = scientificName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getEffects() { return effects; }
    public void setEffects(String effects) { this.effects = effects; }

    public String getDistribution() { return distribution; }
    public void setDistribution(String distribution) { this.distribution = distribution; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getNameDong() { return nameDong; }
    public void setNameDong(String nameDong) { this.nameDong = nameDong; }
}
