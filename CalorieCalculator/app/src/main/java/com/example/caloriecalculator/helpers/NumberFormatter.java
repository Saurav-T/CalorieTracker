package com.example.caloriecalculator.helpers;

import java.util.Locale;

public class NumberFormatter {

    public static String formatCalories(double value) {
        if (value < 10000) {
            return String.format(Locale.getDefault(), "%.0f", value);
        } else {
            double inK = value / 1000.0;
            return String.format(Locale.getDefault(), "%.1f", inK) + "k";
        }
    }

    public static String formatMacro(double grams) {
        if (grams < 1000) {
            return String.format(Locale.getDefault(), "%.0f", grams);
        } else {
            double inKg = grams / 1000.0;
            return String.format(Locale.getDefault(), "%.1f", inKg) + "k";
        }
    }

    public static String formatServingSize(double value) {
        if (value < 1000) {
            return String.format(Locale.getDefault(), "%.0f", value);
        } else {
            double inKg = value / 1000.0;
            return String.format(Locale.getDefault(), "%.1f", inKg) + "k";
        }
    }
}