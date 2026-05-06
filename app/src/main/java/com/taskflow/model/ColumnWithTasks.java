/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

/**
 * ColumnWithTasks - Relationship class for Column with its Tasks.
 * 
 * Used for loading Kanban board columns with all their tasks
 * in a single query.
 */
public class ColumnWithTasks {

    @Embedded
    private Column column;

    @Relation(
        parentColumn = "id",
        entityColumn = "column_id"
    )
    private List<Task> tasks;

    // ========================================
    // GETTERS & SETTERS
    // ========================================
    
    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Get the task count for this column
     */
    public int getTaskCount() {
        return tasks != null ? tasks.size() : 0;
    }
}
