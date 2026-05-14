package com.example.caloriecalculator.bottomsheets;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.helpers.FilterPrefs;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = "FilterSheet";

    private Chip chipAll, chipVegetarian, chipNonVegetarian;
    private Chip chipGrains, chipMilkProducts, chipFruitProducts, chipEggs;
    private Chip chipMeatAndPoultry, chipVegetables, chipSeedsAndNuts;
    private Chip chipSugarAndSugarProducts, chipNABeverages, chipABeverages, chipDiscretionary;

    private FilterPrefs prefs;
    private String currentFilter = "All";

    public static FilterBottomSheetFragment newInstance() {
        return new FilterBottomSheetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new FilterPrefs(requireContext());
        currentFilter = prefs.getDietary().equals("All") && prefs.getCategory().equals("All")
                ? "All" : prefs.getCategory().equals("All") ? prefs.getDietary() : prefs.getCategory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_food_filter, container, false);
        initViews(view);
        updateChipSelection();
        setupChipListeners();
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

    private void initViews(View view) {
        chipAll = view.findViewById(R.id.chipAll);
        chipVegetarian = view.findViewById(R.id.chipVegetarian);
        chipNonVegetarian = view.findViewById(R.id.chipNonVegetarian);
        chipGrains = view.findViewById(R.id.chipGrains);
        chipMilkProducts = view.findViewById(R.id.chipMilkProducts);
        chipFruitProducts = view.findViewById(R.id.chipFruitProducts);
        chipEggs = view.findViewById(R.id.chipEggs);
        chipMeatAndPoultry = view.findViewById(R.id.chipMeatAndPoultry);
        chipVegetables = view.findViewById(R.id.chipVegetables);
        chipSeedsAndNuts = view.findViewById(R.id.chipSeedsAndNuts);
        chipSugarAndSugarProducts = view.findViewById(R.id.chipSugarAndSugarProducts);
        chipNABeverages = view.findViewById(R.id.chipNABeverages);
        chipABeverages = view.findViewById(R.id.chipABeverages);
        chipDiscretionary = view.findViewById(R.id.chipDiscretionary);
    }

    private void updateChipSelection() {
        clearAllChips();

        switch (currentFilter) {
            case "Vegetarian": chipVegetarian.setChecked(true); break;
            case "Non-Vegetarian": chipNonVegetarian.setChecked(true); break;
            case "Grains": chipGrains.setChecked(true); break;
            case "Milk and Milk Products": chipMilkProducts.setChecked(true); break;
            case "Fruit and Fruit Products": chipFruitProducts.setChecked(true); break;
            case "Eggs": chipEggs.setChecked(true); break;
            case "Meat and Poultry": chipMeatAndPoultry.setChecked(true); break;
            case "Vegetables": chipVegetables.setChecked(true); break;
            case "Seeds And Nuts": chipSeedsAndNuts.setChecked(true); break;
            case "Sugar And Sugar Products": chipSugarAndSugarProducts.setChecked(true); break;
            case "Non Alcoholic Beverages": chipNABeverages.setChecked(true); break;
            case "Alcoholic Beverages": chipABeverages.setChecked(true); break;
            case "Discretionary": chipDiscretionary.setChecked(true); break;
            default: chipAll.setChecked(true); break;
        }
    }

    private void clearAllChips() {
        chipAll.setChecked(false);
        chipVegetarian.setChecked(false);
        chipNonVegetarian.setChecked(false);
        chipGrains.setChecked(false);
        chipMilkProducts.setChecked(false);
        chipFruitProducts.setChecked(false);
        chipEggs.setChecked(false);
        chipMeatAndPoultry.setChecked(false);
        chipVegetables.setChecked(false);
        chipSeedsAndNuts.setChecked(false);
        chipSugarAndSugarProducts.setChecked(false);
        chipNABeverages.setChecked(false);
        chipABeverages.setChecked(false);
        chipDiscretionary.setChecked(false);
    }

    private void setupChipListeners() {

        chipAll.setOnClickListener(v -> selectFilter("All"));
        chipVegetarian.setOnClickListener(v -> selectFilter("Vegetarian"));
        chipNonVegetarian.setOnClickListener(v -> selectFilter("Non-Vegetarian"));
        chipGrains.setOnClickListener(v -> selectFilter("Grains"));
        chipMilkProducts.setOnClickListener(v -> selectFilter("Milk and Milk Products"));
        chipFruitProducts.setOnClickListener(v -> selectFilter("Fruit and Fruit Products"));
        chipEggs.setOnClickListener(v -> selectFilter("Eggs"));
        chipMeatAndPoultry.setOnClickListener(v -> selectFilter("Meat and Poultry"));
        chipVegetables.setOnClickListener(v -> selectFilter("Vegetables"));
        chipSeedsAndNuts.setOnClickListener(v -> selectFilter("Seeds And Nuts"));
        chipSugarAndSugarProducts.setOnClickListener(v -> selectFilter("Sugar And Sugar Products"));
        chipNABeverages.setOnClickListener(v -> selectFilter("Non Alcoholic Beverages"));
        chipABeverages.setOnClickListener(v -> selectFilter("Alcoholic Beverages"));
        chipDiscretionary.setOnClickListener(v -> selectFilter("Discretionary"));
    }

    private void selectFilter(String filterValue) {
        currentFilter = filterValue;
        updateChipSelection();
        saveToPrefs();
        applyFilter();
    }

    private void saveToPrefs() {

        if ("Vegetarian".equals(currentFilter) || "Non-Vegetarian".equals(currentFilter)) {
            prefs.saveDietary(currentFilter);
            prefs.saveCategory("All");
        } else {
            prefs.saveDietary("All");
            prefs.saveCategory(currentFilter);
        }
        Log.d(TAG, "💾 Saved filter: " + currentFilter);
    }

    private void applyFilter() {
        String summary = currentFilter.equals("All") ? "All foods" : currentFilter;
        Toast.makeText(getContext(), "🔍 " + summary, Toast.LENGTH_SHORT).show();
        notifyFoodFragment();
        dismiss();
    }

    private void notifyFoodFragment() {
        Bundle filterBundle = new Bundle();
        if ("Vegetarian".equals(currentFilter) || "Non-Vegetarian".equals(currentFilter)) {
            filterBundle.putString("dietary_filter", currentFilter);
            filterBundle.putString("category_filter", "All");
        } else {
            filterBundle.putString("dietary_filter", "All");
            filterBundle.putString("category_filter", currentFilter);
        }
        getParentFragmentManager().setFragmentResult("filter_applied", filterBundle);
        Log.d(TAG, "📤 Sent filter: " + currentFilter);
    }
}