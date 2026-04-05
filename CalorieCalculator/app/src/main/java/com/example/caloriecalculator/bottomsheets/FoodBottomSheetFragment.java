package com.example.caloriecalculator.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.models.FoodItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;

import java.io.Serializable;

public class FoodBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_FOOD_ITEM = "food_item";

    private FoodItem foodItem;
    private TextView foodName, foodCategory, servingSize, servingMeasure, servingCalorie;
    private TextView foodFats, foodProtein, foodCarbs;
    private MaterialCardView dietaryIndicator;
    private Button editButton, deleteButton;

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

        // CUSTOMIZE: Remove bottom padding + set background
        dialog.setOnShowListener(d -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                // Remove bottom padding
                bottomSheet.setPadding(0, 0, 0, 0);

                // Set custom background color
                bottomSheet.setBackgroundColor(getResources().getColor(R.color.white, null));

            }
        });

        return dialog;
    }

    private void initViews(View view) {
        foodName = view.findViewById(R.id.food_name);
        foodCategory = view.findViewById(R.id.food_category);
        servingSize = view.findViewById(R.id.serving_size);
        servingMeasure = view.findViewById(R.id.serving_measure);
        servingCalorie = view.findViewById(R.id.serving_calorie);
        foodFats = view.findViewById(R.id.food_fats);
        foodProtein = view.findViewById(R.id.food_protein);
        foodCarbs = view.findViewById(R.id.food_carbs);
        dietaryIndicator = view.findViewById(R.id.indicator_layout); // Veg/Non-veg indicator
        editButton = view.findViewById(R.id.edit);
        deleteButton = view.findViewById(R.id.delete);
    }

    private void bindData() {
        if (foodItem == null) return;

        foodName.setText(foodItem.getName());
        foodCategory.setText(foodItem.getCategory());
        servingSize.setText(foodItem.getServingSize());
        servingMeasure.setText(foodItem.getUnit());
        servingCalorie.setText(foodItem.getCalories());
        foodFats.setText(foodItem.getFats());
        foodProtein.setText(foodItem.getProtein());
        foodCarbs.setText(foodItem.getCarbs());

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

    private void setupClickListeners() {
        editButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit: " + foodItem.getName(), Toast.LENGTH_SHORT).show();
            dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            // TODO: Implement delete from DB
            Toast.makeText(getContext(), "Delete: " + foodItem.getName(), Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }
}