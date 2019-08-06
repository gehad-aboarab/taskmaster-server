package taskmaster.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {

    private static final String PATH = System.getProperty("user.home") + "/.taskmaster";
    private static final String PROPERTIES_FILE = ".properties";

    private Properties properties;

    public PropertiesUtil() {
        Properties defaults = new Properties();
        String[] keys = {"HOST", "DATABASE_NAME", "PORT", "USER", "PASSWORD"};
        String[] values = {"localhost", "taskmaster", "3306", "root", "root"};
        for (int i = 0; i < keys.length; ++i)
            defaults.setProperty(keys[i], values[i]);
        properties = new Properties(defaults);
        try (FileInputStream fileInputStream = new FileInputStream(PATH + "/" + PROPERTIES_FILE)) {
            properties.load(fileInputStream);
        } catch (FileNotFoundException e) {
            System.out.println("DEBUG: no properties file found, default values will be used");
            System.out.println("DEBUG: looking for file: " + PATH + "/" + PROPERTIES_FILE);
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        Properties p = new PropertiesUtil().getProperties();
        p.list(System.out);
    }

    public Properties getProperties() {
        return properties;
    }

}
