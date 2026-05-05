package com.taskflow.ui.board;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.taskflow.R;
import com.taskflow.databinding.ItemTaskCardBinding;
import com.taskflow.model.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * TaskCardAdapter - RecyclerView adapter for task cards in Kanban columns.
 * 
 * Displays task details including priority, tags, progress, due date, and assignee.
 * Supports long-press for drag initiation.
 */
public class TaskCardAdapter extends ListAdapter<Task, TaskCardAdapter.TaskViewHolder> {

    private final OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
        void onTaskMoved(Task task, long newColumnId, int newPosition);
    }

    public TaskCardAdapter(OnTaskActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.getStatus().equals(newItem.getStatus()) &&
                   oldItem.getPriority().equals(newItem.getPriority()) &&
                   oldItem.getCategory().equals(newItem.getCategory()) &&
                   oldItem.getProgress() == newItem.getProgress() &&
                   oldItem.getPosition() == newItem.getPosition() &&
                   (oldItem.getDueDate() == null ? newItem.getDueDate() == null :
                    oldItem.getDueDate().equals(newItem.getDueDate()));
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskCardBinding binding = ItemTaskCardBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemTaskCardBinding binding;

        TaskViewHolder(ItemTaskCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Task task) {
            android.content.Context context = binding.getRoot().getContext();
            
            // Title
            binding.textTitle.setText(task.getTitle());
            
            // Description
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                binding.textDescription.setText(task.getDescription());
                binding.textDescription.setVisibility(View.VISIBLE);
            } else {
                binding.textDescription.setVisibility(View.GONE);
            }
            
            // Priority chip
            String priority = task.getPriority();
            if (!Task.Priority.NONE.equals(priority)) {
                binding.chipPriority.setVisibility(View.VISIBLE);
                binding.chipPriority.setText(getPriorityText(context, priority));
                setPriorityChipStyle(priority);
            } else {
                binding.chipPriority.setVisibility(View.GONE);
            }

            binding.chipCategory.setText(Task.categoryLabel(context, task.getCategory()));
            binding.chipCategory.setTextColor(context.getColor(R.color.on_surface_variant));
            binding.chipCategory.setVisibility(View.VISIBLE);
            
            // Progress
            int progress = task.getProgress();
            if (progress > 0 && progress < 100) {
                binding.progressContainer.setVisibility(View.VISIBLE);
                binding.progressBar.setProgress(progress);
                binding.textProgress.setText(context.getString(R.string.task_progress_percent, progress));
                
                // Color progress bar based on completion
                if (progress >= 75) {
                    binding.progressBar.setIndicatorColor(context.getColor(R.color.success));
                } else {
                    binding.progressBar.setIndicatorColor(context.getColor(R.color.primary));
                }
            } else {
                binding.progressContainer.setVisibility(View.GONE);
            }
            
            // Due date
            if (task.getDueDate() != null) {
                binding.dueDateContainer.setVisibility(View.VISIBLE);
                binding.textDueDate.setText(formatDueDate(context, task.getDueDate()));
                
                // Color if overdue
                if (task.isOverdue()) {
                    binding.textDueDate.setTextColor(context.getColor(R.color.danger));
                } else if (task.isDueToday()) {
                    binding.textDueDate.setTextColor(context.getColor(R.color.warning));
                } else {
                    binding.textDueDate.setTextColor(context.getColor(R.color.text_tertiary));
                }
            } else {
                binding.dueDateContainer.setVisibility(View.GONE);
            }
            
            // Assignee
            if (task.getAssigneeId() != null) {
                binding.assigneeContainer.setVisibility(View.VISIBLE);
                // In a real app, we'd load the assignee details
                // For now, show placeholder initials
                binding.textAssigneeInitials.setText("?");
            } else {
                binding.assigneeContainer.setVisibility(View.GONE);
            }
            
            // Tags - would need to load from relationship
            // For demo, hide the tag group
            binding.chipGroupTags.setVisibility(View.GONE);
            
            // Click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
            
            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTaskLongClick(task);
                }
                return true;
            });
            
            // Accessibility
            StringBuilder contentDesc = new StringBuilder();
            contentDesc.append(context.getString(R.string.a11y_task_card, task.getTitle()));
            if (!Task.Priority.NONE.equals(priority)) {
                contentDesc.append(", ");
                contentDesc.append(context.getString(R.string.a11y_task_priority, getPriorityText(context, priority)));
            }
            if (progress > 0) {
                contentDesc.append(", ");
                contentDesc.append(context.getString(R.string.a11y_task_progress, progress));
            }
            binding.getRoot().setContentDescription(contentDesc.toString());
        }

        private String getPriorityText(android.content.Context context, String priority) {
            switch (priority) {
                case Task.Priority.URGENT: return context.getString(R.string.priority_urgent);
                case Task.Priority.HIGH: return context.getString(R.string.priority_high);
                case Task.Priority.MEDIUM: return context.getString(R.string.priority_medium);
                case Task.Priority.LOW: return context.getString(R.string.priority_low);
                default: return context.getString(R.string.priority_none);
            }
        }

        private void setPriorityChipStyle(String priority) {
            android.content.Context context = binding.getRoot().getContext();
            int bgColor, textColor;
            
            switch (priority) {
                case Task.Priority.URGENT:
                    bgColor = R.color.priority_urgent_bg;
                    textColor = R.color.priority_urgent;
                    break;
                case Task.Priority.HIGH:
                    bgColor = R.color.priority_high_bg;
                    textColor = R.color.priority_high;
                    break;
                case Task.Priority.MEDIUM:
                    bgColor = R.color.priority_medium_bg;
                    textColor = R.color.priority_medium;
                    break;
                case Task.Priority.LOW:
                    bgColor = R.color.priority_low_bg;
                    textColor = R.color.priority_low;
                    break;
                default:
                    bgColor = R.color.priority_none_bg;
                    textColor = R.color.priority_none;
            }
            
            binding.chipPriority.setChipBackgroundColorResource(bgColor);
            binding.chipPriority.setTextColor(context.getColor(textColor));
        }

        private String formatDueDate(android.content.Context context, long dueDate) {
            if (isToday(dueDate)) {
                return context.getString(R.string.date_today);
            }
            if (isTomorrow(dueDate)) {
                return context.getString(R.string.date_tomorrow);
            }
            if (isYesterday(dueDate)) {
                return context.getString(R.string.date_yesterday);
            }
            
            long now = System.currentTimeMillis();
            long diff = dueDate - now;
            long dayMs = 24 * 60 * 60 * 1000L;
            
            // Within a week
            if (diff > 0 && diff < 7 * dayMs) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                return sdf.format(new Date(dueDate));
            }
            
            // Overdue
            if (diff < 0) {
                long daysAgo = Math.abs(diff) / dayMs;
                if (daysAgo == 0) daysAgo = 1;
                return context.getString(R.string.date_days_ago, (int) daysAgo);
            }
            
            // Future
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d", Locale.getDefault());
            return sdf.format(new Date(dueDate));
        }

        private boolean isToday(long timestamp) {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal2.setTimeInMillis(timestamp);
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }

        private boolean isTomorrow(long timestamp) {
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.DAY_OF_YEAR, 1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTimeInMillis(timestamp);
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }

        private boolean isYesterday(long timestamp) {
            Calendar cal1 = Calendar.getInstance();
            cal1.add(Calendar.DAY_OF_YEAR, -1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTimeInMillis(timestamp);
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                   cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }
    }
}
