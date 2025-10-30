package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    void saveAndLoadEmptyFileTest() {
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty());
        assertTrue(loadedManager.getAllEpics().isEmpty());
        assertTrue(loadedManager.getAllSubtasks().isEmpty());
    }

    @Test
    void saveMultipleTasksTest() {
        Task task = new Task("Task1", "Description");
        Epic epic = new Epic("Epic1", "Description");
        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(new Subtask("Subtask1", "Description", epic.getId()));

        manager.save();

        assertTrue(tempFile.length() > 0);

        try {
            String content = Files.readString(tempFile.toPath());
            assertTrue(content.contains("Task1"));
            assertTrue(content.contains("Epic1"));
            assertTrue(content.contains("Subtask1"));
            assertTrue(content.contains("TASK"));
            assertTrue(content.contains("EPIC"));
            assertTrue(content.contains("SUBTASK"));
        } catch (IOException e) {
            fail("Ошибка чтения файла");
        }
    }

    @Test
    void loadMultipleTasksTest() {
        Task task = new Task("Task1", "Description1");
        Epic epic = new Epic("Epic1", "Description");
        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubtask(new Subtask("Subtask1", "Description", epic.getId()));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        Task loadedTask = loadedManager.getAllTasks().getFirst();
        assertEquals("Task1", loadedTask.getName());
        assertEquals(Status.NEW, loadedTask.getStatus());

        Epic loadedEpic = loadedManager.getAllEpics().getFirst();
        assertEquals("Epic1", loadedEpic.getName());

        Subtask loadedSubtask = loadedManager.getAllSubtasks().getFirst();
        assertEquals("Subtask1", loadedSubtask.getName());
        assertEquals(loadedEpic.getId(), loadedSubtask.getEpicId());
    }

    @Test
    void saveToInvalidFileShouldThrowException() {
        File invalidFile = new File("/invalid/path/tasks.csv");
        FileBackedTaskManager invalidManager = new FileBackedTaskManager(invalidFile);

        Task task = new Task("Task", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());

        assertThrows(Exception.class, () -> {
            invalidManager.createTask(task);
        }, "Должно быть исключение при сохранении в невалидный файл");
    }

    @Test
    void loadFromFileShouldWorkWithoutException() {
        Task task = new Task("Task", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        manager.createTask(task);

        assertDoesNotThrow(() -> {
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        }, "Загрузка из файла не должна вызывать исключение");
    }

    @Test
    void loadFromEmptyFileShouldWork() {
        assertDoesNotThrow(() -> {
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        }, "Загрузка из пустого файла не должна вызывать исключение");
    }

    @Test
    void saveAndLoadTasksCorrectly() {
        Task task = new Task("Task", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now());
        Epic epic = new Epic("Epic", "Description");

        manager.createTask(task);
        manager.createEpic(epic);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertNotNull(loadedManager.getTaskById(task.getId()), "Задача должна загрузиться из файла");
        assertNotNull(loadedManager.getEpicById(epic.getId()), "Эпик должен загрузиться из файла");
    }
}
