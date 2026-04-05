package com.example.caloriecalculator.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.caloriecalculator.MainActivity;
import com.example.caloriecalculator.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

public class StartUpFragment extends Fragment {

    private TextInputEditText rangeMin;
    private MaterialCardView getStartedButton;

    public StartUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_up, container, false);

        initViews(view);
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        rangeMin = view.findViewById(R.id.rangeMin);
        getStartedButton = view.findViewById(R.id.get_started);
    }

    private void setupClickListeners() {
        getStartedButton.setOnClickListener(v -> handleGetStarted());
    }

    private void handleGetStarted() {
        String dailyGoalInput = rangeMin.getText().toString().trim();
        String dailyGoalToSave = TextUtils.isEmpty(dailyGoalInput) ? "-" : dailyGoalInput;

        // Store daily goal in SharedPreferences
        getActivity().getSharedPreferences("app_prefs", getActivity().MODE_PRIVATE)
                .edit()
                .putString("daily_calorie_goal", dailyGoalToSave)
                .putBoolean("has_completed_onboarding", true)
                .apply();

        // Complete onboarding and navigate to home
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).completeOnboarding();
        }
    }
}