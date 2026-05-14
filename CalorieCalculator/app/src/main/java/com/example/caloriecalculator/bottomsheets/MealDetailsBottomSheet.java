package com.example.caloriecalculator.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.adapters.MealContentsAdapter;
import com.example.caloriecalculator.database.DatabaseHelper;
import com.example.caloriecalculator.models.MealItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import java.util.Locale;

public class MealDetailsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_MEAL_ID = "meal_id";

    private DatabaseHelper dbHelper;
    private MealItem meal;
    private MealContentsAdapter contentsAdapter;

    private TextView mealName, mealCalorie, mealFats, mealProtein, mealCarbs;
    private TextView fatUnit, proteinUnit, carbUnit;
    private MaterialCardView indicatorLayout;
    private ImageView innerDietaryIcon;
    private MaterialCardView deleteButton;

    public static MealDetailsBottomSheet newInstance(long mealId) {
        MealDetailsBottomSheet fragment = new MealDetailsBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_MEAL_ID, mealId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_recent_meals, container, false);

        bindViews(view);
        loadMealData();
        setupRecyclerView(view);
        setupClickListeners();

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(R.attr.bottomSheetBgColor, typedValue, true);

        dialog.setOnShowListener(d -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setPadding(0, 0, 0, 0);
                bottomSheet.setBackgroundColor(typedValue.data);
            }
        });
        return dialog;
    }

    private void bindViews(View view) {
        mealName = view.findViewById(R.id.meal_name);
        mealCalorie = view.findViewById(R.id.meal_calorie);
        mealFats = view.findViewById(R.id.meal_fats);
        mealProtein = view.findViewById(R.id.meal_protein);
        mealCarbs = view.findViewById(R.id.meal_carbs);
        fatUnit = view.findViewById(R.id.fat_unit);
        proteinUnit = view.findViewById(R.id.protein_unit);
        carbUnit = view.findViewById(R.id.carb_unit);
        indicatorLayout = view.findViewById(R.id.indicator_layout);
        innerDietaryIcon = view.findViewById(R.id.inner_dietary_icon);
        deleteButton = view.findViewById(R.id.delete);
    }

    private void loadMealData() {
        if (getArguments() == null) return;

        long mealId = getArguments().getLong(ARG_MEAL_ID);
        meal = dbHelper.getMealById(mealId);

        if (meal != null) {
            mealName.setText(meal.getMealName());
            mealCalorie.setText(formatCalories(meal.getTotalCalories()));
            setMacroValue(mealFats, fatUnit, meal.getTotalFats());
            setMacroValue(mealProtein, proteinUnit, meal.getTotalProtein());
            setMacroValue(mealCarbs, carbUnit, meal.getTotalCarbs());

            updateDietaryIndicator();
        }
    }

    private String formatCalories(double calories) {
        if (calories < 10000) {
            return String.format(Locale.getDefault(), "%.0f", calories);
        } else {
            return String.format(Locale.getDefault(), "%.1f", calories/1000.0) + "k";
        }
    }

    private void updateDietaryIndicator() {
        if (meal == null || indicatorLayout == null) return;

        boolean isNonVeg = false;

        for (MealItem.MealFoodSnapshot snapshot : meal.getFoodSnapshots()) {
            if (snapshot.foodName.toLowerCase().contains("chicken") ||
                    snapshot.foodName.toLowerCase().contains("egg") ||
                    snapshot.foodName.toLowerCase().contains("meat") ||
                    snapshot.foodName.toLowerCase().contains("fish") ||
                    snapshot.foodName.toLowerCase().contains("mutton")) {
                isNonVeg = true;
                break;
            }
        }

        if (isNonVeg) {
            // Red for Non-Veg
            indicatorLayout.setStrokeColor(getResources().getColor(R.color.red, null));
            if (innerDietaryIcon != null) {
                innerDietaryIcon.setColorFilter(getResources().getColor(R.color.red, null));
            }
        } else {
            // Green for Veg
            indicatorLayout.setStrokeColor(getResources().getColor(R.color.green, null));
            if (innerDietaryIcon != null) {
                innerDietaryIcon.setColorFilter(getResources().getColor(R.color.green, null));
            }
        }
    }

    private void setMacroValue(TextView valueView, TextView unitView, double grams) {
        if (grams < 1000) {
            valueView.setText(String.format(Locale.getDefault(), "%.1f", grams));
            unitView.setText("g");
        } else {
            double inKg = grams / 1000.0;
            valueView.setText(String.format(Locale.getDefault(), "%.1f", inKg));
            unitView.setText("kg");
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.meal_contents);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        contentsAdapter = new MealContentsAdapter();
        if (meal != null) {
            contentsAdapter.updateSnapshots(meal.getFoodSnapshots());
        }
        recyclerView.setAdapter(contentsAdapter);
    }

    private void setupClickListeners() {
        deleteButton.setOnClickListener(v -> {
            if (meal != null) {
                new Thread(() -> {
                    dbHelper.deleteMeal(meal.getId());
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "🗑️ " + meal.getMealName() + " deleted!", Toast.LENGTH_SHORT).show();
                        Bundle result = new Bundle();
                        result.putLong("meal_id", meal.getId());
                        getParentFragmentManager().setFragmentResult("meal_deleted", result);
                        dismiss();
                    });
                }).start();
            }
        });
    }
}