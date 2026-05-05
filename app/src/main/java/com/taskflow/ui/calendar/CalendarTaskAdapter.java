package com.taskflow.ui.calendar;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.taskflow.R;
import com.taskflow.model.Task;

/**
 * Calendar Task Adapter
 * 
 * Displays tasks for a selected date in the calendar view.
 * Uses compact card layout with priority indicators.
 */
public class CalendarTaskAdapter extends ListAdapter<Task, CalendarTaskAdapter.TaskViewHolder> {

    private final TaskClickListener listener;

    public interface TaskClickListener {
        void onTaskClick(Task task);
        void onTaskCheckChanged(Task task, boolean isChecked);
    }

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.equals(newItem);
        }
    };

    public CalendarTaskAdapter(TaskClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_compact, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final View priorityIndicator;
        private final CheckBox checkbox;
        private final TextView textTitle;
        private final Chip chipStatus;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            priorityIndicator = itemView.findViewById(R.id.view_priority);
            checkbox = itemView.findViewById(R.id.checkbox_complete);
            textTitle = itemView.findViewById(R.id.text_title);
            chipStatus = itemView.findViewById(R.id.chip_status);
        }

        void bind(Task task) {
            // Title with strikethrough if completed
            textTitle.setText(task.getTitle());
            if (task.isCompleted()) {
                textTitle.setPaintFlags(textTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textTitle.setAlpha(0.6f);
            } else {
                textTitle.setPaintFlags(textTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                textTitle.setAlpha(1.0f);
            }

            // Checkbox
            checkbox.setOnCheckedChangeListener(null);
            checkbox.setChecked(task.isCompleted());
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskCheckChanged(task, isChecked);
                }
            });

            // Priority indicator color
            if (priorityIndicator != null) {
                int colorRes = getPriorityColor(task.getPriority());
                priorityIndicator.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), colorRes));
            }

            // Status chip
            bindStatusChip(task.getStatus());

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
        }

        private int getPriorityColor(String priority) {
            if (priority == null) return R.color.priority_none;
            switch (priority) {
                case Task.Priority.URGENT:
                    return R.color.priority_urgent;
                case Task.Priority.HIGH:
                    return R.color.priority_high;
                case Task.Priority.MEDIUM:
                    return R.color.priority_medium;
                case Task.Priority.LOW:
                    return R.color.priority_low;
                case Task.Priority.NONE:
                default:
                    return R.color.priority_none;
            }
        }

        private void bindStatusChip(String status) {
            if (chipStatus == null || status == null) return;

            int colorRes;
            int textRes;

            switch (status) {
                case Task.Status.IN_PROGRESS:
                    colorRes = R.color.status_in_progress;
                    textRes = R.string.status_in_progress;
                    break;
                case Task.Status.REVIEW:
                    colorRes = R.color.status_review;
                    textRes = R.string.status_review;
                    break;
                case Task.Status.DONE:
                    colorRes = R.color.status_done;
                    textRes = R.string.status_done;
                    break;
                case Task.Status.BLOCKED:
                    colorRes = R.color.status_blocked;
                    textRes = R.string.status_blocked;
                    break;
                default:
                    colorRes = R.color.status_todo;
                    textRes = R.string.status_todo;
                    break;
            }

            chipStatus.setText(textRes);
            chipStatus.setChipBackgroundColorResource(colorRes);
        }
    }
}
