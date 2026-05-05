package com.taskflow.data.repo;

import android.app.Application;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;

import com.taskflow.data.dao.BoardDao;
import com.taskflow.data.dao.ColumnDao;
import com.taskflow.data.dao.SavedViewDao;
import com.taskflow.data.dao.TagDao;
import com.taskflow.data.dao.TaskDao;
import com.taskflow.data.dao.UserDao;
import com.taskflow.data.db.TaskFlowDatabase;
import com.taskflow.model.Board;
import com.taskflow.model.BoardWithColumns;
import com.taskflow.model.Column;
import com.taskflow.model.ColumnWithTasks;
import com.taskflow.model.SavedView;
import com.taskflow.model.Tag;
import com.taskflow.model.Task;
import com.taskflow.model.TaskTag;
import com.taskflow.model.TaskWithTags;
import com.taskflow.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * TaskRepository - Single source of truth for all data operations.
 * 
 * Abstracts data sources and provides a clean API for ViewModels.
 * All database operations run on background threads via the executor.
 */
public class TaskRepository {

    // ========================================
    // DAOs
    // ========================================
    
    private final TaskDao taskDao;
    private final ColumnDao columnDao;
    private final BoardDao boardDao;
    private final TagDao tagDao;
    private final UserDao userDao;
    private final SavedViewDao savedViewDao;
    
    private final ExecutorService executor;
    private final Application application;

    // ========================================
    // CONSTRUCTOR
    // ========================================
    
    public TaskRepository(Application application) {
        this.application = application;
        TaskFlowDatabase db = TaskFlowDatabase.getDatabase(application);
        taskDao = db.taskDao();
        columnDao = db.columnDao();
        boardDao = db.boardDao();
        tagDao = db.tagDao();
        userDao = db.userDao();
        savedViewDao = db.savedViewDao();
        executor = TaskFlowDatabase.databaseWriteExecutor;
    }

    // ========================================
    // TASK OPERATIONS
    // ========================================
    
    public void insertTask(Task task) {
        insertTask(task, (Consumer<Long>) null);
    }

    /**
     * Inserts task and notifies on the main thread with the generated row id.
     */
    public void insertTask(Task task, Consumer<Long> onInserted) {
        executor.execute(() -> {
            task.setPosition(taskDao.getNextPosition(task.getColumnId()));
            long id = taskDao.insert(task);
            if (onInserted != null) {
                ContextCompat.getMainExecutor(application).execute(() -> onInserted.accept(id));
            }
        });
    }

    public void insertTask(Task task, List<Long> tagIds) {
        executor.execute(() -> {
            task.setPosition(taskDao.getNextPosition(task.getColumnId()));
            long taskId = taskDao.insert(task);
            if (tagIds != null) {
                for (Long tagId : tagIds) {
                    tagDao.addTagToTask(new TaskTag(taskId, tagId));
                }
            }
        });
    }

    public void updateTask(Task task) {
        executor.execute(() -> {
            task.touch();
            taskDao.update(task);
        });
    }

    public void deleteTask(Task task) {
        executor.execute(() -> taskDao.delete(task));
    }

    public void deleteTaskById(long taskId) {
        executor.execute(() -> taskDao.deleteById(taskId));
    }

    public void deleteTasksByIds(List<Long> taskIds) {
        executor.execute(() -> taskDao.deleteByIds(taskIds));
    }

    public LiveData<Task> getTaskById(long taskId) {
        return taskDao.getTaskById(taskId);
    }

    public LiveData<TaskWithTags> getTaskWithTagsById(long taskId) {
        return taskDao.getTaskWithTagsById(taskId);
    }

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getTasksByColumn(long columnId) {
        return taskDao.getTasksByColumn(columnId);
    }

    public LiveData<List<TaskWithTags>> getTasksWithTagsByColumn(long columnId) {
        return taskDao.getTasksWithTagsByColumn(columnId);
    }

