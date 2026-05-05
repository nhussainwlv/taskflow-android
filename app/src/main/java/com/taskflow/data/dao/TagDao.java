package com.taskflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.taskflow.model.Tag;
import com.taskflow.model.TaskTag;

import java.util.List;

/**
 * TagDao - Data Access Object for Tag operations.
 */
@Dao
public interface TagDao {

    // ========================================
    // BASIC CRUD
    // ========================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Tag tag);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Tag> tags);

    @Update
    void update(Tag tag);

    @Delete
    void delete(Tag tag);

    @Query("DELETE FROM tags WHERE id = :tagId")
    void deleteById(long tagId);

    // ========================================
    // QUERIES
    // ========================================
    
    @Query("SELECT * FROM tags WHERE id = :tagId")
    LiveData<Tag> getTagById(long tagId);

    @Query("SELECT * FROM tags WHERE id = :tagId")
    Tag getTagByIdSync(long tagId);

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    Tag getTagByName(String name);

    @Query("SELECT * FROM tags ORDER BY name ASC")
    LiveData<List<Tag>> getAllTags();

    @Query("SELECT * FROM tags ORDER BY name ASC")
    List<Tag> getAllTagsSync();

    // ========================================
    // TASK-TAG RELATIONSHIP
    // ========================================
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addTagToTask(TaskTag taskTag);

    @Query("DELETE FROM task_tags WHERE task_id = :taskId AND tag_id = :tagId")
    void removeTagFromTask(long taskId, long tagId);

    @Query("DELETE FROM task_tags WHERE task_id = :taskId")
    void removeAllTagsFromTask(long taskId);

    @Query("SELECT t.* FROM tags t INNER JOIN task_tags tt ON t.id = tt.tag_id WHERE tt.task_id = :taskId ORDER BY t.name ASC")
    LiveData<List<Tag>> getTagsForTask(long taskId);

    @Query("SELECT t.* FROM tags t INNER JOIN task_tags tt ON t.id = tt.tag_id WHERE tt.task_id = :taskId ORDER BY t.name ASC")
    List<Tag> getTagsForTaskSync(long taskId);

    // ========================================
    // SEARCH
    // ========================================
    
    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<Tag>> searchTags(String query);

    // ========================================
    // STATISTICS
    // ========================================
    
    @Query("SELECT COUNT(*) FROM task_tags WHERE tag_id = :tagId")
    LiveData<Integer> getTagUsageCount(long tagId);
}
