package com.example.caloriecalculator.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.database.DatabaseHelper;
import com.example.caloriecalculator.fragments.EditFoodFragment;
import com.example.caloriecalculator.fragments.FoodFragment;
import com.example.caloriecalculator.models.FoodItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import java.io.Serializable;
import java.util.Locale;

public class FoodBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_FOOD_ITEM = "food_item";

    private FoodItem foodItem;
    private TextView foodName, foodCategory, servingSize, servingUnit, servingCalorie;
    private TextView foodFats, foodProtein, foodCarbs;

    private TextView fatUnit, proteinUnit, carbUnit;
    private MaterialCardView dietaryIndicator;
    private MaterialCardView editButton, deleteButton;

    public static FoodBottomSheetFragment newInstance(FoodItem foodItem) {
        FoodBottomSheetFragment fragment = new FoodBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FOOD_ITEM, (Serializable) foodItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            foodItem = (FoodItem) getArguments().getSerializable(ARG_FOOD_ITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_food_details, container, false);

        initViews(view);
        bindData();
        setupClickListeners();

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(R.attr.bottomSheetBgColor, typedValue, true);

        // CUSTOMIZE: Remove bottom padding + set background
        dialog.setOnShowListener(d -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // Remove bottom padding
                bottomSheet.setPadding(0, 0, 0, 0);
                // Set custom background color
                bottomSheet.setBackgroundColor(typedValue.data);

            }
        });

        return dialog;
    }

    private void initViews(View view) {
        foodName = view.findViewById(R.id.food_name);
        foodCategory = view.findViewById(R.id.food_category);
        servingSize = view.findViewById(R.id.serving_size);
        servingUnit = view.findViewById(R.id.serving_unit);
        servingCalorie = view.findViewById(R.id.serving_calorie);
        foodFats = view.findViewById(R.id.food_fats);
        foodProtein = view.findViewById(R.id.food_protein);
        foodCarbs = view.findViewById(R.id.food_carbs);
        fatUnit = view.findViewById(R.id.fat_unit);
        proteinUnit = view.findViewById(R.id.protein_unit);
        carbUnit = view.findViewById(R.id.carb_unit);
        dietaryIndicator = view.findViewById(R.id.indicator_layout); // Veg/Non-veg indicator
        editButton = view.findViewById(R.id.edit);
        deleteButton = view.findViewById(R.id.delete);
    }

    private void bindData() {
        if (foodItem == null) return;

        foodName.setText(foodItem.getName());
        foodCategory.setText(foodItem.getCategory());
        servingSize.setText(foodItem.getServingSize());
        servingUnit.setText(foodItem.getUnit());
        servingCalorie.setText(formatCalories(parseDouble(foodItem.getCalories())));
        setMacroValue(foodFats, fatUnit, parseDouble(foodItem.getFats()));
        setMacroValue(foodProtein, proteinUnit, parseDouble(foodItem.getProtein()));
        setMacroValue(foodCarbs, carbUnit, parseDouble(foodItem.getCarbs()));

        // FIXED: Dietary indicator for MaterialCardView
        if ("Non-Vegetarian".equals(foodItem.getDietaryPref())) {
            dietaryIndicator.setStrokeColor(getResources().getColor(R.color.red, null));
            // Optional: Change tint of inner ImageView
            ImageView innerIcon = dietaryIndicator.findViewById(R.id.inner_dietary_icon); // Add this ID
            if (innerIcon != null) {
                innerIcon.setColorFilter(getResources().getColor(R.color.red, null));
            }
        } else {
            dietaryIndicator.setStrokeColor(getResources().getColor(R.color.green, null)); // Use your veg green color
            ImageView innerIcon = dietaryIndicator.findViewById(R.id.inner_dietary_icon);
            if (innerIcon != null) {
                innerIcon.setColorFilter(getResources().getColor(R.color.green, null));
            }
        }
    }

    private String formatCalories(double calories) {
        if (calories < 10000) {
            return String.format(Locale.getDefault(), "%.0f", calories);
        } else {
            double inK = calories / 1000.0;
            return String.format(Locale.getDefault(), "%.1f", inK) + "k";
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


    private void setupClickListeners() {
        editButton.setOnClickListener(v -> {
            if (foodItem != null) {
                EditFoodFragment editFragment = EditFoodFragment.newInstance(foodItem);
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, editFragment);
                transaction.addToBackStack("EditFoodFragment");
                transaction.commit();
            }
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            if (foodItem != null) {
                new Thread(() -> {
                    DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
                    boolean deleted = dbHelper.deleteFoodItem(foodItem.getId());

                    requireActivity().runOnUiThread(() -> {
                        if (deleted) {
                            Toast.makeText(getContext(), "🗑️ " + foodItem.getName() + " deleted!", Toast.LENGTH_SHORT).show();

                            // ✅ BULLETPROOF: Force refresh FoodFragment
                            FoodFragment foodFragment = (FoodFragment) getParentFragmentManager().findFragmentById(R.id.fragment_container);
                            if (foodFragment != null) {
                                foodFragment.onFoodDeleted(foodItem.getId());
                            }

                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "❌ Failed to delete", Toast.LENGTH_SHORT).show();
                        }
                    });
                }).start();
            }
        });
    }


}