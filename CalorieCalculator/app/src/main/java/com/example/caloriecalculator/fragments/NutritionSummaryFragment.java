package com.example.caloriecalculator.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.helpers.NutritionCalculator;
import com.example.caloriecalculator.models.UserProfile;
import com.google.android.material.card.MaterialCardView;

public class NutritionSummaryFragment extends Fragment {

    private UserProfile profile;
    private NutritionCalculator.DailyRecommendation rec;
    private ImageView backButton;
    private TextView bmiText, healthyRangeText, caloriesText;
    private TextView proteinText, carbsText, fatsText;

    public static NutritionSummaryFragment newInstance() {
        return new NutritionSummaryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profile = NutritionCalculator.loadProfile(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result_preview, container, false);

        initViews(view);
        setupBackButton();
        setupTitle(view);
        calculateAndDisplayResults();
        MaterialCardView continueButton = view.findViewById(R.id.continuebutton);
        continueButton.setVisibility(View.GONE);

        return view;
    }

    private void initViews(View view) {
        bmiText = view.findViewById(R.id.bmi);
        healthyRangeText = view.findViewById(R.id.healthyRange);
        caloriesText = view.findViewById(R.id.caloriesNeeded);
        proteinText = view.findViewById(R.id.protein);
        carbsText = view.findViewById(R.id.carbs);
        fatsText = view.findViewById(R.id.fats);
        backButton = view.findViewById(R.id.back_button);
    }

    private void setupTitle(View view) {
        TextView title = view.findViewById(R.id.title);
        if (title != null) {
            title.setText("Nutrition Summary");
        }
    }

    private void calculateAndDisplayResults() {
        if (profile == null || profile.getAge() == 0) {
            showNoProfileMessage();
            return;
        }

        rec = NutritionCalculator.calculate(profile);
        if (rec == null) {
            showError("Could not calculate nutrition");
            return;
        }

        safeSetText(bmiText, String.format("BMI: %.1f (%s)", rec.bmi, rec.bmiCategory));
        safeSetText(healthyRangeText, rec.healthyWeightRange);
        safeSetText(caloriesText, rec.dailyCalories + " kcal/day");
        safeSetText(proteinText,  rec.proteinG + "g");
        safeSetText(carbsText,  rec.carbsG + "g");
        safeSetText(fatsText,  + rec.fatG + "g");
    }

    private void showNoProfileMessage() {
        safeSetText(bmiText, "N/A");
        safeSetText(healthyRangeText, "Complete your profile to see nutrition summary");
        safeSetText(caloriesText, "N/A");
        safeSetText(proteinText, "N/A");
        safeSetText(carbsText, "N/A");
        safeSetText(fatsText, "N/A");
        
        Toast.makeText(getContext(), "👤 Complete your profile first in Settings", Toast.LENGTH_LONG).show();
    }

    private void showError(String message) {
        Toast.makeText(getContext(), "⚠️ " + message, Toast.LENGTH_SHORT).show();
    }

    private void safeSetText(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }

    private void openProfileEdit() {
        ProfileEditFragment profileFragment = ProfileEditFragment.newInstance();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack("profile_edit")
                .commit();
    }

    private void setupBackButton() {
        backButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });
    }
}