package taskmaster.database.tables;

import taskmaster.database.DatabaseManager;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

public class UserTokens {

    public static String getToken(int userId) throws SQLException {
        String queryString = "SELECT * FROM user_token where user_id = ?";
        try (PreparedStatement preparedStatement = DatabaseManager.getInstance().prepareStatement(queryString)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("token");
            }

            return null;
        }
    }

    public static String issueToken(int userId) throws SQLException {
        String token = getToken(userId);
        if (token != null)
            return token;

        Random r = new SecureRandom();
        token = new BigInteger(130, r).toString(32);
        String updateString = "INSERT INTO user_token VALUES (?, ?)";
        PreparedStatement preparedStatement = DatabaseManager.getInstance().prepareStatement(updateString);
        preparedStatement.setInt(1, userId);
        preparedStatement.setString(2, token);
        preparedStatement.executeUpdate();
        return token;
    }

    public static Integer getUserId(String token) throws SQLException {
        System.out.println("[UserTokens] Getting user ID for token = " + token);
        String query = "SELECT user_id FROM user_token WHERE token = ?";
        System.out.println("[UserTokens] Query string: " + query);
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            System.out.println("[UserTokens] Preparing statement...");
            preparedStatement.setString(1, token);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                System.out.println("[UserTokens] No matching user found, returning");
                return null;
            }

            Integer retVal = resultSet.getInt("user_id");
            System.out.println("[UserTokens] Found match with user = " + retVal);
            return retVal;
        }
    }

}
