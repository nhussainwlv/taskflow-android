package com.taskflow.data.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.taskflow.data.dao.BoardDao;
import com.taskflow.data.dao.ColumnDao;
import com.taskflow.data.dao.SavedViewDao;
import com.taskflow.data.dao.TagDao;
import com.taskflow.data.dao.TaskDao;
import com.taskflow.data.dao.UserDao;
import com.taskflow.model.Board;
import com.taskflow.model.Column;
import com.taskflow.model.SavedView;
import com.taskflow.model.Tag;
import com.taskflow.model.Task;
import com.taskflow.model.TaskTag;
import com.taskflow.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TaskFlowDatabase - Room Database for TaskFlow app.
 * 
 * Singleton pattern ensures only one instance of the database exists.
 * Includes sample data seeding for first-run experience.
 */
@Database(
    entities = {
        Task.class,
        Column.class,
        Board.class,
        Tag.class,
        User.class,
        TaskTag.class,
        SavedView.class
    },
    version = 2,
    exportSchema = true
)
public abstract class TaskFlowDatabase extends RoomDatabase {

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN category TEXT NOT NULL DEFAULT 'general'");
        }
    };

    // ========================================
    // DAOs
    // ========================================
    
    public abstract TaskDao taskDao();
    public abstract ColumnDao columnDao();
    public abstract BoardDao boardDao();
    public abstract TagDao tagDao();
    public abstract UserDao userDao();
    public abstract SavedViewDao savedViewDao();

    // ========================================
    // SINGLETON INSTANCE
    // ========================================
    
    private static volatile TaskFlowDatabase INSTANCE;
    
    // Executor for background database operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Get the singleton database instance.
     * Creates and seeds the database on first run.
     */
    public static TaskFlowDatabase getInstance(final Context context) {
        return getDatabase(context);
    }

    /**
     * Get the singleton database instance.
     * Creates and seeds the database on first run.
     */
    public static TaskFlowDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TaskFlowDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            TaskFlowDatabase.class,
                            "taskflow_database"
                    )
                            .addMigrations(MIGRATION_1_2)
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback to seed the database with sample data on creation.
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            
            // Seed sample data on background thread
            databaseWriteExecutor.execute(() -> {
                seedSampleData(INSTANCE);
            });
        }
    };

    /**
     * Seed the database with sample data for demonstration.
     * Creates users, tags, a board with columns, and sample tasks.
     */
    private static void seedSampleData(TaskFlowDatabase db) {
        // ========================================
        // SEED USERS
        // ========================================
        
        UserDao userDao = db.userDao();
        
        User currentUser = new User("You");
        currentUser.setEmail("you@taskflow.app");
        currentUser.setCurrentUser(true);
        currentUser.setAvatarColor(6);
        long currentUserId = userDao.insert(currentUser);
        
        User alex = new User("Alex Chen");
        alex.setEmail("alex@taskflow.app");
        alex.setAvatarColor(1);
        long alexId = userDao.insert(alex);
        
        User sarah = new User("Sarah Miller");
        sarah.setEmail("sarah@taskflow.app");
        sarah.setAvatarColor(4);
        long sarahId = userDao.insert(sarah);
        
        User mike = new User("Mike Johnson");
        mike.setEmail("mike@taskflow.app");
        mike.setAvatarColor(7);
        long mikeId = userDao.insert(mike);

        // ========================================
        // SEED TAGS
        // ========================================
        
        TagDao tagDao = db.tagDao();
        
        Tag bugTag = new Tag("Bug", Tag.Colors.RED);
        bugTag.setBackgroundColor("#FEE2E2");
        long bugTagId = tagDao.insert(bugTag);
        
        Tag featureTag = new Tag("Feature", Tag.Colors.BLUE);
        featureTag.setBackgroundColor("#DBEAFE");
        long featureTagId = tagDao.insert(featureTag);
        
        Tag urgentTag = new Tag("Urgent", Tag.Colors.ORANGE);
        urgentTag.setBackgroundColor("#FFEDD5");
        long urgentTagId = tagDao.insert(urgentTag);
        
        Tag designTag = new Tag("Design", Tag.Colors.PURPLE);
        designTag.setBackgroundColor("#F3E8FF");
        long designTagId = tagDao.insert(designTag);
        
        Tag backendTag = new Tag("Backend", Tag.Colors.GREEN);
        backendTag.setBackgroundColor("#DCFCE7");
        long backendTagId = tagDao.insert(backendTag);
        
        Tag frontendTag = new Tag("Frontend", Tag.Colors.CYAN);
        frontendTag.setBackgroundColor("#CFFAFE");
        long frontendTagId = tagDao.insert(frontendTag);
        
        Tag docsTag = new Tag("Documentation", Tag.Colors.GRAY);
        docsTag.setBackgroundColor("#F3F4F6");
        long docsTagId = tagDao.insert(docsTag);

        // ========================================
        // SEED BOARD
        // ========================================
        
        BoardDao boardDao = db.boardDao();
        
        Board projectBoard = new Board("Product Launch");
        projectBoard.setDescription("Q1 Product Launch - Mobile App Release");
        projectBoard.setColor("#4F46E5");
        projectBoard.setIcon("🚀");
        long boardId = boardDao.insert(projectBoard);

        // ========================================
        // SEED COLUMNS
        // ========================================
        
        ColumnDao columnDao = db.columnDao();
        
        Column todoColumn = new Column("To Do", boardId, 0);
        todoColumn.setColor("#64748B");
        long todoColumnId = columnDao.insert(todoColumn);
        
        Column inProgressColumn = new Column("In Progress", boardId, 1);
        inProgressColumn.setColor("#3B82F6");
        inProgressColumn.setWipLimit(3);
        long inProgressColumnId = columnDao.insert(inProgressColumn);
        
        Column reviewColumn = new Column("In Review", boardId, 2);
        reviewColumn.setColor("#8B5CF6");
        long reviewColumnId = columnDao.insert(reviewColumn);
        
        Column doneColumn = new Column("Done", boardId, 3);
        doneColumn.setColor("#22C55E");
        doneColumn.setDoneColumn(true);
        long doneColumnId = columnDao.insert(doneColumn);

        // ========================================
        // SEED TASKS
        // ========================================
        
        TaskDao taskDao = db.taskDao();
        long now = System.currentTimeMillis();
        long dayMs = 24 * 60 * 60 * 1000;

        // TO DO Tasks
        Task task1 = new Task("Implement push notifications", todoColumnId, boardId);
        task1.setDescription("Add Firebase Cloud Messaging for real-time notifications");
        task1.setCategory(Task.Category.WORK);
        task1.setPriority(Task.Priority.HIGH);
        task1.setDueDate(now + (3 * dayMs));
        task1.setAssigneeId(currentUserId);
        task1.setPosition(0);
        task1.setEstimatedDuration(240);
        long task1Id = taskDao.insert(task1);
        tagDao.addTagToTask(new TaskTag(task1Id, featureTagId));
        tagDao.addTagToTask(new TaskTag(task1Id, backendTagId));

        Task task2 = new Task("Design dark mode theme", todoColumnId, boardId);
        task2.setDescription("Create dark theme color palette and component styles");
        task2.setCategory(Task.Category.STUDY);
        task2.setPriority(Task.Priority.MEDIUM);
        task2.setDueDate(now + (5 * dayMs));
        task2.setAssigneeId(sarahId);
        task2.setPosition(1);
        long task2Id = taskDao.insert(task2);
        tagDao.addTagToTask(new TaskTag(task2Id, designTagId));
        tagDao.addTagToTask(new TaskTag(task2Id, frontendTagId));

        Task task3 = new Task("Write API documentation", todoColumnId, boardId);
        task3.setDescription("Document all REST endpoints with examples");
        task3.setCategory(Task.Category.GENERAL);
        task3.setPriority(Task.Priority.LOW);
        task3.setDueDate(now + (7 * dayMs));
        task3.setAssigneeId(mikeId);
        task3.setPosition(2);
        long task3Id = taskDao.insert(task3);
        tagDao.addTagToTask(new TaskTag(task3Id, docsTagId));

        // IN PROGRESS Tasks
        Task task4 = new Task("Fix login crash on Android 14", inProgressColumnId, boardId);
        task4.setDescription("App crashes when biometric prompt is cancelled. Needs null check.");
        task4.setCategory(Task.Category.PERSONAL);
        task4.setStatus(Task.Status.IN_PROGRESS);
        task4.setPriority(Task.Priority.URGENT);
        task4.setDueDate(now + dayMs);
        task4.setAssigneeId(alexId);
        task4.setPosition(0);
        task4.setProgress(60);
        task4.setStartDate(now - (2 * dayMs));
        long task4Id = taskDao.insert(task4);
        tagDao.addTagToTask(new TaskTag(task4Id, bugTagId));
        tagDao.addTagToTask(new TaskTag(task4Id, urgentTagId));

        Task task5 = new Task("Implement offline data sync", inProgressColumnId, boardId);
        task5.setDescription("Use WorkManager for background sync when connection restored");
        task5.setCategory(Task.Category.STUDY);
        task5.setStatus(Task.Status.IN_PROGRESS);
        task5.setPriority(Task.Priority.HIGH);
        task5.setDueDate(now + (2 * dayMs));
        task5.setAssigneeId(currentUserId);
        task5.setPosition(1);
        task5.setProgress(35);
        task5.setStartDate(now - dayMs);
        long task5Id = taskDao.insert(task5);
        tagDao.addTagToTask(new TaskTag(task5Id, featureTagId));
        tagDao.addTagToTask(new TaskTag(task5Id, backendTagId));

        // IN REVIEW Tasks
        Task task6 = new Task("Add swipe-to-delete gesture", reviewColumnId, boardId);
        task6.setDescription("Implement ItemTouchHelper for swipe actions in task list");
        task6.setCategory(Task.Category.GENERAL);
        task6.setStatus(Task.Status.REVIEW);
        task6.setPriority(Task.Priority.MEDIUM);
        task6.setAssigneeId(alexId);
        task6.setPosition(0);
        task6.setProgress(90);
        long task6Id = taskDao.insert(task6);
        tagDao.addTagToTask(new TaskTag(task6Id, featureTagId));
        tagDao.addTagToTask(new TaskTag(task6Id, frontendTagId));

        // DONE Tasks
        Task task7 = new Task("Set up CI/CD pipeline", doneColumnId, boardId);
        task7.setDescription("Configure GitHub Actions for automated builds and tests");
        task7.setCategory(Task.Category.GENERAL);
        task7.setStatus(Task.Status.DONE);
        task7.setPriority(Task.Priority.HIGH);
        task7.setAssigneeId(mikeId);
        task7.setPosition(0);
        task7.setProgress(100);
        task7.setCompletedAt(now - dayMs);
        long task7Id = taskDao.insert(task7);
        tagDao.addTagToTask(new TaskTag(task7Id, backendTagId));

        Task task8 = new Task("Create app icon and splash screen", doneColumnId, boardId);
        task8.setDescription("Design adaptive icon and animated splash for Android 12+");
        task8.setCategory(Task.Category.WORK);
        task8.setStatus(Task.Status.DONE);
        task8.setPriority(Task.Priority.MEDIUM);
        task8.setAssigneeId(sarahId);
        task8.setPosition(1);
        task8.setProgress(100);
        task8.setCompletedAt(now - (2 * dayMs));
        long task8Id = taskDao.insert(task8);
        tagDao.addTagToTask(new TaskTag(task8Id, designTagId));

        // ========================================
        // SEED SECOND BOARD
        // ========================================
        
        Board marketingBoard = new Board("Marketing Campaign");
        marketingBoard.setDescription("Social media and content strategy");
        marketingBoard.setColor("#EC4899");
        marketingBoard.setIcon("📣");
        marketingBoard.setPosition(1);
        long marketingBoardId = boardDao.insert(marketingBoard);

        Column mTodoColumn = new Column("Ideas", marketingBoardId, 0);
        mTodoColumn.setColor("#64748B");
        long mTodoColumnId = columnDao.insert(mTodoColumn);
        
        Column mInProgressColumn = new Column("Creating", marketingBoardId, 1);
        mInProgressColumn.setColor("#3B82F6");
        long mInProgressColumnId = columnDao.insert(mInProgressColumn);
        
        Column mPublishedColumn = new Column("Published", marketingBoardId, 2);
        mPublishedColumn.setColor("#22C55E");
        mPublishedColumn.setDoneColumn(true);
        columnDao.insert(mPublishedColumn);

        Task mTask1 = new Task("Blog post: Getting Started Guide", mTodoColumnId, marketingBoardId);
        mTask1.setDescription("Write comprehensive onboarding guide for new users");
        mTask1.setCategory(Task.Category.STUDY);
        mTask1.setPriority(Task.Priority.HIGH);
        mTask1.setDueDate(now + (4 * dayMs));
        mTask1.setPosition(0);
        taskDao.insert(mTask1);

        Task mTask2 = new Task("Create demo video", mInProgressColumnId, marketingBoardId);
        mTask2.setDescription("2-minute product walkthrough for landing page");
        mTask2.setCategory(Task.Category.HEALTH);
        mTask2.setStatus(Task.Status.IN_PROGRESS);
        mTask2.setPriority(Task.Priority.URGENT);
        mTask2.setDueDate(now + dayMs);
        mTask2.setProgress(45);
        mTask2.setPosition(0);
        taskDao.insert(mTask2);

        // ========================================
        // SEED SAVED VIEW
        // ========================================
        
        SavedViewDao savedViewDao = db.savedViewDao();
        
        SavedView myTasksView = new SavedView("My Tasks", Board.ViewType.LIST);
        myTasksView.setFilters("{\"assignee\":" + currentUserId + "}");
        myTasksView.setSort("{\"field\":\"due_date\",\"direction\":\"ASC\"}");
        savedViewDao.insert(myTasksView);
        
        SavedView urgentView = new SavedView("Urgent & High Priority", Board.ViewType.LIST);
        urgentView.setFilters("{\"priority\":[\"URGENT\",\"HIGH\"]}");
        urgentView.setSort("{\"field\":\"priority\",\"direction\":\"DESC\"}");
        savedViewDao.insert(urgentView);
    }
}
