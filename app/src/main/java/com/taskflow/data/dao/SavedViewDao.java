/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.taskflow.model.SavedView;

import java.util.List;

/**
 * SavedViewDao - Data Access Object for SavedView operations.
 */
@Dao
public interface SavedViewDao {

    // ========================================
    // BASIC CRUD
    // ========================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SavedView savedView);

    @Update
    void update(SavedView savedView);

    @Delete
    void delete(SavedView savedView);

    @Query("DELETE FROM saved_views WHERE id = :viewId")
    void deleteById(long viewId);

    // ========================================
    // QUERIES
    // ========================================
    
    @Query("SELECT * FROM saved_views WHERE id = :viewId")
    LiveData<SavedView> getSavedViewById(long viewId);

    @Query("SELECT * FROM saved_views WHERE id = :viewId")
    SavedView getSavedViewByIdSync(long viewId);

    @Query("SELECT * FROM saved_views ORDER BY name ASC")
    LiveData<List<SavedView>> getAllSavedViews();

    @Query("SELECT * FROM saved_views WHERE view_type = :viewType ORDER BY name ASC")
    LiveData<List<SavedView>> getSavedViewsByType(String viewType);

    @Query("SELECT * FROM saved_views WHERE board_id = :boardId OR board_id IS NULL ORDER BY name ASC")
    LiveData<List<SavedView>> getSavedViewsForBoard(Long boardId);

    @Query("SELECT * FROM saved_views WHERE is_default = 1 AND view_type = :viewType LIMIT 1")
    SavedView getDefaultView(String viewType);

    // ========================================
    // DEFAULT VIEW MANAGEMENT
    // ========================================
    
    @Query("UPDATE saved_views SET is_default = 0 WHERE view_type = :viewType")
    void clearDefaultForType(String viewType);

    @Query("UPDATE saved_views SET is_default = 1 WHERE id = :viewId")
    void setAsDefault(long viewId);
}
