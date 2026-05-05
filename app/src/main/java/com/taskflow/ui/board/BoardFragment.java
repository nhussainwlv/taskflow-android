package com.taskflow.ui.board;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.taskflow.R;
import com.taskflow.databinding.FragmentBoardBinding;
import com.taskflow.model.Board;
import com.taskflow.model.Column;
import com.taskflow.model.ColumnWithTasks;
import com.taskflow.model.Task;
import com.taskflow.viewmodel.BoardViewModel;

import java.util.List;

/**
 * BoardFragment - Kanban board view with draggable columns and cards.
 * 
 * Features:
 * - Horizontal scrolling columns
 * - Drag & drop tasks between columns
 * - Column management (add, rename, delete, reorder)
 * - Quick add tasks
 */
public class BoardFragment extends Fragment implements 
        ColumnAdapter.OnColumnActionListener,
        TaskCardAdapter.OnTaskActionListener {

    private FragmentBoardBinding binding;
    private BoardViewModel viewModel;
    private NavController navController;
    
    private ColumnAdapter columnAdapter;
    private RecyclerView.RecycledViewPool sharedPool;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBoardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        navController = NavHostFragment.findNavController(this);
        viewModel = new ViewModelProvider(this).get(BoardViewModel.class);
        
        // Get board ID from arguments
        long boardId = 1L; // Default
        if (getArguments() != null) {
            boardId = getArguments().getLong("boardId", 1L);
        }
        viewModel.setCurrentBoardId(boardId);
        
        setupToolbar();
        setupRecyclerView();
        observeViewModel();
    }

    /**
     * Setup toolbar with navigation and actions.
     */
    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> 
            navController.navigateUp()
        );
        
        // View mode toggle
        binding.toggleViewMode.check(R.id.btn_board_view);
        binding.toggleViewMode.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked && checkedId == R.id.btn_list_view) {
                // Navigate to list view
                Bundle args = new Bundle();
                args.putLong("boardId", viewModel.getCurrentBoardId().getValue());
                navController.navigate(R.id.listFragment, args);
            }
        });
        
        // More options menu
        binding.btnMore.setOnClickListener(v -> showBoardMenu(v));
    }

    /**
     * Setup horizontal columns RecyclerView with shared pool.
     */
    private void setupRecyclerView() {
        // Create shared pool for task cards across columns
        sharedPool = new RecyclerView.RecycledViewPool();
        sharedPool.setMaxRecycledViews(0, 20); // Task cards
        
        columnAdapter = new ColumnAdapter(this, this, sharedPool);
        
        binding.recyclerColumns.setAdapter(columnAdapter);
        binding.recyclerColumns.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        
        // Optimize for fixed size
        binding.recyclerColumns.setHasFixedSize(false);
        binding.recyclerColumns.setItemViewCacheSize(5);
    }

    /**
     * Observe ViewModel data.
     */
    private void observeViewModel() {
        // Board info
        viewModel.getCurrentBoard().observe(getViewLifecycleOwner(), board -> {
            if (board != null) {
                updateBoardHeader(board);
            }
        });
        
        // Columns with tasks
        viewModel.getColumnsWithTasks().observe(getViewLifecycleOwner(), columns -> {
            if (columns != null && !columns.isEmpty()) {
                columnAdapter.submitList(columns);
                binding.recyclerColumns.setVisibility(View.VISIBLE);
                binding.emptyState.getRoot().setVisibility(View.GONE);
            } else {
                binding.recyclerColumns.setVisibility(View.GONE);
                showEmptyState();
            }
        });
        
        // Loading state
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
    }

    /**
     * Update board header with board info.
     */
    private void updateBoardHeader(Board board) {
        binding.textBoardName.setText(board.getName());
        
        if (board.getDescription() != null && !board.getDescription().isEmpty()) {
            binding.textBoardDescription.setText(board.getDescription());
            binding.textBoardDescription.setVisibility(View.VISIBLE);
        } else {
            binding.textBoardDescription.setVisibility(View.GONE);
        }
        
        if (board.getIcon() != null && !board.getIcon().isEmpty()) {
            binding.textBoardIcon.setText(board.getIcon());
            binding.textBoardIcon.setVisibility(View.VISIBLE);
        } else {
            binding.textBoardIcon.setVisibility(View.GONE);
        }
    }

    /**
     * Show empty state when no columns.
     */
    private void showEmptyState() {
        binding.emptyState.getRoot().setVisibility(View.VISIBLE);
        binding.emptyState.imageEmpty.setImageResource(R.drawable.ic_empty_tasks);
        binding.emptyState.textEmptyTitle.setText(R.string.empty_board_title);
        binding.emptyState.textEmptyDescription.setText(R.string.empty_board_desc);
        binding.emptyState.btnEmptyAction.setVisibility(View.VISIBLE);
        binding.emptyState.btnEmptyAction.setText(R.string.board_add_column);
        binding.emptyState.btnEmptyAction.setOnClickListener(v -> showAddColumnDialog());
    }

    /**
     * Show board options menu.
     */
    private void showBoardMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_board, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_add_column) {
                showAddColumnDialog();
                return true;
            } else if (id == R.id.action_filter) {
                // Show filter bottom sheet
                return true;
            } else if (id == R.id.action_sort) {
                // Show sort options
                return true;
            }
            return false;
        });
        popup.show();
    }

    /**
     * Show dialog to add a new column.
     */
    private void showAddColumnDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_text_input, null);
        TextInputEditText input = dialogView.findViewById(R.id.text_input);
        input.setHint(R.string.board_column_name);
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.board_add_column)
            .setView(dialogView)
            .setPositiveButton(R.string.action_add, (dialog, which) -> {
                String name = input.getText() != null ? input.getText().toString().trim() : "";
                if (!name.isEmpty()) {
                    viewModel.addColumn(name);
                    Snackbar.make(binding.getRoot(), R.string.snackbar_column_created, Snackbar.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.action_cancel, null)
            .show();
    }

    // ========================================
    // COLUMN ACTION CALLBACKS
    // ========================================
    
    @Override
    public void onColumnMenuClick(Column column, View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_column, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_rename) {
                showRenameColumnDialog(column);
                return true;
            } else if (id == R.id.action_delete) {
                showDeleteColumnConfirmation(column);
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public void onAddTaskClick(Column column) {
        showQuickAddTaskDialog(column);
    }

    @Override
    public void onColumnCollapse(Column column, boolean collapsed) {
        viewModel.toggleColumnCollapsed(column.getId(), collapsed);
    }

    private void showRenameColumnDialog(Column column) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_text_input, null);
        TextInputEditText input = dialogView.findViewById(R.id.text_input);
        input.setText(column.getName());
        input.setHint(R.string.board_column_name);
        input.selectAll();
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.board_rename_column)
            .setView(dialogView)
            .setPositiveButton(R.string.action_save, (dialog, which) -> {
                String name = input.getText() != null ? input.getText().toString().trim() : "";
                if (!name.isEmpty()) {
                    viewModel.renameColumn(column, name);
                }
            })
            .setNegativeButton(R.string.action_cancel, null)
            .show();
    }

    private void showDeleteColumnConfirmation(Column column) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.board_delete_column)
            .setMessage(getString(R.string.board_delete_column_confirm, column.getName()))
            .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                viewModel.deleteColumn(column);
                Snackbar.make(binding.getRoot(), R.string.snackbar_column_deleted, Snackbar.LENGTH_SHORT).show();
            })
            .setNegativeButton(R.string.action_cancel, null)
            .show();
    }

    private void showQuickAddTaskDialog(Column column) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_text_input, null);
        TextInputEditText input = dialogView.findViewById(R.id.text_input);
        input.setHint(R.string.task_title_hint);
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.board_add_task)
            .setView(dialogView)
            .setPositiveButton(R.string.action_add, (dialog, which) -> {
                String title = input.getText() != null ? input.getText().toString().trim() : "";
                if (!title.isEmpty()) {
                    viewModel.quickAddTask(title, column.getId());
                    Snackbar.make(binding.getRoot(), R.string.snackbar_task_created, Snackbar.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.action_cancel, null)
            .show();
    }

    // ========================================
    // TASK ACTION CALLBACKS
    // ========================================
    
    @Override
    public void onTaskClick(Task task) {
        Bundle args = new Bundle();
        args.putLong("taskId", task.getId());
        navController.navigate(R.id.taskDetailBottomSheet, args);
    }

    @Override
    public void onTaskLongClick(Task task) {
        // Start drag operation - handled by ItemTouchHelper in adapter
    }

    @Override
    public void onTaskMoved(Task task, long newColumnId, int newPosition) {
        viewModel.moveTask(task.getId(), newColumnId, newPosition);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
