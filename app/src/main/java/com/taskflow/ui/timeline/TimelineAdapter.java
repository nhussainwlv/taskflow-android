/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.ui.timeline;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.taskflow.R;
import com.taskflow.model.Task;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Timeline Adapter
 * 
 * RecyclerView adapter for displaying timeline rows with task bars.
 * Each row represents a group (assignee/project) with horizontal task bars.
 */
public class TimelineAdapter extends ListAdapter<TimelineFragment.TimelineRow, TimelineAdapter.TimelineRowViewHolder> {

    private final TimelineInteractionListener listener;
    private float currentScale = 1.0f;
    private static final int PIXELS_PER_DAY = 60; // Base pixels per day

    public interface TimelineInteractionListener {
        void onTaskClick(Task task);
        void onTaskDrag(Task task, long newStartDate, long newEndDate);
    }

    private static final DiffUtil.ItemCallback<TimelineFragment.TimelineRow> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<TimelineFragment.TimelineRow>() {
                @Override
                public boolean areItemsTheSame(@NonNull TimelineFragment.TimelineRow oldItem, 
                                               @NonNull TimelineFragment.TimelineRow newItem) {
                    return oldItem.getGroupId() == newItem.getGroupId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull TimelineFragment.TimelineRow oldItem, 
                                                  @NonNull TimelineFragment.TimelineRow newItem) {
                    return oldItem.getTasks().equals(newItem.getTasks()) &&
                            oldItem.getScale() == newItem.getScale();
                }
            };

    public TimelineAdapter(TimelineInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setScale(float scale) {
        this.currentScale = scale;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimelineRowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timeline_row, parent, false);
        return new TimelineRowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineRowViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class TimelineRowViewHolder extends RecyclerView.ViewHolder {
        private final TextView textGroupName;
        private final LinearLayout containerBars;
        private final View rowBackground;

        TimelineRowViewHolder(@NonNull View itemView) {
            super(itemView);
            textGroupName = itemView.findViewById(R.id.text_group_name);
            containerBars = itemView.findViewById(R.id.container_task_bars);
            rowBackground = itemView.findViewById(R.id.row_background);
        }

        void bind(TimelineFragment.TimelineRow row) {
            // Set group name
            textGroupName.setText(row.getGroupName());
            
            // Clear existing bars
            containerBars.removeAllViews();
            
            // Calculate timeline dimensions
            int pixelsPerDay = (int) (PIXELS_PER_DAY * currentScale);
            long timelineStart = row.getTimelineStart();
            long totalDays = TimeUnit.MILLISECONDS.toDays(
                    row.getTimelineEnd() - row.getTimelineStart());
            int totalWidth = (int) (totalDays * pixelsPerDay);
            
            // Set container width
            ViewGroup.LayoutParams layoutParams = containerBars.getLayoutParams();
            layoutParams.width = totalWidth;
            containerBars.setLayoutParams(layoutParams);
            
            // Add task bars
            for (Task task : row.getTasks()) {
                addTaskBar(task, timelineStart, pixelsPerDay);
            }
            
            // Alternate row background for readability
            int position = getAdapterPosition();
            if (position % 2 == 0) {
                rowBackground.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.surface));
            } else {
                rowBackground.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.surface_variant));
            }
        }

        private void addTaskBar(Task task, long timelineStart, int pixelsPerDay) {
            // Calculate position and width
            long taskDueDate = task.getDueDate() != null ? task.getDueDate() : System.currentTimeMillis();
            
            // Assume task duration of 1 day if no start date
            // In a real app, you'd have both start and end dates
            long taskStartDate = taskDueDate - TimeUnit.DAYS.toMillis(1);
            
            long daysFromStart = TimeUnit.MILLISECONDS.toDays(taskStartDate - timelineStart);
            long taskDuration = Math.max(1, TimeUnit.MILLISECONDS.toDays(taskDueDate - taskStartDate));
            
            int leftMargin = (int) (daysFromStart * pixelsPerDay);
            int barWidth = (int) (taskDuration * pixelsPerDay);
            
            // Only show if within visible range
            if (leftMargin + barWidth < 0 || leftMargin > containerBars.getWidth()) {
                return;
            }
            
            // Create task bar view
            View taskBar = LayoutInflater.from(itemView.getContext())
                    .inflate(R.layout.view_timeline_task_bar, containerBars, false);
            
            TextView textTitle = taskBar.findViewById(R.id.text_task_title);
            View progressOverlay = taskBar.findViewById(R.id.progress_overlay);
            
            // Set task title
            textTitle.setText(task.getTitle());
            
            // Set bar color based on priority
            int colorRes = getPriorityColor(task.getPriority());
            taskBar.setBackgroundTintList(
                    ContextCompat.getColorStateList(itemView.getContext(), colorRes));
            
            // Set progress overlay width
            if (progressOverlay != null && task.getProgress() > 0) {
                ViewGroup.LayoutParams progressParams = progressOverlay.getLayoutParams();
                progressParams.width = (int) (barWidth * (task.getProgress() / 100f));
                progressOverlay.setLayoutParams(progressParams);
                progressOverlay.setVisibility(View.VISIBLE);
            }
            
            // Set layout params with position
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    barWidth, 
                    (int) itemView.getResources().getDimension(R.dimen.timeline_bar_height));
            params.leftMargin = Math.max(0, leftMargin);
            params.topMargin = (int) itemView.getResources().getDimension(R.dimen.spacing_xs);
            params.bottomMargin = (int) itemView.getResources().getDimension(R.dimen.spacing_xs);
            
            taskBar.setLayoutParams(params);
            
            // Click listener
            taskBar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
            
            // Content description for accessibility
            taskBar.setContentDescription(buildContentDescription(task));
            
            containerBars.addView(taskBar);
        }

        private int getPriorityColor(String priority) {
            if (priority == null) return R.color.primary;
            switch (priority) {
                case Task.Priority.URGENT: return R.color.priority_urgent;
                case Task.Priority.HIGH: return R.color.priority_high;
                case Task.Priority.MEDIUM: return R.color.priority_medium;
                case Task.Priority.LOW: return R.color.priority_low;
                case Task.Priority.NONE:
                default: return R.color.primary;
            }
        }

        private String buildContentDescription(Task task) {
            return itemView.getContext().getString(R.string.a11y_timeline_task, 
                    task.getTitle(), 
                    task.getProgress());
        }
    }
}
