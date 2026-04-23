package com.example.caloriecalculator;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.caloriecalculator.fragments.FoodFragment;
import com.example.caloriecalculator.fragments.HomeFragment;
import com.example.caloriecalculator.fragments.SettingsFragment;
import com.example.caloriecalculator.fragments.StartUpFragment;
import com.example.caloriecalculator.helpers.FilterPrefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView nav;
    private HomeFragment homeFragment;
    private FoodFragment foodFragment;
    private SettingsFragment settingsFragment;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setupWindowInsets();
        initPreferences();
        setupBottomNavigation();
        resetFiltersOnAppStart();

        // Load initial fragment based on onboarding status
        loadInitialFragment();
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    private void initPreferences() {
        prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
    }

    private void setupBottomNavigation() {
        nav = findViewById(R.id.bottom_navigation);
        nav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                selectedFragment = getOrCreateHomeFragment();
            } else if (id == R.id.nav_food) {
                selectedFragment = getOrCreateFoodFragment();
            } else if (id == R.id.nav_settings) {
                selectedFragment = getOrCreateSettingsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void loadInitialFragment() {
        boolean hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false);

        if (hasCompletedOnboarding) {
            // Show home fragment and select home tab
            loadFragment(getOrCreateHomeFragment());
            nav.setSelectedItemId(R.id.nav_home);
        } else {
            // Show startup fragment and hide bottom navigation
            loadFragment(new StartUpFragment());
            nav.setVisibility(View.GONE);
        }
    }

    private HomeFragment getOrCreateHomeFragment() {
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        return homeFragment;
    }

    private FoodFragment getOrCreateFoodFragment() {
        if (foodFragment == null) {
            foodFragment = new FoodFragment();
        }
        return foodFragment;
    }

    private SettingsFragment getOrCreateSettingsFragment() {
        if (settingsFragment == null) {
            settingsFragment = new SettingsFragment();
        }
        return settingsFragment;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        // Only add to backstack for navigation fragments, not onboarding
        if (!(fragment instanceof StartUpFragment)) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    // Call this method when onboarding is completed (from StartUpFragment)
    public void completeOnboarding() {
        prefs.edit().putBoolean("has_completed_onboarding", true).apply();

        // Fade in bottom navigation smoothly
        nav.setVisibility(View.VISIBLE);
        nav.setAlpha(0f);
        nav.animate()
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .start();

        // Smooth FADE transition to HomeFragment
        Fragment homeFragment = getOrCreateHomeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, homeFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        // Select home tab after fade completes
        new android.os.Handler().postDelayed(() -> {
            nav.setSelectedItemId(R.id.nav_home);
        }, 200);
    }
    private void resetFiltersOnAppStart() {
        FilterPrefs prefs = new FilterPrefs(this);
        prefs.clear();   // This will reset both dietary and category to "All"
        Log.d("MainActivity", "🔄 Filters reset to All on app restart");
    }
}