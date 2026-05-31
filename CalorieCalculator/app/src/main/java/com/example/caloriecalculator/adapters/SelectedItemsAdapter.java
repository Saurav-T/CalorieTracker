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
import com.example.caloriecalculator.helpers.AppConstants;
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
        ImageView foodIcon, removeIcon;
        TextView foodName, perServing, perServingMeasure, caloriesPerServing;

        public SelectedItemViewHolder(@NonNull View itemView) {
            super(itemView);

            foodIcon = itemView.findViewById(R.id.food_icon);
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
            foodName.setText(foodItem.getName());

            perServing.setText(foodItem.getServingSize());
            perServingMeasure.setText(foodItem.getUnit() != null ? foodItem.getUnit() : "g");

            caloriesPerServing.setText(foodItem.getCalories());

            if (foodItem.getCategoryIcon() != 0) {
                foodIcon.setImageResource(
                        AppConstants.getCategoryIcon(foodItem.getCategory())
                );
                foodIcon.setColorFilter(ContextCompat.getColor(foodIcon.getContext(), R.color.dark_grey));
            } else {
                foodIcon.setImageResource(R.drawable.ic_grocery);
                foodIcon.setColorFilter(ContextCompat.getColor(foodIcon.getContext(), R.color.dark_grey));
            }

            removeIcon.setImageResource(R.drawable.ic_remove);
            removeIcon.setColorFilter(ContextCompat.getColor(removeIcon.getContext(), R.color.white));
        }
    }
}