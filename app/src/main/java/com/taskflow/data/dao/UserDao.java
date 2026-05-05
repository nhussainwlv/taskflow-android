package com.taskflow.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.taskflow.model.User;

import java.util.List;

/**
 * UserDao - Data Access Object for User operations.
 */
@Dao
public interface UserDao {

    // ========================================
    // BASIC CRUD
    // ========================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<User> users);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteById(long userId);

    // ========================================
    // QUERIES
    // ========================================
    
    @Query("SELECT * FROM users WHERE id = :userId")
    LiveData<User> getUserById(long userId);

    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserByIdSync(long userId);

    @Query("SELECT * FROM users ORDER BY name ASC")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT * FROM users ORDER BY name ASC")
    List<User> getAllUsersSync();

    @Query("SELECT * FROM users WHERE is_current_user = 1 LIMIT 1")
    LiveData<User> getCurrentUser();

    @Query("SELECT * FROM users WHERE is_current_user = 1 LIMIT 1")
    User getCurrentUserSync();

    // ========================================
    // SEARCH
    // ========================================
    
    @Query("SELECT * FROM users WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<User>> searchUsers(String query);

    // ========================================
    // CURRENT USER MANAGEMENT
    // ========================================
    
    @Query("UPDATE users SET is_current_user = 0")
    void clearCurrentUser();

    @Query("UPDATE users SET is_current_user = 1 WHERE id = :userId")
    void setCurrentUser(long userId);
}
