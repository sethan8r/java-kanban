package service;

import interfaces.HistoryManager;
import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;
    private Task task;

    @BeforeEach
    void beforeEach() {
        historyManager = Managers.getDefaultHistory();
        task = new Task("Task", "Desc", 1, Status.NEW);
    }

    @Test
    void shouldSavePreviousVersionOfTask() {
        historyManager.add(task);
        Task updatedTask = new Task("TaskUpdated", "DescUpdated", 1, Status.DONE);
        historyManager.add(updatedTask);

        List<Integer> history = historyManager.getHistory();
        assertEquals(2, history.size(), "История должна хранить обе версии задачи");
        assertEquals(1, history.get(0), "ID первой версии должен сохраниться");
        assertEquals(1, history.get(1), "ID второй версии должен сохраниться");
    }
}
