package web;

import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTasksTest {

    private static HttpTaskServer server;
    private static HttpClient client;
    private static final String BASE_URL = "http://localhost:8080";
    InMemoryTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void testCreateTaskAndGetById() throws IOException, InterruptedException {
        String json = """
                    {
                        "name":"TestTask",
                        "description":"desc",
                        "status":"NEW",
                        "duration":10,
                        "startTime":"2025-11-10T10:00:00"
                    }
                """;
        HttpRequest post = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> respPost = client.send(post,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(201, respPost.statusCode());

        HttpRequest getAll = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();
        HttpResponse<String> respAll = client.send(getAll,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respAll.statusCode());
        assertTrue(respAll.body().contains("\"name\":\"TestTask\""));

        HttpRequest getById = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/1"))
                .GET()
                .build();
        HttpResponse<String> respById = client.send(getById,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respById.statusCode());
        assertTrue(respById.body().contains("\"name\":\"TestTask\""));
    }


    @Test
    void testDeleteAllTasks() throws IOException, InterruptedException {
        HttpRequest delAll = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .DELETE()
                .build();
        HttpResponse<String> respDel = client.send(delAll,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respDel.statusCode());

        HttpRequest getAll = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();
        HttpResponse<String> respAll = client.send(getAll,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, respAll.statusCode());
        assertEquals("[]", respAll.body());
    }

    @Test
    void testGetNonExistingTask() throws IOException, InterruptedException {
        HttpRequest getById = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks/999"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(getById,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(404, resp.statusCode());
    }

    @Test
    void testCreateOverlappingTask() throws IOException, InterruptedException {
        String json1 = """
                {
                    "name": "Task1",
                    "description": "desc",
                    "status": "NEW",
                    "duration": 10,
                    "startTime": "2025-11-10T12:00:00"
                }
                """;
        HttpRequest post1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json1))
                .header("Content-Type", "application/json")
                .build();
        client.send(post1, HttpResponse.BodyHandlers.ofString());

        String json2 = """
                {
                    "name": "Task2",
                    "description": "desc2",
                    "status": "NEW",
                    "duration": 10,
                    "startTime": "2025-11-10T12:05:00"
                }
                """;
        HttpRequest post2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json2))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> resp2 = client.send(post2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, resp2.statusCode());
    }

    @Test
    void testGetSubtasksByEpicId() throws IOException, InterruptedException {
        String epicJson = """
                {
                    "name":"Epic1",
                    "description":"desc",
                    "status":"NEW",
                    "subtasksId":[]
                }
                """;
        HttpRequest postEpic = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .header("Content-Type", "application/json")
                .build();
        client.send(postEpic, HttpResponse.BodyHandlers.ofString());

        String subJson = """
                {"name":"Sub1",
                "description":"desc",
                "status":"NEW",
                "epicId":1}
                """;
        HttpRequest postSub = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(subJson))
                .header("Content-Type", "application/json")
                .build();
        client.send(postSub, HttpResponse.BodyHandlers.ofString());

        HttpRequest getSubs = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/epics/1/subtasks"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(getSubs,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
        assertTrue(resp.body().contains("\"name\":\"Sub1\""));
    }

    @Test
    void testGetHistory() throws IOException, InterruptedException {
        HttpRequest getHistory = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/history"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(getHistory,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void testGetPrioritized() throws IOException, InterruptedException {
        HttpRequest getPrioritized = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/prioritized"))
                .GET()
                .build();
        HttpResponse<String> resp = client.send(getPrioritized,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, resp.statusCode());
    }

    @Test
    void testTaskSavedInManagerAfterCreation() throws IOException, InterruptedException {
        String taskJson = """
                {
                    "name": "Test Task",
                    "description": "Test Description",
                    "status": "NEW",
                    "duration": 30,
                    "startTime": "2025-11-10T10:00:00"
                }
                """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> createResponse = client.send(createRequest,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(201, createResponse.statusCode());

        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        assertEquals("Test Task", tasksFromManager.get(0).getName());
        assertEquals("Test Description", tasksFromManager.get(0).getDescription());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/tasks"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("Test Task"));
    }
}
