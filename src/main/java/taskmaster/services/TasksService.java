package taskmaster.services;

import com.google.gson.Gson;
import taskmaster.database.entities.Project;
import taskmaster.database.entities.Task;
import taskmaster.database.tables.Projects;
import taskmaster.database.tables.Tasks;
import taskmaster.database.tables.UserTokens;
import taskmaster.database.tables.Users;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("tasks")
public class TasksService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/by-user/{userId}")
    public Response getByUserId(@PathParam("userId") int userId, @QueryParam("token") String token) {
        try {
            if (!Users.exists(userId))
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            String storedToken = UserTokens.getToken(userId);
            if (storedToken == null || !storedToken.equals(token))
                return Response.status(Response.Status.FORBIDDEN).build();
            List<Task> tasks = Tasks.getByUserId(userId);
            String jsonString = new Gson().toJson(tasks);
            return Response.ok(jsonString, MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[TasksService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/by-project/{projectId}")
    public Response getByProjectId(@PathParam("projectId") int projectId, @QueryParam("token") String token) {
        try {
            Project p = Projects.getById(projectId);
            if (p == null)
                return Response.status(Response.Status.NOT_FOUND).entity("Project not found").build();

            List<Task> tasks = p.fetchTasks();
            return Response.ok(new Gson().toJson(Tasks.getByProjectId(projectId)), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[TasksService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Error connecting to the database").build();
        }
    }

}
