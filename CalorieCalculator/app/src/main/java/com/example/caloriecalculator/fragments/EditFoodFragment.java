package com.example.caloriecalculator.fragments;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.database.DatabaseHelper;
import com.example.caloriecalculator.models.FoodItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

public class EditFoodFragment extends Fragment {

    // UI Views
    private ImageView selectedIcon, back;
    private TextView title, unitTextView;
    private MaterialAutoCompleteTextView dietaryPreferences, category, measure;
    private TextInputEditText foodName, perServingSize, calories, fats, protein, carbs;
    private MaterialCardView saveButton;
    private DatabaseHelper dbHelper;

    private FoodItem editingFoodItem;
    private long editingFoodId = -1;

    private final String[] dietaryOptions = {"Vegetarian", "Non-Vegetarian"};
    private final String[] categories = {
            "Grains", "Milk and Milk Products", "Fruit and Fruit Products", "Eggs",
            "Meat and Poultry", "Vegetables", "Seeds And Nuts", "Sugar And Sugar Products",
            "Non Alcoholic Beverages", "Alcoholic Beverages", "Discretionary"
    };
    private final String[] units = {"g", "ml", "piece", "cup", "tbsp", "tsp"};

    private final int[][] CATEGORY_ICONS = {
            {R.drawable.ic_grains, R.drawable.ic_milk, R.drawable.ic_fruit, R.drawable.ic_egg,
                    R.drawable.ic_meat, R.drawable.ic_vegetable, R.drawable.ic_nuts, R.drawable.ic_sugar,
                    R.drawable.ic_drink, R.drawable.ic_alcohol, R.drawable.ic_snack}
    };
    private final String[] CATEGORY_ICON_NAMES = {
            "Grains", "Milk and Milk Products", "Fruit and Fruit Products", "Eggs",
            "Meat and Poultry", "Vegetables", "Seeds And Nuts", "Sugar And Sugar Products",
            "Non Alcoholic Beverages", "Alcoholic Beverages", "Discretionary"
    };

    private final String[] NON_VEG_CATEGORIES = {"Eggs", "Meat and Poultry"};
    private final String[] USER_DECIDE_CATEGORIES = {
            "Non Alcoholic Beverages", "Alcoholic Beverages", "Discretionary"
    };

    public static EditFoodFragment newInstance(FoodItem foodItem) {
        EditFoodFragment fragment = new EditFoodFragment();
        Bundle args = new Bundle();
        args.putSerializable("food_item", foodItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            editingFoodItem = (FoodItem) getArguments().getSerializable("food_item");
            if (editingFoodItem != null) {
                editingFoodId = editingFoodItem.getId();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_option, container, false);

        hideBottomNavigation();
        initViews(view);
        dbHelper = new DatabaseHelper(requireContext());
        back.setOnClickListener(v -> {
            goBackToFoodFragment();
        });
        title.setText("Edit Food Item");
        setupDropdowns();
        setupClickListeners();
        setupSmartLogic();

        if (editingFoodItem != null) {
            populateFields();
        }

        return view;
    }

    private void initViews(View view) {
        back = view.findViewById(R.id.back_button);
        title = view.findViewById(R.id.title);
        dietaryPreferences = view.findViewById(R.id.dietaryPreferences);
        category = view.findViewById(R.id.category);
        measure = view.findViewById(R.id.measure);
        foodName = view.findViewById(R.id.food_name);
        perServingSize = view.findViewById(R.id.per_serving_size);
        calories = view.findViewById(R.id.calories);
        fats = view.findViewById(R.id.fats);
        protein = view.findViewById(R.id.protein);
        carbs = view.findViewById(R.id.carbs);
        saveButton = view.findViewById(R.id.save);
        selectedIcon = view.findViewById(R.id.selectedIcon);
        unitTextView = view.findViewById(R.id.unit);
    }

    private void populateFields() {
        foodName.setText(editingFoodItem.getName());
        dietaryPreferences.setText(editingFoodItem.getDietaryPref());
        category.setText(editingFoodItem.getCategory());
        measure.setText(editingFoodItem.getUnit());
        perServingSize.setText(editingFoodItem.getServingSize());
        calories.setText(editingFoodItem.getCalories());
        fats.setText(editingFoodItem.getFats());
        protein.setText(editingFoodItem.getProtein());
        carbs.setText(editingFoodItem.getCarbs());

        updateCategoryIcon(editingFoodItem.getCategory());
        updateUnitDisplay();
    }

    private void setupDropdowns() {
        setupDropdown(dietaryPreferences, dietaryOptions, "Dietary preference");
        setupDropdown(category, categories, "Select category");
        setupDropdown(measure, units, "Select unit");
    }

    private void setupSmartLogic() {
        category.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = categories[position];
            autoSetDietaryPreference(selectedCategory);
            updateCategoryIcon(selectedCategory);
            updateUnitDisplay();
        });

