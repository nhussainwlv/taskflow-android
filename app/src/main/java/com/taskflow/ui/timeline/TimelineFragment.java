/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.ui.timeline;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taskflow.R;
import com.taskflow.model.Task;
import com.taskflow.ui.detail.QuickAddBottomSheet;
import com.taskflow.ui.detail.TaskDetailBottomSheet;
import com.taskflow.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Timeline Fragment
 * 
 * Displays tasks in a horizontal timeline/Gantt-like view with pinch-to-zoom
 * capability using ScaleGestureDetector. Tasks are grouped by assignee or project.
 */
public class TimelineFragment extends Fragment implements TimelineAdapter.TimelineInteractionListener {

    private TaskViewModel viewModel;
    private RecyclerView recyclerTimeline;
    private TimelineAdapter adapter;
    private HorizontalScrollView headerScrollView;
    private View emptyState;
    private FloatingActionButton fabAdd;
    private View zoomControls;
    private TextView textZoomLevel;
    
    // Timeline scale (days per screen width)
    private float currentScale = 1.0f;
    private static final float MIN_SCALE = 0.5f;
    private static final float MAX_SCALE = 3.0f;
    
    // Gesture detectors
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    
    // Timeline range
    private long timelineStartDate;
    private long timelineEndDate;
    private int visibleDays = 14; // Default: 2 weeks view
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timeline, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        
        initViews(view);
        setupGestureDetectors();
        setupRecyclerView();
        setupZoomControls(view);
        initializeTimelineRange();
        observeData();
    }

    private void initViews(View view) {
        recyclerTimeline = view.findViewById(R.id.recycler_timeline);
        headerScrollView = view.findViewById(R.id.scroll_header);
        emptyState = view.findViewById(R.id.empty_state);
        fabAdd = view.findViewById(R.id.fab_add);
        zoomControls = view.findViewById(R.id.zoom_controls);
        textZoomLevel = view.findViewById(R.id.text_zoom_level);
        
        fabAdd.setOnClickListener(v -> showQuickAdd());
        
        // Update zoom level display
        updateZoomDisplay();
    }

    private void setupGestureDetectors() {
        // Scale gesture detector for pinch-to-zoom
        scaleGestureDetector = new ScaleGestureDetector(requireContext(), 
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        float scaleFactor = detector.getScaleFactor();
                        currentScale *= scaleFactor;
                        currentScale = Math.max(MIN_SCALE, Math.min(currentScale, MAX_SCALE));
                        
                        updateTimelineScale();
                        updateZoomDisplay();
                        return true;
                    }
                });

        // Regular gesture detector for scrolling
        gestureDetector = new GestureDetector(requireContext(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, 
                                            float distanceX, float distanceY) {
                        // Handle horizontal scrolling
                        if (headerScrollView != null) {
                            headerScrollView.scrollBy((int) distanceX, 0);
                        }
                        return true;
                    }
                });
    }

    private void setupRecyclerView() {
        adapter = new TimelineAdapter(this);
        recyclerTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTimeline.setAdapter(adapter);
        
        // Handle touch events for zoom and scroll
        recyclerTimeline.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            return false; // Allow RecyclerView to handle vertical scrolling
        });
        
        // Sync horizontal scroll between header and content
        recyclerTimeline.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                // Could sync scroll position here if needed
            }
        });
    }

    private void setupZoomControls(View view) {
        View btnZoomIn = view.findViewById(R.id.btn_zoom_in);
        View btnZoomOut = view.findViewById(R.id.btn_zoom_out);
        View btnFitScreen = view.findViewById(R.id.btn_fit_screen);
        
        if (btnZoomIn != null) {
            btnZoomIn.setOnClickListener(v -> {
                currentScale = Math.min(currentScale * 1.2f, MAX_SCALE);
                updateTimelineScale();
                updateZoomDisplay();
            });
        }
        
        if (btnZoomOut != null) {
            btnZoomOut.setOnClickListener(v -> {
                currentScale = Math.max(currentScale / 1.2f, MIN_SCALE);
                updateTimelineScale();
                updateZoomDisplay();
            });
        }
        
        if (btnFitScreen != null) {
            btnFitScreen.setOnClickListener(v -> {
                currentScale = 1.0f;
                updateTimelineScale();
                updateZoomDisplay();
            });
        }
    }

    private void initializeTimelineRange() {
        Calendar calendar = Calendar.getInstance();
        
        // Start from beginning of current week
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        timelineStartDate = calendar.getTimeInMillis();
        
        // End 4 weeks later by default
        calendar.add(Calendar.WEEK_OF_YEAR, 4);
        timelineEndDate = calendar.getTimeInMillis();
    }

    private void observeData() {
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                List<TimelineRow> timelineRows = buildTimelineRows(tasks);
                adapter.submitList(timelineRows);
                updateEmptyState(timelineRows.isEmpty());
            }
        });
    }

    /**
     * Builds timeline rows from tasks, grouped by assignee or board.
     */
    private List<TimelineRow> buildTimelineRows(List<Task> tasks) {
        List<TimelineRow> rows = new ArrayList<>();
        
        // Group tasks by assignee (using assigneeId)
        Map<Long, List<Task>> tasksByAssignee = new HashMap<>();
        List<Task> unassignedTasks = new ArrayList<>();
        
        for (Task task : tasks) {
            // Only include tasks with due dates for timeline
            if (task.getDueDate() != null) {
                if (task.getAssigneeId() != null && task.getAssigneeId() > 0) {
                    tasksByAssignee.computeIfAbsent(task.getAssigneeId(), 
                            k -> new ArrayList<>()).add(task);
                } else {
                    unassignedTasks.add(task);
                }
            }
        }
        
        // Create rows for each assignee group
        for (Map.Entry<Long, List<Task>> entry : tasksByAssignee.entrySet()) {
            TimelineRow row = new TimelineRow();
            row.setGroupId(entry.getKey());
            row.setGroupName("Assignee " + entry.getKey()); // Would fetch real name from User
            row.setTasks(entry.getValue());
            row.setTimelineStart(timelineStartDate);
            row.setTimelineEnd(timelineEndDate);
            row.setScale(currentScale);
            rows.add(row);
        }
        
        // Add unassigned tasks row if any
        if (!unassignedTasks.isEmpty()) {
            TimelineRow row = new TimelineRow();
            row.setGroupId(0);
            row.setGroupName(getString(R.string.timeline_unassigned));
            row.setTasks(unassignedTasks);
            row.setTimelineStart(timelineStartDate);
            row.setTimelineEnd(timelineEndDate);
            row.setScale(currentScale);
            rows.add(row);
        }
        
        return rows;
    }

    private void updateTimelineScale() {
        // Calculate visible days based on scale
        visibleDays = (int) (14 / currentScale);
        
        // Rebuild timeline with new scale
        viewModel.getAllTasks().getValue();
        adapter.setScale(currentScale);
    }

    private void updateZoomDisplay() {
        if (textZoomLevel != null) {
            int percentage = (int) (currentScale * 100);
            textZoomLevel.setText(String.format(Locale.getDefault(), "%d%%", percentage));
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyState != null) {
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerTimeline.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void showQuickAdd() {
        QuickAddBottomSheet bottomSheet = new QuickAddBottomSheet();
        bottomSheet.show(getChildFragmentManager(), QuickAddBottomSheet.TAG);
    }

    // TimelineInteractionListener implementation

    @Override
    public void onTaskClick(Task task) {
        TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(task.getId());
        bottomSheet.show(getChildFragmentManager(), TaskDetailBottomSheet.TAG);
    }

    @Override
    public void onTaskDrag(Task task, long newStartDate, long newEndDate) {
        // Handle task date update from drag
        task.setDueDate(newEndDate);
        viewModel.updateTask(task);
    }
    
    /**
     * Data class representing a row in the timeline view.
     */
    public static class TimelineRow {
        private long groupId;
        private String groupName;
        private List<Task> tasks;
        private long timelineStart;
        private long timelineEnd;
        private float scale;

        public long getGroupId() { return groupId; }
        public void setGroupId(long groupId) { this.groupId = groupId; }
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        public List<Task> getTasks() { return tasks; }
        public void setTasks(List<Task> tasks) { this.tasks = tasks; }
        public long getTimelineStart() { return timelineStart; }
        public void setTimelineStart(long timelineStart) { this.timelineStart = timelineStart; }
        public long getTimelineEnd() { return timelineEnd; }
        public void setTimelineEnd(long timelineEnd) { this.timelineEnd = timelineEnd; }
        public float getScale() { return scale; }
        public void setScale(float scale) { this.scale = scale; }
    }
}
