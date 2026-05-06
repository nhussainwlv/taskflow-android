/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.taskflow.data.db.TaskFlowDatabase;
import com.taskflow.model.Task;
import com.taskflow.util.NotificationHelper;
import com.taskflow.util.PreferencesManager;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Sync Worker
 * 
 * Background worker using WorkManager for:
 * - Checking due/overdue tasks and showing notifications
 * - Performing data synchronization (placeholder for future cloud sync)
 * - Database maintenance tasks
 */
public class SyncWorker extends Worker {

    public static final String WORK_NAME = "taskflow_sync";
    public static final String WORK_NAME_NOTIFICATIONS = "taskflow_notifications";
    
    private final Context context;
    private final TaskFlowDatabase database;
    private final NotificationHelper notificationHelper;
    private final PreferencesManager preferencesManager;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        this.database = TaskFlowDatabase.getInstance(context);
        this.notificationHelper = new NotificationHelper(context);
        this.preferencesManager = PreferencesManager.getInstance(context);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            // Check if notifications are enabled
            if (preferencesManager.isNotificationEnabled()) {
                checkDueTasks();
                checkOverdueTasks();
            }
            
            // Placeholder for cloud sync
            // syncWithCloud();
            
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }

    /**
     * Checks for tasks due today and shows notifications.
     */
    private void checkDueTasks() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();

        List<Task> tasksDueToday = database.taskDao().getTasksDueBetweenSync(startOfDay, endOfDay);
        
        int count = 0;
        for (Task task : tasksDueToday) {
            if (!task.isCompleted()) {
                count++;
                // Show individual notification only if few tasks
                if (count <= 3) {
                    notificationHelper.showTaskDueTodayNotification(task);
                }
            }
        }
        
        // If many tasks, show summary instead
        if (count > 3) {
            notificationHelper.showDailySummaryNotification(count, 0);
        }
    }

    /**
     * Checks for overdue tasks and shows notifications.
     */
    private void checkOverdueTasks() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfToday = calendar.getTimeInMillis();

        List<Task> overdueTasks = database.taskDao().getOverdueTasksSync(startOfToday);
        
        int count = 0;
        for (Task task : overdueTasks) {
            if (!task.isCompleted()) {
                count++;
                // Show individual notification for first overdue task
                if (count == 1) {
                    notificationHelper.showOverdueTaskNotification(task);
                }
            }
        }
        
        // If multiple overdue tasks, show summary
        if (count > 1) {
            notificationHelper.showDailySummaryNotification(0, count);
        }
    }

    /**
     * Schedules the sync worker to run periodically.
     */
    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // Works offline
                .build();

        // Daily sync for notifications
        PeriodicWorkRequest notificationWork = new PeriodicWorkRequest.Builder(
                SyncWorker.class,
                1, TimeUnit.DAYS  // Run daily
        )
                .setConstraints(constraints)
                .setInitialDelay(calculateInitialDelay(), TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_NOTIFICATIONS,
                ExistingPeriodicWorkPolicy.KEEP,
                notificationWork
        );
    }

    /**
     * Calculates initial delay to schedule work at the preferred notification time.
     */
    private static long calculateInitialDelay() {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        
        // Default: 9 AM
        target.set(Calendar.HOUR_OF_DAY, 9);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        
        if (now.after(target)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return target.getTimeInMillis() - now.getTimeInMillis();
    }

    /**
     * Cancels all scheduled sync work.
     */
    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_NOTIFICATIONS);
    }

    /**
     * Runs sync immediately (one-time).
     */
    public static void runNow(Context context) {
        androidx.work.OneTimeWorkRequest workRequest = 
                new androidx.work.OneTimeWorkRequest.Builder(SyncWorker.class).build();
        WorkManager.getInstance(context).enqueue(workRequest);
    }
}
