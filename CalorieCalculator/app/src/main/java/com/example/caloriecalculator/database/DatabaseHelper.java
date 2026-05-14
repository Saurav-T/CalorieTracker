package com.example.caloriecalculator.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.caloriecalculator.models.FoodItem;
import com.example.caloriecalculator.models.MealItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CalorieCalculator.db";

    public static final String CSV_EXPORT_FILENAME = "food_items_export.csv";
    private static final int DATABASE_VERSION = 6;

    // Food table
    public static final String TABLE_FOOD = "food_items";
    public static final String TABLE_MEALS = "meals";
    public static final String TABLE_MEAL_FOODS = "meal_foods";

    // Food Columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DIETARY_PREF = "dietary_pref";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_SERVING_SIZE = "serving_size";
    public static final String COLUMN_CALORIES = "calories";
    public static final String COLUMN_FATS = "fats";
    public static final String COLUMN_PROTEIN = "protein";
    public static final String COLUMN_CARBS = "carbs";
    public static final String COLUMN_CATEGORY_ICON = "category_icon";

    // Meal Columns
    public static final String MEAL_ID = "_id";
    public static final String MEAL_NAME = "meal_name";
    public static final String MEAL_TIMESTAMP = "timestamp";
    public static final String MEAL_TOTAL_CALORIES = "total_calories";
    public static final String MEAL_TOTAL_FATS = "total_fats";
    public static final String MEAL_TOTAL_PROTEIN = "total_protein";
    public static final String MEAL_TOTAL_CARBS = "total_carbs";

    // Meal Food Columns
    public static final String MEAL_FOOD_ID = "_id";
    public static final String MEAL_FOOD_MEAL_ID = "meal_id";
    public static final String MEAL_FOOD_FOOD_ID = "food_id";
    public static final String MEAL_FOOD_SERVING_SIZE = "serving_size";
    public static final String MEAL_FOOD_ITEM_CALORIES = "item_calories";
    public static final String MEAL_FOOD_ITEM_FATS = "item_fats";
    public static final String MEAL_FOOD_ITEM_PROTEIN = "item_protein";
    public static final String MEAL_FOOD_ITEM_CARBS = "item_carbs";


    private static final String CREATE_FOOD_TABLE = "CREATE TABLE " + TABLE_FOOD + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT NOT NULL, " +
            COLUMN_DIETARY_PREF + " TEXT, " +
            COLUMN_CATEGORY + " TEXT NOT NULL, " +
            COLUMN_UNIT + " TEXT, " +
            COLUMN_SERVING_SIZE + " TEXT, " +
            COLUMN_CALORIES + " TEXT, " +
            COLUMN_FATS + " TEXT, " +
            COLUMN_PROTEIN + " TEXT, " +
            COLUMN_CARBS + " TEXT," +
            COLUMN_CATEGORY_ICON + " INTEGER" +
            ")" ;

    private static final String CREATE_MEALS_TABLE =
            "CREATE TABLE " + TABLE_MEALS + " (" +
                    MEAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MEAL_NAME + " TEXT NOT NULL, " +
                    MEAL_TIMESTAMP + " INTEGER NOT NULL, " +
                    MEAL_TOTAL_CALORIES + " REAL NOT NULL, " +
                    MEAL_TOTAL_FATS + " REAL NOT NULL, " +
                    MEAL_TOTAL_PROTEIN + " REAL NOT NULL, " +
                    MEAL_TOTAL_CARBS + " REAL NOT NULL" +
                    ")";

    private static final String CREATE_MEAL_FOODS_TABLE =
            "CREATE TABLE " + TABLE_MEAL_FOODS + " (" +
                    MEAL_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MEAL_FOOD_MEAL_ID + " INTEGER NOT NULL, " +
                    MEAL_FOOD_FOOD_ID + " INTEGER NOT NULL, " +
                    MEAL_FOOD_SERVING_SIZE + " REAL NOT NULL, " +
                    MEAL_FOOD_ITEM_CALORIES + " REAL NOT NULL, " +
                    MEAL_FOOD_ITEM_FATS + " REAL NOT NULL, " +
                    MEAL_FOOD_ITEM_PROTEIN + " REAL NOT NULL, " +
                    MEAL_FOOD_ITEM_CARBS + " REAL NOT NULL, " +
                    "FOREIGN KEY(" + MEAL_FOOD_MEAL_ID + ") REFERENCES " + TABLE_MEALS + "(" + MEAL_ID + "), " +
                    "FOREIGN KEY(" + MEAL_FOOD_FOOD_ID + ") REFERENCES " + TABLE_FOOD + "(" + COLUMN_ID + ")" +
                    ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FOOD_TABLE);
        db.execSQL(CREATE_MEALS_TABLE);
        db.execSQL(CREATE_MEAL_FOODS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_MEAL_FOODS + " ADD COLUMN " + MEAL_FOOD_ITEM_FATS + " REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_MEAL_FOODS + " ADD COLUMN " + MEAL_FOOD_ITEM_PROTEIN + " REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_MEAL_FOODS + " ADD COLUMN " + MEAL_FOOD_ITEM_CARBS + " REAL NOT NULL DEFAULT 0");
        }
    }

    // Insert new food item
    public long insertFoodItem(FoodItem foodItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, foodItem.getName());
        values.put(COLUMN_DIETARY_PREF, foodItem.getDietaryPref());
        values.put(COLUMN_CATEGORY, foodItem.getCategory());
        values.put(COLUMN_UNIT, foodItem.getUnit());
        values.put(COLUMN_SERVING_SIZE, foodItem.getServingSize());
        values.put(COLUMN_CALORIES, foodItem.getCalories());
        values.put(COLUMN_FATS, foodItem.getFats());
        values.put(COLUMN_PROTEIN, foodItem.getProtein());
        values.put(COLUMN_CARBS, foodItem.getCarbs());
        values.put(COLUMN_CATEGORY_ICON, foodItem.getCategoryIcon());

        long id = db.insert(TABLE_FOOD, null, values);
        return id;
    }

    // Get all food items
    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> foodList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FOOD, null);

        if (cursor.moveToFirst()) {
            do {
                FoodItem foodItem = new FoodItem();
                foodItem.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                foodItem.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                foodItem.setDietaryPref(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIETARY_PREF)));
                foodItem.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                foodItem.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT)));
                foodItem.setServingSize(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVING_SIZE)));
                foodItem.setCalories(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)));
                foodItem.setFats(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FATS)));
                foodItem.setProtein(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROTEIN)));
                foodItem.setCarbs(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARBS)));
                foodItem.setCategoryIcon(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ICON)));
                foodList.add(foodItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return foodList;
    }

    public int updateFoodItem(FoodItem foodItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, foodItem.getName());
        values.put(COLUMN_DIETARY_PREF, foodItem.getDietaryPref());
        values.put(COLUMN_CATEGORY, foodItem.getCategory());
        values.put(COLUMN_UNIT, foodItem.getUnit());
        values.put(COLUMN_SERVING_SIZE, foodItem.getServingSize());
        values.put(COLUMN_CALORIES, foodItem.getCalories());
        values.put(COLUMN_FATS, foodItem.getFats());
        values.put(COLUMN_PROTEIN, foodItem.getProtein());
        values.put(COLUMN_CARBS, foodItem.getCarbs());
        values.put(COLUMN_CATEGORY_ICON, foodItem.getCategoryIcon());

        int rowsUpdated = db.update(TABLE_FOOD, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(foodItem.getId())});
        return rowsUpdated;
    }

    public List<FoodItem> searchFoodItems(String query) {
        List<FoodItem> foodList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String searchQuery = "%" + query + "%";
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_FOOD + " WHERE " + COLUMN_NAME + " LIKE ? OR " +
                        COLUMN_CATEGORY + " LIKE ?",
                new String[]{searchQuery, searchQuery}
        );

        if (cursor.moveToFirst()) {
            do {
                FoodItem foodItem = new FoodItem();
                foodItem.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                foodItem.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                foodItem.setDietaryPref(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DIETARY_PREF)));
                foodItem.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                foodItem.setUnit(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT)));
                foodItem.setServingSize(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SERVING_SIZE)));
                foodItem.setCalories(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)));
                foodItem.setFats(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FATS)));
                foodItem.setProtein(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROTEIN)));
                foodItem.setCarbs(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARBS)));
                foodItem.setCategoryIcon(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ICON)));  // ← ADD THIS
                foodList.add(foodItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return foodList;
    }

    public boolean deleteFoodItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_FOOD, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        return rowsDeleted > 0;
    }

    // Get food count
    public int getFoodCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FOOD, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // Meal Methods
    public long insertMeal(MealItem meal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MEAL_NAME, meal.getMealName());
        values.put(MEAL_TIMESTAMP, meal.getTimestamp());
        values.put(MEAL_TOTAL_CALORIES, meal.getTotalCalories());
        values.put(MEAL_TOTAL_FATS, meal.getTotalFats());
        values.put(MEAL_TOTAL_PROTEIN, meal.getTotalProtein());
        values.put(MEAL_TOTAL_CARBS, meal.getTotalCarbs());

        long mealId = db.insert(TABLE_MEALS, null, values);
        insertMealFoods(db, mealId, meal.getFoodItems());
        return mealId;
    }

    private void insertMealFoods(SQLiteDatabase db, long mealId, List<MealItem.MealFoodItem> foodItems) {
        for (MealItem.MealFoodItem foodItem : foodItems) {
            ContentValues foodValues = new ContentValues();
            foodValues.put(MEAL_FOOD_MEAL_ID, mealId);
            foodValues.put(MEAL_FOOD_FOOD_ID, foodItem.foodItem.getId());
            foodValues.put(MEAL_FOOD_SERVING_SIZE, foodItem.servingSize);

            try {
                foodValues.put(MEAL_FOOD_ITEM_CALORIES, Double.parseDouble(foodItem.foodItem.getCalories()));
                foodValues.put(MEAL_FOOD_ITEM_FATS, Double.parseDouble(foodItem.foodItem.getFats()));
                foodValues.put(MEAL_FOOD_ITEM_PROTEIN, Double.parseDouble(foodItem.foodItem.getProtein()));
                foodValues.put(MEAL_FOOD_ITEM_CARBS, Double.parseDouble(foodItem.foodItem.getCarbs()));
            } catch (NumberFormatException e) {
                foodValues.put(MEAL_FOOD_ITEM_CALORIES, 0.0);
                foodValues.put(MEAL_FOOD_ITEM_FATS, 0.0);
                foodValues.put(MEAL_FOOD_ITEM_PROTEIN, 0.0);
                foodValues.put(MEAL_FOOD_ITEM_CARBS, 0.0);
            }

            db.insert(TABLE_MEAL_FOODS, null, foodValues);
        }
    }

    public List<MealItem> getAllMeals() {
        List<MealItem> meals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // ✅ Sort by timestamp DESC (newest first)
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_MEALS + " ORDER BY " + MEAL_TIMESTAMP + " DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                MealItem meal = new MealItem();
                meal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MEAL_ID)));
                meal.setMealName(cursor.getString(cursor.getColumnIndexOrThrow(MEAL_NAME)));
                meal.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(MEAL_TIMESTAMP)));  // ✅ Long
                meal.setTotalCalories(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_CALORIES)));
                meal.setTotalFats(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_FATS)));
                meal.setTotalProtein(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_PROTEIN)));
                meal.setTotalCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_CARBS)));
                meals.add(meal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return meals;
    }
    public List<MealItem> getTodayMeals() {
        long todayStart = getTodayStartTimestamp();
        String[] args = {String.valueOf(todayStart)};
        List<MealItem> meals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_MEALS + " WHERE " + MEAL_TIMESTAMP + " >= ? ORDER BY " + MEAL_TIMESTAMP + " DESC",
                args
        );
        if (cursor.moveToFirst()) {
            do {
                MealItem meal = new MealItem();
                meal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MEAL_ID)));
                meal.setMealName(cursor.getString(cursor.getColumnIndexOrThrow(MEAL_NAME)));
                meal.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(MEAL_TIMESTAMP)));  // ✅ Long
                meal.setTotalCalories(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_CALORIES)));
                meal.setTotalFats(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_FATS)));
                meal.setTotalProtein(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_PROTEIN)));
                meal.setTotalCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_CARBS)));
                meals.add(meal);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return meals;
    }

    private long getTodayStartTimestamp() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public MealItem getMealById(long mealId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_MEALS + " WHERE " + MEAL_ID + " = ?",
                new String[]{String.valueOf(mealId)}
        );

        MealItem meal = null;
        if (cursor.moveToFirst()) {
            meal = new MealItem();
            meal.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MEAL_ID)));
            meal.setMealName(cursor.getString(cursor.getColumnIndexOrThrow(MEAL_NAME)));
            meal.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(MEAL_TIMESTAMP)));
            meal.setTotalCalories(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_CALORIES)));
            meal.setTotalFats(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_FATS)));
            meal.setTotalProtein(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_PROTEIN)));
            meal.setTotalCarbs(cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_TOTAL_CARBS)));

            // ✅ Use snapshots instead of foodItems
            meal.setFoodSnapshots(getMealFoodSnapshots(mealId));
        }
        cursor.close();
        return meal;
    }

    private List<MealItem.MealFoodItem> getMealFoodItems(long mealId) {
        List<MealItem.MealFoodItem> foodItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT mf.*, f.* FROM " + TABLE_MEAL_FOODS + " mf " +
                        "JOIN " + TABLE_FOOD + " f ON mf." + MEAL_FOOD_FOOD_ID + " = f." + COLUMN_ID +
                        " WHERE mf." + MEAL_FOOD_MEAL_ID + " = ?",
                new String[]{String.valueOf(mealId)}
        );

        if (cursor.moveToFirst()) {
            do {
                // Create FoodItem
                FoodItem foodItem = new FoodItem();
                foodItem.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                foodItem.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                foodItem.setCalories(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CALORIES)));
                // Add other fields as needed...

                MealItem.MealFoodItem mealFoodItem = new MealItem.MealFoodItem(foodItem,
                        cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_FOOD_SERVING_SIZE)));
                foodItems.add(mealFoodItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return foodItems;
    }

    // Add this METHOD to DatabaseHelper.java (after getMealFoodItems method)
    private List<MealItem.MealFoodSnapshot> getMealFoodSnapshots(long mealId) {
        List<MealItem.MealFoodSnapshot> snapshots = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT mf.*, f." + COLUMN_NAME + " FROM " + TABLE_MEAL_FOODS + " mf " +
                        "JOIN " + TABLE_FOOD + " f ON mf." + MEAL_FOOD_FOOD_ID + " = f." + COLUMN_ID +
                        " WHERE mf." + MEAL_FOOD_MEAL_ID + " = ?",
                new String[]{String.valueOf(mealId)}
        );

        if (cursor.moveToFirst()) {
            do {
                MealItem.MealFoodSnapshot snapshot = new MealItem.MealFoodSnapshot(
                        cursor.getLong(cursor.getColumnIndexOrThrow(MEAL_FOOD_FOOD_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_FOOD_SERVING_SIZE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_FOOD_ITEM_CALORIES)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_FOOD_ITEM_FATS)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_FOOD_ITEM_PROTEIN)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(MEAL_FOOD_ITEM_CARBS))
                );
                snapshots.add(snapshot);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return snapshots;
    }

    public boolean deleteMeal(long mealId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Delete meal foods first
            db.delete(TABLE_MEAL_FOODS, MEAL_FOOD_MEAL_ID + "=?", new String[]{String.valueOf(mealId)});
            // Delete meal
            int rowsDeleted = db.delete(TABLE_MEALS, MEAL_ID + "=?", new String[]{String.valueOf(mealId)});
            db.setTransactionSuccessful();
            return rowsDeleted > 0;
        } finally {
            db.endTransaction();
        }
    }

    //CSV Methods

    public boolean exportFoodItemsToCsv(Context context, Uri outputUri) {
        try {
            List<FoodItem> foodItems = getAllFoodItems();
            if (foodItems.isEmpty()) {
                Log.w("DatabaseHelper", "No food items to export");
                return false;
            }

            Log.d("DatabaseHelper", "Exporting " + foodItems.size() + " food items");

            OutputStream outputStream = context.getContentResolver().openOutputStream(outputUri);
            if (outputStream == null) {
                Log.e("DatabaseHelper", "Cannot open output stream");
                return false;
            }

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"))) {
                // Write header
                writer.write("id,name,dietary_pref,category,unit,serving_size,calories,fats,protein,carbs,category_icon\n");
                writer.flush();

                // Write data
                int count = 0;
                for (FoodItem food : foodItems) {
                    writer.write(Long.toString(food.getId()));
                    writer.write(",");
                    writer.write(escapeCsvField(food.getName()));
                    writer.write(",");
                    writer.write(escapeCsvField(food.getDietaryPref() != null ? food.getDietaryPref() : ""));
                    writer.write(",");
                    writer.write(escapeCsvField(food.getCategory()));
                    writer.write(",");
                    writer.write(escapeCsvField(food.getUnit() != null ? food.getUnit() : ""));
                    writer.write(",");
                    writer.write(escapeCsvField(food.getServingSize() != null ? food.getServingSize() : ""));
                    writer.write(",");
                    writer.write(escapeCsvField(food.getCalories() != null ? food.getCalories() : ""));
                    writer.write(",");
                    writer.write(escapeCsvField(food.getFats() != null ? food.getFats() : ""));
                    writer.write(",");
                    writer.write(escapeCsvField(food.getProtein() != null ? food.getProtein() : ""));
                    writer.write(",");
                    writer.write(escapeCsvField(food.getCarbs() != null ? food.getCarbs() : ""));
                    writer.write(",");
                    writer.write(Integer.toString(food.getCategoryIcon()));
                    writer.write("\n");
                    count++;
                }
                writer.flush();
                Log.d("DatabaseHelper", "Successfully wrote " + count + " rows to CSV");
                return true;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Export failed", e);
            return false;
        }
    }

    public int importFoodItemsFromCsv(Context context, Uri csvUri) {
        InputStream inputStream = null;
        BufferedReader reader = null;
        SQLiteDatabase db = null;

        try {
            inputStream = context.getContentResolver().openInputStream(csvUri);
            if (inputStream == null) {
                Log.e("DatabaseHelper", "Cannot open input stream");
                return -1;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            int importedCount = 0;
            int skippedCount = 0;
            boolean isFirstLine = true;

            // FIXED: Get writable DB once and keep it open
            db = this.getWritableDatabase();

            // FIXED: Wait for any locks to clear
            waitForDatabaseUnlock(db);

            db.beginTransaction();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] columns = parseCsvLine(line);
                if (columns.length < 11) {
                    skippedCount++;
                    continue;
                }

                FoodItem foodItem = parseCsvRow(columns);
                if (foodItem != null && isValidFoodItem(foodItem)) {
                    // FIXED: Check existence WITHOUT opening new DB connection
                    if (!foodItemExistsInTransaction(db, foodItem.getName(), foodItem.getCategory())) {
                        long id = insertFoodItemInTransaction(db, foodItem);
                        if (id > 0) {
                            importedCount++;
                            Log.d("DatabaseHelper", "Imported: " + foodItem.getName());
                        }
                    } else {
                        skippedCount++;
                    }
                } else {
                    skippedCount++;
                    Log.w("DatabaseHelper", "Skipped invalid: " + (columns.length > 1 ? columns[1] : "unknown"));
                }
            }

            db.setTransactionSuccessful();
            Log.d("DatabaseHelper", "Import complete: " + importedCount + " imported, " + skippedCount + " skipped");
            return importedCount;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Import failed", e);
            return -1;
        } finally {
            // FIXED: Proper cleanup
            if (db != null && db.inTransaction()) {
                try {
                    db.endTransaction();
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "Transaction end failed", e);
                }
            }
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                    Log.e("DatabaseHelper", "DB close failed", e);
                }
            }
            closeQuietly(reader);
            closeQuietly(inputStream);
        }
    }

    private void waitForDatabaseUnlock(SQLiteDatabase db) {
        int maxWait = 10; // 5 seconds max
        while (maxWait-- > 0) {
            try {
                db.execSQL("PRAGMA busy_timeout = 500"); // 500ms timeout
                return;
            } catch (Exception e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }


    private String[] parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                // End of field
                columns.add(field.toString().trim());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }

        // Add last field
        columns.add(field.toString().trim());

        // Ensure we have at least 11 columns (pad with empty if needed)
        while (columns.size() < 11) {
            columns.add("");
        }

        Log.d("CSVParser", "Parsed line to " + columns.size() + " columns: " +
                Arrays.toString(columns.toArray()));

        return columns.toArray(new String[0]);
    }


    private FoodItem parseCsvRow(String[] columns) {
        try {
            if (columns.length < 11) {
                Log.w("CSVParser", "Too few columns: " + columns.length);
                return null;
            }

            FoodItem foodItem = new FoodItem();

            foodItem.setName(cleanCsvField(columns[1]));
            foodItem.setDietaryPref(cleanCsvField(columns[2]));
            foodItem.setCategory(cleanCsvField(columns[3]));
            foodItem.setUnit(cleanCsvField(columns[4]));
            foodItem.setServingSize(cleanCsvField(columns[5]));
            foodItem.setCalories(cleanCsvField(columns[6]));
            foodItem.setFats(cleanCsvField(columns[7]));
            foodItem.setProtein(cleanCsvField(columns[8]));
            foodItem.setCarbs(cleanCsvField(columns[9]));

            try {
                foodItem.setCategoryIcon(Integer.parseInt(cleanCsvField(columns[10])));
            } catch (NumberFormatException e) {
                foodItem.setCategoryIcon(0);
            }

            Log.d("CSVParser", "Parsed food: " + foodItem.getName() + " | " + foodItem.getCategory());
            return foodItem;

        } catch (Exception e) {
            Log.e("CSVParser", "Parse row failed", e);
            return null;
        }
    }

    private String cleanCsvField(String field) {
        if (field == null) return "";

        // Remove surrounding quotes
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1);
        }

        // Unescape double quotes
        field = field.replace("\"\"", "\"");

        return field.trim();
    }

    private boolean foodItemExistsInTransaction(SQLiteDatabase db, String name, String category) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_FOOD +
                            " WHERE " + COLUMN_NAME + " = ? AND " + COLUMN_CATEGORY + " = ?",
                    new String[]{name, category}
            );
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
            return false;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Exists check failed", e);
            return false;
        } finally {
            closeQuietly(cursor);
        }
    }

    private long insertFoodItemInTransaction(SQLiteDatabase db, FoodItem foodItem) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, foodItem.getName());
        values.put(COLUMN_DIETARY_PREF, foodItem.getDietaryPref());
        values.put(COLUMN_CATEGORY, foodItem.getCategory());
        values.put(COLUMN_UNIT, foodItem.getUnit());
        values.put(COLUMN_SERVING_SIZE, foodItem.getServingSize());
        values.put(COLUMN_CALORIES, foodItem.getCalories());
        values.put(COLUMN_FATS, foodItem.getFats());
        values.put(COLUMN_PROTEIN, foodItem.getProtein());
        values.put(COLUMN_CARBS, foodItem.getCarbs());
        values.put(COLUMN_CATEGORY_ICON, foodItem.getCategoryIcon());

        try {
            return db.insert(TABLE_FOOD, null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Insert failed for " + foodItem.getName(), e);
            return -1;
        }
    }
    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }


    private boolean isValidFoodItem(FoodItem foodItem) {
        // Log all validation steps
        Log.d("Validator", "Validating: " + foodItem.getName() + " | Cat: '" +
                foodItem.getCategory() + "' | Dietary: '" + foodItem.getDietaryPref() + "'");

        // 1. Required: Name
        String name = foodItem.getName();
        if (name == null || name.trim().isEmpty()) {
            Log.w("Validator", "❌ EMPTY NAME");
            return false;
        }

        // 2. Required: Category - case-insensitive exact match
        String category = foodItem.getCategory();
        if (category == null || category.trim().isEmpty()) {
            Log.w("Validator", "❌ EMPTY CATEGORY");
            return false;
        }

        category = category.trim();
        String[] validCategories = getCategories();
        boolean validCategory = false;
        String matchedCategory = null;

        for (String cat : validCategories) {
            if (cat.trim().equalsIgnoreCase(category)) {
                validCategory = true;
                matchedCategory = cat;
                break;
            }
        }

        if (!validCategory) {
            Log.w("Validator", "❌ INVALID CATEGORY: '" + category + "' not in: " +
                    Arrays.toString(validCategories));
            return false;
        }
        Log.d("Validator", "✅ Category OK: '" + category + "' → '" + matchedCategory + "'");

        // 3. Dietary (optional)
        String dietary = foodItem.getDietaryPref();
        if (dietary != null && !dietary.trim().isEmpty()) {
            dietary = dietary.trim();
            if (!"Vegetarian".equalsIgnoreCase(dietary) &&
                    !"Non-Vegetarian".equalsIgnoreCase(dietary)) {
                Log.w("Validator", "❌ INVALID DIETARY: '" + dietary + "'");
                return false;
            }
        }
        Log.d("Validator", "✅ Dietary OK: '" + dietary + "'");

        // 4. Numeric fields (allow empty)
        String[] numericFields = {
                foodItem.getCalories(),
                foodItem.getFats(),
                foodItem.getProtein(),
                foodItem.getCarbs()
        };

        for (int i = 0; i < numericFields.length; i++) {
            String field = numericFields[i];
            if (field != null && !field.trim().isEmpty()) {
                try {
                    Double.parseDouble(field.trim());
                    Log.d("Validator", "✅ Numeric OK[" + i + "]: '" + field + "'");
                } catch (NumberFormatException e) {
                    Log.w("Validator", "❌ INVALID NUMERIC[" + i + "]: '" + field + "'");
                    return false;
                }
            } else {
                Log.d("Validator", "⚪ Empty numeric OK[" + i + "]");
            }
        }

        Log.d("Validator", "🎉 FULLY VALID: " + foodItem.getName());
        return true;
    }
    public String[] getDietaryOptions() {
        return new String[]{"Vegetarian", "Non-Vegetarian"};
    }

    public String[] getCategories() {
        return new String[]{
                "Grains",
                "Milk and Milk Products",
                "Fruit and Fruit Products",
                "Eggs",
                "Meat and Poultry",
                "Vegetables",
                "Seeds And Nuts",
                "Sugar And Sugar Products",
                "Non Alcoholic Beverages",
                "Alcoholic Beverages",
                "Discretionary"
        };
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        String escaped = field.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String unescapeCsvField(String field) {
        if (field == null) return "";
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1);
        }
        return field.replace("\"\"", "\"");
    }
}