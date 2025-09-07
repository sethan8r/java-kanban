package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubtaskTest {

    @Test
    void EpicWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask("Name2", "Description2", 1);
        Subtask subtask2 = new Subtask("Name2", "Description2", 1);

        assertEquals(subtask1, subtask2, "Задачи с одинаковым ID должны быть равны.");
    }

    @Test
    void EpicWithSameIdShouldBeHashCode() {
        Subtask subtask1 = new Subtask("Name2", "Description2", 1);
        Subtask subtask2 = new Subtask("Name2", "Description2", 1);

        assertEquals(subtask1.hashCode(), subtask2.hashCode(), "Задачи с одинаковым ID должны быть равны.");
    }

    @Test
    void subtaskShouldReturnCorrectEpicId() {
        int epicId = 5;
        Subtask subtask = new Subtask("Name", "Description", epicId);
        assertEquals(epicId, subtask.getEpicId(),
                "Подзадача должна возвращать корректный ID эпика.");
    }


}
