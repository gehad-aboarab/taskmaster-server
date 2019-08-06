package taskmaster.database.tables;

import com.google.gson.Gson;
import taskmaster.database.DatabaseManager;
import taskmaster.database.entities.Section;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Sections {

    public static Section getById(int id) throws SQLException {
        List<Section> sections = getByColumnName("section_id", id);
        if (sections.isEmpty())
            return null;
        return sections.get(0);
    }

    public static List<Section> getByProjectId(int projectId) throws SQLException {
        return getByColumnName("project_id", projectId);
    }

    public static List<Section> getByColumnName(String columnName, Object value) throws SQLException {
        List<Section> sections = new ArrayList<>();

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        String queryString = "SELECT * from section WHERE " + columnName + " = ?";
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(queryString)) {
            preparedStatement.setObject(1, value);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                int sectionId = resultSet.getInt("section_id");
                String name = resultSet.getString("name");
                String colour = resultSet.getString("colour");
                String description = resultSet.getString("description");
                int projectId = resultSet.getInt("project_id");

                sections.add(new Section(sectionId, name, colour, description, projectId));
            }
        }

        return sections;
    }

    public static Section addSection(String name, String colour, String description, int projectId) throws SQLException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        String queryString = "INSERT INTO section (`name`, `colour`, `description`, `project_id`) VALUES " +
                String.format("('%s', '%s', '%s', %d)", name, colour, description, projectId);
        System.out.println("Query string: " + queryString);
        try (Statement statement = databaseManager.createStatement()) {
            statement.executeUpdate(queryString, Statement.RETURN_GENERATED_KEYS);
            ResultSet resultSet = statement.getGeneratedKeys();
            resultSet.next();
            int sectionId = resultSet.getInt(1);
            return new Section(sectionId, name, colour, description, projectId);
        }
    }

    public static Section editSection(int sectionId, String name, String colour, String description, int projectId) throws SQLException {
        String query = "UPDATE section " +
                "SET name = ?, " +
                "    description = ?, " +
                "    colour = ? " +
                "WHERE section_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, description);
            preparedStatement.setString(3, colour);
            preparedStatement.setInt(4, sectionId);
            int updatedRecords = preparedStatement.executeUpdate();
            if (updatedRecords != 1)
                return null;

            return getById(sectionId);
        }
    }

    public static boolean isTaskIn(int taskId, int sectionId) throws SQLException {
        String query = "SELECT task_id, project_id " +
                "FROM task " +
                "WHERE task_id = ? " +
                "AND section_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, taskId);
            preparedStatement.setInt(2, sectionId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public static boolean deleteSection(int sectionId) throws SQLException {
        String query = "DELETE FROM section " +
                "WHERE section_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, sectionId);
            int updatedRecords = preparedStatement.executeUpdate();
            return updatedRecords == 1;
        }
    }

    public static void main(String[] args) throws SQLException {
        Section s = addSection("test", "test", "test", 1);
        System.out.println(new Gson().toJson(s));
    }
}
