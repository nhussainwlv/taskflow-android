package com.taskflow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.taskflow.data.repo.TaskRepository;
import com.taskflow.model.Board;
import com.taskflow.model.Column;
import com.taskflow.model.Tag;
import com.taskflow.model.Task;
import com.taskflow.model.TaskWithTags;
import com.taskflow.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * TaskViewModel - ViewModel for task list and task detail screens.
 * 
 * Handles:
 * - Task CRUD operations
 * - Filtering and sorting
 * - Multi-select operations
 * - Tag management
 */
public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    
    // Current board context
    private final MutableLiveData<Long> currentBoardId = new MutableLiveData<>();
    
    // Task data
    private final MutableLiveData<Long> selectedTaskId = new MutableLiveData<>();
    private LiveData<TaskWithTags> selectedTaskWithTags;
    
    // List data
    private LiveData<List<TaskWithTags>> tasksWithTags;
    
    // Filter/Sort state
    private final MutableLiveData<String> sortField = new MutableLiveData<>("position");
    private final MutableLiveData<Boolean> sortAscending = new MutableLiveData<>(true);
    private final MutableLiveData<Set<String>> statusFilters = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Set<String>> priorityFilters = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Long> assigneeFilter = new MutableLiveData<>();
    
    // Multi-select state
    private final MutableLiveData<Boolean> isMultiSelectMode = new MutableLiveData<>(false);
    private final MutableLiveData<Set<Long>> selectedTaskIds = new MutableLiveData<>(new HashSet<>());
    
    // All tags for selection
    private final LiveData<List<Tag>> allTags;
    private final LiveData<List<User>> allUsers;
    private final LiveData<List<Column>> columns;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        
        allTags = repository.getAllTags();
        allUsers = repository.getAllUsers();
        
        // Transform board ID to tasks
        tasksWithTags = Transformations.switchMap(currentBoardId, boardId -> {
            if (boardId == null || boardId <= 0) {
                return new MutableLiveData<>(new ArrayList<>());
            }
            return repository.getTasksWithTagsByBoard(boardId);
        });
        
        // Transform selected task ID to task with tags
        selectedTaskWithTags = Transformations.switchMap(selectedTaskId, taskId -> {
            if (taskId == null || taskId <= 0) {
                return new MutableLiveData<>(null);
            }
            return repository.getTaskWithTagsById(taskId);
        });
        
        // Get columns for current board
        columns = Transformations.switchMap(currentBoardId, boardId -> {
            if (boardId == null || boardId <= 0) {
                return new MutableLiveData<>(new ArrayList<>());
            }
            return repository.getColumnsByBoard(boardId);
        });
    }

    // ========================================
    // CONTEXT
    // ========================================
    
    public void setCurrentBoardId(long boardId) {
        currentBoardId.setValue(boardId);
    }

    public LiveData<Long> getCurrentBoardId() {
        return currentBoardId;
    }

    public LiveData<List<Column>> getColumns() {
        return columns;
    }

    // ========================================
    // TASK DATA
    // ========================================
    
    public void selectTask(long taskId) {
        selectedTaskId.setValue(taskId);
    }

    public LiveData<Long> getSelectedTaskId() {
        return selectedTaskId;
    }

    public LiveData<TaskWithTags> getSelectedTaskWithTags() {
        return selectedTaskWithTags;
    }

    public LiveData<List<TaskWithTags>> getTasksWithTags() {
        return tasksWithTags;
    }

    // ========================================
    // TASK CRUD
    // ========================================
    
    public LiveData<List<Task>> getAllTasks() {
        return repository.getAllTasks();
    }

    public LiveData<Task> getTaskById(long taskId) {
        return repository.getTaskById(taskId);
    }

    public void createTask(Task task, List<Long> tagIds) {
        repository.insertTask(task, tagIds);
    }

    public void insertTask(Task task) {
        repository.insertTask(task);
    }

    public void insertTask(Task task, Consumer<Long> onInserted) {
        repository.insertTask(task, onInserted);
    }

    public LiveData<List<Board>> getAllBoards() {
        return repository.getAllBoards();
    }

    public LiveData<List<Column>> getColumnsForBoard(long boardId) {
        return repository.getColumnsByBoard(boardId);
    }

    public LiveData<List<User>> getUsers() {
        return repository.getAllUsers();
    }

    public LiveData<TaskWithTags> getTaskWithTagsById(long taskId) {
        return repository.getTaskWithTagsById(taskId);
    }

    public void updateTask(Task task) {
        repository.updateTask(task);
    }

    public void deleteTask(Task task) {
        repository.deleteTask(task);
    }

    public void deleteTask(long taskId) {
        repository.deleteTaskById(taskId);
    }

    public void deleteTaskById(long taskId) {
        repository.deleteTaskById(taskId);
    }

    public void completeTaskById(long taskId) {
        repository.completeTask(taskId);
    }

    public void updateTaskTags(long taskId, List<Long> tagIds) {
        repository.updateTaskTags(taskId, tagIds);
    }

    public void addTagToTask(long taskId, long tagId) {
        repository.addTagToTask(taskId, tagId);
    }

    public void removeTagFromTask(long taskId, long tagId) {
        repository.removeTagFromTask(taskId, tagId);
    }

    // ========================================
    // FILTER/SORT
    // ========================================
    
    public void setSortField(String field) {
        sortField.setValue(field);
    }

    public LiveData<String> getSortField() {
        return sortField;
    }

    public void toggleSortDirection() {
        Boolean current = sortAscending.getValue();
        sortAscending.setValue(current == null || !current);
    }

    public LiveData<Boolean> getSortAscending() {
        return sortAscending;
    }

    public void addStatusFilter(String status) {
        Set<String> current = statusFilters.getValue();
        if (current == null) current = new HashSet<>();
        current.add(status);
        statusFilters.setValue(current);
    }

    public void removeStatusFilter(String status) {
        Set<String> current = statusFilters.getValue();
        if (current != null) {
            current.remove(status);
            statusFilters.setValue(current);
        }
    }

    public void clearStatusFilters() {
        statusFilters.setValue(new HashSet<>());
    }

    public LiveData<Set<String>> getStatusFilters() {
        return statusFilters;
    }

    public void addPriorityFilter(String priority) {
        Set<String> current = priorityFilters.getValue();
        if (current == null) current = new HashSet<>();
        current.add(priority);
        priorityFilters.setValue(current);
    }

    public void removePriorityFilter(String priority) {
        Set<String> current = priorityFilters.getValue();
        if (current != null) {
            current.remove(priority);
            priorityFilters.setValue(current);
        }
    }

    public void clearPriorityFilters() {
        priorityFilters.setValue(new HashSet<>());
    }

    public LiveData<Set<String>> getPriorityFilters() {
        return priorityFilters;
    }

    public void setAssigneeFilter(Long userId) {
        assigneeFilter.setValue(userId);
    }

    public void clearAssigneeFilter() {
        assigneeFilter.setValue(null);
    }

    public LiveData<Long> getAssigneeFilter() {
        return assigneeFilter;
    }

    public void clearAllFilters() {
        clearStatusFilters();
        clearPriorityFilters();
        clearAssigneeFilter();
    }

    public boolean hasActiveFilters() {
        Set<String> statuses = statusFilters.getValue();
        Set<String> priorities = priorityFilters.getValue();
        Long assignee = assigneeFilter.getValue();
        
        return (statuses != null && !statuses.isEmpty()) ||
               (priorities != null && !priorities.isEmpty()) ||
               assignee != null;
    }

    // ========================================
    // MULTI-SELECT
    // ========================================
    
    public void enableMultiSelectMode() {
        isMultiSelectMode.setValue(true);
    }

    public void disableMultiSelectMode() {
        isMultiSelectMode.setValue(false);
        selectedTaskIds.setValue(new HashSet<>());
    }

    public void toggleMultiSelectMode() {
        Boolean current = isMultiSelectMode.getValue();
        if (current != null && current) {
            disableMultiSelectMode();
        } else {
            enableMultiSelectMode();
        }
    }

    public LiveData<Boolean> isMultiSelectMode() {
        return isMultiSelectMode;
    }

    public void toggleTaskSelection(long taskId) {
        Set<Long> current = selectedTaskIds.getValue();
        if (current == null) current = new HashSet<>();
        
        if (current.contains(taskId)) {
            current.remove(taskId);
        } else {
            current.add(taskId);
        }
        selectedTaskIds.setValue(current);
    }

    public void selectAllTasks(List<Long> taskIds) {
        selectedTaskIds.setValue(new HashSet<>(taskIds));
    }

    public void deselectAllTasks() {
        selectedTaskIds.setValue(new HashSet<>());
    }

    public LiveData<Set<Long>> getSelectedTaskIds() {
        return selectedTaskIds;
    }

    public int getSelectedCount() {
        Set<Long> current = selectedTaskIds.getValue();
        return current != null ? current.size() : 0;
    }

    // ========================================
    // BULK OPERATIONS
    // ========================================
    
    public void deleteSelectedTasks() {
        Set<Long> ids = selectedTaskIds.getValue();
        if (ids != null && !ids.isEmpty()) {
            repository.deleteTasksByIds(new ArrayList<>(ids));
            disableMultiSelectMode();
        }
    }

    public void updateSelectedTasksStatus(String status) {
        Set<Long> ids = selectedTaskIds.getValue();
        if (ids != null && !ids.isEmpty()) {
            repository.updateTaskStatusBulk(new ArrayList<>(ids), status);
            disableMultiSelectMode();
        }
    }

    public void assignSelectedTasks(Long assigneeId) {
        Set<Long> ids = selectedTaskIds.getValue();
        if (ids != null && !ids.isEmpty()) {
            repository.assignTasksBulk(new ArrayList<>(ids), assigneeId);
            disableMultiSelectMode();
        }
    }

    public void moveSelectedTasksToColumn(long columnId) {
        Set<Long> ids = selectedTaskIds.getValue();
        if (ids != null && !ids.isEmpty()) {
            repository.moveTasksToColumnBulk(new ArrayList<>(ids), columnId);
            disableMultiSelectMode();
        }
    }

    public void archiveSelectedTasks() {
        Set<Long> ids = selectedTaskIds.getValue();
        if (ids != null && !ids.isEmpty()) {
            repository.archiveTasksBulk(new ArrayList<>(ids));
            disableMultiSelectMode();
        }
    }

    // ========================================
    // TAGS & USERS
    // ========================================
    
    public LiveData<List<Tag>> getAllTags() {
        return allTags;
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public void createTag(String name, String color) {
        Tag tag = new Tag(name, color);
        repository.insertTag(tag);
    }

    /** JSON export for portability (Settings). */
    public void exportAllDataAsJson(Consumer<String> onResult) {
        repository.exportAllDataAsJson(onResult);
    }
}
