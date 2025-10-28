package service;

import exception.NotFoundException;
import interfaces.HistoryManager;
import interfaces.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    protected final Set<Task> sortedTaskByTime =
            new TreeSet<>(Comparator.comparing(Task::getStartTime));


    protected int nextId = 1;

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        for (Task t : tasks.values()) {
            sortedTaskByTime.remove(t);
        }

        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            for (Integer sId : epic.getSubtasksId()) {
                Task sub = subtasks.get(sId);
                sortedTaskByTime.remove(sub);
            }
        }

        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask s : subtasks.values()) {
            sortedTaskByTime.remove(s);
        }

        for (Epic epic : epics.values()) {
            epic.getSubtasksId().clear();
            updateEpicStatus(epic);
            epic.updateTimeAndDuration(new ArrayList<>());
        }
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);

        if (task != null) historyManager.add(task);

        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);

        if (epic != null) historyManager.add(epic);

        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);

        if (subtask != null) historyManager.add(subtask);

        return subtask;
    }

    @Override
    public void createTask(Task task) {
        if (task == null) throw new IllegalArgumentException("Передан null объект");
        if (isTimeOverlap(task)) throw new IllegalArgumentException("Задачи пересекаются по времени");

        task.setId(nextId++);
        tasks.put(task.getId(), task);

        if (task.getStartTime() != null) {
            sortedTaskByTime.add(task);
        }
    }

    @Override
    public void createEpic(Epic epic) {
        if (epic == null) throw new IllegalArgumentException("Передан null объект");

        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubtask(Subtask subtask) {
        if (subtask == null) throw new IllegalArgumentException("Передан null объект");

        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);

        if (epic == null) throw new NotFoundException("Объекта с ID " + epicId + " не существует");
        if (isTimeOverlap(subtask)) throw new IllegalArgumentException("Задачи пересекаются по времени");

        if (subtask.getId() == epicId) {
            throw new IllegalArgumentException("Подзадача не может иметь тот же ID, что и её эпик");
        }

        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);

        if (subtask.getStartTime() != null) {
            sortedTaskByTime.add(subtask);
        }

        epic.getSubtasksId().add(subtask.getId());
        epic.updateTimeAndDuration(getSubtaskByEpicId(epicId));
        updateEpicStatus(epic);
    }

    @Override
    public void updateTask(Task task) {
        if (task == null) throw new IllegalArgumentException("Передан null объект");
        if (!tasks.containsKey(task.getId())) throw new NotFoundException("Объекта с ID " + task.getId()
                + " не существует");
        if (isTimeOverlap(task)) throw new IllegalArgumentException("Задачи пересекаются по времени");

        sortedTaskByTime.remove(tasks.get(task.getId()));
        tasks.put(task.getId(), task);

        if (task.getStartTime() != null) {
            sortedTaskByTime.add(task);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epic == null) throw new IllegalArgumentException("Передан null объект");
        if (!epics.containsKey(epic.getId())) throw new NotFoundException("Объекта с ID " + epic.getId()
                + " не существует");

        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask == null) throw new IllegalArgumentException("Передан null объект");
        if (!subtasks.containsKey(subtask.getId())) throw new NotFoundException("Объекта с ID " +
                subtask.getId() + " не существует");
        if (!epics.containsKey(subtask.getEpicId())) throw new NotFoundException("Объекта с ID " +
                subtask.getEpicId() + " не существует");
        if (isTimeOverlap(subtask)) throw new IllegalArgumentException("Задачи пересекаются по времени");

        sortedTaskByTime.remove(subtasks.get(subtask.getId()));
        subtasks.put(subtask.getId(), subtask);

        if (subtask.getStartTime() != null) {
            sortedTaskByTime.add(subtask);
        }

        Epic epic = epics.get(subtask.getEpicId());
        epic.updateTimeAndDuration(getSubtaskByEpicId(epic.getId()));
        updateEpicStatus(epic);
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.get(id);

        if (!tasks.containsKey(id)) throw new NotFoundException("Объекта с ID " + id
                + " не существует");

        sortedTaskByTime.remove(task);
        tasks.remove(id);

        historyManager.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        if (!epics.containsKey(id)) throw new NotFoundException("Объекта с ID " + id
                + " не существует");

        Epic epic = epics.get(id);
        List<Integer> subtaskId = epic.getSubtasksId();
        for (Integer sId : subtaskId) {
            Task sub = subtasks.get(sId);
            subtasks.remove(sId);
            sortedTaskByTime.remove(sub);
            historyManager.remove(sId);
        }
        epics.remove(id);

        historyManager.remove(id);
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask sub = subtasks.get(id);

        if (!subtasks.containsKey(id)) throw new NotFoundException("Объекта с ID " + id
                + " не существует");

        sortedTaskByTime.remove(sub);
        Epic epic = epics.get(subtasks.get(id).getEpicId());
        epic.getSubtasksId().remove((Integer) id);
        epic.updateTimeAndDuration(getSubtaskByEpicId(epic.getId()));
        updateEpicStatus(epic);

        subtasks.remove(id);

        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getSubtaskByEpicId(int id) {
        if (!epics.containsKey(id)) throw new NotFoundException("Объекта с ID " + id
                + " не существует");

        Epic epic = epics.get(id);

        List<Subtask> result = new ArrayList<>();
        for (Integer sId : epic.getSubtasksId()) {
            result.add(subtasks.get(sId));
        }

        return result;
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        if (epic.getSubtasksId().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        List<Integer> subtaskIds = epic.getSubtasksId();

        boolean allNew = true;
        boolean allDone = true;

        for (int sId : subtaskIds) {
            Subtask subtask = subtasks.get(sId);
            Status status = subtask.getStatus();

            if (status != Status.DONE) allDone = false;

            if (status != Status.NEW) allNew = false;

            if (!allDone && !allNew) break;
        }

        if (allNew) epic.setStatus(Status.NEW);
        else if (allDone) epic.setStatus(Status.DONE);
        else epic.setStatus(Status.IN_PROGRESS);
    }

    @Override
    public List<Task> getHistory() {

        return historyManager.getHistory();
    }
    protected boolean isTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null) return false;

        return sortedTaskByTime.stream()
                .filter(t -> t.getStartTime() != null && !t.equals(newTask))
                .anyMatch(t -> !(newTask.getEndTime().isBefore(t.getStartTime())
                        || newTask.getStartTime().isAfter(t.getEndTime())));
    }
}
