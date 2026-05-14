package com.example.caloriecalculator.helpers;

import android.util.Log;

import com.example.caloriecalculator.models.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class SearchHelper {

    private static final String TAG = "SearchHelper";
    private List<FoodItem> allFoods = new ArrayList<>();

    public void updateFoods(List<FoodItem> foods) {
        allFoods.clear();
        allFoods.addAll(foods);
        Log.d(TAG, "✅ Updated with " + allFoods.size() + " foods");
    }

    public void addFood(FoodItem food) {
        allFoods.add(food);
        Log.d(TAG, "➕ Added: " + food.getName());
    }

    public void removeFood(long id) {
        allFoods.removeIf(item -> item.getId() == id);
        Log.d(TAG, "🗑️ Removed ID: " + id);
    }
    public List<FoodItem> search(String query, String categoryFilter, String dietaryFilter) {
        Log.d(TAG, "🔍 Searching: q='" + query + "', cat='" + categoryFilter + "', diet='" + dietaryFilter + "'");

        List<FoodItem> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        int checked = 0, matched = 0;
        for (FoodItem item : allFoods) {
            checked++;
            String nameLower = item.getName().toLowerCase();
            String category = item.getCategory() != null ? item.getCategory() : "";
            String dietary = item.getDietaryPref() != null ? item.getDietaryPref() : "";

            boolean nameMatch = lowerQuery.isEmpty() || nameLower.contains(lowerQuery);
            boolean categoryMatch = categoryFilter.isEmpty() ||
                    categoryFilter.equalsIgnoreCase("All") ||
                    category.equalsIgnoreCase(categoryFilter);
            boolean dietaryMatch = dietaryFilter.isEmpty() ||
                    dietaryFilter.equalsIgnoreCase("All") ||
                    dietary.equalsIgnoreCase(dietaryFilter);

            if (nameMatch && categoryMatch && dietaryMatch) {
                results.add(item);
                matched++;
                Log.d(TAG, "✅ MATCH: " + item.getName() + " (cat=" + category + ", diet=" + dietary + ")");
            }
        }

        Log.d(TAG, "✅ Found " + matched + "/" + checked + " matches");
        return results;
    }

    public int size() {
        return allFoods.size();
    }

    public void clear() {
        allFoods.clear();
    }
}