package com.taskflow.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Board Entity - Top-level container for Kanban columns and tasks.
 * 
 * Boards organize related columns and tasks together. They support
 * multiple views (Kanban, List, Calendar, Timeline) and can be
 * customized with colors and descriptions.
 */
@Entity(
    tableName = "boards",
    indices = {
        @Index(value = "position"),
        @Index(value = "is_favorite")
    }
)
public class Board {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name = "";

    @ColumnInfo(name = "description")
    private String description;

    /**
     * Board color for visual identification
     */
    @ColumnInfo(name = "color")
    private String color;

    /**
     * Icon name or emoji for the board
     */
    @ColumnInfo(name = "icon")
    private String icon;

    /**
     * Position for ordering boards
     */
    @ColumnInfo(name = "position", defaultValue = "0")
    private int position = 0;

    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    private boolean isFavorite = false;

    @ColumnInfo(name = "is_archived", defaultValue = "0")
    private boolean isArchived = false;

    /**
     * Default view type: BOARD, LIST, CALENDAR, TIMELINE
     */
    @NonNull
    @ColumnInfo(name = "default_view", defaultValue = "BOARD")
    private String defaultView = ViewType.BOARD;

    @ColumnInfo(name = "created_at")
    private long createdAt = System.currentTimeMillis();

    @ColumnInfo(name = "updated_at")
    private long updatedAt = System.currentTimeMillis();

    @ColumnInfo(name = "last_accessed_at")
    private long lastAccessedAt = System.currentTimeMillis();

    // ========================================
    // VIEW TYPE CONSTANTS
    // ========================================
    
    public static class ViewType {
        public static final String BOARD = "BOARD";
        public static final String LIST = "LIST";
        public static final String CALENDAR = "CALENDAR";
        public static final String TIMELINE = "TIMELINE";
    }

    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public Board() {
    }

    public Board(@NonNull String name) {
        this.name = name;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    @NonNull
    public String getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(@NonNull String defaultView) {
        this.defaultView = defaultView;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(long lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }

    public void accessed() {
        this.lastAccessedAt = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return id == board.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
