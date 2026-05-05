package com.taskflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.taskflow.model.Task;
import com.taskflow.model.TaskWithTags;

import java.util.List;

/**
 * TaskDao - Data Access Object for Task operations.
 * 
 * Provides comprehensive CRUD operations and advanced queries for
 * filtering, sorting, and searching tasks.
 */
@Dao
public interface TaskDao {

    // ========================================
    // BASIC CRUD OPERATIONS
    // ========================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Task task);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Task> tasks);

    @Update
    void update(Task task);

    @Update
    void updateAll(List<Task> tasks);

    @Delete
    void delete(Task task);

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteById(long taskId);

    @Query("DELETE FROM tasks WHERE id IN (:taskIds)")
    void deleteByIds(List<Long> taskIds);

    // ========================================
    // BASIC QUERIES
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<Task> getTaskById(long taskId);

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskByIdSync(long taskId);

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<TaskWithTags> getTaskWithTagsById(long taskId);

    @Query("SELECT * FROM tasks WHERE is_archived = 0 ORDER BY created_at DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks WHERE is_archived = 0 ORDER BY created_at DESC")
    List<Task> getAllTasksSync();

    // ========================================
    // COLUMN/BOARD QUERIES
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE column_id = :columnId AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<Task>> getTasksByColumn(long columnId);

    @Query("SELECT * FROM tasks WHERE column_id = :columnId AND is_archived = 0 ORDER BY position ASC")
    List<Task> getTasksByColumnSync(long columnId);

