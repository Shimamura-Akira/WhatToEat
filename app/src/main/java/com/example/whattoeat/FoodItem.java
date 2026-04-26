package com.example.whattoeat;

import java.util.Objects;
import java.util.UUID;

public class FoodItem {
    private String id;
    private String name;
    private boolean isEnabled;

    public FoodItem(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.isEnabled = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return isEnabled == foodItem.isEnabled && Objects.equals(id, foodItem.id) && Objects.equals(name, foodItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, isEnabled);
    }
}