package com.taskflow.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * SavedView Entity - Stores user-defined filter/sort configurations.
 * 
 * Allows users to save their preferred view settings (filters, sorts, grouping)
 * and quickly switch between different views of their tasks.
 */
@Entity(
    tableName = "saved_views",
    indices = {
        @Index(value = "is_default")
    }
)
public class SavedView {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name = "";

    /**
     * JSON string containing filter configuration
     * Example: {"status":["TODO","IN_PROGRESS"],"priority":["HIGH","URGENT"]}
     */
    @ColumnInfo(name = "filters")
    private String filters;

    /**
     * JSON string containing sort configuration
     * Example: {"field":"due_date","direction":"ASC"}
     */
    @ColumnInfo(name = "sort")
    private String sort;

    /**
     * Group by field: null, "status", "priority", "assignee", "due_date"
     */
    @ColumnInfo(name = "group_by")
    private String groupBy;

    /**
     * View type this saved view applies to: BOARD, LIST, CALENDAR, TIMELINE
     */
    @NonNull
    @ColumnInfo(name = "view_type", defaultValue = "LIST")
    private String viewType = Board.ViewType.LIST;

    /**
     * Optional board ID if this view is board-specific
     */
    @ColumnInfo(name = "board_id")
    private Long boardId;

    @ColumnInfo(name = "is_default", defaultValue = "0")
    private boolean isDefault = false;

    @ColumnInfo(name = "created_at")
    private long createdAt = System.currentTimeMillis();

    @ColumnInfo(name = "updated_at")
    private long updatedAt = System.currentTimeMillis();

    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public SavedView() {
    }

    public SavedView(@NonNull String name, @NonNull String viewType) {
        this.name = name;
        this.viewType = viewType;
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

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    @NonNull
    public String getViewType() {
        return viewType;
    }

    public void setViewType(@NonNull String viewType) {
        this.viewType = viewType;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
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

    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SavedView savedView = (SavedView) o;
        return id == savedView.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