    public LiveData<List<Task>> getTasksByBoard(long boardId) {
        return taskDao.getTasksByBoard(boardId);
    }

    public LiveData<List<TaskWithTags>> getTasksWithTagsByBoard(long boardId) {
        return taskDao.getTasksWithTagsByBoard(boardId);
    }

    public LiveData<List<Task>> getTasksByStatus(String status) {
        return taskDao.getTasksByStatus(status);
    }

    public LiveData<List<Task>> getActiveTasks() {
        return taskDao.getActiveTasks();
    }

    public LiveData<List<Task>> getOverdueTasks() {
        return taskDao.getOverdueTasks(System.currentTimeMillis());
    }

    public LiveData<List<Task>> getTasksDueToday() {
        long now = System.currentTimeMillis();
        long startOfDay = now - (now % (24 * 60 * 60 * 1000));
        long endOfDay = startOfDay + (24 * 60 * 60 * 1000);
        return taskDao.getTasksDueOnDate(startOfDay, endOfDay);
    }

    public LiveData<List<Task>> getTasksDueBetween(long start, long end) {
        return taskDao.getTasksDueBetween(start, end);
    }

    public LiveData<List<TaskWithTags>> getTasksWithTagsDueBetween(long start, long end) {
        return taskDao.getTasksWithTagsDueBetween(start, end);
    }

    public LiveData<List<TaskWithTags>> getTasksByAssignee(long userId) {
        return taskDao.getTasksWithTagsByAssignee(userId);
    }

    public LiveData<List<Task>> getHighPriorityTasks() {
        return taskDao.getHighPriorityTasks();
    }

    public LiveData<List<TaskWithTags>> searchTasksWithTags(String query) {
        return taskDao.searchTasksWithTags(query);
    }

    public LiveData<List<Task>> getTasksForTimeline(long start, long end) {
        return taskDao.getTasksForTimeline(start, end);
    }

    public LiveData<List<TaskWithTags>> getTasksWithTagsForTimeline(long start, long end) {
        return taskDao.getTasksWithTagsForTimeline(start, end);
    }

    // Sorting queries
    public LiveData<List<Task>> getTasksSortedByCreatedDesc(long boardId) {
        return taskDao.getTasksSortedByCreatedDesc(boardId);
    }

    public LiveData<List<Task>> getTasksSortedByDueDateAsc(long boardId) {
        return taskDao.getTasksSortedByDueDateAsc(boardId);
    }

    public LiveData<List<Task>> getTasksSortedByPriorityDesc(long boardId) {
        return taskDao.getTasksSortedByPriorityDesc(boardId);
    }

    public LiveData<List<Task>> getTasksSortedByTitleAsc(long boardId) {
        return taskDao.getTasksSortedByTitleAsc(boardId);
    }

    public LiveData<List<Task>> getTasksSortedByProgressDesc(long boardId) {
        return taskDao.getTasksSortedByProgressDesc(boardId);
    }

    public LiveData<List<Task>> getTasksSortedByUpdatedDesc(long boardId) {
        return taskDao.getTasksSortedByUpdatedDesc(boardId);
    }

    // Task movement and position updates
    public void moveTask(long taskId, long newColumnId, int newPosition) {
        executor.execute(() -> 
            taskDao.moveTask(taskId, newColumnId, newPosition, System.currentTimeMillis())
        );
    }

    public void updateTaskPosition(long taskId, int position) {
        executor.execute(() -> taskDao.updatePosition(taskId, position));
    }

    public void updateTaskPositions(List<Task> tasks) {
        executor.execute(() -> taskDao.updateAll(tasks));
    }

    // Complete task
    public void completeTask(long taskId) {
        executor.execute(() -> 
            taskDao.completeTask(taskId, System.currentTimeMillis())
        );
    }

    // Bulk operations
    public void updateTaskStatusBulk(List<Long> taskIds, String status) {
        executor.execute(() -> 
            taskDao.updateStatusBulk(taskIds, status, System.currentTimeMillis())
        );
    }

