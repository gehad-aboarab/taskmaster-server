package taskmaster.database;

import java.util.Properties;

public class DatabaseInfo {
    public String host;
    public String databaseName;
    public int port;
    public String user;
    public String password;

    public DatabaseInfo(Properties properties) {
        host = properties.getProperty("HOST");
        databaseName = properties.getProperty("DATABASE_NAME");
        String portString = properties.getProperty("PORT");
        try {
            port = Integer.valueOf(portString);
        } catch (NumberFormatException e) {
            System.out.println("DEBUG: bad value for PORT (" + portString + "), using default 3306");
            port = 3306;
        }
        user = properties.getProperty("USER");
        password = properties.getProperty("PASSWORD");
    }

}
