/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.util;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Preferences Manager
 * 
 * Manages app preferences using DataStore, including theme settings,
 * accent color, and saved view configurations.
 * 
 * Note: For simplicity, this implementation uses SharedPreferences.
 * In production, you would use DataStore with coroutines/RxJava.
 */
public class PreferencesManager {

    private static final String PREFS_NAME = "taskflow_prefs";
    
    // Preference keys
    public static final String KEY_THEME_MODE = "theme_mode";
    public static final String KEY_ACCENT_COLOR = "accent_color";
    public static final String KEY_DYNAMIC_COLOR = "dynamic_color";
    public static final String KEY_COMPACT_VIEW = "compact_view";
    public static final String KEY_DEFAULT_BOARD_ID = "default_board_id";
    public static final String KEY_LAST_VIEWED_BOARD = "last_viewed_board";
    public static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";
    public static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    public static final String KEY_NOTIFICATION_TIME = "notification_time";
    public static final String KEY_REDUCE_MOTION = "reduce_motion";
    private static final String KEY_RECENT_SEARCHES = "recent_searches";
    private static final String KEY_LAST_SYNC_MS = "last_sync_wall_clock_ms";
    private static final int MAX_RECENT_SEARCHES = 15;
    private static final String RECENT_SEP = "\u001e";

    /** Applied on top of system font scale (accessibility / readability). */
    public static final String KEY_FONT_SCALE_MULT = "font_scale_mult";
    
    // Theme mode values
    public static final int THEME_SYSTEM = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;
    
    // Default accent color (primary indigo)
    public static final int DEFAULT_ACCENT_COLOR = 0xFF4F46E5;

    private final android.content.SharedPreferences prefs;
    private static PreferencesManager instance;

    private PreferencesManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new PreferencesManager(context);
        }
        return instance;
    }

    // Theme settings

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
    }

    public int getAccentColor() {
        return prefs.getInt(KEY_ACCENT_COLOR, DEFAULT_ACCENT_COLOR);
    }

    public void setAccentColor(int color) {
        prefs.edit().putInt(KEY_ACCENT_COLOR, color).apply();
    }

    public boolean isDynamicColorEnabled() {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, true);
    }

    public void setDynamicColorEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply();
    }

    // View settings

    public boolean isCompactView() {
        return prefs.getBoolean(KEY_COMPACT_VIEW, false);
    }

    public void setCompactView(boolean compact) {
        prefs.edit().putBoolean(KEY_COMPACT_VIEW, compact).apply();
    }

    public long getDefaultBoardId() {
        return prefs.getLong(KEY_DEFAULT_BOARD_ID, 1L);
    }

    public void setDefaultBoardId(long boardId) {
        prefs.edit().putLong(KEY_DEFAULT_BOARD_ID, boardId).apply();
    }

    public long getLastViewedBoard() {
        return prefs.getLong(KEY_LAST_VIEWED_BOARD, 1L);
    }

    public void setLastViewedBoard(long boardId) {
        prefs.edit().putLong(KEY_LAST_VIEWED_BOARD, boardId).apply();
    }

    // Onboarding

    public boolean isOnboardingComplete() {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }

    public void setOnboardingComplete(boolean complete) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, complete).apply();
    }

    // Notifications

    public boolean isNotificationEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    public void setNotificationEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }

    public String getNotificationTime() {
        return prefs.getString(KEY_NOTIFICATION_TIME, "09:00");
    }

    public void setNotificationTime(String time) {
        prefs.edit().putString(KEY_NOTIFICATION_TIME, time).apply();
    }

    // Accessibility

    public boolean isReduceMotionEnabled() {
        return prefs.getBoolean(KEY_REDUCE_MOTION, false);
    }

    public void setReduceMotionEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_REDUCE_MOTION, enabled).apply();
    }

    /**
     * Recent global search queries (most recent first).
     */
    public List<String> getRecentSearches() {
        String raw = prefs.getString(KEY_RECENT_SEARCHES, "");
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        String[] parts = raw.split(RECENT_SEP, -1);
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            if (p != null && !p.isEmpty()) {
                out.add(p);
            }
        }
        return out;
    }

    public void addRecentSearch(String query) {
        if (query == null) return;
        String q = query.trim();
        if (q.isEmpty()) return;

        List<String> list = new ArrayList<>(getRecentSearches());
        list.remove(q);
        list.add(0, q);
        while (list.size() > MAX_RECENT_SEARCHES) {
            list.remove(list.size() - 1);
        }
        prefs.edit().putString(KEY_RECENT_SEARCHES, String.join(RECENT_SEP, list)).apply();
    }

    public void clearRecentSearches() {
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply();
    }

    /**
     * Extra font scale factor on top of system settings (1 = default, >1 = larger in-app text).
     */
    public float getFontScaleMultiplier() {
        return prefs.getFloat(KEY_FONT_SCALE_MULT, 1f);
    }

    public void setFontScaleMultiplier(float multiplier) {
        prefs.edit().putFloat(KEY_FONT_SCALE_MULT, multiplier).apply();
    }

    // Saved Views
    
    public void saveSavedViewJson(String json) {
        prefs.edit().putString("saved_views", json).apply();
    }

    public String getSavedViewsJson() {
        return prefs.getString("saved_views", "[]");
    }

    /**
     * Last time the user exported data or tapped “record sync” (dissertation parity for sync narrative).
     */
    public long getLastSuccessfulSyncWallClockMs() {
        return prefs.getLong(KEY_LAST_SYNC_MS, 0L);
    }

    public void setLastSuccessfulSyncWallClockMs(long millis) {
        prefs.edit().putLong(KEY_LAST_SYNC_MS, millis).apply();
    }

    // Clear all preferences (for testing or reset)
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
