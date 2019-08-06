package taskmaster.services;

import com.google.gson.Gson;
import taskmaster.database.entities.Section;
import taskmaster.database.entities.Task;
import taskmaster.database.tables.Projects;
import taskmaster.database.tables.Sections;
import taskmaster.database.tables.Tasks;
import taskmaster.database.tables.UserTokens;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.sql.SQLException;

@Path("sections")
public class SectionsService {

    @GET
    @Path("/{sectionId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@PathParam("sectionId") int sectionId) {
        // TODO:
        try {
            Section section = Sections.getById(sectionId);
            return Response.ok(new Gson().toJson(section), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[SectionsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }

    }

    // createTask(int sectionId, String description, String name, String date, Integer userId)

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{sectionId}/tasks")
    public Response addTask(@PathParam("sectionId") int sectionId,
                            @FormParam("name") String name,
                            @FormParam("description") String description,
                            @FormParam("date") Date date,
                            @FormParam("userId") Integer userId,
                            @QueryParam("token") String token) {
        try {
            Integer requestingUser = UserTokens.getUserId(token);
            Integer projectId = Projects.getProjectIdBySectionId(sectionId);

            if (requestingUser == null || projectId == null || !Projects.isUserIn(requestingUser, projectId))
                return Response.status(Response.Status.FORBIDDEN).build();
            Task task = Tasks.createTask(sectionId, description, name, date, userId);
            return Response.ok(new Gson().toJson(task), MediaType.APPLICATION_JSON).build();

        } catch (SQLException e) {
            System.out.println("[SectionsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }
    }

}
