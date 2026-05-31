package com.example.caloriecalculator.helpers;

import com.example.caloriecalculator.R;

public class AppConstants {
    public static final String[] CATEGORIES = {
            "Grains",
            "Milk and Milk Products",
            "Fruit and Fruit Products",
            "Eggs",
            "Meat and Poultry",
            "Vegetables",
            "Seeds And Nuts",
            "Sugar And Sugar Products",
            "Non Alcoholic Beverages",
            "Alcoholic Beverages",
            "Discretionary"
    };

    public static final int[] CATEGORY_ICONS = {
            R.drawable.ic_grains,
            R.drawable.ic_milk,
            R.drawable.ic_fruit,
            R.drawable.ic_egg,
            R.drawable.ic_meat,
            R.drawable.ic_vegetable,
            R.drawable.ic_nuts,
            R.drawable.ic_sugar,
            R.drawable.ic_drink,
            R.drawable.ic_alcohol,
            R.drawable.ic_snack
    };

    public static int getCategoryIcon(String category) {

        for (int i = 0; i < CATEGORIES.length; i++) {
            if (CATEGORIES[i].equalsIgnoreCase(category)) {
                return CATEGORY_ICONS[i];
            }
        }

        return R.drawable.ic_grocery; // fallback icon
    }
}
