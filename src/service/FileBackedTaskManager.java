package service;

import exception.ManagerSaveException;
import model.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.load();

        return manager;
    }

    private void load() {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            String line;

            while ((line = br.readLine()) != null && !line.isEmpty()) {
                Task task = fromString(line);
                int id = task.getId();

                if (id >= nextId) {
                    nextId = id + 1;
                }

                switch (task.getType()) {
                    case EPIC:
                        epics.put(id, (Epic) task);
                        break;
                    case SUBTASK:
                        Subtask subtask = (Subtask) task;
                        subtasks.put(id, subtask);
                        Epic epic = epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.getSubtasksId().add(id);
                        }
                        break;
                    case TASK:
                        tasks.put(id, task);
                        break;
                }
            }
        } catch (Exception e) {
            throw new ManagerSaveException("Ошибка загрузки из файла");
        }
    }

    protected void save() {
        try (Writer writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic,duration,startTime\n");

            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }

            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }

            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (Exception e) {
            throw new ManagerSaveException("Ошибка сохранения в файл");
        }
    }

    private String toString(Task task) {
        String epicId = "";
        String duration = "";
        String startTime = "";

        if (task.getType() == TaskType.SUBTASK) {
            epicId = String.valueOf(((Subtask) task).getEpicId());
        }

        if (task.getDuration() != null) {
            duration = String.valueOf(task.getDuration().toMinutes());
        }

        if (task.getStartTime() != null) {
            startTime = task.getStartTime().toString();
        }

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                task.getType(),
                task.getName(),
                task.getStatus(),
                task.getDescription(),
                epicId,
                duration,
                startTime);
    }

    private Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        String epicId = fields.length > 5 ? fields[5] : "";
        String durationStr = fields.length > 6 ? fields[6] : "";
        String startTimeStr = fields.length > 7 ? fields[7] : "";

        Duration duration = Duration.ZERO;
        if (!durationStr.isEmpty()) {
            duration = Duration.ofMinutes(Long.parseLong(durationStr));
        }

        LocalDateTime startTime = null;
        if (!startTimeStr.isEmpty()) {
            startTime = LocalDateTime.parse(startTimeStr);
        }

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status, duration, startTime);
                task.setId(id);

                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);

                return epic;
            case SUBTASK:
                int parentEpicId = Integer.parseInt(epicId);

                return new Subtask(name, description, id, status, parentEpicId, duration, startTime);
            default:

                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }


    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}
