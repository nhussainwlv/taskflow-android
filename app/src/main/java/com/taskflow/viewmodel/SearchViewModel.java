package com.taskflow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.taskflow.data.repo.TaskRepository;
import com.taskflow.model.Board;
import com.taskflow.model.Tag;
import com.taskflow.model.TaskWithTags;
import com.taskflow.model.User;

import java.util.Collections;
import java.util.List;

/**
 * ViewModel for global search (tasks, boards, people, tags) aligned with PWA search scope.
 */
public class SearchViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final MutableLiveData<String> query = new MutableLiveData<>("");

    private final LiveData<List<TaskWithTags>> tasks;
    private final LiveData<List<Board>> boards;
    private final LiveData<List<User>> users;
    private final LiveData<List<Tag>> tags;

    public SearchViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        tasks = Transformations.switchMap(query, this::tasksForQuery);
        boards = Transformations.switchMap(query, this::boardsForQuery);
        users = Transformations.switchMap(query, this::usersForQuery);
        tags = Transformations.switchMap(query, this::tagsForQuery);
    }

    private LiveData<List<TaskWithTags>> tasksForQuery(String q) {
        String n = normalize(q);
        if (n.isEmpty()) {
            MutableLiveData<List<TaskWithTags>> empty = new MutableLiveData<>();
            empty.setValue(Collections.emptyList());
            return empty;
        }
        return repository.searchTasksWithTags(n);
    }

    private LiveData<List<Board>> boardsForQuery(String q) {
        String n = normalize(q);
        if (n.isEmpty()) {
            MutableLiveData<List<Board>> empty = new MutableLiveData<>();
            empty.setValue(Collections.emptyList());
            return empty;
        }
        return repository.searchBoards(n);
    }

    private LiveData<List<User>> usersForQuery(String q) {
        String n = normalize(q);
        if (n.isEmpty()) {
            MutableLiveData<List<User>> empty = new MutableLiveData<>();
            empty.setValue(Collections.emptyList());
            return empty;
        }
        return repository.searchUsers(n);
    }

    private LiveData<List<Tag>> tagsForQuery(String q) {
        String n = normalize(q);
        if (n.isEmpty()) {
            MutableLiveData<List<Tag>> empty = new MutableLiveData<>();
            empty.setValue(Collections.emptyList());
            return empty;
        }
        return repository.searchTags(n);
    }

    private static String normalize(String q) {
        if (q == null) return "";
        return q.trim();
    }

    public void setQuery(String value) {
        query.setValue(value == null ? "" : value);
    }

    public LiveData<String> getQuery() {
        return query;
    }

    public LiveData<List<TaskWithTags>> getTasks() {
        return tasks;
    }

    public LiveData<List<Board>> getBoards() {
        return boards;
    }

    public LiveData<List<User>> getUsers() {
        return users;
    }

    public LiveData<List<Tag>> getTags() {
        return tags;
    }
}
