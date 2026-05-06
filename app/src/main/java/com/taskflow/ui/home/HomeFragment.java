/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.taskflow.R;
import com.taskflow.data.repo.TaskRepository;
import com.taskflow.databinding.FragmentHomeBinding;
import com.taskflow.model.Board;
import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.viewmodel.HomeViewModel;

/**
 * HomeFragment - Dashboard screen showing overview of tasks and boards.
 * 
 * Displays:
 * - Personalized greeting
 * - Today's focus (tasks due today, overdue warnings)
 * - Quick actions
 * - Recent boards
 * - Upcoming deadlines
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private NavController navController;
    
    private BoardCardAdapter boardAdapter;
    private TaskCompactAdapter taskAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get NavController
        navController = NavHostFragment.findNavController(this);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        // Setup UI
        setupRecyclerViews();
        setupClickListeners();
        setupSwipeRefresh();
        
        // Observe data
        observeViewModel();
    }

    /**
     * Initialize RecyclerViews with adapters.
     */
    private void setupRecyclerViews() {
        // Recent Boards - Horizontal list
        boardAdapter = new BoardCardAdapter(board -> {
            // Navigate to board
            viewModel.onBoardAccessed(board.getId());
            Bundle args = new Bundle();
            args.putLong("boardId", board.getId());
            navController.navigate(R.id.boardFragment, args);
        });
        binding.recyclerRecentBoards.setAdapter(boardAdapter);
        binding.recyclerRecentBoards.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        
        // Upcoming Tasks - Vertical list
        taskAdapter = new TaskCompactAdapter(
            // On task click
            task -> {
                Bundle args = new Bundle();
                args.putLong("taskId", task.getId());
                navController.navigate(R.id.taskDetailBottomSheet, args);
            },
            // On checkbox toggle
            task -> viewModel.completeTask(task)
        );
        binding.recyclerUpcomingTasks.setAdapter(taskAdapter);
        binding.recyclerUpcomingTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    /**
     * Setup click listeners for interactive elements.
     */
    private void setupClickListeners() {
        binding.taskflowBrand.setOnClickListener(v -> navController.navigate(R.id.homeFragment));

        // Search bar
        binding.searchBar.setOnClickListener(v -> 
            navController.navigate(R.id.searchFragment)
        );
        
        // Profile button
        binding.profileButton.setOnClickListener(v -> 
            navController.navigate(R.id.settingsFragment)
        );
        
        // Quick Add action
        binding.actionQuickAdd.setOnClickListener(v -> 
            navController.navigate(R.id.quickAddBottomSheet)
        );
        
        // My Tasks action
        binding.actionMyTasks.setOnClickListener(v -> {
            // Navigate to list with "My Tasks" filter
            navController.navigate(R.id.listFragment);
        });
        
        // View all boards
        binding.btnViewAllBoards.setOnClickListener(v -> 
            navController.navigate(R.id.boardFragment)
        );
        
        // View all tasks
        binding.btnViewAllTasks.setOnClickListener(v -> 
            navController.navigate(R.id.listFragment)
        );
    }

    /**
     * Setup swipe-to-refresh functionality.
     */
    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    /**
     * Observe ViewModel LiveData and update UI.
     */
    private void observeViewModel() {
        // Greeting
        viewModel.getGreeting().observe(getViewLifecycleOwner(), greeting -> {
            binding.textGreeting.setText(greeting);
        });
        
        // Current user
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.textAvatarInitials.setText(user.getInitials());
                boolean guest = TaskRepository.GUEST_USER_EMAIL.equalsIgnoreCase(
                        user.getEmail() != null ? user.getEmail() : "");
                binding.textUserName.setText(guest
                        ? getString(R.string.home_subtitle_guest)
                        : getString(R.string.home_subtitle_productive));

                // Set avatar color
                int colorRes = getAvatarColor(user.getAvatarColor());
                binding.profileButton.setBackgroundTintList(
                    getResources().getColorStateList(colorRes, requireContext().getTheme())
                );
            }
        });
        
        // Tasks due today count
        viewModel.getDueTodayCount().observe(getViewLifecycleOwner(), count -> {
            int taskCount = count != null ? count : 0;
            String text = getResources().getQuantityString(
                R.plurals.tasks_due_today_plural, taskCount, taskCount
            );
            binding.textTasksDueToday.setText(text);
        });
        
        // Overdue count
        viewModel.getOverdueCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0) {
                binding.overdueWarning.setVisibility(View.VISIBLE);
                binding.textOverdueCount.setText(
                    getString(R.string.home_overdue_warning, count)
                );
            } else {
                binding.overdueWarning.setVisibility(View.GONE);
            }
        });
        
        // Recent boards
        viewModel.getRecentBoards().observe(getViewLifecycleOwner(), boards -> {
            if (boards != null && !boards.isEmpty()) {
                boardAdapter.submitList(boards);
                binding.recyclerRecentBoards.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerRecentBoards.setVisibility(View.GONE);
            }
        });
        
        // Upcoming tasks
        viewModel.getUpcomingTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                taskAdapter.submitList(tasks);
                binding.recyclerUpcomingTasks.setVisibility(View.VISIBLE);
                binding.emptyState.getRoot().setVisibility(View.GONE);
            } else {
                binding.recyclerUpcomingTasks.setVisibility(View.GONE);
                showEmptyState();
            }
        });
        
        // Loading state
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Could show skeleton loaders here
        });
    }

    /**
     * Show empty state when no upcoming tasks.
     */
    private void showEmptyState() {
        binding.emptyState.getRoot().setVisibility(View.VISIBLE);
        binding.emptyState.imageEmpty.setImageResource(R.drawable.ic_empty_tasks);
        binding.emptyState.textEmptyTitle.setText(R.string.home_no_tasks_due);
        binding.emptyState.textEmptyDescription.setText(R.string.empty_calendar_desc);
        binding.emptyState.btnEmptyAction.setVisibility(View.VISIBLE);
        binding.emptyState.btnEmptyAction.setText(R.string.board_add_task);
        binding.emptyState.btnEmptyAction.setOnClickListener(v -> 
            navController.navigate(R.id.quickAddBottomSheet)
        );
    }

    /**
     * Get avatar background color resource based on index.
     */
    private int getAvatarColor(int colorIndex) {
        switch (colorIndex) {
            case 1: return R.color.avatar_1;
            case 2: return R.color.avatar_2;
            case 3: return R.color.avatar_3;
            case 4: return R.color.avatar_4;
            case 5: return R.color.avatar_5;
            case 6: return R.color.avatar_6;
            case 7: return R.color.avatar_7;
            case 8: return R.color.avatar_8;
            default: return R.color.avatar_6;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
