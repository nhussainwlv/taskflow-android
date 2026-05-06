/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.StrictMode;

import androidx.work.Configuration;

import com.google.android.material.color.DynamicColors;
import com.taskflow.sync.SyncWorker;

/**
 * TaskFlowApplication - Application class for TaskFlow.
 * 
 * Handles global app initialization including:
 * - Dynamic colors (Android 12+)
 * - Notification channels
 * - WorkManager configuration
 * - StrictMode for debug builds
 */
public class TaskFlowApplication extends Application implements Configuration.Provider {

    public static final String CHANNEL_REMINDERS = "reminders";
    public static final String CHANNEL_UPDATES = "updates";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Enable StrictMode in debug builds
        if (BuildConfig.ENABLE_STRICT_MODE) {
            enableStrictMode();
        }
        
        // Apply dynamic colors (Material You) on Android 12+
        DynamicColors.applyToActivitiesIfAvailable(this);
        
        // Create notification channels
        createNotificationChannels();

        // WorkManager: this Application implements Configuration.Provider — do not call
        // WorkManager.initialize() here; that can crash with "already initialized" when merged
        // with startup. First WorkManager.getInstance() uses getWorkManagerConfiguration().

        // Schedule background sync worker for notifications
        SyncWorker.schedule(this);
    }

    /**
     * Configure StrictMode for detecting performance issues in debug builds.
     */
    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build());
    }

    /**
     * Create notification channels for Android 8.0+
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            
            // Reminders channel (high importance for task due dates)
            NotificationChannel remindersChannel = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    getString(R.string.notification_channel_reminders),
                    NotificationManager.IMPORTANCE_HIGH
            );
            remindersChannel.setDescription(getString(R.string.notification_channel_reminders_desc));
            remindersChannel.enableVibration(true);
            remindersChannel.setShowBadge(true);
            manager.createNotificationChannel(remindersChannel);
            
            // Updates channel (default importance for general updates)
            NotificationChannel updatesChannel = new NotificationChannel(
                    CHANNEL_UPDATES,
                    getString(R.string.notification_channel_updates),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            updatesChannel.setDescription(getString(R.string.notification_channel_updates_desc));
            manager.createNotificationChannel(updatesChannel);
        }
    }

    /**
     * WorkManager configuration for background sync tasks.
     */
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(BuildConfig.DEBUG ? android.util.Log.DEBUG : android.util.Log.ERROR)
                .build();
    }
}
