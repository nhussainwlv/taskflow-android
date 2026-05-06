/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.ui.detail;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.taskflow.R;
import com.taskflow.model.Board;
import com.taskflow.model.Column;
import com.taskflow.model.Task;
import com.taskflow.util.PreferencesManager;
import com.taskflow.viewmodel.TaskViewModel;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Quick Add — board/column selection, dates, navigate to full editor when requested.
 */
public class QuickAddBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "QuickAddBottomSheet";
    private static final String ARG_PRESET_DATE = "preset_date";

    private TaskViewModel viewModel;

    private TextInputLayout inputLayoutTitle;
    private TextInputEditText editTitle;
    private TextView textBoardName;
    private TextView textColumnName;
    private TextView textCategoryName;
    private View viewColumnColor;
    private ChipGroup chipGroupDueDate;
    private MaterialButton btnMoreOptions;
    private MaterialButton btnAddTask;

    private Long selectedDueDate = null;
    private long selectedBoardId;
    private long selectedColumnId;
    private String selectedCategory = Task.Category.GENERAL;

    public static QuickAddBottomSheet newInstance(long presetDate) {
        QuickAddBottomSheet fragment = new QuickAddBottomSheet();
        Bundle args = new Bundle();
        args.putLong(ARG_PRESET_DATE, presetDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedBoardId = PreferencesManager.getInstance(requireContext()).getDefaultBoardId();
        selectedColumnId = 1L;
        if (getArguments() != null) {
            long presetDate = getArguments().getLong(ARG_PRESET_DATE, -1);
            if (presetDate > 0) {
                selectedDueDate = presetDate;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_quick_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        initViews(view);
        setupListeners();
        loadBoardAndColumns();

        refreshCategoryChip();
        editTitle.post(editTitle::requestFocus);
    }

    private void refreshCategoryChip() {
        textCategoryName.setText(Task.categoryLabel(requireContext(), selectedCategory));
    }

    private void initViews(View view) {
        inputLayoutTitle = view.findViewById(R.id.input_layout_title);
        editTitle = view.findViewById(R.id.edit_title);
        textBoardName = view.findViewById(R.id.text_board_name);
        textColumnName = view.findViewById(R.id.text_column_name);
        textCategoryName = view.findViewById(R.id.text_category_name);
        viewColumnColor = view.findViewById(R.id.view_column_color);
        chipGroupDueDate = view.findViewById(R.id.chip_group_due_date);
        btnMoreOptions = view.findViewById(R.id.btn_more_options);
        btnAddTask = view.findViewById(R.id.btn_add_task);
    }

    private void loadBoardAndColumns() {
        Observer<List<Board>> boardObs = new Observer<List<Board>>() {
            @Override
            public void onChanged(List<Board> boards) {
                viewModel.getAllBoards().removeObserver(this);
                if (boards == null || boards.isEmpty()) {
                    textBoardName.setText(R.string.empty_board_title);
                    return;
                }
                Board match = null;
                for (Board b : boards) {
                    if (b.getId() == selectedBoardId) {
                        match = b;
                        break;
                    }
                }
                if (match == null) {
                    match = boards.get(0);
                    selectedBoardId = match.getId();
                }
                textBoardName.setText(match.getName());
                loadColumnsForCurrentBoard(false);
            }
        };
        viewModel.getAllBoards().observe(getViewLifecycleOwner(), boardObs);
    }

    /** @param pickerMode when true only refresh chip UI from existing selection */
    private void loadColumnsForCurrentBoard(boolean pickerMode) {
        Observer<List<Column>> colObs = new Observer<List<Column>>() {
            @Override
            public void onChanged(List<Column> columns) {
                viewModel.getColumnsForBoard(selectedBoardId).removeObserver(this);
                if (columns == null || columns.isEmpty()) {
                    textColumnName.setText(R.string.board_no_columns);
                    return;
                }
                Column sel = null;
                for (Column c : columns) {
                    if (c.getId() == selectedColumnId) {
                        sel = c;
                        break;
                    }
                }
                if (sel == null) {
                    sel = columns.get(0);
                    selectedColumnId = sel.getId();
                }
                textColumnName.setText(sel.getName());
                applyColumnColor(sel.getColor());
                if (!pickerMode) {
                    setupDueDateChips();
                }
            }
        };
        viewModel.getColumnsForBoard(selectedBoardId).observe(getViewLifecycleOwner(), colObs);
    }

    private void applyColumnColor(String hex) {
        try {
            if (hex != null && hex.startsWith("#") && hex.length() >= 4) {
                int c = Color.parseColor(hex);
                viewColumnColor.setBackgroundTintList(ColorStateList.valueOf(c));
                return;
            }
        } catch (IllegalArgumentException ignored) {
        }
        viewColumnColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9CA3AF")));
    }

    private void setupListeners() {
        editTitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createTask();
                return true;
            }
            return false;
        });

        View cardBoardSelector = requireView().findViewById(R.id.card_board_selector);
        cardBoardSelector.setOnClickListener(v -> showBoardPicker());

        View cardColumnSelector = requireView().findViewById(R.id.card_column_selector);
        cardColumnSelector.setOnClickListener(v -> showColumnPicker());

        View cardCategorySelector = requireView().findViewById(R.id.card_category_selector);
        cardCategorySelector.setOnClickListener(v -> showCategoryPicker());

        setupDueDateChips();

        btnMoreOptions.setOnClickListener(v -> openFullEditor());

        btnAddTask.setOnClickListener(v -> createTask());
    }

    private void setupDueDateChips() {
        Chip chipToday = chipGroupDueDate.findViewById(R.id.chip_due_today);
        Chip chipTomorrow = chipGroupDueDate.findViewById(R.id.chip_due_tomorrow);
        Chip chipNextWeek = chipGroupDueDate.findViewById(R.id.chip_due_next_week);
        Chip chipCustom = chipGroupDueDate.findViewById(R.id.chip_due_custom);

        if (chipToday != null) {
            chipToday.setOnClickListener(v -> {
                selectedDueDate = getDateForToday();
                chipGroupDueDate.check(R.id.chip_due_today);
            });
        }
        if (chipTomorrow != null) {
            chipTomorrow.setOnClickListener(v -> {
                selectedDueDate = getDateForTomorrow();
                chipGroupDueDate.check(R.id.chip_due_tomorrow);
            });
        }
        if (chipNextWeek != null) {
            chipNextWeek.setOnClickListener(v -> {
                selectedDueDate = getDateForNextWeek();
                chipGroupDueDate.check(R.id.chip_due_next_week);
            });
        }
        if (chipCustom != null) {
            chipCustom.setOnClickListener(v -> showDatePicker());
        }

        if (selectedDueDate != null) {
            if (isSameDay(selectedDueDate, getDateForToday())) {
                chipGroupDueDate.check(R.id.chip_due_today);
            } else if (isSameDay(selectedDueDate, getDateForTomorrow())) {
                chipGroupDueDate.check(R.id.chip_due_tomorrow);
            } else {
                chipGroupDueDate.check(R.id.chip_due_custom);
                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("MMM d", Locale.getDefault());
                chipCustom.setText(sdf.format(new java.util.Date(selectedDueDate)));
            }
        }
    }

    private void showBoardPicker() {
        Observer<List<Board>> obs = new Observer<List<Board>>() {
            @Override
            public void onChanged(List<Board> boards) {
                viewModel.getAllBoards().removeObserver(this);
                if (boards == null || boards.isEmpty()) return;
                String[] titles = new String[boards.size()];
                for (int i = 0; i < boards.size(); i++) {
                    titles[i] = boards.get(i).getName();
                }
                int idx = -1;
                for (int i = 0; i < boards.size(); i++) {
                    if (boards.get(i).getId() == selectedBoardId) {
                        idx = i;
                        break;
                    }
                }
                if (idx < 0) idx = 0;
                final int[] choice = {idx};
                int start = idx >= 0 ? idx : 0;
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.pick_board_title)
                        .setSingleChoiceItems(titles, start, (d, which) -> choice[0] = which)
                        .setPositiveButton(R.string.action_apply, (d, w) -> {
                            Board b = boards.get(choice[0]);
                            selectedBoardId = b.getId();
                            selectedColumnId = -1;
                            textBoardName.setText(b.getName());
                            loadColumnsForCurrentBoard(true);
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        };
        viewModel.getAllBoards().observe(getViewLifecycleOwner(), obs);
    }

    private void showCategoryPicker() {
        String[] labels = getResources().getStringArray(R.array.task_category_labels);
        String[] values = getResources().getStringArray(R.array.task_category_values);
        int selected = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(selectedCategory)) {
                selected = i;
                break;
            }
        }
        final int[] choice = {selected};
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.pick_category_title)
                .setSingleChoiceItems(labels, selected, (d, which) -> choice[0] = which)
                .setPositiveButton(R.string.action_apply, (d, w) -> {
                    int ix = choice[0];
                    if (ix >= 0 && ix < values.length) {
                        selectedCategory = values[ix];
                        refreshCategoryChip();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showColumnPicker() {
        Observer<List<Column>> obs = new Observer<List<Column>>() {
            @Override
            public void onChanged(List<Column> cols) {
                viewModel.getColumnsForBoard(selectedBoardId).removeObserver(this);
                if (cols == null || cols.isEmpty()) {
                    Snackbar.make(requireView(), R.string.board_no_columns, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                String[] titles = new String[cols.size()];
                for (int i = 0; i < cols.size(); i++) {
                    titles[i] = cols.get(i).getName();
                }
                int idx = -1;
                for (int i = 0; i < cols.size(); i++) {
                    if (cols.get(i).getId() == selectedColumnId) {
                        idx = i;
                        break;
                    }
                }
                if (idx < 0) idx = 0;
                final int[] choice = {idx};
                int start = idx >= 0 ? idx : 0;
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.pick_column_title)
                        .setSingleChoiceItems(titles, start, (d, which) -> choice[0] = which)
                        .setPositiveButton(R.string.action_apply, (d, w) -> {
                            Column col = cols.get(choice[0]);
                            selectedColumnId = col.getId();
                            textColumnName.setText(col.getName());
                            applyColumnColor(col.getColor());
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        };
        viewModel.getColumnsForBoard(selectedBoardId).observe(getViewLifecycleOwner(), obs);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.task_due_date)
                .setSelection(selectedDueDate != null ? selectedDueDate : System.currentTimeMillis())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDueDate = selection;
            chipGroupDueDate.check(R.id.chip_due_custom);

            Chip chipCustom = chipGroupDueDate.findViewById(R.id.chip_due_custom);
            if (chipCustom != null) {
                java.text.SimpleDateFormat sdf =
                        new java.text.SimpleDateFormat("MMM d", Locale.getDefault());
                chipCustom.setText(sdf.format(new java.util.Date(selection)));
            }
        });

        datePicker.show(getChildFragmentManager(), "date_picker");
    }

    private void createTask() {
        String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";

        if (title.isEmpty()) {
            inputLayoutTitle.setError(getString(R.string.error_title_required));
            return;
        }
        inputLayoutTitle.setError(null);

        Task task = buildTaskSkeleton(title);

        viewModel.insertTask(task, taskId -> {
            if (!isAdded()) return;
            View anchor = requireActivity().findViewById(android.R.id.content);
            Snackbar.make(anchor, R.string.snackbar_task_created, Snackbar.LENGTH_SHORT).show();
            dismiss();
        });
    }

    @NonNull
    private Task buildTaskSkeleton(String title) {
        Task task = new Task();
        task.setTitle(title);
        task.setDueDate(selectedDueDate);
        long colId = selectedColumnId > 0 ? selectedColumnId : 1L;
        task.setColumnId(colId);
        task.setBoardId(selectedBoardId);
        task.setCategory(selectedCategory);
        task.setStatus(Task.Status.TODO);
        task.setPriority(Task.Priority.MEDIUM);
        task.setProgress(0);
        task.setCompleted(false);
        task.setArchived(false);
        task.setCreatedAt(System.currentTimeMillis());
        task.setUpdatedAt(System.currentTimeMillis());
        return task;
    }

    private void openFullEditor() {
        String title = editTitle.getText() != null ? editTitle.getText().toString().trim() : "";

        if (title.isEmpty()) {
            inputLayoutTitle.setError(getString(R.string.error_title_required));
            return;
        }
        inputLayoutTitle.setError(null);

        Task task = buildTaskSkeleton(title);
        viewModel.insertTask(task, taskId -> {
            dismiss();
            Bundle args = new Bundle();
            args.putLong("taskId", taskId);
            NavHostFragment.findNavController(QuickAddBottomSheet.this)
                    .navigate(R.id.taskDetailBottomSheet, args);
        });
    }

    private long getDateForToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTimeInMillis();
    }

    private long getDateForTomorrow() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTimeInMillis();
    }

    private long getDateForNextWeek() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTimeInMillis();
    }

    private boolean isSameDay(long date1, long date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeInMillis(date2);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
