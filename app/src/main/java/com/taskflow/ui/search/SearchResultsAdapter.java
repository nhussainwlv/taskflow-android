/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.taskflow.R;
import com.taskflow.model.Board;
import com.taskflow.model.Tag;
import com.taskflow.model.Task;
import com.taskflow.model.TaskWithTags;
import com.taskflow.model.User;

import java.util.Locale;

/**
 * RecyclerView adapter for global search results.
 */
public class SearchResultsAdapter extends ListAdapter<SearchListItem, RecyclerView.ViewHolder> {

    public interface Listener {
        void onTaskClick(long taskId);

        void onBoardClick(long boardId);

        void onUserClick(User user);

        void onTagClick(Tag tag);

        void onRecentClick(String query);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ROW = 1;

    private final Listener listener;

    public SearchResultsAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<SearchListItem> DIFF = new DiffUtil.ItemCallback<SearchListItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull SearchListItem oldItem, @NonNull SearchListItem newItem) {
            if (oldItem.type != newItem.type) return false;
            switch (oldItem.type) {
                case HEADER:
                    return oldItem.headerTitleRes == newItem.headerTitleRes;
                case TASK:
                    return oldItem.taskWithTags.getTask().getId() == newItem.taskWithTags.getTask().getId();
                case BOARD:
                    return oldItem.board.getId() == newItem.board.getId();
                case USER:
                    return oldItem.user.getId() == newItem.user.getId();
                case TAG:
                    return oldItem.tag.getId() == newItem.tag.getId();
                case RECENT:
                    return oldItem.recentQuery.equals(newItem.recentQuery);
                default:
                    return false;
            }
        }

        @Override
        public boolean areContentsTheSame(@NonNull SearchListItem oldItem, @NonNull SearchListItem newItem) {
            return oldItem.equals(newItem);
        }
    };

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type == SearchListItem.RowType.HEADER ? TYPE_HEADER : TYPE_ROW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inflater.inflate(R.layout.item_search_section, parent, false);
            return new HeaderVH(v);
        }
        View v = inflater.inflate(R.layout.item_search_row, parent, false);
        return new RowVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SearchListItem item = getItem(position);
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).bind(item.headerTitleRes);
        } else if (holder instanceof RowVH) {
            ((RowVH) holder).bind(item, listener);
        }
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        private final TextView textSection;

        HeaderVH(@NonNull View itemView) {
            super(itemView);
            textSection = itemView.findViewById(R.id.text_section);
        }

        void bind(int titleRes) {
            textSection.setText(titleRes);
        }
    }

    static class RowVH extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView badge;
        private final TextView title;
        private final TextView subtitle;

        RowVH(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_row);
            badge = itemView.findViewById(R.id.text_badge);
            title = itemView.findViewById(R.id.text_title);
            subtitle = itemView.findViewById(R.id.text_subtitle);
        }

        void bind(SearchListItem item, Listener listener) {
            switch (item.type) {
                case TASK:
                    bindTask(item.taskWithTags, listener);
                    break;
                case BOARD:
                    bindBoard(item.board, listener);
                    break;
                case USER:
                    bindUser(item.user, listener);
                    break;
                case TAG:
                    bindTag(item.tag, listener);
                    break;
                case RECENT:
                    bindRecent(item.recentQuery, listener);
                    break;
                default:
                    break;
            }
        }

        private void bindTask(TaskWithTags twt, Listener listener) {
            Task task = twt.getTask();
            subtitle.setVisibility(View.VISIBLE);
            badge.setText(R.string.search_type_task);
            badge.setVisibility(View.VISIBLE);
            title.setText(task.getTitle());
            String meta = humanizeStatus(task.getStatus());
            if (task.getDescription() != null && !task.getDescription().trim().isEmpty()) {
                meta = meta + " · " + truncate(task.getDescription().trim(), 80);
            }
            subtitle.setText(meta);
            card.setOnClickListener(v -> listener.onTaskClick(task.getId()));
        }

        private void bindBoard(Board board, Listener listener) {
            subtitle.setVisibility(View.VISIBLE);
            badge.setText(R.string.search_type_board);
            badge.setVisibility(View.VISIBLE);
            title.setText(board.getName());
            String desc = board.getDescription();
            subtitle.setText(desc != null && !desc.isEmpty() ? desc : itemView.getContext().getString(R.string.nav_board));
            card.setOnClickListener(v -> listener.onBoardClick(board.getId()));
        }

        private void bindUser(User user, Listener listener) {
            subtitle.setVisibility(View.VISIBLE);
            badge.setText(R.string.search_type_member);
            badge.setVisibility(View.VISIBLE);
            title.setText(user.getName());
            String email = user.getEmail();
            subtitle.setText(email != null && !email.isEmpty() ? email : itemView.getContext().getString(R.string.task_assignee));
            card.setOnClickListener(v -> listener.onUserClick(user));
        }

        private void bindTag(Tag tag, Listener listener) {
            subtitle.setVisibility(View.VISIBLE);
            badge.setText(R.string.search_type_tag);
            badge.setVisibility(View.VISIBLE);
            title.setText(tag.getName());
            subtitle.setText(R.string.search_type_tag);
            card.setOnClickListener(v -> listener.onTagClick(tag));
        }

        private void bindRecent(String query, Listener listener) {
            badge.setText(R.string.search_type_recent);
            badge.setVisibility(View.VISIBLE);
            title.setText(query);
            subtitle.setVisibility(View.GONE);
            card.setOnClickListener(v -> listener.onRecentClick(query));
        }

        private static String truncate(String s, int max) {
            if (s.length() <= max) return s;
            return s.substring(0, max - 1) + "…";
        }

        private static String humanizeStatus(String status) {
            if (status == null || status.isEmpty()) return "";
            String[] parts = status.replace('_', ' ').toLowerCase(Locale.US).split("\\s+");
            StringBuilder b = new StringBuilder();
            for (String p : parts) {
                if (p.isEmpty()) continue;
                b.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
            }
            return b.toString().trim();
        }
    }
}
