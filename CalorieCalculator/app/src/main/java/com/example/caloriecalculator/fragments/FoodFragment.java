package com.example.caloriecalculator.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.adapters.FoodAdapter;
import com.example.caloriecalculator.database.DatabaseHelper;
import com.example.caloriecalculator.helpers.SearchHelper;
import com.example.caloriecalculator.models.FoodItem;
import com.example.caloriecalculator.bottomsheets.FilterBottomSheetFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;


public class FoodFragment extends Fragment implements FoodAdapter.OnItemClickListener {

    private static final String TAG = "FoodFragment";

    private TextInputEditText searchFood;
    private TextInputLayout searchLayout;
    private MaterialCardView filterButton, addFoodButton;
    private RecyclerView foodList;
    private LinearLayout emptyOverlay;

    // Core components
    private DatabaseHelper dbHelper;
    private FoodAdapter foodAdapter;
    private SearchHelper searchHelper;

    // Filter state
    private String currentDietaryFilter = "All";
    private String currentCategoryFilter = "All";
    private boolean isFilteringActive = false;
    private String currentSearchQuery = "";

    public FoodFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getParentFragmentManager().setFragmentResultListener("filter_applied", this,
                (requestKey, result) -> {
                    currentDietaryFilter = result.getString("dietary_filter", "All");
                    currentCategoryFilter = result.getString("category_filter", "All");
                    isFilteringActive = true;
                    applyFilters(currentSearchQuery);
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food, container, false);

        initViews(view);
        initDatabase();
        initSearchHelper();
        setupRecyclerView();
        setupClickListeners();
        setupSearch();
        showAllItems();

        return view;
    }

    private void showAllItems() {
        List<FoodItem> allItems = dbHelper.getAllFoodItems();
        foodAdapter.updateList(allItems);
        updateEmptyState(allItems);
    }

    private void updateEmptyState(List<FoodItem> list) {
        if (emptyOverlay != null) {
            emptyOverlay.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void initViews(View view) {
        searchFood = view.findViewById(R.id.search_food);
        searchLayout = view.findViewById(R.id.searchLayout);
        filterButton = view.findViewById(R.id.filter);
        addFoodButton = view.findViewById(R.id.add_food);
        foodList = view.findViewById(R.id.food_list);
        emptyOverlay = view.findViewById(R.id.overlay);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(requireContext());
    }

    private void initSearchHelper() {
        searchHelper = new SearchHelper();
        List<FoodItem> allFoods = dbHelper.getAllFoodItems();
        searchHelper.updateFoods(allFoods);
    }

    private void setupRecyclerView() {
        foodList.setLayoutManager(new LinearLayoutManager(getContext()));
        List<FoodItem> initialList = dbHelper.getAllFoodItems();
        foodAdapter = new FoodAdapter(initialList);
        foodAdapter.setOnItemClickListener(this);
        foodList.setAdapter(foodAdapter);
    }

    private void setupClickListeners() {
        addFoodButton.setOnClickListener(v -> openAddFoodFragment());

        filterButton.setOnClickListener(v -> {
            FilterBottomSheetFragment filterSheet = FilterBottomSheetFragment.newInstance();
            filterSheet.show(getParentFragmentManager(), "FilterBottomSheet");
        });
    }

    private void setupSearch() {
        searchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                applyFilters(currentSearchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
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
        String hint = "Search foods";
        if (isFilteringActive) {
            if (!currentDietaryFilter.equals("All") && !currentCategoryFilter.equals("All")) {
                hint = String.format("Search %s + %s", currentDietaryFilter, currentCategoryFilter);
            } else if (!currentDietaryFilter.equals("All")) {
                hint = String.format("Search %s foods", currentDietaryFilter);
            } else if (!currentCategoryFilter.equals("All")) {
                hint = String.format("Search %s", currentCategoryFilter);
            }
        }
        searchFood.setHint(hint);
    }

    @Override
    public void onItemClick(FoodItem foodItem) {
        com.example.caloriecalculator.bottomsheets.FoodBottomSheetFragment bottomSheet =
                com.example.caloriecalculator.bottomsheets.FoodBottomSheetFragment.newInstance(foodItem);
        bottomSheet.show(getParentFragmentManager(), "FoodBottomSheet");
    }

    private void openAddFoodFragment() {
        AddFoodFragment addFoodFragment = new AddFoodFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, addFoodFragment);
        transaction.addToBackStack("AddFoodFragment");
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDataAndSearch();
    }

    public void refreshDataAndSearch() {
        List<FoodItem> updatedList = dbHelper.getAllFoodItems();
        searchHelper.updateFoods(updatedList);
        applyFilters(currentSearchQuery);
    }

    public void onFoodDeleted(long id) {
        searchHelper.removeFood(id);
        refreshDataAndSearch();
    }

}