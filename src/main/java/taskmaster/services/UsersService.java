package taskmaster.services;

import taskmaster.database.entities.ProjectInvitation;
import taskmaster.database.tables.ProjectInvitations;
import taskmaster.database.tables.UserTokens;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

@Path("users")
public class UsersService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userId}/invitations")
    public Response getInvitations(@PathParam("userId") int userId,
                                   @QueryParam("token") @NotNull String token) {
        try {
            System.out.println("[UsersService] Getting invitations for userId = " + userId);

            Integer userTokenId = UserTokens.getUserId(token);
            if (userTokenId == null) {
                System.out.println("[UsersService] User ID did not match provided token");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            List<ProjectInvitation> invitations = ProjectInvitations.getInvitations(userId);
            System.out.println("[UsersService] Retrieved invitations successfully, invitations = " + invitations.toString());
            return Response.ok(invitations, MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[UsersService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }
    }

}
