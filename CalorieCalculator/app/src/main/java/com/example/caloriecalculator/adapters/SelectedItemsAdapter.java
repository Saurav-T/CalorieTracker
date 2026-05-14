package com.example.caloriecalculator.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.models.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class SelectedItemsAdapter extends RecyclerView.Adapter<SelectedItemsAdapter.SelectedItemViewHolder> {

    private List<FoodItem> selectedItems = new ArrayList<>();
    private OnItemRemovedListener removeListener;

    public interface OnItemRemovedListener {
        void onItemRemoved(FoodItem foodItem, int position);
    }

    public SelectedItemsAdapter() {}

    public void updateItems(List<FoodItem> items) {
        this.selectedItems.clear();
        this.selectedItems.addAll(items);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < selectedItems.size()) {
            selectedItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, selectedItems.size());
        }
    }

    public void clearItems() {
        this.selectedItems.clear();
        notifyDataSetChanged();
    }

    public List<FoodItem> getItems() {
        return new ArrayList<>(selectedItems);
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public SelectedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected, parent, false); // Your exact layout
        return new SelectedItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedItemViewHolder holder, int position) {
        FoodItem foodItem = selectedItems.get(position);
        holder.bind(foodItem, position);
    }

    @Override
    public int getItemCount() {
        return selectedItems.size();
    }

    class SelectedItemViewHolder extends RecyclerView.ViewHolder {
        ImageView habitIcon, removeIcon;
        TextView foodName, perServing, perServingMeasure, caloriesPerServing;

        public SelectedItemViewHolder(@NonNull View itemView) {
            super(itemView);

            habitIcon = itemView.findViewById(R.id.habit_icon);
            foodName = itemView.findViewById(R.id.food_name);
            perServing = itemView.findViewById(R.id.per_serving);
            perServingMeasure = itemView.findViewById(R.id.per_serving_measure);
            caloriesPerServing = itemView.findViewById(R.id.calories_per_serving);
            removeIcon = itemView.findViewById(R.id.remove_icon);


            itemView.findViewById(R.id.remove).setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && removeListener != null) {
                    FoodItem foodItem = selectedItems.get(position);
                    removeListener.onItemRemoved(foodItem, position);
                }
            });
        }

        public void bind(FoodItem foodItem, int position) {
            // Food name
            foodName.setText(foodItem.getName());

            // Serving size
            perServing.setText(foodItem.getServingSize());
            perServingMeasure.setText(foodItem.getUnit() != null ? foodItem.getUnit() : "g");

            // Calories
            caloriesPerServing.setText(foodItem.getCalories());

            // Icon
            if (foodItem.getCategoryIcon() != 0) {
                habitIcon.setImageResource(foodItem.getCategoryIcon());
                habitIcon.setColorFilter(ContextCompat.getColor(habitIcon.getContext(), R.color.dark_grey));
            } else {
                habitIcon.setImageResource(R.drawable.ic_grocery);
                habitIcon.setColorFilter(ContextCompat.getColor(habitIcon.getContext(), R.color.dark_grey));
            }

            // Remove icon
            removeIcon.setImageResource(R.drawable.ic_remove);
            removeIcon.setColorFilter(ContextCompat.getColor(removeIcon.getContext(), R.color.white));
        }
    }
}