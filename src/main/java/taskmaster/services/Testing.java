package taskmaster.services;

import com.google.gson.Gson;
import taskmaster.database.entities.Project;
import taskmaster.database.entities.Section;
import taskmaster.database.entities.Task;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Testing {

    private static HttpURLConnection makePostConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("charset", "utf-8");
        return connection;
    }

    private static Project createProject(int userId, String name, String description, String token) throws IOException {
        String baseUrl = "http://localhost:8080/myapp";
        String servicePath = "/projects/create?token=" + token;

        String parameters = String.format("userId=%d&name=%s&description=%s", userId, name, description);

        URL url = new URL(baseUrl + servicePath);
        HttpURLConnection connection = makePostConnection(url);
        connection.setRequestProperty("Content-Length", "" + parameters.length());
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(parameters);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println("Something went wrong, the request was not OK...");
            return null;
        } else {
            Scanner scanner = new Scanner(connection.getInputStream());
            String projectJson = scanner.nextLine();
            return new Gson().fromJson(projectJson, Project.class);
        }
    }

    private static String makeParameterString(Map<String, Object> map) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            parts.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
        }

        return String.join("&", parts);
    }

    private static String makeParameterString(String[] keys, Object[] values) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%s=%s", keys[0], values[0]));
        for (int i = 1; i < keys.length; ++i) {
            if (values[i] != null && !values[i].toString().isEmpty())
                stringBuilder.append(String.format("&%s=%s", keys[i], values[i]));
        }

        return stringBuilder.toString();
    }

    public static Task addTask(int sectionId, String name, String description, String date, Integer userId, String token) throws IOException {
        String baseUrl = "http://localhost:8080/myapp";
        String servicePath = "/sections/" + sectionId + "/tasks?token=" + token;

        String[] keys = {"name", "description", "date", "userId"};
        Object[] values = {name, description, date, userId};
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keys.length; ++i) {
            if (values[i] != null)
                map.put(keys[i], values[i]);
        }

        //String parameters = String.format("userId=%d&name=%s&description=%s", userId, name, description);

        String parameters = makeParameterString(map);

        URL url = new URL(baseUrl + servicePath);
        HttpURLConnection connection = makePostConnection(url);
        connection.setRequestProperty("Content-Length", "" + parameters.length());
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(parameters);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println("Something went wrong, the request was not OK...");
            return null;
        } else {
            Scanner scanner = new Scanner(connection.getInputStream());
            String taskJson = scanner.nextLine();
            return new Gson().fromJson(taskJson, Task.class);
        }
    }

    public static Section addSection(int projectId, String name, String colour, String description, String token) throws IOException {
        String baseUrl = "http://ec2-18-220-107-100.us-east-2.compute.amazonaws.com:8080/myapp";
        String servicePath = "/projects/" + projectId + "/sections?token=" + token;
        URL url = new URL(baseUrl + servicePath);
        HttpURLConnection connection = makePostConnection(url);
        String[] keys = {"name", "colour", "description"};
        Object[] values = {name, colour, description};

        String parameterString = makeParameterString(keys, values);
        connection.setRequestProperty("Content-Length", parameterString);
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
            writer.write(parameterString);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            System.out.println("Something went wrong; handle the error here...");
            return null;
        } else {
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            Section section = new Gson().fromJson(reader, Section.class);
            // do something with section
            return section;
        }
    }

    public static void main(String[] args) throws Exception {
        int projectId = 1;
        String name = "new section";
        String colour = "#800080";
        String description = "new section description";
        String token = "30rdcv2e9cfs0avdo1cvfqrv9o";

        Section section = addSection(projectId, name, colour, description, token);
        System.out.println(section);
    }
}
