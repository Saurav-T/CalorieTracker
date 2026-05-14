package com.example.caloriecalculator.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.MainActivity;
import com.example.caloriecalculator.R;
import com.example.caloriecalculator.adapters.CalculationItemsAdapter;
import com.example.caloriecalculator.database.DatabaseHelper;
import com.example.caloriecalculator.models.FoodItem;
import com.example.caloriecalculator.models.MealItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalculationFragment extends Fragment implements CalculationItemsAdapter.OnServingChangedListener {

    private static final String ARG_SELECTED_ITEMS = "selected_items";
    private static final String TAG = "CalculationFragment";

    // UI Views
    private TextInputEditText mealNameEdit;
    private RecyclerView foodList;
    private TextView calorieCount, fatsCount, proteinCount, carbsCount;
    private MaterialCardView doneButton;

    // Data
    private DatabaseHelper dbHelper;
    private CalculationItemsAdapter adapter;
    private List<CalculationItemsAdapter.CalculationItem> calculationItems = new ArrayList<>();

    public CalculationFragment() {}

    public static CalculationFragment newInstance(List<FoodItem> selectedItems) {
        CalculationFragment fragment = new CalculationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SELECTED_ITEMS, new ArrayList<>(selectedItems));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculation, container, false);

        initViews(view);
        loadSelectedItems();
        setupRecyclerView();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        mealNameEdit = view.findViewById(R.id.meal_name);
        foodList = view.findViewById(R.id.food_list);
        calorieCount = view.findViewById(R.id.calorie_count);
        fatsCount = view.findViewById(R.id.fats_count);
        proteinCount = view.findViewById(R.id.protein_count);
        carbsCount = view.findViewById(R.id.carbs_count);
        doneButton = view.findViewById(R.id.done);

        // Back button
        ImageView backButton = view.findViewById(R.id.back_button);
        if (backButton != null) {
            backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }
    }

    private void loadSelectedItems() {
        if (getArguments() != null) {
            @SuppressWarnings("unchecked")
            List<FoodItem> items = (List<FoodItem>) getArguments().getSerializable(ARG_SELECTED_ITEMS);
            if (items != null) {
                calculationItems.clear();
                for (FoodItem item : items) {
                    calculationItems.add(new CalculationItemsAdapter.CalculationItem(item));
                }
                Log.d(TAG, "📦 Loaded " + calculationItems.size() + " items for calculation");
            }
        }
    }

    private void setupRecyclerView() {
        foodList.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CalculationItemsAdapter();
        adapter.setOnServingChangedListener(this);

        List<FoodItem> foodItems = new ArrayList<>();
        for (CalculationItemsAdapter.CalculationItem calcItem : calculationItems) {
            foodItems.add(calcItem.foodItem);
        }

        adapter.updateItems(foodItems);
        foodList.setAdapter(adapter);
        updateNutritionTotals();
    }

    private void setupClickListeners() {
        doneButton.setOnClickListener(v -> saveMeal());
    }

    @Override
    public void onServingChanged(CalculationItemsAdapter.CalculationItem item, double newServingSize) {
        updateNutritionTotals();
    }

    private void updateNutritionTotals() {
        calorieCount.setText(String.format("%.1f", adapter.getTotalCalories()));
        fatsCount.setText(String.format("%.1f", adapter.getTotalFats()));
        proteinCount.setText(String.format("%.1f", adapter.getTotalProtein()));
        carbsCount.setText(String.format("%.1f", adapter.getTotalCarbs()));
    }

    private void saveMeal() {
        String mealName = mealNameEdit.getText().toString().trim();
        if (mealName.isEmpty()) {
            mealName = "Untitled Meal";
        }

        MealItem meal = new MealItem();
        meal.setMealName(mealName);
        meal.setTimestamp(System.currentTimeMillis());
        meal.setTotalCalories(adapter.getTotalCalories());
        meal.setTotalFats(adapter.getTotalFats());
        meal.setTotalProtein(adapter.getTotalProtein());
        meal.setTotalCarbs(adapter.getTotalCarbs());

        List<MealItem.MealFoodItem> mealFoods = new ArrayList<>();
        for (CalculationItemsAdapter.CalculationItem calcItem : adapter.getItems()) {
            mealFoods.add(new MealItem.MealFoodItem(calcItem.foodItem, calcItem.servingSize));
        }
        meal.setFoodItems(mealFoods);

        long mealId = dbHelper.insertMeal(meal);
        Log.d(TAG, "💾 SAVED meal ID: " + mealId + " (" + meal.getTotalCalories() + " kcal)");

        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
    }
}