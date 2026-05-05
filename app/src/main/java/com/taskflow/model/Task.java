package com.taskflow.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.taskflow.R;

import java.util.Locale;

/**
 * Task Entity - Core data model for tasks in TaskFlow.
 * 
 * Represents a single task item with all its properties including
 * title, description, status, priority, dates, progress, and relationships.
 * 
 * Features:
 * - Foreign key relationships to Column and User
 * - Indexed fields for efficient querying
 * - Timestamps for creation and updates
 * - Soft delete support with archived flag
 */
@Entity(
    tableName = "tasks",
    foreignKeys = {
        @ForeignKey(
            entity = Column.class,
            parentColumns = "id",
            childColumns = "column_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = User.class,
            parentColumns = "id",
            childColumns = "assignee_id",
            onDelete = ForeignKey.SET_NULL
        )
    },
    indices = {
        @Index(value = "column_id"),
        @Index(value = "assignee_id"),
        @Index(value = "status"),
        @Index(value = "priority"),
        @Index(value = "due_date"),
        @Index(value = "created_at"),
        @Index(value = "position"),
        @Index(value = "category")
    }
)
public class Task {

    // ========================================
    // PRIMARY KEY
    // ========================================
    
    @PrimaryKey(autoGenerate = true)
    private long id;

    // ========================================
    // CORE FIELDS
    // ========================================
    
    @NonNull
    @ColumnInfo(name = "title")
    private String title = "";

    @ColumnInfo(name = "description")
    private String description;

    // ========================================
    // STATUS & PRIORITY
    // ========================================
    
    /**
     * Task status: TODO, IN_PROGRESS, REVIEW, DONE, BLOCKED
     */
    @NonNull
    @ColumnInfo(name = "status", defaultValue = "TODO")
    private String status = Status.TODO;

    /**
     * Priority level: URGENT, HIGH, MEDIUM, LOW, NONE
     */
    @NonNull
    @ColumnInfo(name = "priority", defaultValue = "NONE")
    private String priority = Priority.NONE;

    // ========================================
    // RELATIONSHIPS
    // ========================================
    
    @ColumnInfo(name = "column_id")
    private long columnId;

    @ColumnInfo(name = "assignee_id")
    private Long assigneeId;

    @ColumnInfo(name = "board_id")
    private long boardId;

    // ========================================
    // DATES
    // ========================================
    
    @ColumnInfo(name = "due_date")
    private Long dueDate;

    @ColumnInfo(name = "start_date")
    private Long startDate;

    @ColumnInfo(name = "completed_at")
    private Long completedAt;

    @ColumnInfo(name = "created_at")
    private long createdAt = System.currentTimeMillis();

    @ColumnInfo(name = "updated_at")
    private long updatedAt = System.currentTimeMillis();

    // ========================================
    // PROGRESS & POSITION
    // ========================================
    
    /**
     * Task progress percentage (0-100)
     */
    @ColumnInfo(name = "progress", defaultValue = "0")
    private int progress = 0;

    /**
     * Position within the column for ordering
     */
    @ColumnInfo(name = "position", defaultValue = "0")
    private int position = 0;

    // ========================================
    // FLAGS
    // ========================================
    
    @ColumnInfo(name = "is_archived", defaultValue = "0")
    private boolean isArchived = false;

    @ColumnInfo(name = "has_reminder", defaultValue = "0")
    private boolean hasReminder = false;

    @ColumnInfo(name = "reminder_time")
    private Long reminderTime;

    // ========================================
    // METADATA
    // ========================================
    
    /**
     * Color accent for the task card (optional)
     */
    @ColumnInfo(name = "color")
    private String color;

    /**
     * Estimated duration in minutes
     */
    @ColumnInfo(name = "estimated_duration")
    private Integer estimatedDuration;

    /**
     * High-level grouping (dissertation FR: categories): general, work, personal, …
     */
    @NonNull
    @ColumnInfo(name = "category", defaultValue = "general")
    private String category = Category.GENERAL;

    // ========================================
    // STATUS CONSTANTS
    // ========================================
    
    public static class Status {
        public static final String TODO = "TODO";
        public static final String IN_PROGRESS = "IN_PROGRESS";
        public static final String REVIEW = "REVIEW";
        public static final String DONE = "DONE";
        public static final String BLOCKED = "BLOCKED";
    }

    // ========================================
    // PRIORITY CONSTANTS
    // ========================================
    
    public static class Priority {
        public static final String URGENT = "URGENT";
        public static final String HIGH = "HIGH";
        public static final String MEDIUM = "MEDIUM";
        public static final String LOW = "LOW";
        public static final String NONE = "NONE";
    }