    public void archiveTasksBulk(List<Long> taskIds) {
        executor.execute(() -> 
            taskDao.archiveBulk(taskIds, System.currentTimeMillis())
        );
    }

    public void assignTasksBulk(List<Long> taskIds, Long assigneeId) {
        executor.execute(() -> 
            taskDao.assignBulk(taskIds, assigneeId, System.currentTimeMillis())
        );
    }

    public void moveTasksToColumnBulk(List<Long> taskIds, long columnId) {
        executor.execute(() -> 
            taskDao.moveToColumnBulk(taskIds, columnId, System.currentTimeMillis())
        );
    }

    // Statistics
    public LiveData<Integer> getTaskCount(long boardId) {
        return taskDao.getTaskCount(boardId);
    }

    public LiveData<Integer> getCompletedTaskCount(long boardId) {
        return taskDao.getCompletedTaskCount(boardId);
    }

    public LiveData<Integer> getOverdueCount() {
        return taskDao.getOverdueCount(System.currentTimeMillis());
    }

    public LiveData<Integer> getTasksDueTodayCount() {
        long now = System.currentTimeMillis();
        long startOfDay = now - (now % (24 * 60 * 60 * 1000));
        long endOfDay = startOfDay + (24 * 60 * 60 * 1000);
        return taskDao.getTasksDueTodayCount(startOfDay, endOfDay);
    }

    // ========================================
    // COLUMN OPERATIONS
    // ========================================
    
    public void insertColumn(Column column) {
        executor.execute(() -> {
            column.setPosition(columnDao.getNextPosition(column.getBoardId()));
            columnDao.insert(column);
        });
    }

    public void updateColumn(Column column) {
        executor.execute(() -> {
            column.touch();
            columnDao.update(column);
        });
    }

    public void deleteColumn(Column column) {
        executor.execute(() -> columnDao.delete(column));
    }

    public LiveData<Column> getColumnById(long columnId) {
        return columnDao.getColumnById(columnId);
    }

    public LiveData<List<Column>> getColumnsByBoard(long boardId) {
        return columnDao.getColumnsByBoard(boardId);
    }

    public LiveData<List<ColumnWithTasks>> getColumnsWithTasksByBoard(long boardId) {
        return columnDao.getColumnsWithTasksByBoard(boardId);
    }

    public void updateColumnPosition(long columnId, int position) {
        executor.execute(() -> columnDao.updatePosition(columnId, position));
    }

    public void updateColumnPositions(List<Column> columns) {
        executor.execute(() -> columnDao.updateAll(columns));
    }

    public void setColumnCollapsed(long columnId, boolean collapsed) {
        executor.execute(() -> columnDao.setCollapsed(columnId, collapsed));
    }

    public LiveData<Integer> getColumnTaskCount(long columnId) {
        return taskDao.getColumnTaskCount(columnId);
    }

    // ========================================
    // BOARD OPERATIONS
    // ========================================
    
    public void insertBoard(Board board) {
        executor.execute(() -> {
            board.setPosition(boardDao.getNextPosition());
            boardDao.insert(board);
        });
    }

