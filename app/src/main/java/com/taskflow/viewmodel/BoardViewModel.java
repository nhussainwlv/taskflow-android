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
import com.taskflow.model.ColumnWithTasks;
import com.taskflow.model.Task;

import java.util.List;

/**
 * BoardViewModel - ViewModel for the Kanban Board screen.
 * 
 * Manages:
 * - Board and column data
 * - Task movement between columns
 * - Column operations (add, rename, reorder, delete)
 * - Drag & drop state
 */
public class BoardViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    
    // Current board ID
    private final MutableLiveData<Long> currentBoardId = new MutableLiveData<>();
    
    // Board data
    private LiveData<Board> currentBoard;
    private LiveData<List<ColumnWithTasks>> columnsWithTasks;
    
    // Drag state
    private final MutableLiveData<Task> draggedTask = new MutableLiveData<>();
    private final MutableLiveData<Integer> dragTargetColumn = new MutableLiveData<>();
    
    // UI state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public BoardViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        
        // Transform board ID to board data
        currentBoard = Transformations.switchMap(currentBoardId, boardId -> {
            if (boardId == null || boardId <= 0) {
                return new MutableLiveData<>(null);
            }
            return repository.getBoardById(boardId);
        });
        
        // Transform board ID to columns with tasks
        columnsWithTasks = Transformations.switchMap(currentBoardId, boardId -> {
            if (boardId == null || boardId <= 0) {
                return new MutableLiveData<>(null);
            }
            isLoading.setValue(false);
            return repository.getColumnsWithTasksByBoard(boardId);
        });
    }

    // ========================================
    // BOARD MANAGEMENT
    // ========================================
    
    /**
     * Set the current board to display.
     */
    public void setCurrentBoardId(long boardId) {
        isLoading.setValue(true);
        currentBoardId.setValue(boardId);
        repository.updateBoardLastAccessed(boardId);
    }

    public LiveData<Long> getCurrentBoardId() {
        return currentBoardId;
    }

    public LiveData<Board> getCurrentBoard() {
        return currentBoard;
    }

    public LiveData<List<ColumnWithTasks>> getColumnsWithTasks() {
        return columnsWithTasks;
    }

    // ========================================
    // COLUMN OPERATIONS
    // ========================================
    
    /**
     * Add a new column to the board.
     */
    public void addColumn(String name) {
        Long boardId = currentBoardId.getValue();
        if (boardId == null) return;
        
        Column column = new Column(name, boardId, 0);
        repository.insertColumn(column);
    }

    /**
     * Update column name.
     */
    public void renameColumn(Column column, String newName) {
        column.setName(newName);
        repository.updateColumn(column);
    }

    /**
     * Delete a column and all its tasks.
     */
    public void deleteColumn(Column column) {
        repository.deleteColumn(column);
    }

    /**
     * Reorder columns after drag.
     */
    public void reorderColumns(List<Column> columns) {
        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setPosition(i);
        }
        repository.updateColumnPositions(columns);
    }

    /**
     * Toggle column collapsed state.
     */
    public void toggleColumnCollapsed(long columnId, boolean collapsed) {
        repository.setColumnCollapsed(columnId, collapsed);
    }

    // ========================================
    // TASK OPERATIONS
    // ========================================
    
    /**
     * Move a task to a different column and/or position.
     */
    public void moveTask(long taskId, long newColumnId, int newPosition) {
        repository.moveTask(taskId, newColumnId, newPosition);
    }

    /**
     * Reorder tasks within a column after drag.
     */
    public void reorderTasks(List<Task> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setPosition(i);
        }
        repository.updateTaskPositions(tasks);
    }

    /**
     * Quick add a task to a column.
     */
    public void quickAddTask(String title, long columnId) {
        Long boardId = currentBoardId.getValue();
        if (boardId == null) return;
        
        Task task = new Task(title, columnId, boardId);
        repository.insertTask(task);
    }

    /**
     * Delete a task.
     */
    public void deleteTask(long taskId) {
        repository.deleteTaskById(taskId);
    }

    /**
     * Update task status.
     */
    public void updateTaskStatus(Task task, String newStatus) {
        task.setStatus(newStatus);
        repository.updateTask(task);
    }

    // ========================================
    // DRAG STATE
    // ========================================
    
    public void setDraggedTask(Task task) {
        draggedTask.setValue(task);
    }

    public LiveData<Task> getDraggedTask() {
        return draggedTask;
    }

    public void setDragTargetColumn(Integer columnIndex) {
        dragTargetColumn.setValue(columnIndex);
    }

    public LiveData<Integer> getDragTargetColumn() {
        return dragTargetColumn;
    }

    public void clearDragState() {
        draggedTask.setValue(null);
        dragTargetColumn.setValue(null);
    }

    // ========================================
    // UI STATE
    // ========================================
    
    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}
