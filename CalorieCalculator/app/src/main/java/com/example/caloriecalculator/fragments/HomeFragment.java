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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.CalorieCalculatorActivity;
import com.example.caloriecalculator.R;
import com.example.caloriecalculator.adapters.RecentMealsAdapter;
import com.example.caloriecalculator.bottomsheets.MealDetailsBottomSheet;
import com.example.caloriecalculator.database.DatabaseHelper;
import com.example.caloriecalculator.models.MealItem;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Views
    private View fragmentView;
    private TextView totalCalorieToday, carbConsumption, proteinConsumption, fatConsumption;
    private TextView todayDate, todayDay, goalCalorie, carbUnit, proteinUnit, fatUnit;
    private RecyclerView recentMealsRecycler;

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
                (requestKey, result) -> {
                    refreshData();  // Auto-refresh when meal deleted
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      fragmentView = inflater.inflate(R.layout.fragment_home, container, false);

        // ✅ Bind ALL views from THIS view
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
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(requireContext());
    }

    private void setupRecyclerView() {
        if (recentMealsRecycler == null) return;  // ✅ Safety

        recentMealsRecycler.setLayoutManager(new LinearLayoutManager(
                getContext(), LinearLayoutManager.VERTICAL, false
        ));

        recentMealsAdapter = new RecentMealsAdapter();
        recentMealsRecycler.setAdapter(recentMealsAdapter);
        recentMealsAdapter.setOnMealClickListener(meal -> showMealDetailsBottomSheet(meal));

        refreshRecentMealsWithOverlay();
    }

    private void showMealDetailsBottomSheet(MealItem meal) {
        MealDetailsBottomSheet bottomSheet = MealDetailsBottomSheet.newInstance(meal.getId());
        bottomSheet.show(getParentFragmentManager(), "MealDetailsBottomSheet");
    }

    private void loadTodayStats() {
        if (totalCalorieToday == null) return;

        List<MealItem> allMeals = dbHelper.getAllMeals();
        double totalCal = 0, carbs = 0, protein = 0, fats = 0;

        for (MealItem meal : allMeals) {
            totalCal += meal.getTotalCalories();
            carbs += meal.getTotalCarbs();
            protein += meal.getTotalProtein();
            fats += meal.getTotalFats();
        }

        // Apply smart formatting
        totalCalorieToday.setText(formatCalories(totalCal));

        setMacroValue(carbConsumption, carbUnit, carbs);
        setMacroValue(proteinConsumption, proteinUnit, protein);
        setMacroValue(fatConsumption, fatUnit, fats);

        // Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        todayDate.setText(dateFormat.format(new Date()));
        if (todayDay != null) todayDay.setText(dayFormat.format(new Date()));

        loadDailyGoal();
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
            if (inK % 1 < 0.1) {
                // Show as whole number if decimal is very small (e.g. 12k instead of 12.0k)
                return String.format(Locale.getDefault(), "%.0fk", inK);
            } else {
                return String.format(Locale.getDefault(), "%.1fk", inK);
            }
        }
    }

    private void loadDailyGoal() {
        if (goalCalorie == null) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE);
        String dailyGoalStr = prefs.getString("daily_calorie_goal", null);

        if (dailyGoalStr != null && !dailyGoalStr.isEmpty()) {
            try {
                double dailyGoal = Double.parseDouble(dailyGoalStr);
                goalCalorie.setText(String.format(Locale.getDefault(), "%.0f", dailyGoal));
            } catch (NumberFormatException e) {
                goalCalorie.setText("-");
            }
        } else {
            goalCalorie.setText("-");  // ✅ Default if not set
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
        loadTodayStats();      // ✅ Refresh totals
        refreshRecentMealsWithOverlay();   // ✅ Refresh list
    }

    private void refreshRecentMealsWithOverlay() {
        if (recentMealsAdapter == null) return;  // ✅ Safety

        List<MealItem> recentMeals = dbHelper.getAllMeals();
        List<MealItem> top5 = new ArrayList<>();
        if (!recentMeals.isEmpty()) {
            top5 = recentMeals.subList(0, Math.min(5, recentMeals.size()));
        }
        recentMealsAdapter.updateMeals(top5);

        // ✅ Safe overlay toggle
        if (fragmentView != null) {
            LinearLayout overlay = fragmentView.findViewById(R.id.overlay);
            if (overlay != null) {
                overlay.setVisibility(recentMeals.isEmpty() ? View.VISIBLE : View.GONE);
            }
        }
    }
    public void refreshData() {
        loadTodayStats();
        refreshRecentMealsWithOverlay();
    }
}