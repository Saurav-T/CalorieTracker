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
        getStartedButton = view.findViewById(R.id.get_started);
    }

    private void setupClickListeners() {
        getStartedButton.setOnClickListener(v -> handleGetStarted());
    }

    private void handleGetStarted() {
        ProfileSetupFragment profileFragment = ProfileSetupFragment.newInstance();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit();
    }
}