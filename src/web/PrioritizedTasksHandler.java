package web;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedTasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public PrioritizedTasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (!exchange.getRequestMethod().equals("GET") || !exchange.getRequestURI().getPath().equals("/prioritized")) {
            try {
                sendServerError(exchange, "Некорректный путь или метод");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            List<Task> tasks = manager.getAllTasks();
            tasks.sort((a, b) -> {
                if (a.getStartTime() == null) return 1;
                if (b.getStartTime() == null) return -1;
                return a.getStartTime().compareTo(b.getStartTime());
            });
            sendText(exchange, gson.toJson(tasks), 200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
