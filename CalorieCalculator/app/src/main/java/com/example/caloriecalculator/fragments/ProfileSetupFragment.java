package com.example.caloriecalculator.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.helpers.NutritionCalculator;
import com.example.caloriecalculator.models.UserProfile;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileSetupFragment extends Fragment {

    private TextInputEditText ageInput, weightInput, heightInput;
    private MaterialRadioButton radioMale, radioFemale;
    private MaterialAutoCompleteTextView activityLevelInput;
    private MaterialRadioButton radioLoss, radioMaintain, radioGain;
    private MaterialCardView saveButton;

    public static ProfileSetupFragment newInstance() {
        return new ProfileSetupFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_setup, container, false);

        initViews(view);
        setupActivityDropdown();
        loadExistingProfile();
        setupSaveButton();

        return view;
    }

    private void initViews(View view) {
        ageInput = view.findViewById(R.id.age);
        weightInput = view.findViewById(R.id.weight);
        heightInput = view.findViewById(R.id.height);
        radioMale = view.findViewById(R.id.radioMale);
        radioFemale = view.findViewById(R.id.radioFemale);
        activityLevelInput = view.findViewById(R.id.activity_level);
        radioLoss = view.findViewById(R.id.radioLoss);
        radioMaintain = view.findViewById(R.id.radioMaintain);
        radioGain = view.findViewById(R.id.radioGain);
        saveButton = view.findViewById(R.id.save);
    }

    private void setupActivityDropdown() {
        String[] activities = {
                "Sedentary", "Lightly Active", "Moderately Active",
                "Very Active", "Extra Active"
        };

        setupDropdown(activityLevelInput, activities, "Select Activity Level");
    }

    private void setupDropdown(MaterialAutoCompleteTextView autoComplete, String[] items, String hint) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, items);

        autoComplete.setAdapter(adapter);
        autoComplete.setThreshold(0);
        autoComplete.setHint(hint);

        autoComplete.setOnClickListener(v -> autoComplete.showDropDown());
        autoComplete.setOnTouchListener((v, event) -> {
            autoComplete.showDropDown();
            return false;
        });
        autoComplete.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) autoComplete.post(() -> autoComplete.showDropDown());
        });
    }

    private void loadExistingProfile() {
        UserProfile profile = NutritionCalculator.loadProfile(requireContext());

        if (profile.getAge() > 0) {
            ageInput.setText(String.valueOf(profile.getAge()));
        }
        if (profile.getWeightKg() > 0) {
            weightInput.setText(String.format("%.1f", profile.getWeightKg()));
        }
        if (profile.getHeightCm() > 0) {
            heightInput.setText(String.valueOf((int) profile.getHeightCm()));
        }

        // Gender
        if ("male".equalsIgnoreCase(profile.getGender())) {
            radioMale.setChecked(true);
        } else if ("female".equalsIgnoreCase(profile.getGender())) {
            radioFemale.setChecked(true);
        }

        // Activity Level
        if (profile.getActivityLevel() != null && !profile.getActivityLevel().isEmpty()) {
            activityLevelInput.setText(profile.getActivityLevel());
        }

        // Goal
        if ("weight loss".equalsIgnoreCase(profile.getGoal())) {
            radioLoss.setChecked(true);
        } else if ("weight gain".equalsIgnoreCase(profile.getGoal())) {
            radioGain.setChecked(true);
        } else {
            radioMaintain.setChecked(true);
        }
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                UserProfile profile = createProfile();
                NutritionCalculator.saveProfile(requireContext(), profile);

                NutritionCalculator.DailyRecommendation rec = NutritionCalculator.calculate(profile);
                if (rec != null) {
                    requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE)
                            .edit()
                            .putString("daily_calorie_goal", String.valueOf(rec.dailyCalories))
                            .apply();
                }

                Toast.makeText(requireContext(), "✅ Profile saved successfully!", Toast.LENGTH_SHORT).show();

                ResultPreviewFragment resultFragment = ResultPreviewFragment.newInstance(profile);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, resultFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private boolean validateInputs() {
        String ageStr = ageInput.getText().toString().trim();
        String weightStr = weightInput.getText().toString().trim();
        String heightStr = heightInput.getText().toString().trim();
        String activityStr = activityLevelInput.getText().toString().trim();

        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty() || activityStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!radioMale.isChecked() && !radioFemale.isChecked()) {
            Toast.makeText(getContext(), "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int age = Integer.parseInt(ageStr);
            double weight = Double.parseDouble(weightStr);
            double height = Double.parseDouble(heightStr);

            if (age < 12 || age > 100) {
                Toast.makeText(getContext(), "Age should be between 12-100", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (weight < 30 || weight > 200) {
                Toast.makeText(getContext(), "Weight should be between 30-200 kg", Toast.LENGTH_SHORT).show();
                return false;
            }
            if (height < 140 || height > 220) {
                Toast.makeText(getContext(), "Height should be between 140-220 cm", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private UserProfile createProfile() {
        UserProfile profile = new UserProfile();
        profile.setAge(Integer.parseInt(ageInput.getText().toString().trim()));
        profile.setWeightKg(Double.parseDouble(weightInput.getText().toString().trim()));
        profile.setHeightCm(Double.parseDouble(heightInput.getText().toString().trim()));
        profile.setGender(radioMale.isChecked() ? "male" : "female");
        profile.setActivityLevel(activityLevelInput.getText().toString().trim());

        if (radioLoss.isChecked()) profile.setGoal("weight loss");
        else if (radioGain.isChecked()) profile.setGoal("weight gain");
        else profile.setGoal("maintenance");

        return profile;
    }
}