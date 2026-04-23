package com.example.caloriecalculator.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class FilterPrefs {
    private static final String PREF_NAME = "filter_prefs";
    private static final String KEY_DIETARY = "selected_dietary";
    private static final String KEY_CATEGORY = "selected_category";

    private SharedPreferences prefs;

    public FilterPrefs(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveDietary(String dietary) {
        prefs.edit().putString(KEY_DIETARY, dietary).apply();
    }

    public void saveCategory(String category) {
        prefs.edit().putString(KEY_CATEGORY, category).apply();
    }

    public String getDietary() {
        return prefs.getString(KEY_DIETARY, "All");
    }

    public String getCategory() {
        return prefs.getString(KEY_CATEGORY, "All");
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}