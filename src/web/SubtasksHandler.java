package web;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.TaskManager;
import model.Subtask;

import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public SubtasksHandler(TaskManager manager, Gson gson) {
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
        if (path.matches("/subtasks/\\d+")) {
            int id = Integer.parseInt(path.substring("/subtasks/".length()));
            Subtask subtask = manager.getSubtaskById(id);
            if (subtask != null) {
                sendText(exchange, gson.toJson(subtask), 200);
            } else {
                sendNotFound(exchange, "Подзадача не найдена");
            }
        } else if (path.equals("/subtasks")) {
            List<Subtask> subtasks = manager.getAllSubtasks();
            sendText(exchange, gson.toJson(subtasks), 200);
        } else if (path.matches("/epics/\\d+/subtasks")) {
            int epicId = Integer.parseInt(path.split("/")[2]);
            List<Subtask> subtasks = manager.getSubtaskByEpicId(epicId);
            sendText(exchange, gson.toJson(subtasks), 200);
        } else {
            sendNotFound(exchange, "Некорректный путь");
        }
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!path.equals("/subtasks")) {
            sendNotFound(exchange, "Некорректный путь");
            return;
        }

        String body = readText(exchange);
        Subtask subtask = gson.fromJson(body, Subtask.class);

        try {
            if (subtask.getId() == 0) {
                manager.createSubtask(subtask);
                sendText(exchange, "Подзадача создана", 201);
            } else {
                manager.updateSubtask(subtask);
                sendText(exchange, "Подзадача обновлена", 201);
            }
        } catch (IllegalArgumentException e) {
            sendHasInteractions(exchange, e.getMessage());
        } catch (Exception e) {
            sendServerError(exchange, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/subtasks/\\d+")) {
            int id = Integer.parseInt(path.substring("/subtasks/".length()));
            try {
                manager.deleteSubtaskById(id);
                sendText(exchange, "Подзадача удалена", 200);
            } catch (Exception e) {
                sendNotFound(exchange, "Подзадача не найдена");
            }
        } else if (path.equals("/subtasks")) {
            manager.deleteAllSubtasks();
            sendText(exchange, "Все подзадачи удалены", 200);
        } else {
            sendNotFound(exchange, "Некорректный путь");
        }
    }
}
