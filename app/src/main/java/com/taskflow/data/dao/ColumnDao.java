package com.taskflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.taskflow.model.Column;
import com.taskflow.model.ColumnWithTasks;

import java.util.List;

/**
 * ColumnDao - Data Access Object for Kanban Column operations.
 */
@Dao
public interface ColumnDao {

    // ========================================
    // BASIC CRUD
    // ========================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Column column);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Column> columns);

    @Update
    void update(Column column);

    @Update
    void updateAll(List<Column> columns);

    @Delete
    void delete(Column column);

    @Query("DELETE FROM columns WHERE id = :columnId")
    void deleteById(long columnId);

    // ========================================
    // QUERIES
    // ========================================
    
    @Query("SELECT * FROM columns WHERE id = :columnId")
    LiveData<Column> getColumnById(long columnId);

    @Query("SELECT * FROM columns WHERE id = :columnId")
    Column getColumnByIdSync(long columnId);

    @Query("SELECT * FROM columns WHERE board_id = :boardId ORDER BY position ASC")
    LiveData<List<Column>> getColumnsByBoard(long boardId);

    @Query("SELECT * FROM columns WHERE board_id = :boardId ORDER BY position ASC")
    List<Column> getColumnsByBoardSync(long boardId);

    @Transaction
    @Query("SELECT * FROM columns WHERE board_id = :boardId ORDER BY position ASC")
    LiveData<List<ColumnWithTasks>> getColumnsWithTasksByBoard(long boardId);

    @Transaction
    @Query("SELECT * FROM columns WHERE board_id = :boardId ORDER BY position ASC")
    List<ColumnWithTasks> getColumnsWithTasksByBoardSync(long boardId);

    // ========================================
    // POSITION MANAGEMENT
    // ========================================
    
    @Query("UPDATE columns SET position = :position WHERE id = :columnId")
    void updatePosition(long columnId, int position);

    @Query("SELECT COALESCE(MAX(position), 0) + 1 FROM columns WHERE board_id = :boardId")
    int getNextPosition(long boardId);

    @Query("UPDATE columns SET position = position + 1 WHERE board_id = :boardId AND position >= :startPosition")
    void shiftPositionsUp(long boardId, int startPosition);

    @Query("UPDATE columns SET position = position - 1 WHERE board_id = :boardId AND position > :deletedPosition")
    void shiftPositionsDown(long boardId, int deletedPosition);

    // ========================================
    // COLLAPSE STATE
    // ========================================
    
    @Query("UPDATE columns SET is_collapsed = :collapsed WHERE id = :columnId")
    void setCollapsed(long columnId, boolean collapsed);

    // ========================================
    // STATISTICS
    // ========================================
    
    @Query("SELECT COUNT(*) FROM columns WHERE board_id = :boardId")
    LiveData<Integer> getColumnCount(long boardId);

    @Query("SELECT * FROM columns WHERE board_id = :boardId AND is_done_column = 1 LIMIT 1")
    Column getDoneColumn(long boardId);
}
