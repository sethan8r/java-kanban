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
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void beforeEach() {
        historyManager = Managers.getDefaultHistory();
        task1 = new Task("Task", "Desc", 1, Status.NEW);
        task2 = new Task("Task2", "Desc2", 2, Status.IN_PROGRESS);
        task3 = new Task("Task3", "Desc3", 3, Status.DONE);
    }

    @Test
    void shouldRemoveDuplicatesAndKeepLastVersion() {
        historyManager.add(task1);

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
        historyManager.add(task1);

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

    @Test
    void emptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void duplicateTasksInHistory() {
        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.add(task1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Дубликаты не должны добавляться в историю");
        assertEquals(task2, history.get(0), "Первая задача должна быть task2");
        assertEquals(task1, history.get(1), "Вторая задача должна быть task1 (перемещена в конец)");
    }

    @Test
    void removeFromBeginning() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должно остаться 2 задачи");
        assertFalse(history.contains(task1), "Задача task1 должна быть удалена");
        assertEquals(task2, history.get(0), "Первой должна быть task2");
        assertEquals(task3, history.get(1), "Второй должна быть task3");
    }

    @Test
    void removeFromMiddle() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должно остаться 2 задачи");
        assertFalse(history.contains(task2), "Задача task2 должна быть удалена");
        assertEquals(task1, history.get(0), "Первой должна быть task1");
        assertEquals(task3, history.get(1), "Второй должна быть task3");
    }

    @Test
    void removeFromEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должно остаться 2 задачи");
        assertFalse(history.contains(task3), "Задача task3 должна быть удалена");
        assertEquals(task1, history.get(0), "Первой должна быть task1");
        assertEquals(task2, history.get(1), "Второй должна быть task2");
    }

    @Test
    void addAndGetHistory() {
        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "В истории должно быть 2 задачи");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task2, history.get(1), "Вторая задача не совпадает");
    }
}
