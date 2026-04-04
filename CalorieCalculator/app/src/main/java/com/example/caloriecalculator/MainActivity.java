package com.example.caloriecalculator;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.caloriecalculator.fragments.CalculatorFragment;
import com.example.caloriecalculator.fragments.HomeFragment;
import com.example.caloriecalculator.fragments.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView nav;
    HomeFragment homeFragment;
    CalculatorFragment calculatorFragment;
    SettingsFragment settingsFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                if (homeFragment == null) {
                    homeFragment = HomeFragment.newInstance("", "");
                }
                selectedFragment = homeFragment;
            } else if (id == R.id.nav_calculator) {
                if (calculatorFragment == null) {
                    calculatorFragment = new CalculatorFragment();
                }
                selectedFragment = calculatorFragment;
            } else if (id == R.id.nav_settings) {
                if (settingsFragment == null) {
                    settingsFragment = new SettingsFragment();
                }
                selectedFragment = settingsFragment;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }
            return false;
        });
    }
}