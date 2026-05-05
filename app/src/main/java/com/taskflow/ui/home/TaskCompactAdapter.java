package com.taskflow.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.taskflow.R;
import com.taskflow.databinding.ItemTaskCompactBinding;
import com.taskflow.model.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * TaskCompactAdapter - RecyclerView adapter for compact task items.
 * 
 * Displays task summary with due date, status chip, and completion checkbox.
 * Used on the home screen for upcoming deadlines.
 */
public class TaskCompactAdapter extends ListAdapter<Task, TaskCompactAdapter.TaskViewHolder> {

    private final OnTaskClickListener clickListener;
    private final OnTaskCompleteListener completeListener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskCompleteListener {
        void onTaskComplete(Task task);
    }

    public TaskCompactAdapter(OnTaskClickListener clickListener, OnTaskCompleteListener completeListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
        this.completeListener = completeListener;
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
                   (oldItem.getDueDate() == null ? newItem.getDueDate() == null :
                    oldItem.getDueDate().equals(newItem.getDueDate()));
        }
    };

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTaskCompactBinding binding = ItemTaskCompactBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new TaskViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final ItemTaskCompactBinding binding;

        TaskViewHolder(ItemTaskCompactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Task task) {
            android.content.Context context = binding.getRoot().getContext();
            
            // Set title
            binding.textTitle.setText(task.getTitle());
            
            // Set priority indicator color
            int priorityColor = getPriorityColor(task.getPriority());
            binding.viewPriority.setBackgroundTintList(
                context.getColorStateList(priorityColor)
            );
            
            // Set due date
            if (task.getDueDate() != null) {
                binding.textDueDate.setText(formatDueDate(context, task.getDueDate()));
                binding.textDueDate.setVisibility(android.view.View.VISIBLE);
                
                // Color if overdue
                if (task.isOverdue()) {
                    binding.textDueDate.setTextColor(context.getColor(R.color.danger));
                } else {
                    binding.textDueDate.setTextColor(context.getColor(R.color.text_tertiary));
                }
            } else {
                binding.textDueDate.setVisibility(android.view.View.GONE);
            }
            
            // Set status chip
            binding.chipStatus.setText(getStatusText(context, task.getStatus()));
            setStatusChipStyle(task.getStatus());
            
            // Set checkbox state
            binding.checkboxComplete.setChecked(Task.Status.DONE.equals(task.getStatus()));
            binding.checkboxComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && completeListener != null) {
                    completeListener.onTaskComplete(task);
                }
            });
            
            // Click listener
            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onTaskClick(task);
                }
            });
            
            // Accessibility
            binding.getRoot().setContentDescription(
                context.getString(R.string.a11y_task_card, task.getTitle()) + ", " +
                context.getString(R.string.a11y_task_status, getStatusText(context, task.getStatus()))
            );
        }

        private int getPriorityColor(String priority) {
            switch (priority) {
                case Task.Priority.URGENT: return R.color.priority_urgent;
                case Task.Priority.HIGH: return R.color.priority_high;
                case Task.Priority.MEDIUM: return R.color.priority_medium;
                case Task.Priority.LOW: return R.color.priority_low;
                default: return R.color.priority_none;
            }
        }

        private String formatDueDate(android.content.Context context, long dueDate) {
            long now = System.currentTimeMillis();
            long diff = dueDate - now;
            long dayMs = 24 * 60 * 60 * 1000L;
            
            // Today
            if (isToday(dueDate)) {
                return context.getString(R.string.date_today);
            }
            
            // Tomorrow
            if (isTomorrow(dueDate)) {
                return context.getString(R.string.date_tomorrow);
            }
            
            // Yesterday
            if (isYesterday(dueDate)) {
                return context.getString(R.string.date_yesterday);
            }
            
            // Within a week
            if (diff > 0 && diff < 7 * dayMs) {
                SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                return sdf.format(new Date(dueDate));
            }
            
            // Overdue
            if (diff < 0) {
                long daysAgo = Math.abs(diff) / dayMs;
                return context.getString(R.string.date_days_ago, (int) daysAgo);
            }
            
            // Future date
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

        private String getStatusText(android.content.Context context, String status) {
            switch (status) {
                case Task.Status.TODO: return context.getString(R.string.status_todo);
                case Task.Status.IN_PROGRESS: return context.getString(R.string.status_in_progress);
                case Task.Status.REVIEW: return context.getString(R.string.status_review);
                case Task.Status.DONE: return context.getString(R.string.status_done);
                case Task.Status.BLOCKED: return context.getString(R.string.status_blocked);
                default: return status;
            }
        }

        private void setStatusChipStyle(String status) {
            android.content.Context context = binding.getRoot().getContext();
            int bgColor, textColor;
            
            switch (status) {
                case Task.Status.IN_PROGRESS:
                    bgColor = R.color.status_in_progress_bg;
                    textColor = R.color.status_in_progress;
                    break;
                case Task.Status.REVIEW:
                    bgColor = R.color.status_review_bg;
                    textColor = R.color.status_review;
                    break;
                case Task.Status.DONE:
                    bgColor = R.color.status_done_bg;
                    textColor = R.color.status_done;
                    break;
                case Task.Status.BLOCKED:
                    bgColor = R.color.status_blocked_bg;
                    textColor = R.color.status_blocked;
                    break;
                default: // TODO
                    bgColor = R.color.status_todo_bg;
                    textColor = R.color.status_todo;
            }
            
            binding.chipStatus.setChipBackgroundColorResource(bgColor);
            binding.chipStatus.setTextColor(context.getColor(textColor));
        }
    }
}
