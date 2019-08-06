package taskmaster.database.entities;

import taskmaster.database.DatabaseManager;
import taskmaster.database.tables.Projects;
import taskmaster.database.tables.Tasks;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {

    private int userId;
    private String name;
    private String email;
    private String password;

    public User(int userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, name, email, password);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId &&
                Objects.equals(name, user.name) &&
                Objects.equals(email, user.email) &&
                Objects.equals(password, user.password);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId:" + userId +
                ", name:\"" + name + '\"' +
                ", email:'" + email + '\"' +
                ", password:\"" + password + '\"' +
                '}';
    }

    public List<Project> fetchProjects() throws SQLException {
        List<Project> projects = new ArrayList<>();
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        PreparedStatement preparedStatement = databaseManager.prepareStatement("SELECT * FROM user_project WHERE user_id = ?");
        preparedStatement.setInt(1, userId);
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            int projectId = resultSet.getInt("project_id");
            projects.add(Projects.getById(projectId));
        }

        return projects;
    }

    public List<Task> fetchTasks() throws SQLException {
        return Tasks.getByUserId(userId);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {


        this.password = password;
    }

}
