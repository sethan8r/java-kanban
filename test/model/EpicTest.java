package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void EpicWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Name2", "Description2", 1);
        Epic epic2 = new Epic("Name2", "Description2", 1);

        assertEquals(epic1, epic2, "Задачи с одинаковым ID должны быть равны.");
    }

    @Test
    void EpicWithSameIdShouldBeHashCode() {
        Epic epic1 = new Epic("Name2", "Description2", 1);
        Epic epic2 = new Epic("Name2", "Description2", 1);

        assertEquals(epic1.hashCode(), epic2.hashCode(), "Задачи с одинаковым ID должны быть равны.");
    }

    @Test
    void EpicShouldNotBeEqualToNull() {
        Epic epic = new Epic("Name", "Description", 1);
        assertNotEquals(null, epic, "Эпик не должна быть равна null.");
    }

    @Test
    void newEpicShouldHaveNewStatus() {
        Epic epic = new Epic("Name", "Description");
        assertEquals(Status.NEW, epic.getStatus(), "Новый эпик должен иметь статус NEW.");
    }
}