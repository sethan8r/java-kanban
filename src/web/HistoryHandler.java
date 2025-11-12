package web;


import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) {
        if (!exchange.getRequestMethod().equals("GET") || !exchange.getRequestURI().getPath().equals("/history")) {
            try {
                sendServerError(exchange, "Некорректный путь или метод");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try {
            List<Task> history = manager.getHistory();
            sendText(exchange, gson.toJson(history), 200);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
