package service;

import interfaces.HistoryManager;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Integer> history = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 10;

    @Override
    public void add(Task task) {
        if (task == null) return;

        history.addLast(task.getId());

        if(history.size() > MAX_HISTORY_SIZE) history.removeFirst();
    }

    @Override
    public List<Integer> getHistory() {

        return new ArrayList<>(history);
    }
}
