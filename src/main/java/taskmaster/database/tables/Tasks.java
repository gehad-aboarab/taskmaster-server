package taskmaster.database.tables;

import taskmaster.database.DatabaseManager;
import taskmaster.database.entities.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Tasks {

    public static List<Task> getByUserId(int userId) throws SQLException {
        return getByColumnName("user_id", userId);
    }

    public static List<Task> getByProjectId(int projectId) throws SQLException {
        return getByColumnName("project_id", projectId);
    }

    public static List<Task> getBySectionId(int sectionId) throws SQLException {
        return getByColumnName("section_id", sectionId);
    }

    public static Task getById(int taskId) throws SQLException {
        List<Task> tasks = getByColumnName("task_id", taskId);
        if (tasks.isEmpty())
            return null;
        return tasks.get(0);
    }

    private static List<Task> getByColumnName(String columnName, Object value) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        String queryString = "SELECT * from task WHERE " + columnName + " = ?";
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(queryString)) {
            preparedStatement.setObject(1, value);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int taskId = resultSet.getInt("task_id");
                String description = resultSet.getString("description");
                String name = resultSet.getString("name");
                Date date = resultSet.getDate("date");
                int projectId = resultSet.getInt("project_id");
                int sectionId = resultSet.getInt("section_id");
                Integer userId = resultSet.getInt("user_id");
                boolean completed = resultSet.getBoolean("completed");
                if (resultSet.wasNull())
                    userId = null;
                tasks.add(new Task(taskId, description, name, date, projectId, sectionId, userId, completed));
            }
        }

        return tasks;
    }

    public static Task createTask(int sectionId, String description, String name, Date date, Integer userId) throws SQLException {
        Integer projectId = Projects.getProjectIdBySectionId(sectionId);
        if (projectId == null)
            return null;
        String query = "INSERT INTO task " +
                "(name, description, date, project_id, section_id, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setDate(3, date);
            preparedStatement.setInt(4, projectId);
            preparedStatement.setInt(5, sectionId);
            if (userId == null) {
                preparedStatement.setNull(6, Types.INTEGER);
            } else {
                preparedStatement.setInt(6, userId);
            }

            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (!resultSet.next())
                throw new SQLException();
            int taskId = resultSet.getInt(1);
            return getById(taskId);
        }
    }

    public static Task editTask(int taskId, String description, String name, Date date, int sectionId, Integer userId) throws SQLException {
        String query = "UPDATE task\n" +
                "SET name = ?, " +
                "    description = ?, " +
                "    date = ?, " +
                "    section_id = ?, " +
                "    user_id = ? " +
                "WHERE task_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setDate(3, date);
            preparedStatement.setInt(4, sectionId);
            if (userId == null)
                preparedStatement.setNull(5, Types.INTEGER);
            else
                preparedStatement.setInt(5, userId);
            preparedStatement.setInt(6, taskId);

            int updatedRecords = preparedStatement.executeUpdate();
            if (updatedRecords != 1)
                return null;

            return getById(taskId);
        }
    }

    public static boolean deleteTask(int taskId) throws SQLException {
        String query = "DELETE FROM task " +
                "WHERE task_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, taskId);
            int updatedRecords = preparedStatement.executeUpdate();
            return updatedRecords == 1;
        }
    }

    public static int deleteBySectionId(int sectionId) throws SQLException {
        String query = "DELETE FROM task " +
                "WHERE section_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, sectionId);
            int updatedRecords = preparedStatement.executeUpdate();
            return updatedRecords;
        }
    }

    public static void main(String[] args) throws SQLException {
        int sectionId = 1;
        String description = "test task description";
        String name = "test task";
        String date = "2018-11-29";
        int userId = 2;
        Task task = createTask(sectionId, description, name, null, null);
        System.out.println(task);
    }

    public static boolean editStatus(int taskId, boolean completed) throws SQLException {
        System.out.printf("[Tasks] Editing task status, taskId = %d, completed = %b\n", taskId, completed);
        String query = "UPDATE task " +
                "SET completed = ? " +
                "WHERE task_id = ?";

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, completed ? 1 : 0);
            preparedStatement.setInt(2, taskId);
            System.out.println("[Tasks] Prepared statement successfully");
            int updatedRecords = preparedStatement.executeUpdate();

            System.out.println("[Tasks] updatedRecords = " + updatedRecords);
            return updatedRecords == 1;
        }
    }
}
