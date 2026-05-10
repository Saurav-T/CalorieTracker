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
                "Sedentary",
                "Lightly Active",
                "Moderately Active",
                "Very Active",
                "Extra Active"
        };

        setupDropdown(activityLevelInput, activities, "Select Activity Level");
    }

    // 🔥 Reusable Improved Dropdown Method
    private void setupDropdown(MaterialAutoCompleteTextView autoCompleteTextView,
                               String[] items, String hint) {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                items
        );

        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(0);           // Show immediately
        autoCompleteTextView.setHint(hint);

        // Background for dropdown
        try {
            autoCompleteTextView.setDropDownBackgroundResource(R.drawable.dropdown_background);
        } catch (Exception e) {
            // Fallback if drawable doesn't exist
            autoCompleteTextView.setDropDownBackgroundResource(android.R.color.white);
        }

        // SINGLE TAP → Show dropdown
        autoCompleteTextView.setOnClickListener(v -> {
            autoCompleteTextView.showDropDown();
        });

        // Touch listener
        autoCompleteTextView.setOnTouchListener((v, event) -> {
            autoCompleteTextView.showDropDown();
            return false;
        });

        // Focus → Auto show
        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextView.post(() -> autoCompleteTextView.showDropDown());
            }
        });

        // Optional: Prevent free typing
        // autoCompleteTextView.setKeyListener(null);
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            if (validateInputs()) {
                UserProfile profile = createProfile();
                NutritionCalculator.saveProfile(requireContext(), profile);

                ResultPreviewFragment resultFragment = ResultPreviewFragment.newInstance(profile);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, resultFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private boolean validateInputs() {
        if (ageInput.getText().toString().trim().isEmpty() ||
                weightInput.getText().toString().trim().isEmpty() ||
                heightInput.getText().toString().trim().isEmpty() ||
                activityLevelInput.getText().toString().trim().isEmpty() ||
                (!radioMale.isChecked() && !radioFemale.isChecked())) {

            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
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