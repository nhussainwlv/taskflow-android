/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.ui.board;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.taskflow.R;
import com.taskflow.databinding.ItemBoardColumnBinding;
import com.taskflow.model.Column;
import com.taskflow.model.ColumnWithTasks;
import com.taskflow.model.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ColumnAdapter - RecyclerView adapter for Kanban columns.
 * 
 * Each column contains a nested RecyclerView for task cards.
 * Supports drag & drop reordering of tasks via ItemTouchHelper.
 */
public class ColumnAdapter extends ListAdapter<ColumnWithTasks, ColumnAdapter.ColumnViewHolder> {

    private final OnColumnActionListener columnListener;
    private final TaskCardAdapter.OnTaskActionListener taskListener;
    private final RecyclerView.RecycledViewPool sharedPool;

    public interface OnColumnActionListener {
        void onColumnMenuClick(Column column, View anchor);
        void onAddTaskClick(Column column);
        void onColumnCollapse(Column column, boolean collapsed);
    }

    public ColumnAdapter(
            OnColumnActionListener columnListener,
            TaskCardAdapter.OnTaskActionListener taskListener,
            RecyclerView.RecycledViewPool sharedPool) {
        super(DIFF_CALLBACK);
        this.columnListener = columnListener;
        this.taskListener = taskListener;
        this.sharedPool = sharedPool;
    }

    private static final DiffUtil.ItemCallback<ColumnWithTasks> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<ColumnWithTasks>() {
        @Override
        public boolean areItemsTheSame(@NonNull ColumnWithTasks oldItem, @NonNull ColumnWithTasks newItem) {
            return oldItem.getColumn().getId() == newItem.getColumn().getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ColumnWithTasks oldItem, @NonNull ColumnWithTasks newItem) {
            Column oldCol = oldItem.getColumn();
            Column newCol = newItem.getColumn();
            
            boolean columnSame = oldCol.getName().equals(newCol.getName()) &&
                    oldCol.isCollapsed() == newCol.isCollapsed() &&
                    oldItem.getTaskCount() == newItem.getTaskCount();
            
            return columnSame;
        }
    };

    @NonNull
    @Override
    public ColumnViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBoardColumnBinding binding = ItemBoardColumnBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ColumnViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ColumnViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ColumnViewHolder extends RecyclerView.ViewHolder {
        private final ItemBoardColumnBinding binding;
        private TaskCardAdapter taskAdapter;

        ColumnViewHolder(ItemBoardColumnBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            
            setupTasksRecyclerView();
        }

        private void setupTasksRecyclerView() {
            taskAdapter = new TaskCardAdapter(taskListener);
            
            binding.recyclerTasks.setAdapter(taskAdapter);
            binding.recyclerTasks.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));
            binding.recyclerTasks.setRecycledViewPool(sharedPool);
            binding.recyclerTasks.setHasFixedSize(false);
            binding.recyclerTasks.setNestedScrollingEnabled(true);
            
            // Setup drag & drop
            ItemTouchHelper touchHelper = new ItemTouchHelper(new TaskTouchCallback());
            touchHelper.attachToRecyclerView(binding.recyclerTasks);
        }

        void bind(ColumnWithTasks columnWithTasks) {
            Column column = columnWithTasks.getColumn();
            List<Task> tasks = columnWithTasks.getTasks();
            
            // Column name
            binding.textColumnName.setText(column.getName());
            
            // Task count
            int taskCount = tasks != null ? tasks.size() : 0;
            binding.textTaskCount.setText(String.valueOf(taskCount));
            
            // Column color
            if (column.getColor() != null) {
                try {
                    binding.viewColor.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor(column.getColor()))
                    );
                } catch (Exception e) {
                    binding.viewColor.setBackgroundTintList(
                        binding.getRoot().getContext().getColorStateList(R.color.status_todo)
                    );
                }
            }
            
            // WIP limit warning
            int wipLimit = column.getWipLimit();
            if (wipLimit > 0 && taskCount > wipLimit) {
                binding.wipWarning.setVisibility(View.VISIBLE);
                binding.textWipWarning.setText(
                    binding.getRoot().getContext().getString(R.string.board_task_count, wipLimit) + " limit exceeded"
                );
            } else {
                binding.wipWarning.setVisibility(View.GONE);
            }
            
            // Collapsed state
            boolean collapsed = column.isCollapsed();
            binding.recyclerTasks.setVisibility(collapsed ? View.GONE : View.VISIBLE);
            binding.btnAddTask.setVisibility(collapsed ? View.GONE : View.VISIBLE);
            binding.btnCollapse.setRotation(collapsed ? -90 : 0);
            
            // Submit tasks to nested adapter
            // Sort by position
            if (tasks != null) {
                List<Task> sortedTasks = new ArrayList<>(tasks);
                Collections.sort(sortedTasks, (a, b) -> Integer.compare(a.getPosition(), b.getPosition()));
                taskAdapter.submitList(sortedTasks);
            } else {
                taskAdapter.submitList(new ArrayList<>());
            }
            
            // Click listeners
            binding.btnColumnMenu.setOnClickListener(v -> {
                if (columnListener != null) {
                    columnListener.onColumnMenuClick(column, v);
                }
            });
            
            binding.btnAddTask.setOnClickListener(v -> {
                if (columnListener != null) {
                    columnListener.onAddTaskClick(column);
                }
            });
            
            binding.btnCollapse.setOnClickListener(v -> {
                if (columnListener != null) {
                    columnListener.onColumnCollapse(column, !collapsed);
                }
            });
            
            // Accessibility
            binding.getRoot().setContentDescription(
                binding.getRoot().getContext().getString(R.string.a11y_column, column.getName(), taskCount)
            );
        }

        /**
         * ItemTouchHelper callback for drag & drop reordering.
         */
        private class TaskTouchCallback extends ItemTouchHelper.SimpleCallback {
            
            TaskTouchCallback() {
                super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, 
                                  @NonNull RecyclerView.ViewHolder viewHolder, 
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getBindingAdapterPosition();
                int toPos = target.getBindingAdapterPosition();
                
                if (fromPos != RecyclerView.NO_POSITION && toPos != RecyclerView.NO_POSITION) {
                    // Swap items in the adapter
                    List<Task> currentList = new ArrayList<>(taskAdapter.getCurrentList());
                    Collections.swap(currentList, fromPos, toPos);
                    
                    // Update positions
                    for (int i = 0; i < currentList.size(); i++) {
                        currentList.get(i).setPosition(i);
                    }
                    
                    taskAdapter.submitList(currentList);
                    return true;
                }
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used - swipe disabled
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                    // Elevate card during drag
                    viewHolder.itemView.setAlpha(0.9f);
                    viewHolder.itemView.setScaleX(1.02f);
                    viewHolder.itemView.setScaleY(1.02f);
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, 
                                  @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                
                // Reset appearance
                viewHolder.itemView.setAlpha(1f);
                viewHolder.itemView.setScaleX(1f);
                viewHolder.itemView.setScaleY(1f);
                
                // Notify listener of new order
                // This would trigger a save to database
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        }
    }
}
