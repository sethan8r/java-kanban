package storage;

import model.Task;

public class Node {
    private Task item;
    private Node next;
    private Node last;

    public Node(Task item, Node next, Node last) {
        this.item = item;
        this.next = next;
        this.last = last;
    }

    public Task getItem() {
        return item;
    }

    public void setItem(Task item) {
        this.item = item;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getLast() {
        return last;
    }

    public void setLast(Node last) {
        this.last = last;
    }
}