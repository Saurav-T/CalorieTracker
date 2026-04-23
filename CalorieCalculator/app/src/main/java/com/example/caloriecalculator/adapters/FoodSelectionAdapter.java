package com.example.caloriecalculator.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.models.FoodItem;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FoodSelectionAdapter extends RecyclerView.Adapter<FoodSelectionAdapter.FoodViewHolder> {

    private List<FoodItem> foodList;
    private Set<Long> selectedItems = new HashSet<>();
    private OnItemSelectionChangedListener listener;
    private OnItemClickListener itemClickListener; // For bottomsheet

    // 🔥 Filter state (same as FoodFragment)
    private String currentDietaryFilter = "All";
    private String currentCategoryFilter = "All";

    public interface OnItemSelectionChangedListener {
        void onSelectionChanged(int selectedCount);
        void onItemClicked(FoodItem foodItem, boolean isSelected);
    }

    public interface OnItemClickListener {
        void onItemClick(FoodItem foodItem);
    }

    public FoodSelectionAdapter(List<FoodItem> foodList) {
        this.foodList = new ArrayList<>(foodList);
    }

    public void updateList(List<FoodItem> newList) {
        this.foodList.clear();
        this.foodList.addAll(newList);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public List<FoodItem> getSelectedItems() {
        List<FoodItem> selected = new ArrayList<>();
        for (FoodItem item : foodList) {
            if (selectedItems.contains(item.getId())) {
                selected.add(item);
            }
        }
        return selected;
    }

    public void setOnItemSelectionChangedListener(OnItemSelectionChangedListener listener) {
        this.listener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    // 🔥 Filter methods
    public void setFilters(String dietaryFilter, String categoryFilter) {
        this.currentDietaryFilter = dietaryFilter;
        this.currentCategoryFilter = categoryFilter;
    }

    public void clearFilters() {
        this.currentDietaryFilter = "All";
        this.currentCategoryFilter = "All";
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selection, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem foodItem = foodList.get(position);
        holder.bind(foodItem);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView habitIcon;
        TextView foodName, perServing, perServingMeasure, caloriesPerServing;
        MaterialCheckBox selectionCheckbox;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            habitIcon = itemView.findViewById(R.id.habit_icon);
            foodName = itemView.findViewById(R.id.food_name);
            perServing = itemView.findViewById(R.id.per_serving);
            perServingMeasure = itemView.findViewById(R.id.per_serving_measure);
            caloriesPerServing = itemView.findViewById(R.id.calories_per_serving);
            selectionCheckbox = itemView.findViewById(R.id.selection);
        }

        public void bind(FoodItem foodItem) {
            // Set food details
            foodName.setText(foodItem.getName());
            perServing.setText(foodItem.getServingSize());
            perServingMeasure.setText(foodItem.getUnit() != null ? foodItem.getUnit() : "g");
            caloriesPerServing.setText(foodItem.getCalories());

            // Set category icon
            if (foodItem.getCategoryIcon() != 0) {
                habitIcon.setImageResource(foodItem.getCategoryIcon());
            } else {
                habitIcon.setImageResource(R.drawable.ic_grocery);
            }
            habitIcon.setColorFilter(ContextCompat.getColor(habitIcon.getContext(), R.color.dark_grey));

            // Selection state
            boolean isSelected = selectedItems.contains(foodItem.getId());
            selectionCheckbox.setChecked(isSelected);

            // 🔥 Click handling - Checkbox toggle vs Full item click
            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    // Show bottomsheet (like FoodFragment)
                    itemClickListener.onItemClick(foodItem);
                }
            });

            selectionCheckbox.setOnClickListener(v -> toggleSelection(foodItem));
        }

        private void toggleSelection(FoodItem foodItem) {
            long id = foodItem.getId();
            boolean wasSelected = selectedItems.contains(id);

            if (wasSelected) {
                selectedItems.remove(id);
            } else {
                selectedItems.add(id);
            }

            selectionCheckbox.setChecked(!wasSelected);

            // Notify listeners
            if (listener != null) {
                listener.onSelectionChanged(selectedItems.size());
                listener.onItemClicked(foodItem, !wasSelected);
            }
        }
    }
    public void deselectItem(long itemId) {
        if (selectedItems.remove(itemId)) {
            Log.d("FoodSelectionAdapter", "✅ Deselected item ID: " + itemId);
            notifyDataSetChanged();
            if (listener != null) {
                listener.onSelectionChanged(selectedItems.size());
            }
        } else {
            Log.d("FoodSelectionAdapter", "⚠️ Item ID " + itemId + " was not selected");
        }
    }
}