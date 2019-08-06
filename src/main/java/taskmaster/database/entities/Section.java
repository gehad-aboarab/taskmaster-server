package taskmaster.database.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Section {

    private int id;
    private String name;
    private String colour;
    private String description;
    private int project_id = -1;

    private List<Task> tasks = new ArrayList<>();

    public Section(int id, String name, String colour, String description, int project_id) {
        this.id = id;
        this.name = name;
        this.colour = colour;
        this.description = description;
        this.project_id = project_id;
    }

    public Section(int id, String name, String colour, String description) {
        this.id = id;
        this.name = name;
        this.colour = colour;
        this.description = description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, colour, description, project_id, tasks);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Section section = (Section) o;
        return id == section.id &&
                project_id == section.project_id &&
                Objects.equals(name, section.name) &&
                Objects.equals(colour, section.colour) &&
                Objects.equals(description, section.description) &&
                Objects.equals(tasks, section.tasks);
    }

    @Override
    public String toString() {
        return "Section{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", colour='" + colour + '\'' +
                ", description='" + description + '\'' +
                ", project_id=" + project_id +
                ", tasks=" + tasks +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getProject_id() {
        return project_id;
    }

    public void setProject_id(int project_id) {
        this.project_id = project_id;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

}
