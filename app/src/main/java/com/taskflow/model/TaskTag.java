/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/**
 * TaskTag Junction Table - Many-to-many relationship between Tasks and Tags.
 * 
 * Allows a task to have multiple tags and a tag to be applied to multiple tasks.
 */
@Entity(
    tableName = "task_tags",
    primaryKeys = {"task_id", "tag_id"},
    foreignKeys = {
        @ForeignKey(
            entity = Task.class,
            parentColumns = "id",
            childColumns = "task_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Tag.class,
            parentColumns = "id",
            childColumns = "tag_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index(value = "task_id"),
        @Index(value = "tag_id")
    }
)
public class TaskTag {

    @ColumnInfo(name = "task_id")
    private long taskId;

    @ColumnInfo(name = "tag_id")
    private long tagId;

    @ColumnInfo(name = "created_at")
    private long createdAt = System.currentTimeMillis();

    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public TaskTag() {
    }

    public TaskTag(long taskId, long tagId) {
        this.taskId = taskId;
        this.tagId = tagId;
    }

    // ========================================
    // GETTERS & SETTERS
    // ========================================
    
    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
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
        TaskTag taskTag = (TaskTag) o;
        return taskId == taskTag.taskId && tagId == taskTag.tagId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(taskId * 31 + tagId);
    }
}