    @Transaction
    @Query("SELECT * FROM tasks WHERE column_id = :columnId AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<TaskWithTags>> getTasksWithTagsByColumn(long columnId);

    @Query("SELECT * FROM tasks WHERE board_id = :boardId AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<Task>> getTasksByBoard(long boardId);

    @Transaction
    @Query("SELECT * FROM tasks WHERE board_id = :boardId AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<TaskWithTags>> getTasksWithTagsByBoard(long boardId);

    // ========================================
    // STATUS FILTERS
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE status = :status AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<Task>> getTasksByStatus(String status);

    @Query("SELECT * FROM tasks WHERE status IN (:statuses) AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<Task>> getTasksByStatuses(List<String> statuses);

    @Query("SELECT * FROM tasks WHERE status != 'DONE' AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<Task>> getActiveTasks();

    @Query("SELECT * FROM tasks WHERE status = 'DONE' AND is_archived = 0 ORDER BY completed_at DESC")
    LiveData<List<Task>> getCompletedTasks();

    // ========================================
    // PRIORITY FILTERS
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE priority = :priority AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<Task>> getTasksByPriority(String priority);

    @Query("SELECT * FROM tasks WHERE priority IN (:priorities) AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<Task>> getTasksByPriorities(List<String> priorities);

    @Query("SELECT * FROM tasks WHERE priority IN ('URGENT', 'HIGH') AND status != 'DONE' AND is_archived = 0 ORDER BY priority ASC, position ASC")
    LiveData<List<Task>> getHighPriorityTasks();

    // ========================================
    // DATE FILTERS
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE due_date IS NOT NULL AND due_date < :now AND status != 'DONE' AND is_archived = 0 ORDER BY due_date ASC")
    LiveData<List<Task>> getOverdueTasks(long now);

    @Query("SELECT * FROM tasks WHERE due_date >= :startOfDay AND due_date < :endOfDay AND is_archived = 0 ORDER BY due_date ASC")
    LiveData<List<Task>> getTasksDueOnDate(long startOfDay, long endOfDay);

    @Query("SELECT * FROM tasks WHERE due_date >= :start AND due_date < :end AND is_archived = 0 ORDER BY due_date ASC")
    LiveData<List<Task>> getTasksDueBetween(long start, long end);

    @Query("SELECT * FROM tasks WHERE due_date IS NOT NULL AND is_archived = 0 ORDER BY due_date ASC")
    LiveData<List<Task>> getTasksWithDueDate();

    @Transaction
    @Query("SELECT * FROM tasks WHERE due_date >= :start AND due_date < :end AND is_archived = 0 ORDER BY due_date ASC")
    LiveData<List<TaskWithTags>> getTasksWithTagsDueBetween(long start, long end);

    // ========================================
    // ASSIGNEE FILTERS
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE assignee_id = :userId AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<Task>> getTasksByAssignee(long userId);

    @Transaction
    @Query("SELECT * FROM tasks WHERE assignee_id = :userId AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<TaskWithTags>> getTasksWithTagsByAssignee(long userId);

    @Query("SELECT * FROM tasks WHERE assignee_id IS NULL AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<Task>> getUnassignedTasks();

    // ========================================
    // SEARCH
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' "
            + "OR category LIKE '%' || :query || '%') AND is_archived = 0 ORDER BY updated_at DESC")
    LiveData<List<Task>> searchTasks(String query);

    @Transaction
    @Query("SELECT * FROM tasks WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' "
            + "OR category LIKE '%' || :query || '%') AND is_archived = 0 ORDER BY updated_at DESC")
    LiveData<List<TaskWithTags>> searchTasksWithTags(String query);

    // ========================================
    // SORTING QUERIES
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE board_id = :boardId AND is_archived = 0 ORDER BY created_at DESC")
    LiveData<List<Task>> getTasksSortedByCreatedDesc(long boardId);

    @Query("SELECT * FROM tasks WHERE board_id = :boardId AND is_archived = 0 ORDER BY created_at ASC")
    LiveData<List<Task>> getTasksSortedByCreatedAsc(long boardId);

    // Room's SQL parser does not support NULLS FIRST/LAST; put non-null dates first using CASE.
    @Query("SELECT * FROM tasks WHERE board_id = :boardId AND is_archived = 0 ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END ASC, due_date ASC")
    LiveData<List<Task>> getTasksSortedByDueDateAsc(long boardId);

    @Query("SELECT * FROM tasks WHERE board_id = :boardId AND is_archived = 0 ORDER BY CASE WHEN due_date IS NULL THEN 1 ELSE 0 END ASC, due_date DESC")
    LiveData<List<Task>> getTasksSortedByDueDateDesc(long boardId);

    @Query("SELECT * FROM tasks WHERE board_id = :boardId AND is_archived = 0 ORDER BY " +
           "CASE priority " +
           "WHEN 'URGENT' THEN 1 " +
           "WHEN 'HIGH' THEN 2 " +
           "WHEN 'MEDIUM' THEN 3 " +
           "WHEN 'LOW' THEN 4 " +
           "ELSE 5 END ASC")
    LiveData<List<Task>> getTasksSortedByPriorityDesc(long boardId);

    @Query("SELECT * FROM tasks WHERE board_id = :boardId AND is_archived = 0 ORDER BY title ASC")
    LiveData<List<Task>> getTasksSortedByTitleAsc(long boardId);

    @Query("SELECT * FROM tasks WHERE board_id = :boardId AND is_archived = 0 ORDER BY progress DESC")
    LiveData<List<Task>> getTasksSortedByProgressDesc(long boardId);

    @Query("SELECT * FROM tasks WHERE board_id = :boardId AND is_archived = 0 ORDER BY updated_at DESC")
    LiveData<List<Task>> getTasksSortedByUpdatedDesc(long boardId);

    // ========================================
    // POSITION UPDATES
    // ========================================
    
    @Query("UPDATE tasks SET position = :position WHERE id = :taskId")
    void updatePosition(long taskId, int position);

    @Query("UPDATE tasks SET column_id = :columnId, position = :position, updated_at = :timestamp WHERE id = :taskId")
    void moveTask(long taskId, long columnId, int position, long timestamp);

    @Query("SELECT COALESCE(MAX(position), 0) + 1 FROM tasks WHERE column_id = :columnId")
    int getNextPosition(long columnId);

    // ========================================
    // BULK OPERATIONS
    // ========================================
    
    @Query("UPDATE tasks SET status = :status, updated_at = :timestamp WHERE id IN (:taskIds)")
    void updateStatusBulk(List<Long> taskIds, String status, long timestamp);

    @Query("UPDATE tasks SET is_archived = 1, updated_at = :timestamp WHERE id IN (:taskIds)")
    void archiveBulk(List<Long> taskIds, long timestamp);

    @Query("UPDATE tasks SET assignee_id = :assigneeId, updated_at = :timestamp WHERE id IN (:taskIds)")
    void assignBulk(List<Long> taskIds, Long assigneeId, long timestamp);

    @Query("UPDATE tasks SET column_id = :columnId, updated_at = :timestamp WHERE id IN (:taskIds)")
    void moveToColumnBulk(List<Long> taskIds, long columnId, long timestamp);

    // ========================================
    // STATISTICS
    // ========================================
    
    @Query("SELECT COUNT(*) FROM tasks WHERE board_id = :boardId AND is_archived = 0")
    LiveData<Integer> getTaskCount(long boardId);

    @Query("SELECT COUNT(*) FROM tasks WHERE board_id = :boardId AND status = 'DONE' AND is_archived = 0")
    LiveData<Integer> getCompletedTaskCount(long boardId);

    @Query("SELECT COUNT(*) FROM tasks WHERE column_id = :columnId AND is_archived = 0")
    LiveData<Integer> getColumnTaskCount(long columnId);

    @Query("SELECT COUNT(*) FROM tasks WHERE due_date IS NOT NULL AND due_date < :now AND status != 'DONE' AND is_archived = 0")
    LiveData<Integer> getOverdueCount(long now);

    @Query("SELECT COUNT(*) FROM tasks WHERE due_date >= :startOfDay AND due_date < :endOfDay AND is_archived = 0")
    LiveData<Integer> getTasksDueTodayCount(long startOfDay, long endOfDay);

    // ========================================
    // TIMELINE QUERIES
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE start_date IS NOT NULL AND due_date IS NOT NULL AND is_archived = 0 " +
           "AND ((start_date >= :start AND start_date < :end) OR (due_date >= :start AND due_date < :end) OR (start_date < :start AND due_date >= :end)) " +
           "ORDER BY start_date ASC")
    LiveData<List<Task>> getTasksForTimeline(long start, long end);

    @Transaction
    @Query("SELECT * FROM tasks WHERE start_date IS NOT NULL AND due_date IS NOT NULL AND is_archived = 0 " +
           "AND ((start_date >= :start AND start_date < :end) OR (due_date >= :start AND due_date < :end) OR (start_date < :start AND due_date >= :end)) " +
           "ORDER BY start_date ASC")
    LiveData<List<TaskWithTags>> getTasksWithTagsForTimeline(long start, long end);

    // ========================================
    // REMINDERS
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE has_reminder = 1 AND reminder_time IS NOT NULL AND reminder_time > :now AND status != 'DONE' AND is_archived = 0 ORDER BY reminder_time ASC")
    List<Task> getUpcomingReminders(long now);

    @Query("UPDATE tasks SET has_reminder = 0 WHERE id = :taskId")
    void clearReminder(long taskId);

    // ========================================
    // SYNC QUERIES (for WorkManager)
    // ========================================
    
    @Query("SELECT * FROM tasks WHERE due_date >= :start AND due_date < :end AND is_archived = 0")
    List<Task> getTasksDueBetweenSync(long start, long end);

    @Query("SELECT * FROM tasks WHERE due_date IS NOT NULL AND due_date < :now AND status != 'DONE' AND is_archived = 0")
    List<Task> getOverdueTasksSync(long now);

    // ========================================
    // COMPLETION UPDATES
    // ========================================
    
    @Query("UPDATE tasks SET status = 'DONE', completed_at = :timestamp, updated_at = :timestamp WHERE id = :taskId")
    void completeTask(long taskId, long timestamp);
}
