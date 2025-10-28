package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Epic extends Task {
    private List<Integer> subtasksId = new ArrayList<>();
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

    @Override
    public Duration getDuration() {
        return super.getDuration();
    }

    @Override
    public LocalDateTime getStartTime() {
        return super.getStartTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        return super.getEndTime();
    }

    public void updateTimeAndDuration(List<Subtask> subtasks) {
        if (subtasks.isEmpty()) {
            setDuration(Duration.ZERO);
            setStartTime(null);
            return;
        }

        Duration totalDuration = Duration.ZERO;
        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;

        for (Subtask s : subtasks) {
            if (s.getStartTime() != null) {
                if (earliestStart == null || s.getStartTime().isBefore(earliestStart)) {
                    earliestStart = s.getStartTime();
                }

                LocalDateTime taskEnd = s.getEndTime();
                if (taskEnd != null && (latestEnd == null || taskEnd.isAfter(latestEnd))) {
                    latestEnd = taskEnd;
                }
            }

            totalDuration = totalDuration.plus(
                    s.getDuration() != null ? s.getDuration() : Duration.ZERO
            );
        }

        setDuration(totalDuration);
        setStartTime(earliestStart);
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
