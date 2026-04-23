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
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class CalculationItemsAdapter extends RecyclerView.Adapter<CalculationItemsAdapter.CalculationViewHolder> {

    private List<CalculationItem> calculationItems = new ArrayList<>();
    private OnServingChangedListener servingListener;

    // ✅ SIMPLIFIED INTERFACE (no remove for calculation screen)
    public interface OnServingChangedListener {
        void onServingChanged(CalculationItem item, double newServingSize);
    }

    // ✅ DOUBLE PRECISION
    public static class CalculationItem {
        public FoodItem foodItem;
        public double servingSize = 1.0;  // ✅ Double

        public CalculationItem(FoodItem foodItem) {
            this.foodItem = foodItem;
        }

        public double getCalories() {
            try {
                return Double.parseDouble(foodItem.getCalories()) * servingSize;
            } catch (Exception e) {
                return 0.0;
            }
        }

        public double getFats() {
            try {
                return Double.parseDouble(foodItem.getFats()) * servingSize;
            } catch (Exception e) {
                return 0.0;
            }
        }

        public double getProtein() {
            try {
                return Double.parseDouble(foodItem.getProtein()) * servingSize;
            } catch (Exception e) {
                return 0.0;
            }
        }

        public double getCarbs() {
            try {
                return Double.parseDouble(foodItem.getCarbs()) * servingSize;
            } catch (Exception e) {
                return 0.0;
            }
        }
    }

    public CalculationItemsAdapter() {}

    public void updateItems(List<FoodItem> foodItems) {
        this.calculationItems.clear();
        for (FoodItem foodItem : foodItems) {
            this.calculationItems.add(new CalculationItem(foodItem));
        }
        notifyDataSetChanged();
    }

    public void setOnServingChangedListener(OnServingChangedListener listener) {
        this.servingListener = listener;
    }

    public List<CalculationItem> getItems() {
        return new ArrayList<>(calculationItems);
    }

    // ✅ DOUBLE TOTALS
    public double getTotalCalories() {
        double total = 0.0;
        for (CalculationItem item : calculationItems) total += item.getCalories();
        return total;
    }

    public double getTotalFats() {
        double total = 0.0;
        for (CalculationItem item : calculationItems) total += item.getFats();
        return total;
    }

    public double getTotalProtein() {
        double total = 0.0;
        for (CalculationItem item : calculationItems) total += item.getProtein();
        return total;
    }

    public double getTotalCarbs() {
        double total = 0.0;
        for (CalculationItem item : calculationItems) total += item.getCarbs();
        return total;
    }

    @NonNull
    @Override
    public CalculationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calculation, parent, false);
        return new CalculationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalculationViewHolder holder, int position) {
        CalculationItem item = calculationItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return calculationItems.size();
    }

    class CalculationViewHolder extends RecyclerView.ViewHolder {
        TextView foodName, perServing, perServingMeasure, caloriesPerServing;
        TextView servingSizeText, subIcon, addIcon;
        MaterialCardView subServingCard, addServingCard;
        ImageView foodIcon;

        public CalculationViewHolder(@NonNull View itemView) {
            super(itemView);
            foodName = itemView.findViewById(R.id.food_name);
            perServing = itemView.findViewById(R.id.per_serving);
            perServingMeasure = itemView.findViewById(R.id.per_serving_measure);
            caloriesPerServing = itemView.findViewById(R.id.calories_per_serving);
            servingSizeText = itemView.findViewById(R.id.serving_size);
            subIcon = itemView.findViewById(R.id.sub_icon);
            addIcon = itemView.findViewById(R.id.add_icon);
            subServingCard = itemView.findViewById(R.id.sub_serving);
            addServingCard = itemView.findViewById(R.id.add_serving);
            foodIcon = itemView.findViewById(R.id.food_icon);
        }

        public void bind(CalculationItem item) {
            FoodItem foodItem = item.foodItem;

            foodName.setText(foodItem.getName());
            perServing.setText(foodItem.getServingSize());
            perServingMeasure.setText(foodItem.getUnit() != null ? foodItem.getUnit() : "g");
            caloriesPerServing.setText(foodItem.getCalories());

            if (foodItem.getCategoryIcon() != 0) {
                foodIcon.setImageResource(foodItem.getCategoryIcon());
                foodIcon.setColorFilter(ContextCompat.getColor(foodIcon.getContext(), R.color.dark_grey));
            } else {
                foodIcon.setImageResource(R.drawable.ic_grocery);
                foodIcon.setColorFilter(ContextCompat.getColor(foodIcon.getContext(), R.color.dark_grey));
            }


            // ✅ Display serving with 1 decimal
            servingSizeText.setText(String.format("%.1f", item.servingSize));

            // +/- Buttons (0.5 increments)
            subServingCard.setOnClickListener(v -> {
                if (item.servingSize > 0.5) {
                    item.servingSize -= 0.5;
                    servingSizeText.setText(String.format("%.1f", item.servingSize));
                    if (servingListener != null) {
                        servingListener.onServingChanged(item, item.servingSize);
                    }
                }
            });

            addServingCard.setOnClickListener(v -> {
                item.servingSize += 0.5;
                servingSizeText.setText(String.format("%.1f", item.servingSize));
                if (servingListener != null) {
                    servingListener.onServingChanged(item, item.servingSize);
                }
            });
        }
    }
}