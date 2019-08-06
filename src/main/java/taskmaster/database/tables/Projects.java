package taskmaster.database.tables;

import taskmaster.database.DatabaseManager;
import taskmaster.database.entities.Project;
import taskmaster.database.entities.Section;
import taskmaster.database.entities.Task;
import taskmaster.database.entities.User;

import javax.validation.constraints.NotNull;
import javax.xml.crypto.Data;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Projects {

    public static Project getById(int id) throws SQLException {
        DatabaseManager manager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = manager.prepareStatement("SELECT * FROM project WHERE project_id = ?")) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;

            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            return new Project(id, name, description);
        }
    }

    private static Project retrieveProjectInfo(int id) throws SQLException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        String query = "SELECT * FROM project WHERE project_id = ?";
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;

            String name = resultSet.getString("name");
            String description = resultSet.getString("description");
            return new Project(id, name, description);
        }
    }

    public static Project getProjectById(int projectId) throws SQLException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        Project project = retrieveProjectInfo(projectId);
        if (project == null)
            return null;

        String query = "SELECT section.name AS section_name, " +
                "       colour, " +
                "       section.description AS section_description, " +
                "       task_id, " +
                "       task.description AS task_description, " +
                "       task.name AS task_name, " +
                "       task.completed AS completed," +
                "       date, " +
                "       project_id, " +
                "       section_id, " +
                "       user_id " +
                "FROM section LEFT JOIN task USING (section_id, project_id) " +
                "WHERE project_id = ? " +
                "ORDER BY section_id";

        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, projectId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<Integer, Section> map = new HashMap<>();
            while (resultSet.next()) {
                int sectionId = resultSet.getInt("section_id");
                String sectionName = resultSet.getString("section_name");
                String sectionDescription = resultSet.getString("section_description");
                String sectionColour = resultSet.getString("colour");
                if (!map.containsKey(sectionId)) {
                    Section newSection = new Section(sectionId, sectionName, sectionColour, sectionDescription, projectId);

                    map.put(sectionId, newSection);
                }

                int taskId = resultSet.getInt("task_id");
                if (resultSet.wasNull())
                    continue;

                String taskName = resultSet.getString("task_name");
                String taskDescription = resultSet.getString("task_description");
                boolean taskCompleted = resultSet.getBoolean("completed");
                Date date = resultSet.getDate("date");
                Integer userId = resultSet.getInt("user_id");
                if (resultSet.wasNull())
                    userId = null;

                Task task = new Task(taskId, taskDescription, taskName, date, project.getProjectId(), sectionId, userId, taskCompleted);

                map.get(sectionId).getTasks().add(task);
            }

            project.setSections(new ArrayList<>(map.values()));
            return project;
        }
    }

    public static List<Project> getByUserId(int userId) throws SQLException {
        String query = "select * from " +
                "              ((select project.project_id," +
                "                       project.name AS project_name, " +
                "                       project.description AS project_description " +
                "                   FROM user, user_project, project " +
                "                   WHERE (user.user_id = ? " +
                "                            AND user.user_id = user_project.user_id " +
                "                            AND project.project_id = user_project.project_id)) AS project_ids " +
                "                      LEFT JOIN " +
                "                      (SELECT section.name AS section_name, " +
                "                              colour, " +
                "                              section.description AS section_description, " +
                "                              task_id, " +
                "                              task.description AS task_description, " +
                "                              task.name AS task_name, " +
                "                              task.completed AS completed," +
                "                              date, " +
                "                              project_id, " +
                "                              section_id, " +
                "                              user_id " +
                "                       FROM section LEFT JOIN task USING (section_id, project_id) " +
                "                       ORDER BY section_id) AS project_info " +
                "                      USING (project_id))";

        List<Project> projects = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<Integer, Project> projectMap = new HashMap<>();
            Map<Integer, Section> sectionMap = new HashMap<>();
            while (resultSet.next()) {
                int projectId = resultSet.getInt("project_id");
                if (!projectMap.containsKey(projectId)) {
                    String projectName = resultSet.getString("project_name");
                    String projectDescription = resultSet.getString("project_description");
                    Project project = new Project(projectId, projectName, projectDescription);
                    projects.add(project);
                    projectMap.put(projectId, project);
                }

                int sectionId = resultSet.getInt("section_id");

                if (resultSet.wasNull())
                    continue;

                String sectionName = resultSet.getString("section_name");
                String sectionDescription = resultSet.getString("section_description");
                String sectionColour = resultSet.getString("colour");
                if (!sectionMap.containsKey(sectionId)) {
                    Section newSection = new Section(sectionId, sectionName, sectionColour, sectionDescription, projectId);
                    projectMap.get(projectId).getSections().add(newSection);
                    sectionMap.put(sectionId, newSection);
                }

                int taskId = resultSet.getInt("task_id");

                if (resultSet.wasNull())
                    continue;
                String taskName = resultSet.getString("task_name");
                String taskDescription = resultSet.getString("task_description");
                Date date = resultSet.getDate("date");
                Integer taskUserId = resultSet.getInt("user_id");
                if (resultSet.wasNull())
                    taskUserId = null;

                boolean taskCompleted = resultSet.getBoolean("completed");

                Task task = new Task(taskId, taskDescription, taskName, date, projectId, sectionId, taskUserId, taskCompleted);
                sectionMap.get(sectionId).getTasks().add(task);
            }
        }
        return projects;
    }

    public static boolean addUserToProject(int projectId, int userId) throws SQLException {
        System.out.printf("[Projects] Adding user to project, projectId = %d, userId = %d", projectId, userId);

        String query = "INSERT INTO user_project " +
                "(user_id, project_id) " +
                "VALUES (?, ?)";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, projectId);

            System.out.println("[Projects] Statement prepared successfully");

            int updatedRecords = preparedStatement.executeUpdate();

            if (updatedRecords == 1) {
                System.out.println("[Projects] Added user successfully");
                return true;
            } else {
                System.out.println("[Projects] Failed to add user");
                return false;
            }
        }
    }

    public static Project create(int userId, String name, String description) throws SQLException {
        String query = "INSERT INTO project " +
                "(name, description) " +
                "VALUES (?, ?)";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (!resultSet.next())
                return null;

            int projectId = resultSet.getInt(1);
            addUserToProject(projectId, userId);

            return getProjectById(projectId);
        }

    }

    public static boolean isUserIn(int userId, int projectId) throws SQLException {
        String query = "SELECT * " +
                "FROM user_project " +
                "WHERE user_id = ? " +
                "AND project_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, projectId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public static boolean isSectionIn(int sectionId, int projectId) throws SQLException {
        String query = "SELECT * " +
                "FROM section " +
                "WHERE section_id = ? " +
                "AND project_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, sectionId);
            preparedStatement.setInt(2, projectId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public static Integer getProjectIdBySectionId(int sectionId) throws SQLException {
        String query = "SELECT project_id FROM section WHERE section_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, sectionId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;
            return resultSet.getInt("project_id");
        }
    }

    public static List<User> getUsers(int projectId) throws SQLException {
        String query = "SELECT user.user_id, " +
                "       email, " +
                "       password, " +
                "       name " +
                "FROM user JOIN user_project " +
                "USING (user_id) " +
                "WHERE project_id = ?";
        List<User> users = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, projectId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("user_id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                User user = new User(id, name, email, password);
                users.add(user);
            }
        }
        return users;
    }

    public static void main(String[] args) throws SQLException {
        Integer id = getProjectIdBySectionId(3);
        System.out.println(id);
    }

    public static Project editProject(int projectId, String name, String description) throws SQLException {
        String query = "UPDATE project " +
                "SET name = ?, " +
                "    description = ? " +
                "WHERE project_id = ?;";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setInt(3, projectId);
            int updatedRecords = preparedStatement.executeUpdate();
            if (updatedRecords != 1)
                return null;
            return getProjectById(projectId);
        }
    }

    public static boolean deleteProject(int projectId) throws SQLException {
        System.out.println("[Projects] Deleting project with projectId = " + projectId);
        String query = "DELETE FROM project " +
                "WHERE project_id = ?";

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, projectId);
            System.out.println("[Projects] Prepared statement successfully...");
            int updatedRecords = preparedStatement.executeUpdate();
            System.out.println("[Projects] updatedRecords = " + updatedRecords);
            return updatedRecords == 1;
        }
    }

    public static boolean removeUser(int projectId, int userId) throws SQLException {
        String query = "DELETE FROM user_project " +
                "WHERE project_id = ? " +
                "AND user_id = ?";

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, projectId);
            preparedStatement.setInt(2, userId);

            int updatedRecords = preparedStatement.executeUpdate();

            return updatedRecords == 1;
        }
    }

}
