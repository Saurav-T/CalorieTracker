package com.example.caloriecalculator.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.bottomsheets.FoodBottomSheetFragment;
import com.example.caloriecalculator.models.FoodItem;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    
    private List<FoodItem> foodList = new ArrayList<>();
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(FoodItem foodItem);
    }

    public FoodAdapter(List<FoodItem> foodList) {
        this.foodList = foodList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem foodItem = foodList.get(position);
        Log.d("FoodAdapter", "Binding: " + foodItem.getName() +
                " | Icon ID: " + foodItem.getCategoryIcon());
        // Bind data
        holder.foodName.setText(foodItem.getName());
        holder.icon.setImageResource(foodItem.getCategoryIcon());
        holder.servingSize.setText(foodItem.getServingSize());
        holder.servingUnit.setText(foodItem.getUnit());
        holder.calories.setText(foodItem.getCalories());
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public void updateList(List<FoodItem> newList) {
        foodList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    private FoodItem getItemAt(int position) {
        if (position >= 0 && position < foodList.size()) {
            return foodList.get(position);
        }
        return null;
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView foodName, servingSize, servingUnit, calories;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.food_icon);
            foodName = itemView.findViewById(R.id.food_name);
            servingSize = itemView.findViewById(R.id.per_serving);
            servingUnit = itemView.findViewById(R.id.per_serving_measure);
            calories = itemView.findViewById(R.id.calories_per_serving); // Add this ID to layout

            // Click to open BottomSheet
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    FoodItem foodItem = getItemAt(position);
                    if (foodItem != null) {
                        listener.onItemClick(foodItem);
                    }
                }
            });
        }
    }
}