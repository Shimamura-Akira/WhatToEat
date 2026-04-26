package com.example.whattoeat;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private static final String PREF_NAME = "WhatToEatPrefs";
    private static final String KEY_FOOD_LIST = "food_list";

    private final SharedPreferences prefs;
    private final Gson gson;

    public DataManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<FoodItem> getFoodList() {
        String json = prefs.getString(KEY_FOOD_LIST, null);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<FoodItem>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveFoodList(List<FoodItem> foodList) {
        String json = gson.toJson(foodList);
        prefs.edit().putString(KEY_FOOD_LIST, json).apply();
    }
}