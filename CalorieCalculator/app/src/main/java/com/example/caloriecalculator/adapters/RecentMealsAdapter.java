package com.example.caloriecalculator.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.models.MealItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentMealsAdapter extends RecyclerView.Adapter<RecentMealsAdapter.MealViewHolder> {

    private List<MealItem> recentMeals = new ArrayList<>();

    public interface OnMealClickListener {
        void onMealClick(MealItem meal);
    }

    private OnMealClickListener listener;

    public void setOnMealClickListener(OnMealClickListener listener) {
        this.listener = listener;
    }


    public RecentMealsAdapter() {}

    public void updateMeals(List<MealItem> meals) {
        this.recentMeals.clear();
        this.recentMeals.addAll(meals);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_meal, parent, false);
        return new MealViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        MealItem meal = recentMeals.get(position);
        holder.bind(meal);
    }

    @Override
    public int getItemCount() {
        return recentMeals.size();
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        private final TextView mealName;
        private final TextView mealCalories;
        private final TextView mealTimeAgo;
        private final OnMealClickListener listener;

        public MealViewHolder(@NonNull View itemView, OnMealClickListener listener) {
            super(itemView);
            this.listener = listener;
            mealName = itemView.findViewById(R.id.meal_name);
            mealCalories = itemView.findViewById(R.id.recent_meal_calorie);
            mealTimeAgo = itemView.findViewById(R.id.meal_time_ago);

        }

        public void bind(MealItem meal) {
            mealName.setText(meal.getMealName().length() > 15 ?
                    meal.getMealName().substring(0, 12) + "..." : meal.getMealName());

            mealCalories.setText(String.format(Locale.getDefault(), "%.0f", meal.getTotalCalories()));

            mealTimeAgo.setText(getTimeAgo(meal.getTimestamp()));
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMealClick(meal);
                }
            });
        }

        private String getTimeAgo(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;

            if (diff < 60 * 1000L) return "Just now";
            if (diff < 60 * 60 * 1000L) return (diff / (60 * 1000)) + "m";
            if (diff < 24 * 60 * 60 * 1000L) return (diff / (60 * 60 * 1000)) + "h";
            return new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(timestamp));
        }
    }
}