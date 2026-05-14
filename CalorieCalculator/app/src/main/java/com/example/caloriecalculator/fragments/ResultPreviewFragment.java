package com.example.caloriecalculator.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.helpers.NutritionCalculator;
import com.example.caloriecalculator.models.UserProfile;

public class ResultPreviewFragment extends Fragment {

    private UserProfile profile;
    private NutritionCalculator.DailyRecommendation rec;

    private TextView bmiText, healthyRangeText, caloriesText;
    private TextView proteinText, carbsText, fatsText;

    public static ResultPreviewFragment newInstance(UserProfile profile) {
        ResultPreviewFragment fragment = new ResultPreviewFragment();
        Bundle args = new Bundle();
        args.putSerializable("profile", profile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            profile = (UserProfile) getArguments().getSerializable("profile");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result_preview, container, false);

        initViews(view);
        calculateAndDisplayResults();

        view.findViewById(R.id.continuebutton).setOnClickListener(v -> completeOnboarding());

        return view;
    }

    private void initViews(View view) {
        bmiText = view.findViewById(R.id.bmi);
        healthyRangeText = view.findViewById(R.id.healthyRange);
        caloriesText = view.findViewById(R.id.caloriesNeeded);
        proteinText = view.findViewById(R.id.protein);
        carbsText = view.findViewById(R.id.carbs);
        fatsText = view.findViewById(R.id.fats);
    }

    private void calculateAndDisplayResults() {
        if (profile == null) {
            showError("Profile data is missing");
            return;
        }

        rec = NutritionCalculator.calculate(profile);

        if (rec == null) {
            showError("Could not calculate nutrition. Please check your inputs.");
            return;
        }

        if (bmiText != null) {
            bmiText.setText(String.format("%.1f (%s)", rec.bmi, rec.bmiCategory));
        }
        if (healthyRangeText != null) {
            healthyRangeText.setText(rec.healthyWeightRange);
        }
        if (caloriesText != null) {
            caloriesText.setText(rec.dailyCalories + " kcal");
        }
        if (proteinText != null) proteinText.setText(rec.proteinG + "g");
        if (carbsText != null) carbsText.setText(rec.carbsG + "g");
        if (fatsText != null) fatsText.setText(rec.fatG + "g");
    }

    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        if (caloriesText != null) caloriesText.setText("N/A");
    }

    private void completeOnboarding() {
        if (rec == null) {
            Toast.makeText(getContext(), "Please complete your profile first", Toast.LENGTH_SHORT).show();
            return;
        }

        requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE)
                .edit()
                .putBoolean("has_completed_onboarding", true)
                .putString("daily_calorie_goal", String.valueOf(rec.dailyCalories))
                .apply();

        if (requireActivity() instanceof com.example.caloriecalculator.MainActivity) {
            ((com.example.caloriecalculator.MainActivity) requireActivity()).completeOnboarding();
        }
    }
}