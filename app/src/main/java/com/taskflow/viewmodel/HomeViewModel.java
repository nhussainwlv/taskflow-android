package com.taskflow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.taskflow.data.repo.TaskRepository;
import com.taskflow.model.Board;
import com.taskflow.model.Task;
import com.taskflow.model.User;

import java.util.Calendar;
import java.util.List;

/**
 * HomeViewModel - ViewModel for the Home/Dashboard screen.
 * 
 * Provides data for:
 * - Recent boards
 * - Today's tasks / My Tasks
 * - Upcoming deadlines
 * - Overdue tasks
 * - Statistics
 */
public class HomeViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    
    // Dashboard data
    private final LiveData<List<Board>> recentBoards;
    private final LiveData<List<Task>> tasksDueToday;
    private final LiveData<List<Task>> overdueTasks;
    private final LiveData<List<Task>> upcomingTasks;
    private final LiveData<User> currentUser;
    
    // Statistics
    private final LiveData<Integer> overdueCount;
    private final LiveData<Integer> dueTodayCount;
    
    // Greeting message
    private final MutableLiveData<String> greeting = new MutableLiveData<>();
    
    // Loading state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        
        // Initialize data
        recentBoards = repository.getRecentBoards(5);
        tasksDueToday = repository.getTasksDueToday();
        overdueTasks = repository.getOverdueTasks();
        currentUser = repository.getCurrentUser();
        overdueCount = repository.getOverdueCount();
        dueTodayCount = repository.getTasksDueTodayCount();
        
        // Get tasks due in next 7 days
        long now = System.currentTimeMillis();
        long weekFromNow = now + (7 * 24 * 60 * 60 * 1000L);
        upcomingTasks = repository.getTasksDueBetween(now, weekFromNow);
        
        // Set greeting based on time of day
        updateGreeting();
        
        // Mark as loaded once initial data is available
        isLoading.setValue(false);
    }

    /**
     * Update greeting based on current time.
     */
    private void updateGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        
        if (hour >= 5 && hour < 12) {
            greeting.setValue("Good morning");
        } else if (hour >= 12 && hour < 17) {
            greeting.setValue("Good afternoon");
        } else {
            greeting.setValue("Good evening");
        }
    }

    // ========================================
    // GETTERS
    // ========================================
    
    public LiveData<List<Board>> getRecentBoards() {
        return recentBoards;
    }

    public LiveData<List<Task>> getTasksDueToday() {
        return tasksDueToday;
    }

    public LiveData<List<Task>> getOverdueTasks() {
        return overdueTasks;
    }

    public LiveData<List<Task>> getUpcomingTasks() {
        return upcomingTasks;
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Integer> getOverdueCount() {
        return overdueCount;
    }

    public LiveData<Integer> getDueTodayCount() {
        return dueTodayCount;
    }

    public LiveData<String> getGreeting() {
        return greeting;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    // ========================================
    // ACTIONS
    // ========================================
    
    /**
     * Mark a task as complete.
     */
    public void completeTask(Task task) {
        task.setStatus(Task.Status.DONE);
        task.setProgress(100);
        repository.updateTask(task);
    }

    /**
     * Update board last accessed timestamp.
     */
    public void onBoardAccessed(long boardId) {
        repository.updateBoardLastAccessed(boardId);
    }

    /**
     * Refresh data (called on pull-to-refresh).
     */
    public void refresh() {
        updateGreeting();
        // LiveData will automatically refresh from database
    }
}
