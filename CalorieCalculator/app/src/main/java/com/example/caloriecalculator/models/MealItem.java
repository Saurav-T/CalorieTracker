package com.example.caloriecalculator.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealItem implements Serializable {
    private long id;
    private String mealName;
    private long timestamp;
    private double totalCalories;
    private double totalFats;
    private double totalProtein;
    private double totalCarbs;
    private List<MealFoodItem> foodItems = new ArrayList<>();

    public static class MealFoodItem implements Serializable {
        public FoodItem foodItem;
        public double servingSize;

        public MealFoodItem(FoodItem foodItem, double servingSize) {
            this.foodItem = foodItem;
            this.servingSize = servingSize;
        }
    }

    public MealItem() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getMealName() { return mealName; }
    public void setMealName(String mealName) { this.mealName = mealName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public double getTotalCalories() { return totalCalories; }
    public void setTotalCalories(double totalCalories) { this.totalCalories = totalCalories; }
    public double getTotalFats() { return totalFats; }
    public void setTotalFats(double totalFats) { this.totalFats = totalFats; }
    public double getTotalProtein() { return totalProtein; }
    public void setTotalProtein(double totalProtein) { this.totalProtein = totalProtein; }
    public double getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(double totalCarbs) { this.totalCarbs = totalCarbs; }

    public List<MealFoodItem> getFoodItems() { return foodItems; }

    private List<MealFoodSnapshot> foodSnapshots = new ArrayList<>();

    public List<MealFoodSnapshot> getFoodSnapshots() {
        return foodSnapshots;
    }
    public void setFoodSnapshots(List<MealFoodSnapshot> foodSnapshots) {
        this.foodSnapshots = foodSnapshots;
    }
    public void setFoodItems(List<MealFoodItem> foodItems) { this.foodItems = foodItems; }
    // Add to MealItem.java (or create new class)
    public static class MealFoodSnapshot implements Serializable {
        public long foodId;
        public String foodName;
        public double servingSize;
        public double calories;
        public double fats;
        public double protein;
        public double carbs;

        public MealFoodSnapshot(long foodId, String foodName, double servingSize,
                                double calories, double fats, double protein, double carbs) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.servingSize = servingSize;
            this.calories = calories;
            this.fats = fats;
            this.protein = protein;
            this.carbs = carbs;
        }
    }
}