package com.taskflow.ui.search;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.taskflow.R;
import com.taskflow.databinding.FragmentSearchBinding;
import com.taskflow.model.Board;
import com.taskflow.model.Tag;
import com.taskflow.model.TaskWithTags;
import com.taskflow.model.User;
import com.taskflow.util.PreferencesManager;
import com.taskflow.viewmodel.SearchViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Global search: tasks (title/description), boards, people, tags — aligned with TaskFlow PWA scope.
 */
public class SearchFragment extends Fragment implements SearchResultsAdapter.Listener {

    private static final long DEBOUNCE_MS = 220;
    private static final int MAX_TASKS = 25;
    private static final int MAX_BOARDS = 15;
    private static final int MAX_PEOPLE = 15;
    private static final int MAX_TAGS = 15;

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private SearchResultsAdapter adapter;
    private PreferencesManager prefs;

    private List<TaskWithTags> latestTasks = Collections.emptyList();
    private List<Board> latestBoards = Collections.emptyList();
    private List<User> latestUsers = Collections.emptyList();
    private List<Tag> latestTags = Collections.emptyList();

    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefs = PreferencesManager.getInstance(requireContext());
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        adapter = new SearchResultsAdapter(this);
        binding.recyclerResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerResults.setAdapter(adapter);

        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(SearchFragment.this).navigateUp());

        binding.btnClearRecent.setOnClickListener(v -> {
            prefs.clearRecentSearches();
            mergeResults();
        });

        TextInputEditText edit = binding.editSearch;
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                scheduleQueryUpdate(s != null ? s.toString() : "");
            }
        });
        edit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String q = edit.getText() != null ? edit.getText().toString().trim() : "";
                if (!q.isEmpty()) {
                    prefs.addRecentSearch(q);
                }
                flushQueryNow(edit.getText() != null ? edit.getText().toString() : "");
                return true;
            }
            return false;
        });

        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            latestTasks = tasks != null ? tasks : Collections.emptyList();
            mergeResults();
        });
        viewModel.getBoards().observe(getViewLifecycleOwner(), boards -> {
            latestBoards = boards != null ? boards : Collections.emptyList();
            mergeResults();
        });
        viewModel.getUsers().observe(getViewLifecycleOwner(), users -> {
            latestUsers = users != null ? users : Collections.emptyList();
            mergeResults();
        });
        viewModel.getTags().observe(getViewLifecycleOwner(), tags -> {
            latestTags = tags != null ? tags : Collections.emptyList();
            mergeResults();
        });

        viewModel.getQuery().observe(getViewLifecycleOwner(), q -> mergeResults());

        edit.post(edit::requestFocus);
    }

    private void scheduleQueryUpdate(String raw) {
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
        debounceRunnable = () -> viewModel.setQuery(raw);
        debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_MS);
    }

    private void flushQueryNow(String raw) {
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
            debounceRunnable = null;
        }
        viewModel.setQuery(raw);
    }

    private void mergeResults() {
        if (binding == null) return;
        String q = viewModel.getQuery().getValue();
        if (q == null) q = "";
        String trimmed = q.trim();

        if (trimmed.isEmpty()) {
            List<String> recent = prefs.getRecentSearches();
            binding.rowRecentHeader.setVisibility(recent.isEmpty() ? View.GONE : View.VISIBLE);
            binding.btnClearRecent.setVisibility(recent.isEmpty() ? View.GONE : View.VISIBLE);
            binding.textIdleHint.setVisibility(recent.isEmpty() ? View.VISIBLE : View.GONE);
            binding.emptyState.setVisibility(View.GONE);

            List<SearchListItem> items = new ArrayList<>();
            for (String r : recent) {
                items.add(SearchListItem.recent(r));
            }
            adapter.submitList(items);
            binding.recyclerResults.setVisibility(View.VISIBLE);
            return;
        }

        binding.rowRecentHeader.setVisibility(View.GONE);
        binding.textIdleHint.setVisibility(View.GONE);
        binding.btnClearRecent.setVisibility(View.GONE);

        List<SearchListItem> items = new ArrayList<>();
        int taskCap = Math.min(latestTasks.size(), MAX_TASKS);
        if (taskCap > 0) {
            items.add(SearchListItem.header(R.string.search_section_tasks));
            for (int i = 0; i < taskCap; i++) {
                items.add(SearchListItem.task(latestTasks.get(i)));
            }
        }
        int boardCap = Math.min(latestBoards.size(), MAX_BOARDS);
        if (boardCap > 0) {
            items.add(SearchListItem.header(R.string.search_section_boards));
            for (int i = 0; i < boardCap; i++) {
                items.add(SearchListItem.board(latestBoards.get(i)));
            }
        }
        int userCap = Math.min(latestUsers.size(), MAX_PEOPLE);
        if (userCap > 0) {
            items.add(SearchListItem.header(R.string.search_section_people));
            for (int i = 0; i < userCap; i++) {
                items.add(SearchListItem.user(latestUsers.get(i)));
            }
        }
        int tagCap = Math.min(latestTags.size(), MAX_TAGS);
        if (tagCap > 0) {
            items.add(SearchListItem.header(R.string.search_section_tags));
            for (int i = 0; i < tagCap; i++) {
                items.add(SearchListItem.tag(latestTags.get(i)));
            }
        }

        adapter.submitList(items);
        boolean empty = items.isEmpty();
        binding.emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerResults.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void rememberSearchQuery() {
        if (binding == null) return;
        TextInputEditText edit = binding.editSearch;
        if (edit.getText() == null) return;
        String q = edit.getText().toString().trim();
        if (!q.isEmpty()) {
            prefs.addRecentSearch(q);
        }
    }

    @Override
    public void onTaskClick(long taskId) {
        rememberSearchQuery();
        Bundle args = new Bundle();
        args.putLong("taskId", taskId);
        NavHostFragment.findNavController(this).navigate(R.id.taskDetailBottomSheet, args);
    }

    @Override
    public void onBoardClick(long boardId) {
        rememberSearchQuery();
        Bundle args = new Bundle();
        args.putLong("boardId", boardId);
        NavHostFragment.findNavController(this).navigate(R.id.boardFragment, args);
    }

    @Override
    public void onUserClick(User user) {
        rememberSearchQuery();
        String msg = user.getEmail() != null && !user.getEmail().isEmpty()
                ? user.getEmail()
                : user.getName();
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onTagClick(Tag tag) {
        rememberSearchQuery();
        Snackbar.make(binding.getRoot(),
                getString(R.string.search_type_tag) + ": " + tag.getName(),
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onRecentClick(String query) {
        binding.editSearch.setText(query);
        binding.editSearch.setSelection(query.length());
        flushQueryNow(query);
        prefs.addRecentSearch(query);
    }

    @Override
    public void onDestroyView() {
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
        binding = null;
        super.onDestroyView();
    }
}
