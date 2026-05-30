package com.example.caloriecalculator.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.CalorieCalculatorActivity;
import com.example.caloriecalculator.R;
import com.example.caloriecalculator.adapters.RecentMealsAdapter;
import com.example.caloriecalculator.bottomsheets.MealDetailsBottomSheet;
import com.example.caloriecalculator.database.DatabaseHelper;
import com.example.caloriecalculator.helpers.NutritionCalculator;
import com.example.caloriecalculator.models.MealItem;
import com.example.caloriecalculator.models.UserProfile;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private View fragmentView;
    private TextView totalCalorieToday, carbConsumption, proteinConsumption, fatConsumption;
    private TextView todayDate, todayDay, goalCalorie, carbUnit, proteinUnit, fatUnit;
    private TextView insightEmoji, insightScore, insightStatus, insightRecommendation; // ✅ NEW
    private RecyclerView recentMealsRecycler;
    private LinearLayout overlay;

    // Data
    private DatabaseHelper dbHelper;
    private RecentMealsAdapter recentMealsAdapter;
    private String mParam1;
    private String mParam2;

    public HomeFragment() {}

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        getParentFragmentManager().setFragmentResultListener("meal_deleted", this,
                (requestKey, result) -> refreshData());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        bindViews(fragmentView);
        initDatabase();
        setupRecyclerView();
        loadTodayStats();
        setupClickListeners();

        return fragmentView;
    }

    private void bindViews(View view) {

        totalCalorieToday = view.findViewById(R.id.total_calorie_today);
        carbConsumption = view.findViewById(R.id.carb_consumption);
        proteinConsumption = view.findViewById(R.id.protein_consumption);
        fatConsumption = view.findViewById(R.id.fat_consumption);
        carbUnit = view.findViewById(R.id.carb_unit);
        proteinUnit = view.findViewById(R.id.protein_unit);
        fatUnit = view.findViewById(R.id.fat_unit);
        todayDate = view.findViewById(R.id.today_date);
        todayDay = view.findViewById(R.id.today_day);
        goalCalorie = view.findViewById(R.id.goal_calorie);
        recentMealsRecycler = view.findViewById(R.id.recent_meals_recycler);
        insightEmoji = view.findViewById(R.id.insight_emoji);
        insightScore = view.findViewById(R.id.insight_score);
        insightStatus = view.findViewById(R.id.insight_status);
        insightRecommendation = view.findViewById(R.id.insight_recommendation);
        overlay = view.findViewById(R.id.overlay);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(requireContext());
    }

    private void setupRecyclerView() {
        if (recentMealsRecycler == null) return;
        recentMealsRecycler.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false));
        recentMealsAdapter = new RecentMealsAdapter();
        recentMealsRecycler.setAdapter(recentMealsAdapter);
        recentMealsAdapter.setOnMealClickListener(this::showMealDetailsBottomSheet);
        refreshRecentMealsWithOverlay();
    }

    private void showMealDetailsBottomSheet(MealItem meal) {
        MealDetailsBottomSheet bottomSheet = MealDetailsBottomSheet.newInstance(meal.getId());
        bottomSheet.show(getParentFragmentManager(), "MealDetailsBottomSheet");
    }

    private void loadTodayStats() {
        if (totalCalorieToday == null) return;

        List<MealItem> todayMeals = dbHelper.getTodayMeals();
        double totalCal = 0, carbs = 0, protein = 0, fats = 0;

        for (MealItem meal : todayMeals) {
            totalCal += meal.getTotalCalories();
            carbs += meal.getTotalCarbs();
            protein += meal.getTotalProtein();
            fats += meal.getTotalFats();
        }

        totalCalorieToday.setText(formatCalories(totalCal));
        setMacroValue(carbConsumption, carbUnit, carbs);
        setMacroValue(proteinConsumption, proteinUnit, protein);
        setMacroValue(fatConsumption, fatUnit, fats);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        todayDate.setText(dateFormat.format(new Date()));
        if (todayDay != null) todayDay.setText(dayFormat.format(new Date()));

        loadDailyGoalAndInsights(totalCal, carbs, protein, fats);
    }

    private void loadDailyGoalAndInsights(double todayCal, double carbs, double protein, double fats) {
        loadDailyGoalFromPrefs();
        UserProfile profile = NutritionCalculator.loadProfile(requireContext());
        NutritionCalculator.NutritionInsight insight = NutritionCalculator.generateInsight(
                profile, todayCal, protein, carbs, fats);
        if (insightEmoji != null) insightEmoji.setText(insight.emoji);
        if (insightScore != null) insightScore.setText(insight.balanceScore + "/100");
        if (insightStatus != null) insightStatus.setText(insight.status);
        if (insightRecommendation != null) insightRecommendation.setText(insight.recommendation);
    }


    private void loadDailyGoalFromPrefs() {
        if (goalCalorie == null) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE);

        String profileGoalStr = prefs.getString("daily_calorie_goal_profile", null);
        if (profileGoalStr != null && !profileGoalStr.isEmpty()) {
            try {
                double profileGoal = Double.parseDouble(profileGoalStr);
                goalCalorie.setText(String.format(Locale.getDefault(), "%.0f", profileGoal));
                return;
            } catch (NumberFormatException ignored) {}
        }

        String manualGoalStr = prefs.getString("daily_calorie_goal", null);
        if (manualGoalStr != null && !manualGoalStr.isEmpty()) {
            try {
                double manualGoal = Double.parseDouble(manualGoalStr);
                goalCalorie.setText(String.format(Locale.getDefault(), "%.0f", manualGoal));
                return;
            } catch (NumberFormatException ignored) {}
        }

        goalCalorie.setText("-");
    }

    private void setMacroValue(TextView valueText, TextView unitText, double grams) {
        if (grams < 1000) {
            valueText.setText(String.format(Locale.getDefault(), "%.0f", grams));
            unitText.setText("g");
        } else {
            double inKg = grams / 1000.0;
            valueText.setText(String.format(Locale.getDefault(), "%.1f", inKg));
            unitText.setText("kg");
        }
    }

    private String formatCalories(double calories) {
        if (calories < 10000) {
            return String.format(Locale.getDefault(), "%.0f", calories);
        } else {
            double inK = calories / 1000.0;
            return String.format(Locale.getDefault(), "%.0fk", inK);
        }
    }

    private void setupClickListeners() {
        if (fragmentView == null) return;

        MaterialCardView addCalculationBtn = fragmentView.findViewById(R.id.add_calculation);
        if (addCalculationBtn != null) {
            addCalculationBtn.setOnClickListener(v -> openFoodSelectionFragment());
        }
    }

    private void openFoodSelectionFragment() {
        Intent intent = new Intent(requireContext(), CalorieCalculatorActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        loadTodayStats();
        refreshRecentMealsWithOverlay();
    }

    private void refreshRecentMealsWithOverlay() {
        if (recentMealsAdapter == null) return;

        List<MealItem> recentMeals = dbHelper.getAllMeals();
        List<MealItem> top5 = new ArrayList<>();
        if (!recentMeals.isEmpty()) {
            top5 = recentMeals.subList(0, Math.min(5, recentMeals.size()));
        }

        recentMealsAdapter.updateMeals(top5);

        if (overlay != null) {
            overlay.setVisibility(recentMeals.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recentMealsAdapter != null) {
            recentMealsAdapter.setOnMealClickListener(null);
        }
        dbHelper = null;
        fragmentView = null;
    }
}