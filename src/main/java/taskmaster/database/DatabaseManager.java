package taskmaster.database;

import com.google.gson.Gson;
import taskmaster.database.entities.User;
import taskmaster.database.tables.UserTokens;
import taskmaster.database.tables.Users;

import java.sql.*;
import java.util.Scanner;

public class DatabaseManager {

    private static DatabaseManager databaseManager = null;
    private Connection connection;

    private DatabaseInfo databaseInfo = new DatabaseInfo(new PropertiesUtil().getProperties());

    private DatabaseManager() throws SQLException {
        initConnection();
    }

    public static void main(String[] args) throws SQLException {
        User user = Users.getByAccount("b00062014@aus.edu", "aSlgk5pr");
        String retVal = new Gson().toJson(user) + "\r\n" + UserTokens.issueToken(user.getUserId());
        Scanner scanner = new Scanner(retVal);
        String userString = scanner.nextLine();
        String token = scanner.nextLine();
        System.out.printf("userString: %s, token: %s\n", userString, token);
        System.out.println(new Gson().fromJson(userString, User.class));

        if (true)
            return;
    }

    private void initConnection() throws SQLException {
        String connectionUrl = String.format("jdbc:mysql://%s:%d/%s", databaseInfo.host, databaseInfo.port, databaseInfo.databaseName);
        connection = DriverManager.getConnection(connectionUrl, databaseInfo.user, databaseInfo.password);
        connection.setAutoCommit(true);
    }

    public ResultSet rawQuery(String query) throws SQLException {
        if (connection == null)
            initConnection();
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public PreparedStatement prepareStatement(String query) throws SQLException {
        if (connection == null || connection.isClosed())
            initConnection();
        return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    }

    public Statement createStatement() throws SQLException {
        if (connection == null || connection.isClosed())
            initConnection();
        return connection.createStatement();
    }

    public static DatabaseManager getInstance() throws SQLException {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
        }
        return databaseManager;
    }

}
