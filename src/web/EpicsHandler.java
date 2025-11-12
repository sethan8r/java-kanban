package web;


import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.TaskManager;
import model.Epic;
import model.Subtask;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager, Gson gson) {
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
                sendServerError(exchange, e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleGet(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+/subtasks")) {
            int epicId = Integer.parseInt(path.split("/")[2]);
            List<Subtask> subtasks = manager.getSubtaskByEpicId(epicId);
            sendText(exchange, gson.toJson(subtasks), 200);
        } else if (path.matches("/epics/\\d+")) {
            int id = Integer.parseInt(path.substring("/epics/".length()));
            Epic epic = manager.getEpicById(id);
            if (epic != null) {
                sendText(exchange, gson.toJson(epic), 200);
            } else {
                sendNotFound(exchange, "Эпик не найден");
            }
        } else if (path.equals("/epics")) {
            List<Epic> epics = manager.getAllEpics();
            sendText(exchange, gson.toJson(epics), 200);
        } else {
            sendNotFound(exchange, "Некорректный путь");
        }
    }

    private void handlePost(HttpExchange exchange, String path) throws IOException {
        if (!path.equals("/epics")) {
            sendNotFound(exchange, "Некорректный путь");
            return;
        }

        String body = readText(exchange);
        Epic epic = gson.fromJson(body, Epic.class);

        try {
            if (epic.getId() == 0) {
                manager.createEpic(epic);
                sendText(exchange, "Эпик создан", 201);
            } else {
                manager.updateEpic(epic);
                sendText(exchange, "Эпик обновлен", 201);
            }
        } catch (Exception e) {
            sendServerError(exchange, e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/epics/\\d+")) {
            int id = Integer.parseInt(path.substring("/epics/".length()));
            try {
                manager.deleteEpicById(id);
                sendText(exchange, "Эпик удален", 200);
            } catch (Exception e) {
                sendNotFound(exchange, "Эпик не найден");
            }
        } else if (path.equals("/epics")) {
            manager.deleteAllEpics();
            sendText(exchange, "Все эпики удалены", 200);
        } else {
            sendNotFound(exchange, "Некорректный путь");
        }
    }
}
