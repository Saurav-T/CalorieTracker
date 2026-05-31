package com.example.caloriecalculator.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.database.DatabaseHelper;
import com.example.caloriecalculator.helpers.NutritionCalculator;
import com.example.caloriecalculator.models.FoodItem;
import com.example.caloriecalculator.models.MealItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class SettingsFragment extends Fragment {

    private TextInputEditText calorieIntakeInput;
    private TextInputLayout calorieIntakeLayout;
    private MaterialCardView saveButton, resetAppButton;
    private MaterialCardView exportCsvButton, importCsvButton;
    private MaterialCardView profileButton, nutritionSummaryButton;
    private DatabaseHelper dbHelper;
    private ActivityResultLauncher<Intent> exportFileLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Intent> importFileLauncher;
    private static final int STORAGE_PERMISSION_CODE = 1001;

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews(view);
        initActivityResultLaunchers();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        profileButton = view.findViewById(R.id.profile);  // Fixed ID
        nutritionSummaryButton = view.findViewById(R.id.nutrition_summary);
        resetAppButton = view.findViewById(R.id.reset_app);
        exportCsvButton = view.findViewById(R.id.export_csv);
        importCsvButton = view.findViewById(R.id.import_csv);
        dbHelper = new DatabaseHelper(requireContext());
    }

    private void initActivityResultLaunchers() {
        // Export file picker
        exportFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            exportToCsv(uri);
                        }
                    }
                });

        // Import file picker
        importFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            importFromCsv(uri);
                        }
                    }
                });

        // Permission launcher
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    if (permissions.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false) ||
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        performExport();
                    } else {
                        Toast.makeText(getContext(), "Storage permission required for CSV export", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupClickListeners() {
        if (nutritionSummaryButton != null) {
            nutritionSummaryButton.setOnClickListener(v -> openNutritionSummary());
        }
        profileButton.setOnClickListener(v -> openProfileEdit());
        resetAppButton.setOnClickListener(v -> showResetConfirmation());
        exportCsvButton.setOnClickListener(v -> checkStoragePermission());
        importCsvButton.setOnClickListener(v -> openImportFilePicker());
    }

    private void openProfileEdit() {
        ProfileEditFragment profileFragment = ProfileEditFragment.newInstance();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack("profile_edit")
                .commit();
    }

    private void openNutritionSummary() {
        NutritionSummaryFragment summaryFragment = NutritionSummaryFragment.newInstance();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, summaryFragment)
                .addToBackStack("nutrition_summary")
                .commit();
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            performExport();
        } else if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            performExport();
        } else {
            permissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }
    private void performExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/csv")
                .putExtra(Intent.EXTRA_TITLE, "food_items_" + System.currentTimeMillis() + ".csv");
        exportFileLauncher.launch(intent);
    }

    private void exportToCsv(Uri uri) {
        if (dbHelper.getFoodCount() == 0) {
            Toast.makeText(getContext(), "No food items to export. Add some foods first!", Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(() -> {
            boolean success = dbHelper.exportFoodItemsToCsv(requireContext(), uri);
            requireActivity().runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(getContext(),
                            "Exported " + dbHelper.getFoodCount() + " food items to CSV!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "Export failed. Check logcat for details.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void openImportFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        String[] mimeTypes = {
                "text/comma-separated-values",
                "text/csv",
                "text/plain",
                "application/csv",
                "*/*"
        };

        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_TITLE, "Select CSV file");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent chooserIntent = Intent.createChooser(intent, "Select CSV file to import");

        Intent downloadsIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        downloadsIntent.putExtra("android.provider.extra.INITIAL_URI",
                Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADownload"));

        try {
            importFileLauncher.launch(chooserIntent);
        } catch (Exception e) {
            importFileLauncher.launch(downloadsIntent);
        }
    }

    private void importFromCsv(Uri csvUri) {
        new Thread(() -> {
            int importedCount = dbHelper.importFoodItemsFromCsv(requireContext(), csvUri);
            requireActivity().runOnUiThread(() -> {
                if (importedCount > 0) {
                    Toast.makeText(getContext(),
                            "✅ Imported " + importedCount + " food items successfully!",
                            Toast.LENGTH_LONG).show();
                } else if (importedCount == 0) {
                    Toast.makeText(getContext(), "ℹ️ No valid food items found in CSV", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "❌ Import failed - check CSV format", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showResetConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reset App")
                .setMessage("This will delete ALL meals, foods, and settings.\nThis action cannot be undone!")
                .setPositiveButton("Reset Everything", (dialog, which) -> resetAppData())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetAppData() {
        new Thread(() -> {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

                List<MealItem> allMeals = dbHelper.getAllMeals();
                for (MealItem meal : allMeals) {
                    dbHelper.deleteMeal(meal.getId());
                }

                List<FoodItem> allFoods = dbHelper.getAllFoodItems();
                for (FoodItem food : allFoods) {
                    dbHelper.deleteFoodItem(food.getId());
                }

                SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", requireActivity().MODE_PRIVATE);
                prefs.edit().clear().apply();

                NutritionCalculator.clearProfile(requireContext());

                SharedPreferences profilePrefs = requireContext().getSharedPreferences("user_profile", requireContext().MODE_PRIVATE);
                profilePrefs.edit().clear().apply();

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "App has been completely reset!\nProfile, foods, meals & settings cleared.", Toast.LENGTH_LONG).show();

                    if (calorieIntakeInput != null) {
                        calorieIntakeInput.setText("");
                    }

                    refreshHomeFragment();
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Reset failed", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void refreshHomeFragment() {
        Fragment homeFragment = getParentFragmentManager().findFragmentById(R.id.fragment_container);

        if (homeFragment instanceof HomeFragment) {
            HomeFragment home = (HomeFragment) homeFragment;
            home.refreshData();
        } else {
            HomeFragment newHome = new HomeFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, newHome)
                    .commit();
        }
    }
}