package com.example.caloriecalculator.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.caloriecalculator.models.UserProfile;

public class NutritionCalculator {

    public static class DailyRecommendation {
        public double bmi;
        public String bmiCategory;
        public String healthyWeightRange;
        public int dailyCalories;
        public double proteinG;
        public double carbsG;
        public double fatG;
        public String goal;
    }

    public static class NutritionInsight {
        public int balanceScore;
        public String status;
        public String emoji;
        public String mainIssue;
        public String recommendation;
    }

    public static DailyRecommendation calculate(UserProfile profile) {
        if (profile == null) return null;

        DailyRecommendation rec = new DailyRecommendation();

        double weight = profile.getWeightKg();
        double heightCm = profile.getHeightCm();
        double heightM = heightCm / 100.0;
        int age = profile.getAge();

        // BMI
        rec.bmi = weight / (heightM * heightM);
        rec.bmiCategory = getBmiCategory(rec.bmi);
        double minHealthy = Math.round(18.5 * heightM * heightM);
        double maxHealthy = Math.round(24.9 * heightM * heightM);
        rec.healthyWeightRange = minHealthy + " - " + maxHealthy + " kg";

        // BMR (Mifflin-St Jeor)
        double bmr;
        if ("male".equalsIgnoreCase(profile.getGender())) {
            bmr = 10 * weight + 6.25 * heightCm - 5 * age + 5;
        } else {
            bmr = 10 * weight + 6.25 * heightCm - 5 * age - 161;
        }

        // TDEE
        double multiplier = getActivityMultiplier(profile.getActivityLevel());
        double tdee = bmr * multiplier;

        // Apply Goal
        rec.goal = profile.getGoal();
        double targetCalories = tdee;

        if ("weight loss".equalsIgnoreCase(rec.goal)) targetCalories *= 0.80;
        else if ("weight gain".equalsIgnoreCase(rec.goal)) targetCalories *= 1.10;

        rec.dailyCalories = (int) Math.round(targetCalories);

        // Macros
        double proteinPct = "weight loss".equalsIgnoreCase(rec.goal) ? 0.35 : 0.30;
        double carbPct = "weight gain".equalsIgnoreCase(rec.goal) ? 0.50 : 0.45;
        double fatPct = 1.0 - proteinPct - carbPct;

        rec.proteinG = Math.round((targetCalories * proteinPct) / 4);
        rec.carbsG = Math.round((targetCalories * carbPct) / 4);
        rec.fatG = Math.round((targetCalories * fatPct) / 9);

        return rec;
    }

    private static String getBmiCategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    private static double getActivityMultiplier(String level) {
        if (level == null) return 1.2;
        switch (level.toLowerCase()) {
            case "sedentary": return 1.2;
            case "lightly active": return 1.375;
            case "moderately active": return 1.55;
            case "very active": return 1.725;
            case "extra active": return 1.9;
            default: return 1.2;
        }
    }

    public static void saveProfile(Context context, UserProfile profile) {
        SharedPreferences prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE);
        prefs.edit()
                .putFloat("weight", (float) profile.getWeightKg())
                .putFloat("height", (float) profile.getHeightCm())
                .putInt("age", profile.getAge())
                .putString("gender", profile.getGender())
                .putString("activity_level", profile.getActivityLevel())
                .putString("goal", profile.getGoal())
                .apply();
    }

    public static UserProfile loadProfile(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_profile", Context.MODE_PRIVATE);
        UserProfile profile = new UserProfile();

        profile.setWeightKg(prefs.getFloat("weight", 0));
        profile.setHeightCm(prefs.getFloat("height", 0));
        profile.setAge(prefs.getInt("age", 0));
        profile.setGender(prefs.getString("gender", ""));
        profile.setActivityLevel(prefs.getString("activity_level", ""));
        profile.setGoal(prefs.getString("goal", "maintenance"));

        return profile;
    }

    public static NutritionInsight generateInsight(UserProfile profile,
                                                   double todayCalories,
                                                   double todayProtein,
                                                   double todayCarbs,
                                                   double todayFats) {

        NutritionInsight insight = new NutritionInsight();
        DailyRecommendation rec = calculate(profile);

        if (rec == null || profile == null) {
            insight.balanceScore = 40;
            insight.status = "Profile Incomplete";
            insight.emoji = "⚙️";
            insight.recommendation = "Complete your profile to unlock personalized insights";
            return insight;
        }

        double calScore = rec.dailyCalories > 0 ?
                Math.min((todayCalories / rec.dailyCalories) * 100, 100) : 50;

        double protScore = rec.proteinG > 0 ?
                Math.min((todayProtein / rec.proteinG) * 100, 100) : 50;

        double carbScore = rec.carbsG > 0 ?
                Math.min((todayCarbs / rec.carbsG) * 100, 100) : 50;

        double fatScore = rec.fatG > 0 ?
                Math.min((todayFats / rec.fatG) * 100, 100) : 50;

        insight.balanceScore = (int) (
                calScore * 0.35 +
                        protScore * 0.25 +
                        carbScore * 0.20 +
                        fatScore * 0.20
        );

        if (insight.balanceScore >= 85) {
            insight.status = "Excellent";
            insight.emoji = "🏆";
            insight.recommendation = "Outstanding day! Your nutrition is well balanced.";
        }
        else if (insight.balanceScore >= 70) {
            insight.status = "Good";
            insight.emoji = "👍";
            insight.recommendation = "Good progress. Keep maintaining this consistency.";
        }
        else if (calScore < 40) {
            insight.status = "Needs Attention";
            insight.emoji = "🔥";
            insight.mainIssue = "Low Calories";
            insight.recommendation = "You're significantly under your calorie target. Add a balanced meal.";
        }
        else if (protScore < 50) {
            insight.status = "Needs Attention";
            insight.emoji = "🥩";
            insight.mainIssue = "Low Protein";
            insight.recommendation = "Add protein sources like eggs, chicken, dal, or paneer.";
        }
        else if (carbScore < 45) {
            insight.status = "Needs Attention";
            insight.emoji = "🍚";
            insight.mainIssue = "Low Carbs";
            insight.recommendation = "Include more complex carbs like rice, roti, oats or fruits.";
        }
        else if (fatScore < 45) {
            insight.status = "Needs Attention";
            insight.emoji = "🥑";
            insight.mainIssue = "Low Healthy Fats";
            insight.recommendation = "Add healthy fats from nuts, ghee, avocado or seeds.";
        }
        else if (calScore > 115) {
            insight.status = "Over Target";
            insight.emoji = "⚠️";
            insight.recommendation = "You're exceeding your calorie goal. Consider portion control.";
        }
        else {
            insight.status = "Fair";
            insight.emoji = "📊";
            insight.recommendation = "You're on track. Focus on adding variety in vegetables and whole foods.";
        }

        return insight;
    }
}