// Replace your entire MealContentsAdapter.java with this:
package com.example.caloriecalculator.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.models.MealItem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealContentsAdapter extends RecyclerView.Adapter<MealContentsAdapter.ContentViewHolder> {

    private List<MealItem.MealFoodSnapshot> snapshots = new ArrayList<>();

    public void updateSnapshots(List<MealItem.MealFoodSnapshot> snapshots) {
        this.snapshots.clear();
        if (snapshots != null) {
            this.snapshots.addAll(snapshots);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_contents, parent, false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentViewHolder holder, int position) {
        if (position >= 0 && position < snapshots.size()) {
            MealItem.MealFoodSnapshot snapshot = snapshots.get(position);
            holder.bind(snapshot);
        }
    }

    @Override
    public int getItemCount() {
        return snapshots.size();
    }

    static class ContentViewHolder extends RecyclerView.ViewHolder {
        private final TextView foodName, foodServing, foodCalorie;

        public ContentViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.food_name);
            foodServing = itemView.findViewById(R.id.food_serving);
            foodCalorie = itemView.findViewById(R.id.food_calorie);
        }

        public void bind(MealItem.MealFoodSnapshot snapshot) {
            if (foodName != null) foodName.setText(snapshot.foodName);
            if (foodServing != null) foodServing.setText(String.format(Locale.getDefault(), "%.1f", snapshot.servingSize));
            if (foodCalorie != null) foodCalorie.setText(String.format(Locale.getDefault(), "%.0f", snapshot.calories));
        }
    }
}