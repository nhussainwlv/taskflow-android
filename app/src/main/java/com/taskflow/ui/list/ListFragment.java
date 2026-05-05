package com.taskflow.ui.list;

import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.taskflow.R;
import com.taskflow.model.Task;
import com.taskflow.ui.detail.QuickAddBottomSheet;
import com.taskflow.ui.detail.TaskDetailBottomSheet;
import com.taskflow.viewmodel.TaskViewModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * List Fragment
 * 
 * Displays tasks in a vertical list view with multi-select capability,
 * swipe actions for quick task management, and sortable/filterable headers.
 */
public class ListFragment extends Fragment implements TaskListAdapter.TaskInteractionListener {

    private TaskViewModel viewModel;
    private RecyclerView recyclerView;
    private TaskListAdapter adapter;
    private View emptyState;
    private FloatingActionButton fabAdd;
    private ChipGroup chipGroupFilter;
    private ChipGroup chipGroupSort;
    
    // Multi-select support
    private ActionMode actionMode;
    private Set<Long> selectedTaskIds = new HashSet<>();
    
    // Current filter and sort state
    private String currentFilter = "all";
    private String currentSort = "date";

    private List<Task> lastTasks = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        
        initViews(view);
        setupRecyclerView();
        setupFilterChips();
        setupSortChips();
        setupSwipeActions();
        observeData();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_tasks);
        emptyState = view.findViewById(R.id.empty_state);
        fabAdd = view.findViewById(R.id.fab_add);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);
        chipGroupSort = view.findViewById(R.id.chip_group_sort);
        
        // FAB click handler
        fabAdd.setOnClickListener(v -> showQuickAdd());
        
        // Toolbar search
        view.findViewById(R.id.btn_search).setOnClickListener(v -> showSearch());
    }

    private void setupRecyclerView() {
        adapter = new TaskListAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        
        // Add item decoration for spacing
        int spacing = getResources().getDimensionPixelSize(R.dimen.spacing_sm);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull android.graphics.Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = spacing;
            }
        });
    }

    private void setupFilterChips() {
        if (chipGroupFilter != null) {
            chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    Chip chip = group.findViewById(checkedIds.get(0));
                    if (chip != null) {
                        currentFilter = (String) chip.getTag();
                        applyFilters();
                    }
                }
            });
        }
    }

    private void setupSortChips() {
        if (chipGroupSort != null) {
            chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    Chip chip = group.findViewById(checkedIds.get(0));
                    if (chip != null) {
                        currentSort = (String) chip.getTag();
                        applyFilters();
                    }
                }
            });
        }
    }

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            
            private final Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
            private final Drawable checkIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check);
            private final ColorDrawable deleteBackground = new ColorDrawable(
                    ContextCompat.getColor(requireContext(), R.color.danger));
            private final ColorDrawable completeBackground = new ColorDrawable(
                    ContextCompat.getColor(requireContext(), R.color.success));
            
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, 
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Task task = adapter.getTaskAt(position);
                
                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe left to delete
                    deleteTask(task, position);
                } else {
                    // Swipe right to toggle completion
                    toggleTaskCompletion(task, position);
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - (deleteIcon != null ? deleteIcon.getIntrinsicHeight() : 0)) / 2;
                int iconTop = itemView.getTop() + iconMargin;
                int iconBottom = iconTop + (deleteIcon != null ? deleteIcon.getIntrinsicHeight() : 0);

                if (dX < 0) { // Swiping left - delete
                    int iconLeft = itemView.getRight() - iconMargin - (deleteIcon != null ? deleteIcon.getIntrinsicWidth() : 0);
                    int iconRight = itemView.getRight() - iconMargin;
                    
                    if (deleteIcon != null) {
                        deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    }
                    deleteBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom());
                    deleteBackground.draw(c);
                    if (deleteIcon != null) {
                        deleteIcon.draw(c);
                    }
                } else if (dX > 0) { // Swiping right - complete
                    int iconLeft = itemView.getLeft() + iconMargin;
                    int iconRight = iconLeft + (checkIcon != null ? checkIcon.getIntrinsicWidth() : 0);
                    
                    if (checkIcon != null) {
                        checkIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                    }
                    completeBackground.setBounds(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + (int) dX, itemView.getBottom());
                    completeBackground.draw(c);
                    if (checkIcon != null) {
                        checkIcon.draw(c);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
    }

    private void observeData() {
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                lastTasks = tasks;
                applyFilterSortToList();
            }
        });
    }

    private List<Task> filterAndSortTasks(List<Task> tasks) {
        List<Task> filtered = new ArrayList<>();
        
        // Apply filter
        for (Task task : tasks) {
            boolean include = false;
            switch (currentFilter) {
                case "all":
                    include = true;
                    break;
                case "active":
                    include = !task.isCompleted();
                    break;
                case "completed":
                    include = task.isCompleted();
                    break;
                case "today":
                    include = isTaskDueToday(task);
                    break;
                case "overdue":
                    include = isTaskOverdue(task);
                    break;
            }
            if (include) {
                filtered.add(task);
            }
        }
        
        // Apply sort
        switch (currentSort) {
            case "date":
                filtered.sort((t1, t2) -> {
                    if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                    if (t1.getDueDate() == null) return 1;
                    if (t2.getDueDate() == null) return -1;
                    return t1.getDueDate().compareTo(t2.getDueDate());
                });
                break;
            case "priority":
                filtered.sort((t1, t2) -> Integer.compare(
                        Task.priorityRank(t2.getPriority()),
                        Task.priorityRank(t1.getPriority())));
                break;
            case "name":
                filtered.sort((t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
                break;
            case "created":
                filtered.sort((t1, t2) -> Long.compare(t2.getCreatedAt(), t1.getCreatedAt()));
                break;
        }
        
        return filtered;
    }

    private void applyFilters() {
        applyFilterSortToList();
    }

    private void applyFilterSortToList() {
        if (lastTasks == null) {
            lastTasks = new ArrayList<>();
        }
        List<Task> filteredTasks = filterAndSortTasks(lastTasks);
        adapter.submitList(filteredTasks);
        updateEmptyState(filteredTasks.isEmpty());
    }

    private boolean isTaskDueToday(Task task) {
        if (task.getDueDate() == null) return false;
        long now = System.currentTimeMillis();
        long startOfDay = now - (now % (24 * 60 * 60 * 1000));
        long endOfDay = startOfDay + (24 * 60 * 60 * 1000);
        return task.getDueDate() >= startOfDay && task.getDueDate() < endOfDay;
    }

    private boolean isTaskOverdue(Task task) {
        if (task.getDueDate() == null || task.isCompleted()) return false;
        return task.getDueDate() < System.currentTimeMillis();
    }

    private void updateEmptyState(boolean isEmpty) {
        if (emptyState != null) {
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private void deleteTask(Task task, int position) {
        // Store for undo
        Task deletedTask = task;
        
        viewModel.deleteTask(task);
        
        Snackbar.make(recyclerView, R.string.task_deleted, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_undo, v -> {
                    viewModel.insertTask(deletedTask);
                })
                .setAnchorView(fabAdd)
                .show();
    }

    private void toggleTaskCompletion(Task task, int position) {
        task.setCompleted(!task.isCompleted());
        viewModel.updateTask(task);
        adapter.notifyItemChanged(position);
        
        int messageRes = task.isCompleted() ? R.string.task_completed : R.string.task_reopened;
        Snackbar.make(recyclerView, messageRes, Snackbar.LENGTH_SHORT)
                .setAnchorView(fabAdd)
                .show();
    }

    private void showQuickAdd() {
        QuickAddBottomSheet bottomSheet = new QuickAddBottomSheet();
        bottomSheet.show(getChildFragmentManager(), QuickAddBottomSheet.TAG);
    }

    private void showSearch() {
        NavHostFragment.findNavController(this).navigate(R.id.searchFragment);
    }

    // TaskInteractionListener implementation
    
    @Override
    public void onTaskClick(Task task) {
        if (actionMode != null) {
            // In multi-select mode, toggle selection
            toggleSelection(task.getId());
        } else {
            // Open task detail
            TaskDetailBottomSheet bottomSheet = TaskDetailBottomSheet.newInstance(task.getId());
            bottomSheet.show(getChildFragmentManager(), TaskDetailBottomSheet.TAG);
        }
    }

    @Override
    public void onTaskLongClick(Task task) {
        if (actionMode == null) {
            startActionMode();
        }
        toggleSelection(task.getId());
    }

    @Override
    public void onTaskCheckChanged(Task task, boolean isChecked) {
        task.setCompleted(isChecked);
        viewModel.updateTask(task);
    }

    private void startActionMode() {
        actionMode = requireActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.menu_multi_select, menu);
                fabAdd.hide();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.action_delete) {
                    deleteSelectedTasks();
                    mode.finish();
                    return true;
                } else if (itemId == R.id.action_complete) {
                    completeSelectedTasks();
                    mode.finish();
                    return true;
                } else if (itemId == R.id.action_select_all) {
                    selectAllTasks();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                actionMode = null;
                selectedTaskIds.clear();
                adapter.setSelectedIds(selectedTaskIds);
                fabAdd.show();
            }
        });
    }

    private void toggleSelection(long taskId) {
        if (selectedTaskIds.contains(taskId)) {
            selectedTaskIds.remove(taskId);
        } else {
            selectedTaskIds.add(taskId);
        }
        
        adapter.setSelectedIds(selectedTaskIds);
        
        if (actionMode != null) {
            if (selectedTaskIds.isEmpty()) {
                actionMode.finish();
            } else {
                actionMode.setTitle(String.valueOf(selectedTaskIds.size()));
            }
        }
    }

    private void selectAllTasks() {
        List<Task> tasks = adapter.getCurrentList();
        selectedTaskIds.clear();
        for (Task task : tasks) {
            selectedTaskIds.add(task.getId());
        }
        adapter.setSelectedIds(selectedTaskIds);
        if (actionMode != null) {
            actionMode.setTitle(String.valueOf(selectedTaskIds.size()));
        }
    }

    private void deleteSelectedTasks() {
        int count = selectedTaskIds.size();
        for (Long taskId : selectedTaskIds) {
            viewModel.deleteTaskById(taskId);
        }
        
        Snackbar.make(recyclerView, 
                getResources().getQuantityString(R.plurals.tasks_deleted, count, count),
                Snackbar.LENGTH_SHORT)
                .setAnchorView(fabAdd)
                .show();
    }

    private void completeSelectedTasks() {
        int count = selectedTaskIds.size();
        for (Long taskId : selectedTaskIds) {
            viewModel.completeTaskById(taskId);
        }
        
        Snackbar.make(recyclerView,
                getResources().getQuantityString(R.plurals.tasks_completed, count, count),
                Snackbar.LENGTH_SHORT)
                .setAnchorView(fabAdd)
                .show();
    }
}
