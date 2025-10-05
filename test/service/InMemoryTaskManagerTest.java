package service;

import interfaces.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {
    private TaskManager taskManager;
    private Epic epic;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();
        epic = new Epic("Epic", "Description");
        taskManager.createEpic(epic);
    }

    @Test
    void shouldCreateAndFindTaskById() {
        Task task = new Task("Task", "Description");
        taskManager.createTask(task);

        Task foundTask = taskManager.getTaskById(task.getId());
        assertNotNull(foundTask, "Задача должна находиться по ID");
        assertEquals(task, foundTask, "Найденная задача должна совпадать с созданной");
    }

    @Test
    void shouldCreateAndFindEpicById() {
        taskManager.createEpic(epic);

        Epic foundEpic = taskManager.getEpicById(epic.getId());
        assertNotNull(foundEpic, "Эпик должен находиться по ID");
        assertEquals(epic, foundEpic, "Найденный эпик должен совпадать с созданным");
    }

    @Test
    void shouldCreateAndFindSubtaskById() {
        taskManager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());
        taskManager.createSubtask(subtask);

        Subtask foundSubtask = taskManager.getSubtaskById(subtask.getId());
        assertNotNull(foundSubtask, "Подзадача должна находиться по ID");
        assertEquals(subtask, foundSubtask, "Найденная подзадача должна совпадать с созданной");
    }

    @Test
    void shouldNotAllowSubtaskWithSameIdAsEpic() {
        Subtask subtask = new Subtask("Subtask", "Description", epic.getId(), Status.NEW, epic.getId());

        assertThrows(IllegalArgumentException.class, () -> taskManager.createSubtask(subtask),
                "Не должно быть возможности создать подзадачу с ID равным ID ее эпика");
    }

    @Test
    void managersShouldReturnInitializedInstances() {
        TaskManager manager = Managers.getDefault();
        assertNotNull(manager, "Менеджер задач не должен быть null");

        Task task = new Task("Test", "Description");
        manager.createTask(task);
        assertNotNull(manager.getTaskById(task.getId()), "Менеджер должен быть готов к работе");
    }

    @Test
    void shouldAddAndFindDifferentTaskTypes() {
        Task task = new Task("Task", "Description");
        taskManager.createTask(task);
        assertNotNull(taskManager.getTaskById(task.getId()), "Задача должна находиться по ID");


    }

    @Test
    void epicShouldNotContainItselfAsSubtask() {
        taskManager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Description", epic.getId());
        subtask.setId(epic.getId());

        assertThrows(IllegalArgumentException.class,
                () -> taskManager.createSubtask(subtask),
                "Эпик не должен содержать сам себя как подзадачу");
    }

    @Test
    void generatedIdsShouldNotConflict() {
        Task task1 = new Task("Task 1", "Desc 1");
        Task task2 = new Task("Task 2", "Desc 2");

        taskManager.createTask(task1);
        taskManager.createTask(task2);

        assertNotEquals(task1.getId(), task2.getId(), "Менеджер должен выдавать уникальные id для разных задач");

        assertNotNull(taskManager.getTaskById(task1.getId()), "Задача task1 должна храниться в менеджере");
        assertNotNull(taskManager.getTaskById(task2.getId()), "Задача task2 должна храниться в менеджере");
    }

    @Test
    void taskShouldNotChangeAfterAddingToManager() {
        Task task = new Task("Immutable", "Desc", 1, Status.NEW);
        taskManager.createTask(task);

        Task storedTask = taskManager.getTaskById(task.getId());

        assertEquals(task.getName(), storedTask.getName(), "Имя задачи должно сохраняться");
        assertEquals(task.getDescription(), storedTask.getDescription(), "Описание задачи должно сохраняться");
        assertEquals(task.getStatus(), storedTask.getStatus(), "Статус задачи должен сохраняться");
        assertEquals(task.getId(), storedTask.getId(), "Id задачи должен совпадать");
    }

    @Test
    void shouldRemoveSubtaskIdsFromEpicWhenDeleted() {
        Subtask subtask1 = new Subtask("Sub1", "Desc1", epic.getId());
        Subtask subtask2 = new Subtask("Sub2", "Desc2", epic.getId());

        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);

        assertEquals(2, taskManager.getSubtaskByEpicId(epic.getId()).size());

        taskManager.deleteSubtaskById(subtask1.getId());

        List<Subtask> remainingSubtasks = taskManager.getSubtaskByEpicId(epic.getId());
        assertEquals(1, remainingSubtasks.size());
        assertEquals(subtask2.getId(), remainingSubtasks.getFirst().getId());

        assertNull(taskManager.getSubtaskById(subtask1.getId()), "Удаленная подзадача должна возвращать null");
    }

    @Test
    void shouldRemoveTaskFromHistoryWhenDeleted() {
        Task task = new Task("Name", "Desc", 1, Status.NEW);

        taskManager.createTask(task);
        taskManager.getTaskById(task.getId());

        assertEquals(1, taskManager.getHistory().size());

        taskManager.deleteTaskById(task.getId());

        assertTrue(taskManager.getHistory().isEmpty(), "История должна быть пустой после удаления задачи");
    }

    @Test
    void shouldUpdateEpicStatusWhenSubtaskChangedViaSetter() {
        Subtask subtask = new Subtask("Name", "Desc", epic.getId());
        taskManager.createSubtask(subtask);

        subtask.setStatus(Status.DONE);

        taskManager.updateSubtask(subtask);

        assertEquals(Status.DONE, epic.getStatus(), "Статус эпика должен обновиться");
    }


}