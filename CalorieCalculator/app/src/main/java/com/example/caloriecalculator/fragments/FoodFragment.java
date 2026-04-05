package com.example.caloriecalculator.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.adapters.FoodAdapter;
import com.example.caloriecalculator.database.DatabaseHelper;
import com.example.caloriecalculator.models.FoodItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class FoodFragment extends Fragment implements FoodAdapter.OnItemClickListener {

    private TextInputEditText searchFood;
    private TextInputLayout searchLayout;
    private MaterialCardView filterButton, addFoodButton;
    private RecyclerView foodList;

    // NEW: Database and Adapter
    private DatabaseHelper dbHelper;
    private FoodAdapter foodAdapter;

    public FoodFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food, container, false);

        initViews(view);
        initDatabase();
        setupRecyclerView();
        setupClickListeners();
        setupSearch();

        return view;
    }

    private void initViews(View view) {
        searchFood = view.findViewById(R.id.search_food);
        searchLayout = view.findViewById(R.id.searchLayout);
        filterButton = view.findViewById(R.id.filter);
        addFoodButton = view.findViewById(R.id.add_food);
        foodList = view.findViewById(R.id.food_list);
    }

    private void initDatabase() {
        dbHelper = new DatabaseHelper(requireContext());
    }

    private void setupRecyclerView() {
        foodList.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load data from database
        List<FoodItem> foodListData = dbHelper.getAllFoodItems();
        foodAdapter = new FoodAdapter(foodListData);
        foodAdapter.setOnItemClickListener(this);  // Set click listener
        foodList.setAdapter(foodAdapter);
    }

    private void setupClickListeners() {
        // + button - Open AddFoodFragment
        addFoodButton.setOnClickListener(v -> openAddFoodFragment());

        // Filter button
        filterButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Filter coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearch() {
        searchFood.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Live search from database
                List<FoodItem> filteredList = dbHelper.searchFoodItems(s.toString());
                foodAdapter.updateList(filteredList);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // IMPLEMENT: Adapter click listener
    @Override
    public void onItemClick(FoodItem foodItem) {
        // Open BottomSheet with food details
        com.example.caloriecalculator.bottomsheets.FoodBottomSheetFragment bottomSheet =
                com.example.caloriecalculator.bottomsheets.FoodBottomSheetFragment.newInstance(foodItem);
        bottomSheet.show(getParentFragmentManager(), "FoodBottomSheet");
    }

    private void openAddFoodFragment() {
        AddFoodFragment addFoodFragment = new AddFoodFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out
        );
        transaction.replace(R.id.fragment_container, addFoodFragment);
        transaction.addToBackStack("AddFoodFragment");
        transaction.commit();
    }

    // Refresh list when returning from AddFoodFragment
    @Override
    public void onResume() {
        super.onResume();
        if (foodAdapter != null) {
            List<FoodItem> updatedList = dbHelper.getAllFoodItems();
            foodAdapter.updateList(updatedList);
        }
    }
}