package service;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private int nextId = 1;

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
    }

    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.getSubtasksId().clear();
            updateEpicStatus(epic);
        }

        subtasks.clear();
    }

    public Task getTAskById(int id) {
        return tasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void createTask(Task task) {
        if(task == null) throw new IllegalArgumentException("Передан null объект");

        task.setId(nextId++);
        tasks.put(task.getId(), task);
    }

    public void createEpic(Epic epic) {
        if(epic == null) throw new IllegalArgumentException("Передан null объект");

        epic.setId(nextId++);
        epics.put(epic.getId(), epic);
    }

    public void createSubtask(Subtask subtask) {
        if(subtask == null) throw new IllegalArgumentException("Передан null объект");

        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);

        if (epic == null) throw new IllegalArgumentException("Объекта с ID " + epicId + " не существует");

        subtask.setId(nextId++);
        subtasks.put(subtask.getId(), subtask);

        epic.getSubtasksId().add(subtask.getId());
        updateEpicStatus(epic);
    }

    public void updateTask(Task task) {
        if(task == null) throw new IllegalArgumentException("Передан null объект");
        if(!tasks.containsKey(task.getId())) throw new IllegalArgumentException("Объекта с ID " + task.getId()
                + " не существует");

        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        if(epic == null) throw new IllegalArgumentException("Передан null объект");
        if(!epics.containsKey(epic.getId())) throw new IllegalArgumentException("Объекта с ID " + epic.getId()
                + " не существует");

        epics.put(epic.getId(), epic);
    }

    public void updateSubtask(Subtask subtask) {
        if(subtask == null) throw new IllegalArgumentException("Передан null объект");
        if(!subtasks.containsKey(subtask.getId())) throw new IllegalArgumentException("Объекта с ID " +
                subtask.getId() + " не существует");
        if(!epics.containsKey(subtask.getEpicId())) throw new IllegalArgumentException("Объекта с ID " +
                subtask.getEpicId() + " не существует");

        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        updateEpicStatus(epic);
    }

    public void deleteTaskById(int id) {
        if(!tasks.containsKey(id)) throw new IllegalArgumentException("Объекта с ID " + id
                + " не существует");

        tasks.remove(id);
    }

    public void deleteEpicById(int id) {
        if(!epics.containsKey(id)) throw new IllegalArgumentException("Объекта с ID " + id
                + " не существует");

        Epic epic = epics.get(id);
        List<Integer> subtaskId = epic.getSubtasksId();
        for(Integer sId : subtaskId) {
            subtasks.remove(sId);
        }
        epics.remove(id);
    }

    public void deleteSubtaskById(int id) {
        if(!subtasks.containsKey(id)) throw new IllegalArgumentException("Объекта с ID " + id
                + " не существует");

        Epic epic = epics.get(subtasks.get(id).getEpicId());
        epic.getSubtasksId().remove(id);
        updateEpicStatus(epic);

        subtasks.remove(id);
    }

    public List<Subtask> getSubtaskByEpicId(int id) {
        if(!epics.containsKey(id)) throw new IllegalArgumentException("Объекта с ID " + id
                + " не существует");

        Epic epic = epics.get(id);

        List<Subtask> result = new ArrayList<>();
        for (Integer sId : epic.getSubtasksId()) {
            result.add(subtasks.get(sId));
        }

        return result;
    }

    private void updateEpicStatus(Epic epic) {
        if(epic.getSubtasksId().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        List<Integer> subtaskIds = epic.getSubtasksId();

        boolean allNew = true;
        boolean allDone = true;

        for(int sId : subtaskIds) {
            Subtask subtask = subtasks.get(sId);
            Status status = subtask.getStatus();

            if(status != Status.DONE) allDone = false;

            if(status != Status.NEW) allNew = false;

            if(!allDone && !allNew) break;
        }

        if(allNew) epic.setStatus(Status.NEW);
        else if (allDone) epic.setStatus(Status.DONE);
        else epic.setStatus(Status.IN_PROGRESS);
    }
}