    public void insertBoardWithColumns(Board board, List<Column> columns) {
        executor.execute(() -> {
            board.setPosition(boardDao.getNextPosition());
            long boardId = boardDao.insert(board);
            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                column.setBoardId(boardId);
                column.setPosition(i);
                columnDao.insert(column);
            }
        });
    }

    public void updateBoard(Board board) {
        executor.execute(() -> {
            board.touch();
            boardDao.update(board);
        });
    }

    public void deleteBoard(Board board) {
        executor.execute(() -> boardDao.delete(board));
    }

    public LiveData<Board> getBoardById(long boardId) {
        return boardDao.getBoardById(boardId);
    }

    public LiveData<BoardWithColumns> getBoardWithColumnsById(long boardId) {
        return boardDao.getBoardWithColumnsById(boardId);
    }

    public LiveData<List<Board>> getAllBoards() {
        return boardDao.getAllBoards();
    }

    public LiveData<List<BoardWithColumns>> getAllBoardsWithColumns() {
        return boardDao.getAllBoardsWithColumns();
    }

    public LiveData<List<Board>> getFavoriteBoards() {
        return boardDao.getFavoriteBoards();
    }

    public LiveData<List<Board>> getRecentBoards(int limit) {
        return boardDao.getRecentBoards(limit);
    }

    public LiveData<List<Board>> searchBoards(String query) {
        return boardDao.searchBoards(query);
    }

    public void setBoardFavorite(long boardId, boolean favorite) {
        executor.execute(() -> boardDao.setFavorite(boardId, favorite, System.currentTimeMillis()));
    }

    public void setBoardArchived(long boardId, boolean archived) {
        executor.execute(() -> boardDao.setArchived(boardId, archived, System.currentTimeMillis()));
    }

    public void updateBoardLastAccessed(long boardId) {
        executor.execute(() -> boardDao.updateLastAccessed(boardId, System.currentTimeMillis()));
    }

    public void setBoardDefaultView(long boardId, String viewType) {
        executor.execute(() -> boardDao.setDefaultView(boardId, viewType, System.currentTimeMillis()));
    }

    public LiveData<Integer> getBoardCount() {
        return boardDao.getBoardCount();
    }

    // ========================================
    // TAG OPERATIONS
    // ========================================
    
    public void insertTag(Tag tag) {
        executor.execute(() -> tagDao.insert(tag));
    }

    public void updateTag(Tag tag) {
        executor.execute(() -> tagDao.update(tag));
    }

    public void deleteTag(Tag tag) {
        executor.execute(() -> tagDao.delete(tag));
    }

    public LiveData<Tag> getTagById(long tagId) {
        return tagDao.getTagById(tagId);
    }

    public LiveData<List<Tag>> getAllTags() {
        return tagDao.getAllTags();
    }

    public LiveData<List<Tag>> getTagsForTask(long taskId) {
        return tagDao.getTagsForTask(taskId);
    }

    public void addTagToTask(long taskId, long tagId) {
        executor.execute(() -> tagDao.addTagToTask(new TaskTag(taskId, tagId)));
    }

    public void removeTagFromTask(long taskId, long tagId) {
        executor.execute(() -> tagDao.removeTagFromTask(taskId, tagId));
    }

    public void updateTaskTags(long taskId, List<Long> tagIds) {
        executor.execute(() -> {
            tagDao.removeAllTagsFromTask(taskId);
            for (Long tagId : tagIds) {
                tagDao.addTagToTask(new TaskTag(taskId, tagId));
            }
        });
    }

    public LiveData<List<Tag>> searchTags(String query) {
        return tagDao.searchTags(query);
    }

    // ========================================
    // USER OPERATIONS
    // ========================================
    
    public void insertUser(User user) {
        executor.execute(() -> userDao.insert(user));
    }

    public void updateUser(User user) {
        executor.execute(() -> userDao.update(user));
    }

    public void deleteUser(User user) {
        executor.execute(() -> userDao.delete(user));
    }

    public LiveData<User> getUserById(long userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }

    public LiveData<User> getCurrentUser() {
        return userDao.getCurrentUser();
    }

    public LiveData<List<User>> searchUsers(String query) {
        return userDao.searchUsers(query);
    }

    // ========================================
    // SAVED VIEW OPERATIONS
    // ========================================
    
    public void insertSavedView(SavedView savedView) {
        executor.execute(() -> savedViewDao.insert(savedView));
    }

    public void updateSavedView(SavedView savedView) {
        executor.execute(() -> {
            savedView.touch();
            savedViewDao.update(savedView);
        });
    }

    public void deleteSavedView(SavedView savedView) {
        executor.execute(() -> savedViewDao.delete(savedView));
    }

    public LiveData<SavedView> getSavedViewById(long viewId) {
        return savedViewDao.getSavedViewById(viewId);
    }

    public LiveData<List<SavedView>> getAllSavedViews() {
        return savedViewDao.getAllSavedViews();
    }

    public LiveData<List<SavedView>> getSavedViewsByType(String viewType) {
        return savedViewDao.getSavedViewsByType(viewType);
    }

    public LiveData<List<SavedView>> getSavedViewsForBoard(Long boardId) {
        return savedViewDao.getSavedViewsForBoard(boardId);
    }

    public void setSavedViewAsDefault(long viewId, String viewType) {
        executor.execute(() -> {
            savedViewDao.clearDefaultForType(viewType);
            savedViewDao.setAsDefault(viewId);
        });
    }

    /**
     * Exports boards, columns, tasks, users, and tags as JSON (dissertation / portability).
     */
    public void exportAllDataAsJson(Consumer<String> onResult) {
        executor.execute(() -> {
            try {
                JSONObject root = new JSONObject();
                root.put("exportVersion", 1);
                root.put("exportedAt", System.currentTimeMillis());

                JSONArray boardsArr = new JSONArray();
                for (Board b : boardDao.getAllBoardsSync()) {
                    JSONObject bj = new JSONObject();
                    bj.put("id", b.getId());
                    bj.put("name", b.getName());
                    bj.put("description", b.getDescription());
                    bj.put("color", b.getColor());
                    JSONArray colsArr = new JSONArray();
                    for (Column c : columnDao.getColumnsByBoardSync(b.getId())) {
                        JSONObject cj = new JSONObject();
                        cj.put("id", c.getId());
                        cj.put("name", c.getName());
                        cj.put("position", c.getPosition());
                        cj.put("color", c.getColor());
                        JSONArray tasksArr = new JSONArray();
                        for (Task t : taskDao.getTasksByColumnSync(c.getId())) {
                            tasksArr.put(taskToExportJson(t));
                        }
                        cj.put("tasks", tasksArr);
                        colsArr.put(cj);
                    }
                    bj.put("columns", colsArr);
                    boardsArr.put(bj);
                }
                root.put("boards", boardsArr);

                JSONArray usersArr = new JSONArray();
                for (User u : userDao.getAllUsersSync()) {
                    JSONObject uj = new JSONObject();
                    uj.put("id", u.getId());
                    uj.put("name", u.getName());
                    uj.put("email", u.getEmail());
                    usersArr.put(uj);
                }
                root.put("users", usersArr);

                JSONArray tagsArr = new JSONArray();
                for (Tag tag : tagDao.getAllTagsSync()) {
                    JSONObject tj = new JSONObject();
                    tj.put("id", tag.getId());
                    tj.put("name", tag.getName());
                    tj.put("color", tag.getColor());
                    tagsArr.put(tj);
                }
                root.put("tags", tagsArr);

                final String json = root.toString(2);
                ContextCompat.getMainExecutor(application).execute(() -> onResult.accept(json));
            } catch (JSONException e) {
                final String err = "{\"error\":\"Export failed: " + e.getMessage() + "\"}";
                ContextCompat.getMainExecutor(application).execute(() -> onResult.accept(err));
            }
        });
    }

    private static JSONObject taskToExportJson(Task t) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", t.getId());
        o.put("title", t.getTitle());
        o.put("description", t.getDescription() != null ? t.getDescription() : "");
        o.put("status", t.getStatus());
        o.put("priority", t.getPriority());
        o.put("category", t.getCategory());
        o.put("columnId", t.getColumnId());
        o.put("boardId", t.getBoardId());
        o.put("assigneeId", t.getAssigneeId() != null ? t.getAssigneeId() : JSONObject.NULL);
        o.put("dueDate", t.getDueDate() != null ? t.getDueDate() : JSONObject.NULL);
        o.put("progress", t.getProgress());
        o.put("completed", t.isCompleted());
        o.put("archived", t.isArchived());
        o.put("createdAt", t.getCreatedAt());
        o.put("updatedAt", t.getUpdatedAt());
        return o;
    }
}
