package com.example.caloriecalculator.models;

import java.io.Serializable;
public class FoodItem implements Serializable{
    private long id;
    private int categoryIcon;
    private String name;
    private String dietaryPref;
    private String category;
    private String unit;
    private String servingSize;
    private String calories;
    private String fats;
    private String protein;
    private String carbs;

    public FoodItem() {}

    public FoodItem(String name, String dietaryPref, String category, String unit,
                    String servingSize, String calories, String fats, String protein, String carbs) {
        this.name = name;
        this.dietaryPref = dietaryPref;
        this.category = category;
        this.unit = unit;
        this.servingSize = servingSize;
        this.calories = calories;
        this.fats = fats;
        this.protein = protein;
        this.carbs = carbs;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(int categoryIcon) { this.categoryIcon = categoryIcon; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDietaryPref() { return dietaryPref; }
    public void setDietaryPref(String dietaryPref) { this.dietaryPref = dietaryPref; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getServingSize() { return servingSize; }
    public void setServingSize(String servingSize) { this.servingSize = servingSize; }

    public String getCalories() { return calories; }
    public void setCalories(String calories) { this.calories = calories; }

    public String getFats() { return fats; }
    public void setFats(String fats) { this.fats = fats; }

    public String getProtein() { return protein; }
    public void setProtein(String protein) { this.protein = protein; }

    public String getCarbs() { return carbs; }
    public void setCarbs(String carbs) { this.carbs = carbs; }
}