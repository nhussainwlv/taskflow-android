package com.taskflow.ui.list;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.taskflow.R;
import com.taskflow.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Task List Adapter
 * 
 * RecyclerView adapter for displaying tasks in list view with multi-select support,
 * using DiffUtil for efficient updates.
 */
public class TaskListAdapter extends ListAdapter<Task, TaskListAdapter.TaskViewHolder> {

    private final TaskInteractionListener listener;
    private Set<Long> selectedIds = new HashSet<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

    /**
     * Interface for task interaction callbacks
     */
    public interface TaskInteractionListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
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

    public TaskListAdapter(TaskInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_list, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task, selectedIds.contains(task.getId()));
    }

    public Task getTaskAt(int position) {
        return getItem(position);
    }

    public void setSelectedIds(Set<Long> ids) {
        Set<Long> oldSelected = new HashSet<>(selectedIds);
        selectedIds = new HashSet<>(ids);
        
        // Notify only changed items for performance
        for (int i = 0; i < getItemCount(); i++) {
            long taskId = getItem(i).getId();
            boolean wasSelected = oldSelected.contains(taskId);
            boolean isSelected = selectedIds.contains(taskId);
            if (wasSelected != isSelected) {
                notifyItemChanged(i);
            }
        }
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final CheckBox checkbox;
        private final TextView textTitle;
        private final TextView textDueDate;
        private final Chip chipPriority;
        private final Chip chipStatus;
        private final ImageView iconFlag;
        private final ProgressBar progressBar;
        private final TextView textProgress;
        private final View selectedOverlay;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_task);
            checkbox = itemView.findViewById(R.id.checkbox_task);
            textTitle = itemView.findViewById(R.id.text_title);
            textDueDate = itemView.findViewById(R.id.text_due_date);
            chipPriority = itemView.findViewById(R.id.chip_priority);
            chipStatus = itemView.findViewById(R.id.chip_status);
            iconFlag = itemView.findViewById(R.id.icon_flag);
            progressBar = itemView.findViewById(R.id.progress_bar);
            textProgress = itemView.findViewById(R.id.text_progress);
            selectedOverlay = itemView.findViewById(R.id.selected_overlay);
        }

        void bind(Task task, boolean isSelected) {
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
            checkbox.setOnCheckedChangeListener(null); // Clear listener before setting
            checkbox.setChecked(task.isCompleted());
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTaskCheckChanged(task, isChecked);
                }
            });

            // Due date
            if (task.getDueDate() != null) {
                textDueDate.setVisibility(View.VISIBLE);
                textDueDate.setText(dateFormat.format(new Date(task.getDueDate())));
                
                // Color based on overdue status
                boolean isOverdue = !task.isCompleted() && task.getDueDate() < System.currentTimeMillis();
                int colorRes = isOverdue ? R.color.danger : R.color.text_secondary;
                textDueDate.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
            } else {
                textDueDate.setVisibility(View.GONE);
            }

            // Priority chip
            bindPriorityChip(task.getPriority());

            // Status chip
            bindStatusChip(task.getStatus());

            // Progress
            if (task.getProgress() > 0 && !task.isCompleted()) {
                progressBar.setVisibility(View.VISIBLE);
                textProgress.setVisibility(View.VISIBLE);
                progressBar.setProgress(task.getProgress());
                textProgress.setText(String.format(Locale.getDefault(), "%d%%", task.getProgress()));
            } else {
                progressBar.setVisibility(View.GONE);
                textProgress.setVisibility(View.GONE);
            }

            // Selection state
            if (selectedOverlay != null) {
                selectedOverlay.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            }
            card.setStrokeWidth(isSelected ? 
                    (int) itemView.getResources().getDimension(R.dimen.card_stroke_selected) : 0);
            card.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));

            // Click listeners
            card.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });

            card.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTaskLongClick(task);
                    return true;
                }
                return false;
            });

            // Content description for accessibility
            itemView.setContentDescription(buildContentDescription(task));
        }

        private void bindPriorityChip(String priority) {
            if (chipPriority == null) return;
            
            int colorRes;
            int textRes;
            
            if (priority == null) priority = Task.Priority.NONE;
            switch (priority) {
                case Task.Priority.URGENT:
                    colorRes = R.color.priority_urgent;
                    textRes = R.string.priority_urgent;
                    if (iconFlag != null) {
                        iconFlag.setVisibility(View.VISIBLE);
                        iconFlag.setColorFilter(ContextCompat.getColor(itemView.getContext(), colorRes));
                    }
                    break;
                case Task.Priority.HIGH:
                    colorRes = R.color.priority_high;
                    textRes = R.string.priority_high;
                    if (iconFlag != null) iconFlag.setVisibility(View.GONE);
                    break;
                case Task.Priority.MEDIUM:
                    colorRes = R.color.priority_medium;
                    textRes = R.string.priority_medium;
                    if (iconFlag != null) iconFlag.setVisibility(View.GONE);
                    break;
                case Task.Priority.LOW:
                    colorRes = R.color.priority_low;
                    textRes = R.string.priority_low;
                    if (iconFlag != null) iconFlag.setVisibility(View.GONE);
                    break;
                case Task.Priority.NONE:
                default:
                    colorRes = R.color.priority_none;
                    textRes = R.string.priority_none;
                    if (iconFlag != null) iconFlag.setVisibility(View.GONE);
                    break;
            }
            
            chipPriority.setText(textRes);
            chipPriority.setChipBackgroundColorResource(colorRes);
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

        private String buildContentDescription(Task task) {
            StringBuilder sb = new StringBuilder();
            sb.append(task.getTitle());
            
            if (task.isCompleted()) {
                sb.append(", ").append(itemView.getContext().getString(R.string.a11y_completed));
            }
            
            if (task.getDueDate() != null) {
                sb.append(", ").append(itemView.getContext().getString(R.string.a11y_due_date))
                        .append(" ").append(dateFormat.format(new Date(task.getDueDate())));
            }
            
            return sb.toString();
        }
    }
}
