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
import androidx.room.Transaction;
import androidx.room.Update;

import com.taskflow.model.Board;
import com.taskflow.model.BoardWithColumns;

import java.util.List;

/**
 * BoardDao - Data Access Object for Board operations.
 */
@Dao
public interface BoardDao {

    // ========================================
    // BASIC CRUD
    // ========================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Board board);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Board> boards);

    @Update
    void update(Board board);

    @Delete
    void delete(Board board);

    @Query("DELETE FROM boards WHERE id = :boardId")
    void deleteById(long boardId);

    // ========================================
    // QUERIES
    // ========================================
    
    @Query("SELECT * FROM boards WHERE id = :boardId")
    LiveData<Board> getBoardById(long boardId);

    @Query("SELECT * FROM boards WHERE id = :boardId")
    Board getBoardByIdSync(long boardId);

    @Transaction
    @Query("SELECT * FROM boards WHERE id = :boardId")
    LiveData<BoardWithColumns> getBoardWithColumnsById(long boardId);

    @Query("SELECT * FROM boards WHERE is_archived = 0 ORDER BY position ASC")
    LiveData<List<Board>> getAllBoards();

    @Query("SELECT * FROM boards WHERE is_archived = 0 ORDER BY position ASC")
    List<Board> getAllBoardsSync();

    @Transaction
    @Query("SELECT * FROM boards WHERE is_archived = 0 ORDER BY position ASC")
    LiveData<List<BoardWithColumns>> getAllBoardsWithColumns();

    // ========================================
    // FILTERED QUERIES
    // ========================================
    
    @Query("SELECT * FROM boards WHERE is_favorite = 1 AND is_archived = 0 ORDER BY position ASC")
    LiveData<List<Board>> getFavoriteBoards();

    @Query("SELECT * FROM boards WHERE is_archived = 1 ORDER BY updated_at DESC")
    LiveData<List<Board>> getArchivedBoards();

    @Query("SELECT * FROM boards WHERE is_archived = 0 ORDER BY last_accessed_at DESC LIMIT :limit")
    LiveData<List<Board>> getRecentBoards(int limit);

    @Query("SELECT * FROM boards WHERE is_archived = 0 ORDER BY last_accessed_at DESC LIMIT :limit")
    List<Board> getRecentBoardsSync(int limit);

    // ========================================
    // SEARCH
    // ========================================
    
    @Query("SELECT * FROM boards WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') AND is_archived = 0 ORDER BY name ASC")
    LiveData<List<Board>> searchBoards(String query);

    // ========================================
    // STATE UPDATES
    // ========================================
    
    @Query("UPDATE boards SET is_favorite = :favorite, updated_at = :timestamp WHERE id = :boardId")
    void setFavorite(long boardId, boolean favorite, long timestamp);

    @Query("UPDATE boards SET is_archived = :archived, updated_at = :timestamp WHERE id = :boardId")
    void setArchived(long boardId, boolean archived, long timestamp);

    @Query("UPDATE boards SET last_accessed_at = :timestamp WHERE id = :boardId")
    void updateLastAccessed(long boardId, long timestamp);

    @Query("UPDATE boards SET default_view = :viewType, updated_at = :timestamp WHERE id = :boardId")
    void setDefaultView(long boardId, String viewType, long timestamp);

    // ========================================
    // POSITION MANAGEMENT
    // ========================================
    
    @Query("UPDATE boards SET position = :position WHERE id = :boardId")
    void updatePosition(long boardId, int position);

    @Query("SELECT COALESCE(MAX(position), 0) + 1 FROM boards WHERE is_archived = 0")
    int getNextPosition();

    // ========================================
    // STATISTICS
    // ========================================
    
    @Query("SELECT COUNT(*) FROM boards WHERE is_archived = 0")
    LiveData<Integer> getBoardCount();
}
