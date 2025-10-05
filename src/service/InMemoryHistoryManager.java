package service;

import interfaces.HistoryManager;
import model.Task;
import storage.Node;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> history = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) return;

        int id = task.getId();

        remove(id);

        linkLast(task);

        history.put(id, tail);
    }

    @Override
    public void remove(int id) {
        Node node = history.get(id);

        if (node != null) {
            removeNode(node);

            history.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        Node newNode = new Node(task, null, tail);

        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
    }

    private void removeNode(Node node) {
        if (node == null) return;

        Node lastNode = node.getLast();
        Node nextNode = node.getNext();

        if (lastNode != null) {
            lastNode.setNext(nextNode);
        } else {
            head = nextNode;
        }

        if (nextNode != null) {
            nextNode.setLast(lastNode);
        } else {
            tail = lastNode;
        }
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();

        Node current = head;

        while (current != null) {
            tasks.add(current.getItem());
            current = current.getNext();
        }

        return tasks;
    }
}
