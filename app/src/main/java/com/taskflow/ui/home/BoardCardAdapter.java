package com.taskflow.ui.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.taskflow.R;
import com.taskflow.databinding.ItemBoardCardBinding;
import com.taskflow.model.Board;

/**
 * BoardCardAdapter - RecyclerView adapter for board cards.
 * 
 * Displays board information in a horizontal card format.
 * Uses DiffUtil for efficient updates.
 */
public class BoardCardAdapter extends ListAdapter<Board, BoardCardAdapter.BoardViewHolder> {

    private final OnBoardClickListener listener;

    public interface OnBoardClickListener {
        void onBoardClick(Board board);
    }

    public BoardCardAdapter(OnBoardClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Board> DIFF_CALLBACK = new DiffUtil.ItemCallback<Board>() {
        @Override
        public boolean areItemsTheSame(@NonNull Board oldItem, @NonNull Board newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Board oldItem, @NonNull Board newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   (oldItem.getDescription() == null ? newItem.getDescription() == null : 
                    oldItem.getDescription().equals(newItem.getDescription())) &&
                   (oldItem.getColor() == null ? newItem.getColor() == null :
                    oldItem.getColor().equals(newItem.getColor()));
        }
    };

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBoardCardBinding binding = ItemBoardCardBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new BoardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class BoardViewHolder extends RecyclerView.ViewHolder {
        private final ItemBoardCardBinding binding;

        BoardViewHolder(ItemBoardCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Board board) {
            // Set board name
            binding.textName.setText(board.getName());
            
            // Set description
            if (board.getDescription() != null && !board.getDescription().isEmpty()) {
                binding.textDescription.setText(board.getDescription());
                binding.textDescription.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.textDescription.setVisibility(android.view.View.GONE);
            }
            
            // Set icon
            if (board.getIcon() != null && !board.getIcon().isEmpty()) {
                binding.textIcon.setText(board.getIcon());
                binding.textIcon.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.textIcon.setVisibility(android.view.View.GONE);
            }
            
            // Set color bar
            if (board.getColor() != null) {
                try {
                    binding.viewColorBar.setBackgroundColor(Color.parseColor(board.getColor()));
                } catch (Exception e) {
                    binding.viewColorBar.setBackgroundColor(
                        binding.getRoot().getContext().getColor(R.color.primary)
                    );
                }
            }
            
            // Task count - this would need to be fetched separately
            // For now, showing a placeholder
            binding.textTaskCount.setText(binding.getRoot().getContext().getString(R.string.board_task_count, 0));
            
            // Click listener
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBoardClick(board);
                }
            });
            
            // Accessibility
            binding.getRoot().setContentDescription(
                binding.getRoot().getContext().getString(R.string.a11y_task_card, board.getName())
            );
        }
    }
}
