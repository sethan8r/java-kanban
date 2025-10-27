package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Epic extends Task {
    private List<Integer> subtasksId = new ArrayList<>();
    private Duration duration = Duration.ZERO;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
    }

    public Epic(String name, String description, int id) {
        super(name, description, id, Status.NEW, Duration.ZERO, null);
    }

    public List<Integer> getSubtasksId() {
        return subtasksId;
    }

    public void setSubtasksId(List<Integer> subtasksId) {
        this.subtasksId = subtasksId;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void updateTimeAndDuration(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            duration = Duration.ZERO;
            startTime = null;
            endTime = null;
            return;
        }

        duration = Duration.ZERO;
        startTime = null;
        endTime = null;

        for (Subtask s : subtasks) {
            if (s.getStartTime() != null) {
                if (startTime == null || s.getStartTime().isBefore(startTime)) startTime = s.getStartTime();
                LocalDateTime taskEnd = s.getEndTime();
                if (endTime == null || (taskEnd != null && taskEnd.isAfter(endTime))) endTime = taskEnd;
            }
            duration = duration.plus(s.getDuration() != null ? s.getDuration() : Duration.ZERO);
        }
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public String toString() {
        return "Entity.Epic{" +
                "subtasksId=" + subtasksId +
                "} " + super.toString();
    }
}
