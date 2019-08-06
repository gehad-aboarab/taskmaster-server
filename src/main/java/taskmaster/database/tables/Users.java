package taskmaster.database.tables;

import com.google.gson.Gson;
import taskmaster.database.DatabaseManager;
import taskmaster.database.entities.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Users {

    public static User getUserById(int id) throws SQLException {
        DatabaseManager manager = DatabaseManager.getInstance();
        try (PreparedStatement statement = manager.prepareStatement("SELECT * FROM user WHERE user_id = ?")) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                return new User(id, name, email, password);
            }

            return null;
        }
    }

    public static User getByEmail(String email) throws SQLException {
        DatabaseManager manager = DatabaseManager.getInstance();
        try (PreparedStatement statement = manager.prepareStatement("SELECT * FROM user WHERE email = ?")) {
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                return null;

            int id = resultSet.getInt("user_id");
            String name = resultSet.getString("name");
            String password = resultSet.getString("password");

            return new User(id, name, email, password);
        }
    }

    public static User getByAccount(String email, String password) throws SQLException {
        DatabaseManager manager = DatabaseManager.getInstance();
        try (PreparedStatement statement = manager.prepareStatement("SELECT * FROM user WHERE email = ? AND password = ?")) {
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next())
                return null;

            int id = resultSet.getInt("user_id");
            String name = resultSet.getString("name");
            return new User(id, name, email, password);
        }
    }

    public static boolean validateUser(String email, String password) throws SQLException {
        return getByAccount(email, password) != null;
    }

    public static User register(String name, String email, String password) throws SQLException {
        if (getByEmail(email) != null)
            return null;
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        String queryString = "INSERT INTO user (name, email, password) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(queryString)) {
            User user;
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, password);
            int updatedRows = preparedStatement.executeUpdate();
            if (updatedRows == 0)
                return null;
            return getByEmail(email);
        }
    }

    public static boolean exists(int userId) throws SQLException {
        String query = "SELECT * FROM user WHERE user_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();

        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    public static void main(String[] args) throws SQLException {
        User user = register("omar", "b00062014@aus.edu", "3");
        if (user != null) {
            System.out.println("bad");
            return;
        }

        user = register("omar", "a@b", "1");
        System.out.println(new Gson().toJson(user));
    }
}
