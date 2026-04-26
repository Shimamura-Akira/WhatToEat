package com.example.whattoeat;

import java.util.Objects;
import java.util.UUID;

public class FoodItem {
    private String id;
    private String name;

    public FoodItem(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return Objects.equals(id, foodItem.id) && Objects.equals(name, foodItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}