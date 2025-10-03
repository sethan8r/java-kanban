package model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Name1", "Description1", 1, Status.NEW);
        Task task2 = new Task("Name2", "Description2", 1, Status.NEW);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны.");
    }

    @Test
    void tasksWithSameIdShouldHaveSameHashCode() {
        Task task1 = new Task("Name1", "Description1", 1, Status.NEW);
        Task task2 = new Task("Name2", "Description2", 1, Status.DONE);

        assertEquals(task1.hashCode(), task2.hashCode(),
                "Задачи с одинаковым ID должны иметь одинаковый хэш-код.");
    }

    @Test
    void taskShouldNotBeEqualToNull() {
        Task task = new Task("Name", "Description", 1, Status.NEW);
        assertNotEquals(null, task, "Задача не должна быть равна null.");
    }
}
