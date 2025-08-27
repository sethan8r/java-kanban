package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Integer> subtasksId = new ArrayList<>();

    public Epic(String name, String description, int id, Status status) {
        super(name, description, id, Status.NEW);
    }

    public Epic(String name, String description) {
        super(name, description);
    }

    public List<Integer> getSubtasksId() {
        return subtasksId;
    }

    public void setSubtasksId(List<Integer> subtasksId) {
        this.subtasksId = subtasksId;
    }

    @Override
    public String toString() {
        return "Entity.Epic{" +
                "subtasksId=" + subtasksId +
                "} " + super.toString();
    }
}
