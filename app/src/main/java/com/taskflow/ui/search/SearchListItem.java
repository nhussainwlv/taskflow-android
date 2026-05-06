/**
 * Student Name : Naeem Hussain
 * ID : 2365963
 * Module Name : Project and Professionalism
 * Note: Comments in this file are kept brief and readable.
 */

package com.taskflow.ui.search;

import com.taskflow.model.Board;
import com.taskflow.model.Tag;
import com.taskflow.model.TaskWithTags;
import com.taskflow.model.User;

import java.util.Objects;

/**
 * Single row model for global search results list (sections, entities, recents).
 */
public final class SearchListItem {

    public enum RowType {
        HEADER,
        TASK,
        BOARD,
        USER,
        TAG,
        RECENT
    }

    public final RowType type;
    public final int headerTitleRes;
    public final TaskWithTags taskWithTags;
    public final Board board;
    public final User user;
    public final Tag tag;
    public final String recentQuery;

    private SearchListItem(RowType type, int headerTitleRes, TaskWithTags taskWithTags,
                           Board board, User user, Tag tag, String recentQuery) {
        this.type = type;
        this.headerTitleRes = headerTitleRes;
        this.taskWithTags = taskWithTags;
        this.board = board;
        this.user = user;
        this.tag = tag;
        this.recentQuery = recentQuery;
    }

    public static SearchListItem header(int titleRes) {
        return new SearchListItem(RowType.HEADER, titleRes, null, null, null, null, null);
    }

    public static SearchListItem task(TaskWithTags twt) {
        return new SearchListItem(RowType.TASK, 0, twt, null, null, null, null);
    }

    public static SearchListItem board(Board b) {
        return new SearchListItem(RowType.BOARD, 0, null, b, null, null, null);
    }

    public static SearchListItem user(User u) {
        return new SearchListItem(RowType.USER, 0, null, null, u, null, null);
    }

    public static SearchListItem tag(Tag t) {
        return new SearchListItem(RowType.TAG, 0, null, null, null, t, null);
    }

    public static SearchListItem recent(String query) {
        return new SearchListItem(RowType.RECENT, 0, null, null, null, null, query);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SearchListItem)) return false;
        SearchListItem that = (SearchListItem) o;
        if (type != that.type) return false;
        switch (type) {
            case HEADER:
                return headerTitleRes == that.headerTitleRes;
            case TASK:
                return taskWithTags.getTask().getId() == that.taskWithTags.getTask().getId()
                        && Objects.equals(taskWithTags.getTask().getTitle(), that.taskWithTags.getTask().getTitle());
            case BOARD:
                return board.getId() == that.board.getId()
                        && Objects.equals(board.getName(), that.board.getName());
            case USER:
                return user.getId() == that.user.getId()
                        && Objects.equals(user.getName(), that.user.getName());
            case TAG:
                return tag.getId() == that.tag.getId()
                        && Objects.equals(tag.getName(), that.tag.getName());
            case RECENT:
                return Objects.equals(recentQuery, that.recentQuery);
            default:
                return false;
        }
    }

    @Override
    public int hashCode() {
        switch (type) {
            case HEADER:
                return Objects.hash(type, headerTitleRes);
            case TASK:
                return Objects.hash(type, taskWithTags.getTask().getId(), taskWithTags.getTask().getTitle());
            case BOARD:
                return Objects.hash(type, board.getId(), board.getName());
            case USER:
                return Objects.hash(type, user.getId(), user.getName());
            case TAG:
                return Objects.hash(type, tag.getId(), tag.getName());
            case RECENT:
                return Objects.hash(type, recentQuery);
            default:
                return type.hashCode();
        }
    }
}
