/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * User Entity - Represents a user/team member who can be assigned to tasks.
 * 
 * In this offline-first app, users are stored locally. In a real app,
 * this would sync with a backend service.
 */
@Entity(
    tableName = "users",
    indices = {
        @Index(value = "email", unique = true)
    }
)
public class User {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name = "";

    @ColumnInfo(name = "email")
    private String email;

    /**
     * Avatar URL or local path (not used in this offline version)
     */
    @ColumnInfo(name = "avatar_url")
    private String avatarUrl;

    /**
     * User's initials for avatar fallback (e.g., "JD" for John Doe)
     */
    @ColumnInfo(name = "initials")
    private String initials;

    /**
     * Avatar background color index (1-8 for avatar colors)
     */
    @ColumnInfo(name = "avatar_color", defaultValue = "1")
    private int avatarColor = 1;

    /**
     * Whether this is the current/local user
     */
    @ColumnInfo(name = "is_current_user", defaultValue = "0")
    private boolean isCurrentUser = false;

    @ColumnInfo(name = "created_at")
    private long createdAt = System.currentTimeMillis();

    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    public User() {
    }

    public User(@NonNull String name) {
        this.name = name;
        this.initials = generateInitials(name);
    }

    public User(@NonNull String name, String email) {
        this.name = name;
        this.email = email;
        this.initials = generateInitials(name);
    }

    // ========================================
    // UTILITY METHODS
    // ========================================
    
    /**
     * Generate initials from name (e.g., "John Doe" -> "JD")
     */
    public static String generateInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "?";
        }
        
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            if (!parts[i].isEmpty()) {
                initials.append(Character.toUpperCase(parts[i].charAt(0)));
            }
        }
        
        return initials.length() > 0 ? initials.toString() : "?";
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
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
        if (this.initials == null || this.initials.isEmpty()) {
            this.initials = generateInitials(name);
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getInitials() {
        return initials != null ? initials : generateInitials(name);
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public int getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(int avatarColor) {
        this.avatarColor = Math.max(1, Math.min(8, avatarColor));
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
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
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
