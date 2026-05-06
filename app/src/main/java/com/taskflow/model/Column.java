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
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Column Entity - Represents a Kanban column (e.g., To Do, In Progress, Done).
 * 
 * Columns belong to a board and contain tasks. They can be reordered
 * and customized with colors and WIP (Work In Progress) limits.
 */
@Entity(
    tableName = "columns",
    foreignKeys = {
        @ForeignKey(
            entity = Board.class,
            parentColumns = "id",
            childColumns = "board_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "board_id"),
        @Index(value = "position")
    }
)
public class Column {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name = "";

    @ColumnInfo(name = "board_id")
    private long boardId;

    /**
     * Position within the board for ordering columns
     */
    @ColumnInfo(name = "position", defaultValue = "0")
    private int position = 0;

    /**
     * Color for the column header
     */
    @ColumnInfo(name = "color")
    private String color;

    /**
     * Work In Progress limit (0 = no limit)
     */
    @ColumnInfo(name = "wip_limit", defaultValue = "0")
    private int wipLimit = 0;

    /**
     * Whether this is a "done" type column
     */
    @ColumnInfo(name = "is_done_column", defaultValue = "0")
    private boolean isDoneColumn = false;

    /**
     * Whether the column is collapsed in the UI
     */
    @ColumnInfo(name = "is_collapsed", defaultValue = "0")
    private boolean isCollapsed = false;

    @ColumnInfo(name = "created_at")
    private long createdAt = System.currentTimeMillis();

    @ColumnInfo(name = "updated_at")
    private long updatedAt = System.currentTimeMillis();

    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public Column() {
    }

    public Column(@NonNull String name, long boardId, int position) {
        this.name = name;
        this.boardId = boardId;
        this.position = position;
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

    public long getBoardId() {
        return boardId;
    }

    public void setBoardId(long boardId) {
        this.boardId = boardId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getWipLimit() {
        return wipLimit;
    }

    public void setWipLimit(int wipLimit) {
        this.wipLimit = wipLimit;
    }

    public boolean isDoneColumn() {
        return isDoneColumn;
    }

    public void setDoneColumn(boolean doneColumn) {
        isDoneColumn = doneColumn;
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void setCollapsed(boolean collapsed) {
        isCollapsed = collapsed;
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
        Column column = (Column) o;
        return id == column.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