    /** Preset category slugs aligned with {@code R.array.task_category_values}. */
    public static final class Category {
        public static final String GENERAL = "general";
        public static final String WORK = "work";
        public static final String PERSONAL = "personal";
        public static final String STUDY = "study";
        public static final String HEALTH = "health";

        private Category() {
        }
    }

    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public Task() {
    }

    public Task(@NonNull String title, long columnId, long boardId) {
        this.title = title;
        this.columnId = columnId;
        this.boardId = boardId;
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
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
        if (Status.DONE.equals(status) && this.completedAt == null) {
            this.completedAt = System.currentTimeMillis();
            this.progress = 100;
        } else if (!Status.DONE.equals(status)) {
            this.completedAt = null;
        }
    }

    @NonNull
    public String getPriority() {
        return priority;
    }

    public void setPriority(@NonNull String priority) {
        this.priority = priority;
    }

    public long getColumnId() {
        return columnId;
    }

    public void setColumnId(long columnId) {
        this.columnId = columnId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public long getBoardId() {
        return boardId;
    }

    public void setBoardId(long boardId) {
        this.boardId = boardId;
    }

    public Long getDueDate() {
        return dueDate;
    }

    public void setDueDate(Long dueDate) {
        this.dueDate = dueDate;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Long completedAt) {
        this.completedAt = completedAt;
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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        if (this.progress == 100 && !Status.DONE.equals(this.status)) {
            setStatus(Status.DONE);
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public boolean hasReminder() {
        return hasReminder;
    }

    public void setHasReminder(boolean hasReminder) {
        this.hasReminder = hasReminder;
    }

    public Long getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(Long reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    @NonNull
    public String getCategory() {
        return category;
    }

    public void setCategory(@NonNull String category) {
        this.category = normalizeCategorySlug(category);
    }

    /** Coerce arbitrary input to one of the preset slugs ({@link Category}). */
    @NonNull
    public static String normalizeCategorySlug(String raw) {
        if (raw == null) return Category.GENERAL;
        String s = raw.trim().toLowerCase(Locale.US);
        if (s.isEmpty()) return Category.GENERAL;
        if (Category.WORK.equals(s)
                || Category.PERSONAL.equals(s)
                || Category.STUDY.equals(s)
                || Category.HEALTH.equals(s)
                || Category.GENERAL.equals(s)) {
            return s;
        }
        return Category.GENERAL;
    }

    /** User-visible label for a stored category slug. */
    @NonNull
    public static String categoryLabel(@NonNull Context context, @NonNull String categorySlug) {
        String slug = normalizeCategorySlug(categorySlug);
        String[] values = context.getResources().getStringArray(R.array.task_category_values);
        String[] labels = context.getResources().getStringArray(R.array.task_category_labels);
        for (int i = 0; i < values.length && i < labels.length; i++) {
            if (slug.equals(values[i])) {
                return labels[i];
            }
        }
        return labels.length > 0 ? labels[0] : slug;
    }

    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Check if the task is overdue
     */
    public boolean isOverdue() {
        return dueDate != null && 
               dueDate < System.currentTimeMillis() && 
               !Status.DONE.equals(status);
    }

    /**
     * Check if the task is due today
     */
    public boolean isDueToday() {
        if (dueDate == null) return false;
        long now = System.currentTimeMillis();
        long startOfDay = now - (now % (24 * 60 * 60 * 1000));
        long endOfDay = startOfDay + (24 * 60 * 60 * 1000);
        return dueDate >= startOfDay && dueDate < endOfDay;
    }

    /**
     * Check if the task is completed
     */
    public boolean isCompleted() {
        return Status.DONE.equals(status);
    }

    /**
     * Mark task complete/incomplete for UI toggles (maps to {@link #setStatus}).
     */
    public void setCompleted(boolean completed) {
        if (completed) {
            setStatus(Status.DONE);
        } else if (Status.DONE.equals(status)) {
            setStatus(Status.TODO);
        }
        touch();
    }

    /** Sort / compare order: URGENT &gt; HIGH &gt; MEDIUM &gt; LOW &gt; NONE */
    public static int priorityRank(@NonNull String priority) {
        switch (priority) {
            case Priority.URGENT:
                return 5;
            case Priority.HIGH:
                return 4;
            case Priority.MEDIUM:
                return 3;
            case Priority.LOW:
                return 2;
            case Priority.NONE:
            default:
                return 1;
        }
    }

    /**
     * Update the timestamp before saving
     */
    public void touch() {
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
