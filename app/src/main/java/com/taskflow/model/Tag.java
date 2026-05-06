/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Tag Entity - Labels for categorizing and filtering tasks.
 * 
 * Tags support custom colors and can be applied to multiple tasks
 * through the TaskTag junction table.
 */
@Entity(
    tableName = "tags",
    indices = {
        @Index(value = "name", unique = true)
    }
)
public class Tag {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name = "";

    /**
     * Tag color in hex format (e.g., "#4F46E5")
     */
    @ColumnInfo(name = "color")
    private String color;

    /**
     * Background color for the tag chip
     */
    @ColumnInfo(name = "background_color")
    private String backgroundColor;

    @ColumnInfo(name = "created_at")
    private long createdAt = System.currentTimeMillis();

    // ========================================
    // PRESET TAG COLORS
    // ========================================
    
    public static class Colors {
        public static final String RED = "#EF4444";
        public static final String ORANGE = "#F97316";
        public static final String AMBER = "#F59E0B";
        public static final String LIME = "#84CC16";
        public static final String GREEN = "#22C55E";
        public static final String TEAL = "#14B8A6";
        public static final String CYAN = "#06B6D4";
        public static final String BLUE = "#3B82F6";
        public static final String INDIGO = "#6366F1";
        public static final String PURPLE = "#A855F7";
        public static final String PINK = "#EC4899";
        public static final String GRAY = "#6B7280";
    }

    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public Tag() {
    }

    public Tag(@NonNull String name, String color) {
        this.name = name;
        this.color = color;
    }

    // ========================================
    // GETTERS & SETTERS
    // ========================================
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return id == tag.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
