package com.taskflow.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.taskflow.R;
import com.taskflow.model.Tag;
import com.taskflow.model.Task;
import com.taskflow.model.TaskWithTags;
import com.taskflow.model.User;
import com.taskflow.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Task Detail Bottom Sheet — full editing, assignees, tags, duplicate/archive/delete.
 */
public class TaskDetailBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "TaskDetailBottomSheet";
    /** Matches {@code nav_graph} argument name and callers using {@code putLong(\"taskId\", ...)} */
    private static final String ARG_TASK_ID = "taskId";

    private TaskViewModel viewModel;
    private Task currentTask;
    private TaskWithTags currentDetail;
    private long taskId;

    private MaterialCheckBox checkboxComplete;
    private EditText editTitle;
    private EditText editDescription;
    private ChipGroup chipGroupStatus;
    private ChipGroup chipGroupPriority;
    private TextView textDueDate;
    private TextView textAssigneeName;
    private TextView textAssigneeAvatar;
    private Slider sliderProgress;
    private TextView textProgressValue;
    private ChipGroup chipGroupTags;
    private TextView textTimestamps;
    private ImageButton btnMore;
    private View rowDueDate;
    private View rowCategory;
    private TextView textCategory;
    private View rowAssignee;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat timestampFormat = new SimpleDateFormat("MMM d", Locale.getDefault());

    public static TaskDetailBottomSheet newInstance(long taskId) {
        TaskDetailBottomSheet fragment = new TaskDetailBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_TASK_ID, taskId);
        args.putLong("task_id", taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        if (b != null) {
            taskId = b.getLong(ARG_TASK_ID, b.getLong("task_id", -1));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_task_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        initViews(view);
        setupListeners();
        loadTaskDetail();
    }

    private void initViews(View view) {
        checkboxComplete = view.findViewById(R.id.checkbox_complete);
        editTitle = view.findViewById(R.id.edit_title);
        editDescription = view.findViewById(R.id.edit_description);
        chipGroupStatus = view.findViewById(R.id.chip_group_status);
        chipGroupPriority = view.findViewById(R.id.chip_group_priority);
        textDueDate = view.findViewById(R.id.text_due_date);
        textAssigneeName = view.findViewById(R.id.text_assignee_name);
        textAssigneeAvatar = view.findViewById(R.id.text_assignee_avatar);
        sliderProgress = view.findViewById(R.id.slider_progress);
        textProgressValue = view.findViewById(R.id.text_progress_value);
        chipGroupTags = view.findViewById(R.id.chip_group_tags);
        textTimestamps = view.findViewById(R.id.text_timestamps);
        btnMore = view.findViewById(R.id.btn_more);
        rowDueDate = view.findViewById(R.id.row_due_date);
        rowCategory = view.findViewById(R.id.row_category);
        textCategory = view.findViewById(R.id.text_category);
        rowAssignee = view.findViewById(R.id.row_assignee);
    }

    private void setupListeners() {
        checkboxComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentTask != null) {
                currentTask.setCompleted(isChecked);
                saveTask();
            }
        });

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (currentTask != null && !checkedIds.isEmpty()) {
                currentTask.setStatus(getStatusFromChipId(checkedIds.get(0)));
                saveTask();
            }
        });

        chipGroupPriority.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (currentTask != null && !checkedIds.isEmpty()) {
                currentTask.setPriority(getPriorityFromChipId(checkedIds.get(0)));
                saveTask();
            }
        });

        rowDueDate.setOnClickListener(v -> showDatePicker());
        rowCategory.setOnClickListener(v -> showCategoryPicker());
        rowAssignee.setOnClickListener(v -> showAssigneePicker());
        sliderProgress.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && currentTask != null) {
                int progress = (int) value;
                textProgressValue.setText(String.format(Locale.getDefault(), "%d%%", progress));
                currentTask.setProgress(progress);
            }
        });
        sliderProgress.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                saveTask();
            }
        });

        btnMore.setOnClickListener(v -> showMoreOptions());

        editTitle.setOnFocusChangeListener((vv, hasFocus) -> {
            if (!hasFocus && currentTask != null) {
                String newTitle = editTitle.getText().toString().trim();
                if (!newTitle.isEmpty() && !newTitle.equals(currentTask.getTitle())) {
                    currentTask.setTitle(newTitle);
                    saveTask();
                }
            }
        });

        editDescription.setOnFocusChangeListener((vv, hasFocus) -> {
            if (!hasFocus && currentTask != null) {
                String newDesc = editDescription.getText() != null
                        ? editDescription.getText().toString().trim() : "";
                String old = currentTask.getDescription();
                String oldSafe = old == null ? "" : old;
                if (!newDesc.equals(oldSafe)) {
                    currentTask.setDescription(newDesc.isEmpty() ? null : newDesc);
                    saveTask();
                }
            }
        });

        Chip chipAddTag = chipGroupTags.findViewById(R.id.chip_add_tag);
        if (chipAddTag != null) {
            chipAddTag.setOnClickListener(v -> showTagPicker());
        }
    }

    private void loadTaskDetail() {
        viewModel.getTaskWithTagsById(taskId).observe(getViewLifecycleOwner(), twt -> {
            if (twt != null && twt.getTask() != null) {
                currentDetail = twt;
                currentTask = twt.getTask();
                bindTaskData(twt);
            } else {
                dismiss();
            }
        });
    }

    private void bindTaskData(TaskWithTags twt) {
        Task task = twt.getTask();
        editTitle.setText(task.getTitle());
        checkboxComplete.setChecked(task.isCompleted());

        String desc = task.getDescription();
        editDescription.setText(desc != null ? desc : "");

        int statusChipId = getChipIdFromStatus(task.getStatus());
        if (statusChipId != View.NO_ID) {
            chipGroupStatus.check(statusChipId);
        }

        int priorityChipId = getChipIdFromPriority(task.getPriority());
        if (priorityChipId != View.NO_ID) {
            chipGroupPriority.check(priorityChipId);
        }

        if (task.getDueDate() != null) {
            textDueDate.setText(dateFormat.format(new Date(task.getDueDate())));
        } else {
            textDueDate.setText(R.string.task_due_date_hint);
        }

        textCategory.setText(Task.categoryLabel(requireContext(), task.getCategory()));

        User assignee = twt.getAssignee();
        if (assignee != null) {
            textAssigneeName.setText(assignee.getName());
            String initials = assignee.getInitials();
            textAssigneeAvatar.setText(initials != null && !initials.isEmpty()
                    ? initials : assignee.getName().substring(0, 1).toUpperCase(Locale.getDefault()));
        } else if (task.getAssigneeId() != null && task.getAssigneeId() > 0) {
            textAssigneeName.setText(getString(R.string.pick_assignee_title));
            textAssigneeAvatar.setText("?");
        } else {
            textAssigneeName.setText(R.string.task_unassigned);
            textAssigneeAvatar.setText("?");
        }

        sliderProgress.setValue(task.getProgress());
        textProgressValue.setText(String.format(Locale.getDefault(), "%d%%", task.getProgress()));

        populateTagChips(twt.getTags() != null ? twt.getTags() : Collections.emptyList());

        String created = timestampFormat.format(new Date(task.getCreatedAt()));
        String updated = getRelativeTimeSpan(task.getUpdatedAt());
        textTimestamps.setText(getString(R.string.task_timestamps, created, updated));
    }

    private void clearDynamicTagChips() {
        List<View> toRemove = new ArrayList<>();
        for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
            View child = chipGroupTags.getChildAt(i);
            if (child.getId() != R.id.chip_add_tag) {
                toRemove.add(child);
            }
        }
        for (View child : toRemove) {
            chipGroupTags.removeView(child);
        }
    }

    private void populateTagChips(List<Tag> tags) {
        clearDynamicTagChips();
        int insertBefore = chipGroupTags.indexOfChild(chipGroupTags.findViewById(R.id.chip_add_tag));
        if (insertBefore < 0) insertBefore = chipGroupTags.getChildCount();

        int index = insertBefore;
        for (Tag tag : tags) {
            Chip chip = new Chip(requireContext());
            chip.setText(tag.getName());
            chip.setCloseIconVisible(true);
            chip.setTag(tag.getId());
            chip.setOnCloseIconClickListener(v -> {
                viewModel.removeTagFromTask(taskId, tag.getId());
                Snackbar.make(requireView(), R.string.snackbar_task_updated, Snackbar.LENGTH_SHORT).show();
            });
            chipGroupTags.addView(chip, index++);
        }
    }

    private void saveTask() {
        if (currentTask != null) {
            currentTask.setUpdatedAt(System.currentTimeMillis());
            viewModel.updateTask(currentTask);
        }
    }

    private void showCategoryPicker() {
        if (currentTask == null) return;
        String[] labels = getResources().getStringArray(R.array.task_category_labels);
        String[] values = getResources().getStringArray(R.array.task_category_values);
        int selected = 0;
        String cur = currentTask.getCategory();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(cur)) {
                selected = i;
                break;
            }
        }
        final int[] choice = {selected};
        int start = selected;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.pick_category_title)
                .setSingleChoiceItems(labels, start, (d, which) -> choice[0] = which)
                .setPositiveButton(R.string.action_apply, (dialog, idx) -> {
                    int ix = choice[0];
                    if (ix >= 0 && ix < values.length) {
                        currentTask.setCategory(values[ix]);
                        textCategory.setText(labels[ix]);
                        saveTask();
                        Snackbar.make(requireView(), R.string.snackbar_task_updated, Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showDatePicker() {
        if (currentTask == null) return;
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.task_due_date)
                .setSelection(currentTask.getDueDate() != null ?
                        currentTask.getDueDate() : System.currentTimeMillis())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            currentTask.setDueDate(selection);
            textDueDate.setText(dateFormat.format(new Date(selection)));
            saveTask();
        });

        datePicker.show(getChildFragmentManager(), "date_picker");
    }

    private void showAssigneePicker() {
        if (currentTask == null) return;
        Observer<List<User>> observer = new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                viewModel.getUsers().removeObserver(this);
                final List<User> userList = users != null ? users : new ArrayList<>();
                CharSequence[] names = new CharSequence[userList.size() + 1];
                final Long[] ids = new Long[userList.size() + 1];
                names[0] = getString(R.string.unassign_option);
                ids[0] = null;
                for (int i = 0; i < userList.size(); i++) {
                    User u = userList.get(i);
                    names[i + 1] = u.getName();
                    ids[i + 1] = u.getId();
                }

                int selectedIndex = 0;
                Long aid = currentTask.getAssigneeId();
                if (aid != null) {
                    for (int i = 0; i < userList.size(); i++) {
                        if (userList.get(i).getId() == aid) {
                            selectedIndex = i + 1;
                            break;
                        }
                    }
                }

                final int[] choice = {selectedIndex};
                MaterialAlertDialogBuilder b = new MaterialAlertDialogBuilder(requireContext());
                b.setTitle(R.string.pick_assignee_title);
                b.setSingleChoiceItems(names, selectedIndex, (d, which) -> choice[0] = which);
                b.setPositiveButton(R.string.action_apply, (di, which) -> {
                    int wi = choice[0];
                    currentTask.setAssigneeId(ids[wi]);
                    saveTask();
                    if (wi > 0 && wi - 1 < userList.size()) {
                        User picked = userList.get(wi - 1);
                        textAssigneeName.setText(picked.getName());
                        String ini = picked.getInitials();
                        textAssigneeAvatar.setText(ini != null && !ini.isEmpty() ? ini : "?");
                    } else {
                        textAssigneeName.setText(R.string.task_unassigned);
                        textAssigneeAvatar.setText("?");
                    }
                });
                b.setNegativeButton(android.R.string.cancel, null);
                b.show();
            }
        };
        viewModel.getUsers().observe(getViewLifecycleOwner(), observer);
    }

    private void showTagPicker() {
        if (currentTask == null) return;
        Observer<List<Tag>> tagObs = new Observer<List<Tag>>() {
            @Override
            public void onChanged(List<Tag> allTags) {
                viewModel.getAllTags().removeObserver(this);
                if (allTags == null || allTags.isEmpty()) {
                    Snackbar.make(requireView(), R.string.empty_tasks_desc, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                List<Long> assigned = new ArrayList<>();
                List<Tag> current = currentDetail != null && currentDetail.getTags() != null
                        ? currentDetail.getTags() : Collections.emptyList();
                for (Tag t : current) {
                    assigned.add(t.getId());
                }

                CharSequence[] names = new CharSequence[allTags.size()];
                boolean[] checked = new boolean[allTags.size()];
                for (int i = 0; i < allTags.size(); i++) {
                    Tag t = allTags.get(i);
                    names[i] = t.getName();
                    checked[i] = assigned.contains(t.getId());
                }

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.pick_tags_title)
                        .setMultiChoiceItems(names, checked, (d, idx, isChecked) -> checked[idx] = isChecked)
                        .setPositiveButton(R.string.action_apply, (dialog, idx) -> {
                            List<Long> selected = new ArrayList<>();
                            for (int i = 0; i < checked.length; i++) {
                                if (checked[i]) selected.add(allTags.get(i).getId());
                            }
                            viewModel.updateTaskTags(taskId, selected);
                            Snackbar.make(requireView(), R.string.snackbar_task_updated, Snackbar.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        };
        viewModel.getAllTags().observe(getViewLifecycleOwner(), tagObs);
    }

    private void showMoreOptions() {
        PopupMenu popup = new PopupMenu(requireContext(), btnMore);
        popup.getMenuInflater().inflate(R.menu.task_detail_overflow, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_share_task) {
                shareCurrentTask();
                return true;
            } else if (itemId == R.id.action_duplicate_task) {
                duplicateTask();
                return true;
            } else if (itemId == R.id.action_archive_task) {
                archiveCurrentTask();
                return true;
            } else if (itemId == R.id.action_delete_task) {
                confirmDelete();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void shareCurrentTask() {
        if (currentTask == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append(currentTask.getTitle()).append("\n\n");
        if (currentTask.getDescription() != null && !currentTask.getDescription().trim().isEmpty()) {
            sb.append(currentTask.getDescription().trim()).append("\n\n");
        }
        Long due = currentTask.getDueDate();
        if (due != null) {
            sb.append(getString(R.string.a11y_due_date)).append(": ")
                    .append(dateFormat.format(new Date(due))).append('\n');
        }
        sb.append(getString(R.string.task_category_label)).append(": ")
                .append(Task.categoryLabel(requireContext(), currentTask.getCategory())).append('\n');
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, currentTask.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(intent, getString(R.string.action_share)));
    }

    private void duplicateTask() {
        if (currentTask == null) return;
        Task copy = cloneForDuplicate(currentTask);
        viewModel.insertTask(copy, id -> Snackbar.make(requireView(),
                R.string.task_duplicate_created, Snackbar.LENGTH_SHORT).show());
        dismiss();
    }

    private Task cloneForDuplicate(Task t) {
        Task copy = new Task();
        copy.setTitle(t.getTitle() + " (" + getString(R.string.duplicate_suffix) + ")");
        copy.setDescription(t.getDescription());
        copy.setStatus(Task.Status.TODO);
        copy.setPriority(t.getPriority());
        copy.setColumnId(t.getColumnId());
        copy.setBoardId(t.getBoardId());
        copy.setDueDate(t.getDueDate());
        copy.setCategory(t.getCategory());
        copy.setAssigneeId(t.getAssigneeId());
        copy.setCompleted(false);
        copy.setProgress(0);
        copy.setArchived(false);
        copy.setCreatedAt(System.currentTimeMillis());
        copy.setUpdatedAt(System.currentTimeMillis());
        return copy;
    }

    private void archiveCurrentTask() {
        if (currentTask == null) return;
        currentTask.setArchived(true);
        saveTask();
        Snackbar.make(requireView(), R.string.task_archived, Snackbar.LENGTH_SHORT).show();
        dismiss();
    }

    private void confirmDelete() {
        if (currentTask == null) return;
        String titleSafe = currentTask.getTitle().length() > 40
                ? currentTask.getTitle().substring(0, 39) + "…" : currentTask.getTitle();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.action_delete_task)
                .setMessage(getString(R.string.task_delete_confirm, titleSafe))
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    viewModel.deleteTaskById(taskId);
                    dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private String getStatusFromChipId(int chipId) {
        if (chipId == R.id.chip_status_in_progress) return Task.Status.IN_PROGRESS;
        if (chipId == R.id.chip_status_review) return Task.Status.REVIEW;
        if (chipId == R.id.chip_status_done) return Task.Status.DONE;
        return Task.Status.TODO;
    }

    private int getChipIdFromStatus(String status) {
        if (status == null) return R.id.chip_status_todo;
        switch (status) {
            case Task.Status.IN_PROGRESS:
                return R.id.chip_status_in_progress;
            case Task.Status.REVIEW:
                return R.id.chip_status_review;
            case Task.Status.DONE:
                return R.id.chip_status_done;
            default:
                return R.id.chip_status_todo;
        }
    }

    private String getPriorityFromChipId(int chipId) {
        if (chipId == R.id.chip_priority_urgent) return Task.Priority.URGENT;
        if (chipId == R.id.chip_priority_high) return Task.Priority.HIGH;
        if (chipId == R.id.chip_priority_medium) return Task.Priority.MEDIUM;
        if (chipId == R.id.chip_priority_low) return Task.Priority.LOW;
        return Task.Priority.NONE;
    }

    private int getChipIdFromPriority(String priority) {
        if (priority == null) return R.id.chip_priority_low;
        switch (priority) {
            case Task.Priority.URGENT:
                return R.id.chip_priority_urgent;
            case Task.Priority.HIGH:
                return R.id.chip_priority_high;
            case Task.Priority.MEDIUM:
                return R.id.chip_priority_medium;
            case Task.Priority.LOW:
                return R.id.chip_priority_low;
            default:
                return R.id.chip_priority_low;
        }
    }

    private String getRelativeTimeSpan(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / (60 * 1000);
        long hours = diff / (60 * 60 * 1000);
        long days = diff / (24 * 60 * 60 * 1000);

        if (minutes < 1) return getString(R.string.time_just_now);
        if (minutes < 60) return getString(R.string.time_minutes_ago, (int) minutes);
        if (hours < 24) return getString(R.string.time_hours_ago, (int) hours);
        if (days < 7) return getString(R.string.time_days_ago, (int) days);
        return timestampFormat.format(new Date(timestamp));
    }
}
