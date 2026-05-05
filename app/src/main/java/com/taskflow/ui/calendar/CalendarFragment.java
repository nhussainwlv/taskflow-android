package com.taskflow.ui.calendar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
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
import java.util.List;
import java.util.Locale;

/**
 * Calendar Fragment
 * 
 * Displays tasks in a calendar view with task chips for each day.
 * Provides monthly overview with daily task list below.
 */
public class CalendarFragment extends Fragment implements CalendarTaskAdapter.TaskClickListener {

    private TaskViewModel viewModel;
    private CalendarView calendarView;
    private RecyclerView recyclerTasks;
    private CalendarTaskAdapter adapter;
    private View emptyState;
    private TextView textSelectedDate;
    private FloatingActionButton fabAdd;
    
    private long selectedDateMillis;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        
        initViews(view);
        setupCalendar();
        setupRecyclerView();
        observeData();
        
        // Initialize with today's date
        selectedDateMillis = System.currentTimeMillis();
        updateSelectedDateDisplay();
        loadTasksForDate(selectedDateMillis);
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendar_view);
        recyclerTasks = view.findViewById(R.id.recycler_tasks);
        emptyState = view.findViewById(R.id.empty_state);
        textSelectedDate = view.findViewById(R.id.text_selected_date);
        fabAdd = view.findViewById(R.id.fab_add);
        
        // FAB click handler - creates task for selected date
        fabAdd.setOnClickListener(v -> showQuickAddForDate());
    }

    private void setupCalendar() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDateMillis = calendar.getTimeInMillis();
            
            updateSelectedDateDisplay();
            loadTasksForDate(selectedDateMillis);
        });
    }

    private void setupRecyclerView() {
        adapter = new CalendarTaskAdapter(this);
        recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerTasks.setAdapter(adapter);
        
        // Add spacing between items
        int spacing = getResources().getDimensionPixelSize(R.dimen.spacing_sm);
        recyclerTasks.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacing;
            }
        });
    }

    private void observeData() {
        // Observe all tasks to update calendar badges and filter for selected date
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                updateTasksForSelectedDate(tasks);
            }
        });
    }

    private void loadTasksForDate(long dateMillis) {
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                updateTasksForSelectedDate(tasks);
            }
        });
    }

    private void updateTasksForSelectedDate(List<Task> allTasks) {
        List<Task> tasksForDate = filterTasksForDate(allTasks, selectedDateMillis);
        adapter.submitList(tasksForDate);
        updateEmptyState(tasksForDate.isEmpty());
    }

    private List<Task> filterTasksForDate(List<Task> tasks, long dateMillis) {
        List<Task> result = new ArrayList<>();
        
        // Get start and end of the selected day
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();
        
        for (Task task : tasks) {
            if (task.getDueDate() != null) {
                long dueDate = task.getDueDate();
                if (dueDate >= startOfDay && dueDate < endOfDay) {
                    result.add(task);
                }
            }
        }
        
        // Sort by priority (highest first), then by creation time
        result.sort((t1, t2) -> {
            int priorityCompare = Integer.compare(
                    Task.priorityRank(t2.getPriority()),
                    Task.priorityRank(t1.getPriority()));
            if (priorityCompare != 0) return priorityCompare;
            return Long.compare(t1.getCreatedAt(), t2.getCreatedAt());
        });
        
        return result;
    }

    private void updateSelectedDateDisplay() {
        if (textSelectedDate != null) {
            String formattedDate = dateFormat.format(new Date(selectedDateMillis));
            textSelectedDate.setText(formattedDate);
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyState != null) {
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerTasks.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void showQuickAddForDate() {
        QuickAddBottomSheet bottomSheet = QuickAddBottomSheet.newInstance(selectedDateMillis);
        bottomSheet.show(getChildFragmentManager(), QuickAddBottomSheet.TAG);
    }

    // TaskClickListener implementation
    
    @Override
    public void onTaskClick(Task task) {
        TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(task.getId());
        bottomSheet.show(getChildFragmentManager(), TaskDetailBottomSheet.TAG);
    }

    @Override
    public void onTaskCheckChanged(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        viewModel.updateTask(task);
    }
}