        measure.setOnItemClickListener((parent, view, position, id) -> updateUnitDisplay());
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> updateFoodItem());
    }

    private void updateFoodItem() {
        if (foodName.getText().toString().trim().isEmpty()) {
            Toast.makeText(getContext(), "Please enter food name", Toast.LENGTH_SHORT).show();
            return;
        }

        FoodItem updatedItem = new FoodItem(
                foodName.getText().toString().trim(),
                dietaryPreferences.getText().toString(),
                category.getText().toString(),
                measure.getText().toString(),
                perServingSize.getText().toString(),
                calories.getText().toString(),
                fats.getText().toString(),
                protein.getText().toString(),
                carbs.getText().toString()
        );
        updatedItem.setId(editingFoodId);
        updatedItem.setCategoryIcon(getCurrentIconResource());

        int rowsUpdated = dbHelper.updateFoodItem(updatedItem);

        if (rowsUpdated > 0) {
            Toast.makeText(getContext(), "✅ Food updated successfully!", Toast.LENGTH_SHORT).show();
            goBackToFoodFragment();
        } else {
            Toast.makeText(getContext(), "❌ Failed to update food item", Toast.LENGTH_SHORT).show();
        }
    }

    private void autoSetDietaryPreference(String selectedCategory) {
        String dietaryPref;
        if (isNonVegCategory(selectedCategory)) {
            dietaryPref = "Non-Vegetarian";
            dietaryPreferences.setText(dietaryPref);
        } else if (isVegCategory(selectedCategory)) {
            dietaryPref = "Vegetarian";
            dietaryPreferences.setText(dietaryPref);
        } else {
            dietaryPreferences.setText("");
        }
    }

    private boolean isNonVegCategory(String category) {
        for (String nonVeg : NON_VEG_CATEGORIES) {
            if (nonVeg.equals(category)) return true;
        }
        return false;
    }

    private boolean isVegCategory(String category) {
        for (String userDecide : USER_DECIDE_CATEGORIES) {
            if (userDecide.equals(category)) return false;
        }
        return true;
    }

    private void updateCategoryIcon(String selectedCategory) {
        int iconResId = R.drawable.ic_grocery;
        for (int i = 0; i < CATEGORY_ICON_NAMES.length; i++) {
            if (CATEGORY_ICON_NAMES[i].equals(selectedCategory)) {
                iconResId = CATEGORY_ICONS[0][i];
                break;
            }
        }
        selectedIcon.setImageResource(iconResId);
    }

    private void updateUnitDisplay() {
        String selectedUnit = measure.getText().toString();
        unitTextView.setText(selectedUnit.isEmpty() ? "g" : selectedUnit);
    }

    private int getCurrentIconResource() {
        String selectedCategory = category.getText().toString();
        for (int i = 0; i < CATEGORY_ICON_NAMES.length; i++) {
            if (CATEGORY_ICON_NAMES[i].equals(selectedCategory)) {
                return CATEGORY_ICONS[0][i];
            }
        }
        return R.drawable.ic_grocery;
    }

    private void setupDropdown(MaterialAutoCompleteTextView autoCompleteTextView, String[] items, String hint) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, items);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(0);

        autoCompleteTextView.setDropDownBackgroundResource(R.drawable.dropdown_background);
        autoCompleteTextView.setTextColor(getAttrColor(R.attr.dropdownTextColor));

        autoCompleteTextView.setOnClickListener(v -> autoCompleteTextView.showDropDown());

        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextView.post(() -> autoCompleteTextView.showDropDown());
            }
        });
    }

    private void goBackToFoodFragment() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        showBottomNavigation();
    }

    private int getAttrColor(int attrResId) {
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(attrResId, typedValue, true);

        if (typedValue.resourceId != 0) {
            return ContextCompat.getColor(requireContext(), typedValue.resourceId);
        }
        return ContextCompat.getColor(requireContext(), android.R.color.black); // Fallback
    }

    private void hideBottomNavigation() {
        if (getActivity() != null) {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.GONE);
            }
        }
    }

    private void showBottomNavigation() {
        if (getActivity() != null) {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setVisibility(View.VISIBLE);
            }
        }
    }
}