package taskmaster.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import taskmaster.database.entities.Project;
import taskmaster.database.entities.Section;
import taskmaster.database.entities.Task;
import taskmaster.database.entities.User;
import taskmaster.database.tables.*;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

@Path("projects")
public class ProjectsService {

    public static void main(String[] args) {

    }

    public static final int UNKNOWN_ERROR = 506;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/by-user/{userId}")
    public Response getByUserId(@PathParam("userId") int userId,
                                @QueryParam("token") @NotNull String accessToken) {
        try {
            User user = Users.getUserById(userId);
            if (user == null)

                return Response.status(Response.Status.NOT_FOUND).entity("No such user found").build();
            String storedToken = UserTokens.getToken(user.getUserId());
            if (storedToken == null || !storedToken.equals(accessToken))
                return Response.status(Response.Status.FORBIDDEN).build();
            List<Project> projects = Projects.getByUserId(userId);
            return Response.ok(new Gson().toJson(projects), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{projectId}")
    public Response getFullDetails(@PathParam("projectId") int projectId,
                                   @QueryParam("token") @NotNull String token) {
        try {
            Project project = Projects.getProjectById(projectId);
            if (project == null)
                return Response.status(Response.Status.NOT_FOUND).entity("No such task found").build();

            Gson gson = new GsonBuilder().create();
            System.out.println("DEBUG: json string = " + gson.toJson(project));
            System.out.println("DEBUG: project string = " + project.toString());
            return Response.ok(gson.toJson(project), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{projectId}/members")
    public Response getMembers(@PathParam("projectId") int projectId,
                               @QueryParam("token") @NotNull String token) {
        try {
            Integer userId = UserTokens.getUserId(token);
            if (userId == null || !Projects.isUserIn(userId, projectId))
                return Response.status(Response.Status.FORBIDDEN).build();

            List<User> users = Projects.getUsers(projectId);
            String usersJson = new Gson().toJson(users);

            return Response.ok(usersJson, MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.serverError().entity("Error connecting to the database").build();
        }
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{projectId}/members/{userId}")
    public Response deleteMember(@PathParam("projectId") int projectId,
                                 @PathParam("userId") int userId,
                                 @QueryParam("token") String token) {
        try {
            System.out.printf("[ProjectsService] Deleting member, projectId = %d, userId = %d, token = %s\n",
                    projectId,
                    userId,
                    token);

            Integer deleterId = UserTokens.getUserId(token);
            if (deleterId == null) {
                System.out.println("[ProjectsService] No deleter matching token found");
                return Response.status(Response.Status.FORBIDDEN.getStatusCode(), "Bad token").build();
            }

            Project project = Projects.getProjectById(projectId);
            if (project == null) {
                System.out.println("[ProjectsService] No project matching projectId found");
                return Response.status(Response.Status.NOT_FOUND.getStatusCode(), "Project not found").build();
            }

            if (!Projects.isUserIn(deleterId, projectId)) {
                System.out.println("[ProjectsService] Deleter not in project");
                return Response.status(Response.Status.FORBIDDEN.getStatusCode(), "You are not in that project").build();
            }

            if (!Projects.isUserIn(userId, projectId)) {
                System.out.println("[ProjectsService] User not in project");
                return Response.status(Response.Status.NOT_FOUND.getStatusCode(), "User is not in the project").build();
            }

            boolean status = Projects.removeUser(projectId, userId);
            if (status) {
                System.out.println("[ProjectsService] Deleted successfully");
                return Response.accepted("Deleted successfully").build();
            }

            System.out.println("[ProjectsService] Unknown error");
            return Response.status(UNKNOWN_ERROR, "An unknown error occurred").build();
        } catch (SQLException e) {
            System.out.println("[ProjectsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal database error").build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/create")
    public Response addProject(@FormParam("name") @NotNull String name,
                               @FormParam("description") String description,
                               @QueryParam("token") @NotNull String token) {
        try {
            Integer userId = UserTokens.getUserId(token);
            if (userId == null)
                return Response.status(Response.Status.FORBIDDEN).build();
            Project project = Projects.create(userId, name, description);
            return Response.ok(new Gson().toJson(project), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[ProjectsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal database error").build();
        }

    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{projectId}")
    public Response editProject(@PathParam("projectId") @NotNull Integer projectId,
                                @FormParam("name") String name,
                                @FormParam("description") String description,
                                @QueryParam("token") @NotNull String token) {
        try {
            Integer userId = UserTokens.getUserId(token);
            if (userId == null || !Projects.isUserIn(userId, projectId))
                return Response.status(Response.Status.FORBIDDEN).entity("No such user").build();
            Project project = Projects.editProject(projectId, name, description);
            return Response.ok(new Gson().toJson(project), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[ProjectsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Path("/{projectId}")
    public Response deleteProject(@PathParam("projectId") @NotNull Integer projectId,
                                  @QueryParam("token") @NotNull String token) {
        System.out.println("[ProjectsService] Received delete request");
        try {
            Integer userId = UserTokens.getUserId(token);
            if (userId == null || !Projects.isUserIn(userId, projectId)) {
                System.out.println("[ProjectsService] User is not allowed to delete this project");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            if (Projects.deleteProject(projectId)) {
                System.out.println("[ProjectsService] Project deleted successfully");
                return Response.ok().build();
            }

            System.out.println("[ProjectsService] Project not deleted, returning BAD_REQUEST...");
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (SQLException e) {
            System.out.println("[ProjectsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{projectId}/sections")
    public Response addSection(@PathParam("projectId") @NotNull Integer projectId,
                               @FormParam("name") @NotNull String name,
                               @FormParam("colour") @NotNull String colour,
                               @FormParam("description") String description,
                               @QueryParam("token") @NotNull String token) {
        try {
            Integer userId = UserTokens.getUserId(token);
            if (userId == null || !Projects.isUserIn(userId, projectId))
                return Response.status(Response.Status.FORBIDDEN).entity("No such user").build();
            Section section = Sections.addSection(name, colour, description, projectId);
            return Response.ok(new Gson().toJson(section), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[ProjectsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{projectId}/sections/{sectionId}")
    public Response editSection(@PathParam("projectId") @NotNull Integer projectId,
                                @PathParam("sectionId") @NotNull Integer sectionId,
                                @FormParam("name") @NotNull String name,
                                @FormParam("description") String description,
                                @FormParam("colour") String colour,
                                @QueryParam("token") @NotNull String token) {
        try {
            Integer userId = UserTokens.getUserId(token);
            if (userId == null || !Projects.isUserIn(userId, projectId) || !Projects.isSectionIn(sectionId, projectId))
                return Response.status(Response.Status.FORBIDDEN).build();

            Section section = Sections.editSection(sectionId, name, colour, description, projectId);
            if (section == null)
                return Response.status(Response.Status.FORBIDDEN).build();

            return Response.ok(new Gson().toJson(section), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[ProjectsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }

    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{projectId}/sections/{sectionId}")
    public Response deleteSection(@PathParam("projectId") @NotNull Integer projectId,
                                  @PathParam("sectionId") @NotNull Integer sectionId,
                                  @QueryParam("token") @NotNull String token) {
        try {
            Integer userId = UserTokens.getUserId(token);
            if (userId == null
                    || !Projects.isUserIn(userId, projectId)
                    || !Projects.isSectionIn(sectionId, projectId))
                return Response.status(Response.Status.FORBIDDEN).build();

            boolean status = Sections.deleteSection(sectionId);
            if (status)
                return Response.ok().build();
            else
                return Response.serverError().entity("Internal server error").build();

        } catch (SQLException e) {
            System.out.println("[ProjectsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{projectId}/sections/{sectionId}/tasks")
    public Response addTask(@PathParam("projectId") @NotNull Integer projectId,
                            @PathParam("sectionId") @NotNull Integer sectionId,
                            @FormParam("name") @NotNull String name,
                            @FormParam("description") String description,
                            @FormParam("date") Date date,
                            @FormParam("userId") Integer assigneeId,
                            @QueryParam("token") @NotNull String token) {
        try {
            Integer userId = UserTokens.getUserId(token);
            if (userId == null)
                return Response.status(Response.Status.FORBIDDEN).build();
            Task task = Tasks.createTask(sectionId, description, name, date, assigneeId);

            if (task == null)
                return Response.status(Response.Status.NOT_FOUND).build();

            return Response.ok(new Gson().toJson(task), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[ProjectsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{projectId}/sections/{sectionId}/tasks/{taskId}")
    public Response editTask(@PathParam("projectId") int projectId,
                             @PathParam("sectionId") int sectionId,
                             @PathParam("taskId") @NotNull Integer taskId,
                             @FormParam("name") @NotNull String name,
                             @FormParam("description") String description,
                             @FormParam("date") Date date,
                             @FormParam("sectionId") @NotNull Integer newSectionId,
                             @FormParam("userId") Integer assigneeId,
                             @QueryParam("token") @NotNull String token) {
        try {
            Integer userId = UserTokens.getUserId(token);
            if (userId == null
                    || !Projects.isUserIn(userId, projectId)
                    || !Projects.isSectionIn(sectionId, projectId)
                    || !Sections.isTaskIn(taskId, sectionId))
                return Response.status(Response.Status.FORBIDDEN).build();

            Task task = Tasks.editTask(taskId, description, name, date, newSectionId, assigneeId);
            if (task == null)
                return Response.status(Response.Status.FORBIDDEN).build();

            return Response.ok(new Gson().toJson(task), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[ProjectsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }

    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{projectId}/sections/{sectionId}/tasks/{taskId}")
    public Response deleteTask(@PathParam("projectId") @NotNull Integer projectId,
                               @PathParam("sectionId") @NotNull Integer sectionId,
                               @PathParam("taskId") @NotNull Integer taskId,
                               @QueryParam("token") @NotNull String token) {
        try {
            Integer userId = UserTokens.getUserId(token);
            if (userId == null
                    || !Projects.isUserIn(userId, projectId)
                    || !Projects.isSectionIn(sectionId, projectId)
                    || !Sections.isTaskIn(taskId, sectionId))
                return Response.status(Response.Status.FORBIDDEN).build();

            boolean status = Tasks.deleteTask(taskId);
            if (status)
                return Response.accepted().build();
            else
                return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (SQLException e) {
            System.out.println("[ProjectsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }
    }

    @POST
    @Path("/{projectId}/tasks/{taskId}/completed")
    public Response editStatus(@PathParam("projectId") int projectId,
                               @PathParam("taskId") int taskId,
                               @FormParam("completed") boolean completed,
                               @QueryParam("token") String token) {
        try {
            System.out.printf("[ProjectsService] Completing task, projectId = %d, taskId = %d, completed = %b, token = %s\n",
                    projectId,
                    taskId,
                    completed,
                    token);

            Integer userId = UserTokens.getUserId(token);
            if (userId == null) {
                System.out.println("[ProjectsService] Bad token");
                return Response.status(Response.Status.FORBIDDEN.getStatusCode(), "Bad token").build();
            }

            if (!Projects.isUserIn(userId, projectId)) {
                System.out.println("[ProjectsService] User not in project");
                return Response.status(Response.Status.FORBIDDEN.getStatusCode(), "You are not in that project").build();
            }

            Task task = Tasks.getById(taskId);

            if (task == null) {
                System.out.println("[ProjectsService] Task not found");
                return Response.status(Response.Status.NOT_FOUND.getStatusCode(), "Task not found").build();
            }

            if (task.getProjectId() != projectId) {
                System.out.println("[ProjectsService] Task not in given project");
                return Response.status(Response.Status.FORBIDDEN.getStatusCode(), "Task not in given project").build();
            }

            boolean success = Tasks.editStatus(taskId, completed);

            if (success) {
                System.out.println("[ProjectsService] Changed status successfully");
                return Response.accepted("Changed status successfully").build();
            }

            System.out.println("[ProjectsService] Unknown error");
            return Response.status(UNKNOWN_ERROR, "Unknown error").build();

        } catch (SQLException e) {
            System.out.println("[ProjectsService] Database error");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Database error").build();
        }
    }
}
