package com.example.caloriecalculator.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.caloriecalculator.models.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CalorieCalculator.db";
    private static final int DATABASE_VERSION = 1;

    // Food table
    public static final String TABLE_FOOD = "food_items";
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
            COLUMN_CATEGORY_ICON + " INTEGER" +  // ← ADD THIS
            ")" ;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FOOD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
        onCreate(db);
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
        db.close();
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
        db.close();
        return foodList;
    }

    // Search food items
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
        db.close();
        return foodList;
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
        db.close();
        return count;
    }
}