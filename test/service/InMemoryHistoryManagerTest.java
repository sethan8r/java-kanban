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
    void shouldRemoveDuplicatesAndKeepLastVersion() {
        historyManager.add(task);

        Task updatedTask = new Task("TaskUpdated", "DescUpdated", 1, Status.DONE);

        historyManager.add(updatedTask);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "Дубликаты должны удаляться");
        assertEquals("TaskUpdated", history.getFirst().getName(), "Должна сохраниться последняя версия");
        assertEquals(Status.DONE, history.getFirst().getStatus(), "Должна сохраниться последняя версия");
    }

    @Test
    void shouldMaintainInsertionOrder() {
        Task task1 = new Task("Task1", "Desc1", 1, Status.NEW);
        Task task2 = new Task("Task2", "Desc2", 2, Status.IN_PROGRESS);
        Task task3 = new Task("Task3", "Desc3", 3, Status.DONE);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size(), "Все задачи должны быть в истории");
        assertEquals(1, history.get(0).getId(), "Первая задача должна быть первой в истории");
        assertEquals(2, history.get(1).getId(), "Вторая задача должна быть второй в истории");
        assertEquals(3, history.get(2).getId(), "Третья задача должна быть третьей в истории");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        historyManager.add(task);

        assertEquals(1, historyManager.getHistory().size());

        historyManager.remove(1);

        assertTrue(historyManager.getHistory().isEmpty(), "История должна быть пустой после удаления");
    }

    @Test
    void shouldHandleEmptyHistory() {
        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "Новая история должна быть пустой");
    }

    @Test
    void shouldNotAddNullTask() {
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty(), "История должна остаться пустой после добавления null");
    }
}
