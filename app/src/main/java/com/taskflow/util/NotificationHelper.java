package com.taskflow.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.taskflow.R;
import com.taskflow.model.Task;
import com.taskflow.ui.MainActivity;

/**
 * Notification Helper
 * 
 * Handles creation and management of local notifications for task reminders,
 * due date alerts, and other app notifications.
 */
public class NotificationHelper {

    // Notification channel IDs
    public static final String CHANNEL_REMINDERS = "taskflow_reminders";
    public static final String CHANNEL_DUE_TODAY = "taskflow_due_today";
    public static final String CHANNEL_OVERDUE = "taskflow_overdue";
    public static final String CHANNEL_GENERAL = "taskflow_general";

    // Notification IDs (base IDs, task-specific ones will be offset)
    public static final int NOTIFICATION_DAILY_SUMMARY = 1000;
    public static final int NOTIFICATION_TASK_BASE = 2000;

    // Deep link actions
    public static final String ACTION_OPEN_TASK = "com.taskflow.action.OPEN_TASK";
    public static final String ACTION_COMPLETE_TASK = "com.taskflow.action.COMPLETE_TASK";
    public static final String EXTRA_TASK_ID = "task_id";

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }

    /**
     * Creates notification channels for Android O and above.
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            
            // Reminders channel
            NotificationChannel remindersChannel = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    context.getString(R.string.channel_reminders),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            remindersChannel.setDescription(context.getString(R.string.channel_reminders_desc));
            manager.createNotificationChannel(remindersChannel);

            // Due today channel
            NotificationChannel dueTodayChannel = new NotificationChannel(
                    CHANNEL_DUE_TODAY,
                    context.getString(R.string.channel_due_today),
                    NotificationManager.IMPORTANCE_HIGH
            );
            dueTodayChannel.setDescription(context.getString(R.string.channel_due_today_desc));
            manager.createNotificationChannel(dueTodayChannel);

            // Overdue channel
            NotificationChannel overdueChannel = new NotificationChannel(
                    CHANNEL_OVERDUE,
                    context.getString(R.string.channel_overdue),
                    NotificationManager.IMPORTANCE_HIGH
            );
            overdueChannel.setDescription(context.getString(R.string.channel_overdue_desc));
            manager.createNotificationChannel(overdueChannel);

            // General channel
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_GENERAL,
                    context.getString(R.string.channel_general),
                    NotificationManager.IMPORTANCE_LOW
            );
            generalChannel.setDescription(context.getString(R.string.channel_general_desc));
            manager.createNotificationChannel(generalChannel);
        }
    }

    /**
     * Shows a notification for a task due today.
     */
    public void showTaskDueTodayNotification(Task task) {
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setAction(ACTION_OPEN_TASK);
        openIntent.putExtra(EXTRA_TASK_ID, task.getId());
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) task.getId(),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Complete action
        Intent completeIntent = new Intent(context, MainActivity.class);
        completeIntent.setAction(ACTION_COMPLETE_TASK);
        completeIntent.putExtra(EXTRA_TASK_ID, task.getId());

        PendingIntent completePendingIntent = PendingIntent.getActivity(
                context,
                (int) task.getId() + 10000,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_DUE_TODAY)
                .setSmallIcon(R.drawable.ic_shortcut_tasks)
                .setContentTitle(context.getString(R.string.notification_due_today_title))
                .setContentText(task.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(task.getTitle())
                        .setSummaryText(context.getString(R.string.notification_due_today_summary)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_check, 
                        context.getString(R.string.action_complete), 
                        completePendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        try {
            notificationManager.notify(NOTIFICATION_TASK_BASE + (int) task.getId(), builder.build());
        } catch (SecurityException e) {
            // Permission not granted
            e.printStackTrace();
        }
    }

    /**
     * Shows a notification for an overdue task.
     */
    public void showOverdueTaskNotification(Task task) {
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setAction(ACTION_OPEN_TASK);
        openIntent.putExtra(EXTRA_TASK_ID, task.getId());
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) task.getId(),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_OVERDUE)
                .setSmallIcon(R.drawable.ic_priority_urgent)
                .setContentTitle(context.getString(R.string.notification_overdue_title))
                .setContentText(task.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(task.getTitle()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(context.getColor(R.color.danger))
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        try {
            notificationManager.notify(NOTIFICATION_TASK_BASE + (int) task.getId(), builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows daily summary notification with task count.
     */
    public void showDailySummaryNotification(int tasksDueToday, int tasksOverdue) {
        if (tasksDueToday == 0 && tasksOverdue == 0) {
            return; // No notification needed
        }

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                NOTIFICATION_DAILY_SUMMARY,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = context.getString(R.string.notification_daily_summary_title);
        String content;
        
        if (tasksOverdue > 0 && tasksDueToday > 0) {
            content = context.getString(R.string.notification_daily_summary_both,
                    tasksDueToday, tasksOverdue);
        } else if (tasksOverdue > 0) {
            content = context.getResources().getQuantityString(
                    R.plurals.notification_tasks_overdue, tasksOverdue, tasksOverdue);
        } else {
            content = context.getResources().getQuantityString(
                    R.plurals.notification_tasks_due_today, tasksDueToday, tasksDueToday);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_shortcut_tasks)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        try {
            notificationManager.notify(NOTIFICATION_DAILY_SUMMARY, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels a specific task notification.
     */
    public void cancelTaskNotification(long taskId) {
        notificationManager.cancel(NOTIFICATION_TASK_BASE + (int) taskId);
    }

    /**
     * Cancels all notifications.
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
