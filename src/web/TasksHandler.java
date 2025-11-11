package web;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, path);
                    break;
                case "POST":
                    handlePost(exchange, path);
                    break;
                case "DELETE":
                    handleDelete(exchange, path);
                    break;
                default:
                    sendServerError(exchange, "Метод не поддерживается");
            }
        } catch (Exception e) {
            try {
                sendServerError(exchange, "Ошибка сервера: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/tasks/\\d+")) {
            int id = Integer.parseInt(path.substring("/tasks/".length()));
            Task task = manager.getTaskById(id);
            if (task != null) {
                sendText(exchange, gson.toJson(task), 200);
            } else {
                sendNotFound(exchange, "Задача не найдена");
            }
        } else if (path.equals("/tasks")) {
            List<Task> tasks = manager.getAllTasks();
            sendText(exchange, gson.toJson(tasks), 200);
        } else {
            sendNotFound(exchange, "Некорректный путь");
        }
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!path.equals("/tasks")) {
            sendNotFound(exchange, "Некорректный путь");
            return;
        }

        String body = readText(exchange);
        Task task = gson.fromJson(body, Task.class);

        try {
            if (task.getId() == 0) {
                manager.createTask(task);
                sendText(exchange, "Задача создана", 201);
            } else {
                manager.updateTask(task);
                sendText(exchange, "Задача обновлена", 201);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerError(exchange, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/tasks/\\d+")) {
            int id = Integer.parseInt(path.substring("/tasks/".length()));
            try {
                manager.deleteTaskById(id);
                sendText(exchange, "Задача удалена", 200);
            } catch (Exception e) {
                sendNotFound(exchange, "Задача не найдена");
            }
        } else if (path.equals("/tasks")) {
            manager.deleteAllTasks();
            sendText(exchange, "Все задачи удалены", 200);
        } else {
            sendNotFound(exchange, "Некорректный путь");
        }
    }
}