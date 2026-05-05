package com.taskflow.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

/**
 * TaskWithTags - Relationship class for Task with its associated Tags.
 * 
 * Uses Room's @Relation annotation to automatically load tags
 * when querying tasks.
 */
public class TaskWithTags {

    @Embedded
    private Task task;

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = @Junction(
            value = TaskTag.class,
            parentColumn = "task_id",
            entityColumn = "tag_id"
        )
    )
    private List<Tag> tags;

    @Relation(
        parentColumn = "assignee_id",
        entityColumn = "id"
    )
    private User assignee;

    // ========================================
    // GETTERS & SETTERS
    // ========================================
    
    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }
}
