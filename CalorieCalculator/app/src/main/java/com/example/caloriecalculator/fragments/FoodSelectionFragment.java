package com.example.caloriecalculator.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.CalorieCalculatorActivity;
import com.example.caloriecalculator.MainActivity;
import com.example.caloriecalculator.R;
import com.example.caloriecalculator.adapters.FoodSelectionAdapter;
import com.example.caloriecalculator.bottomsheets.FilterBottomSheetFragment;
import com.example.caloriecalculator.bottomsheets.SelectedItemsBottomSheetFragment;
import com.example.caloriecalculator.database.DatabaseHelper;
import com.example.caloriecalculator.helpers.SearchHelper;
import com.example.caloriecalculator.models.FoodItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class FoodSelectionFragment extends Fragment implements FoodSelectionAdapter.OnItemSelectionChangedListener {

    private static final String TAG = "FoodSelectionFragment";

    // UI Views
    private TextInputEditText searchSelectFood;
    private TextInputLayout searchLayout;
    private MaterialCardView filterButton, viewButton, selectButton;
    private TextView selectedItemsNumber, titleText;
    private RecyclerView foodList;
    private LinearLayout emptyOverlay;

    // Core components
    private DatabaseHelper dbHelper;
    private FoodSelectionAdapter foodAdapter;
    private SearchHelper searchHelper;

    // Filter state
    private String currentDietaryFilter = "All";
    private String currentCategoryFilter = "All";
    private boolean isFilteringActive = false;
    private String currentSearchQuery = "";

    public FoodSelectionFragment() {}

    public static FoodSelectionFragment newInstance(String param1, String param2) {
        FoodSelectionFragment fragment = new FoodSelectionFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "🔥 onCreate - Setting up filter listener");

        getParentFragmentManager().setFragmentResultListener("filter_applied", this,
                (requestKey, result) -> {
                    currentDietaryFilter = result.getString("dietary_filter", "All");
                    currentCategoryFilter = result.getString("category_filter", "All");
                    isFilteringActive = true;
                    Log.d(TAG, "🔥 Filter applied: Dietary=" + currentDietaryFilter +
                            ", Category=" + currentCategoryFilter);
                    applyFilters(currentSearchQuery);
                });

        getParentFragmentManager().setFragmentResultListener("item_removed", this,
                (requestKey, result) -> {
                    long removedItemId = result.getLong("removed_item_id", -1);
                    Log.d(TAG, "📥 Received removal: item ID=" + removedItemId);
                    if (removedItemId != -1 && foodAdapter != null) {
                        foodAdapter.deselectItem(removedItemId);
                    }
                });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "🔥 onCreateView");
        View view = inflater.inflate(R.layout.fragment_food_selection, container, false);

        initViews(view);
        initDatabase();
        initSearchHelper();
        setupRecyclerView();
        setupClickListeners();
        setupSearch();
        showAllItems();
        return view;
    }

    private void initViews(View view) {
        searchSelectFood = view.findViewById(R.id.search_select_food);
        searchLayout = view.findViewById(R.id.searchLayout);
        filterButton = view.findViewById(R.id.filter);
        viewButton = view.findViewById(R.id.view);
        selectButton = view.findViewById(R.id.select);
        selectedItemsNumber = view.findViewById(R.id.selected_items_number);
        titleText = view.findViewById(R.id.title);
        foodList = view.findViewById(R.id.food_list);
        emptyOverlay = view.findViewById(R.id.overlay);

        // Back button
        ImageView backButton = view.findViewById(R.id.back_button); // Add this ID to layout if needed
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), MainActivity.class);
                startActivity(intent);
            });

        }
    }

    private void updateEmptyState(List<FoodItem> list) {
        if (emptyOverlay != null) {
            emptyOverlay.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(requireContext());
        Log.d(TAG, "✅ Database initialized");
    }

    private void initSearchHelper() {
        searchHelper = new SearchHelper();
        List<FoodItem> allFoods = dbHelper.getAllFoodItems();
        searchHelper.updateFoods(allFoods);
        Log.d(TAG, "✅ SearchHelper loaded " + allFoods.size() + " items");
    }

    private void setupRecyclerView() {
        foodList.setLayoutManager(new LinearLayoutManager(getContext()));
        List<FoodItem> initialList = dbHelper.getAllFoodItems();
        foodAdapter = new FoodSelectionAdapter(initialList);
        foodAdapter.setOnItemSelectionChangedListener(this);
        foodAdapter.setOnItemClickListener(foodItem -> {
            com.example.caloriecalculator.bottomsheets.FoodBottomSheetFragment bottomSheet =
                    com.example.caloriecalculator.bottomsheets.FoodBottomSheetFragment.newInstance(foodItem);
            bottomSheet.show(getParentFragmentManager(), "FoodBottomSheet");
        });
        foodList.setAdapter(foodAdapter);
        Log.d(TAG, "✅ RecyclerView setup with selection + bottomsheet support");
    }

    private void setupClickListeners() {
        filterButton.setOnClickListener(v -> {
            Log.d(TAG, "🔍 Filter button clicked");
            Log.d(TAG, "🔍 Filter button clicked");
            FilterBottomSheetFragment filterSheet = FilterBottomSheetFragment.newInstance();
            filterSheet.show(getParentFragmentManager(), "FilterBottomSheet");
        });

        viewButton.setOnClickListener(v -> {
            List<FoodItem> selected = foodAdapter.getSelectedItems();
            Log.d(TAG, "👁️ Opening bottomsheet with " + selected.size() + " selected items");

            // ✅ Opens your SelectedItemsBottomSheetFragment
            SelectedItemsBottomSheetFragment bottomSheet =
                    SelectedItemsBottomSheetFragment.newInstance(selected);
            bottomSheet.show(getParentFragmentManager(), "SelectedItemsBottomSheet");
        });

        selectButton.setOnClickListener(v -> {
            List<FoodItem> selected = foodAdapter.getSelectedItems();
            if (!selected.isEmpty()) {
                Log.d(TAG, "✅ Navigating to CalculationFragment with " + selected.size() + " items");
                CalculationFragment calcFragment = CalculationFragment.newInstance(selected);
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, calcFragment)  // Update ID to your container
                        .addToBackStack("calculation")
                        .commit();
            }
        });
    }

    private void setupSearch() {
        searchSelectFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                Log.d(TAG, "🔍 Search query: '" + currentSearchQuery + "'");
                applyFilters(currentSearchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void showAllItems() {
        List<FoodItem> allItems = dbHelper.getAllFoodItems();
        foodAdapter.updateList(allItems);
        updateEmptyState(allItems);
    }

    private void applyFilters(String query) {
        List<FoodItem> filteredList = searchHelper.search(
                query,
                currentCategoryFilter.equals("All") ? "" : currentCategoryFilter,
                currentDietaryFilter.equals("All") ? "" : currentDietaryFilter
        );

        foodAdapter.updateList(filteredList);
        updateEmptyState(filteredList);
        updateFilterHint();
    }

    private void updateFilterHint() {
        String hint = "Search food items";
        if (isFilteringActive) {
            if (!currentDietaryFilter.equals("All") && !currentCategoryFilter.equals("All")) {
                hint = String.format("Search %s + %s", currentDietaryFilter, currentCategoryFilter);
            } else if (!currentDietaryFilter.equals("All")) {
                hint = String.format("Search %s foods", currentDietaryFilter);
            } else if (!currentCategoryFilter.equals("All")) {
                hint = String.format("Search %s", currentCategoryFilter);
            }
        }
        searchSelectFood.setHint(hint);
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        selectedItemsNumber.setText(String.valueOf(selectedCount));
        updateButtonStates(selectedCount);
    }

    @Override
    public void onItemClicked(FoodItem foodItem, boolean isSelected) {

    }

    private void updateButtonStates(int selectedCount) {
        if (selectedCount > 0) {
            selectButton.setCardBackgroundColor(getResources().getColor(R.color.purple, null));
        } else {
            selectButton.setCardBackgroundColor(getResources().getColor(R.color.grey, null));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume - Refreshing data");
        refreshDataAndSearch();
    }

    private void refreshDataAndSearch() {
        List<FoodItem> updatedList = dbHelper.getAllFoodItems();
        searchHelper.updateFoods(updatedList);
        applyFilters(currentSearchQuery);
    }
}