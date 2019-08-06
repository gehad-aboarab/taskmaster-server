package taskmaster.services;

import com.google.gson.Gson;
import taskmaster.database.entities.ProjectInvitation;
import taskmaster.database.entities.User;
import taskmaster.database.tables.ProjectInvitations;
import taskmaster.database.tables.Projects;
import taskmaster.database.tables.UserTokens;
import taskmaster.database.tables.Users;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.List;

@Path("invitations")
public class InvitationsService {

    public static final int NO_INVITEE_FOUND = 418;
    public static final int ALREADY_INVITED = 208;
    public static final int UNSUCCESSFUL_INVITATION = 506;

    public static final int ALREADY_ACCEPTED = 207;
    public static final int ALREADY_DENIED = 208;
    public static final int UNSUCCESSFUL_ACCEPTANCE = 506;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response invite(@FormParam("projectId") @NotNull Integer projectId,
                           @FormParam("inviteeEmail") @NotNull String inviteeEmail,
                           @QueryParam("token") @NotNull String token) {
        try {
            System.out.printf("[InvitationsService] Inviting user, projectId = %d, inviteeEmail = %s, token = %s\n",
                    projectId, inviteeEmail, token);

            System.out.println("[InvitationsService] Checking if inviterId matches token...");
            Integer inviterId = UserTokens.getUserId(token);
            if (inviterId == null) {
                System.out.println("[InvitationsService] No inviter ID matching the provided token found");
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            System.out.println("[InvitationsService] Checking if inviter is in project...");
            if (!Projects.isUserIn(inviterId, projectId)) {
                System.out.println("[InvitationsService] Inviter is not a member of the provided project");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            System.out.println("[InvitationsService] Checking if invitee exists in DB...");
            User invitee = Users.getByEmail(inviteeEmail);
            if (invitee == null) {
                System.out.println("[InvitationsService] No invitee matching the given ID found");
                return Response.status(NO_INVITEE_FOUND).entity("Invitee not found").build();
            }

            int inviteeId = invitee.getUserId();

            System.out.println("[InvitationsService] Checking if invitee has already been invited...");
            if (ProjectInvitations.isInvited(inviteeId, projectId)) {
                return Response.status(ALREADY_INVITED).entity("Invitee already invited").build();
            }

            boolean success = ProjectInvitations.invite(inviterId, inviteeId, projectId);

            if (success) {
                System.out.println("[InvitationsService] Invited successfully");
                return Response.accepted().build();
            } else {
                System.out.println("[InvitationsService] Invitation not successful");
                return Response.status(UNSUCCESSFUL_INVITATION).entity("Unsuccessful invitation").build();
            }

        } catch (SQLException e) {
            System.out.println("[InvitationsService] Error retrieving the information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }
    }

    @POST
    @Path("/{invitationId}/accept")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response accept(@PathParam("invitationId") int invitationId,
                           @FormParam("token") @NotNull String token) {
        try {
            System.out.println("[InvitationsService] Accepting invitation, invitationId = " + invitationId);
            Integer userId = UserTokens.getUserId(token);
            if (userId == null) {
                System.out.println("[InvitationsService] No user matching the given token found");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            ProjectInvitation invitation = ProjectInvitations.getInvitation(invitationId);
            if (invitation.getStatus() == ProjectInvitation.ACCEPTED) {
                System.out.println("[InvitationsService] Invitation was already accepted...");
                return Response.status(ALREADY_ACCEPTED).entity("Invitee already accepted invitation").build();
            }

            if (invitation.getStatus() == ProjectInvitation.DENIED) {
                System.out.println("[InvitationsService] Invitation was already denied...");
                return Response.status(ALREADY_DENIED).entity("Invitee already denied invitation").build();
            }

            boolean success = ProjectInvitations.acceptInvitation(invitationId);
            if (success) {
                System.out.println("[InvitationsService] Accepted invitation successfully");
                return Response.accepted().build();
            } else {
                System.out.println("[InvitationsService] Error while accepting invitation");
                return Response.status(UNSUCCESSFUL_ACCEPTANCE).entity("Internal server error").build();
            }
        } catch (SQLException e) {
            System.out.println("[InvitationsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/{invitationId}/decline")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response decline(@PathParam("invitationId") int invitationId,
                           @FormParam("token") @NotNull String token) {
        try {
            System.out.println("[InvitationsService] Declining invitation, invitationId = " + invitationId);
            Integer userId = UserTokens.getUserId(token);
            if (userId == null) {
                System.out.println("[InvitationsService] No user matching the given token found");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            ProjectInvitation invitation = ProjectInvitations.getInvitation(invitationId);
            if (invitation.getStatus() == ProjectInvitation.ACCEPTED) {
                System.out.println("[InvitationsService] Invitation was already accepted...");
                return Response.status(ALREADY_ACCEPTED).entity("Invitee already accepted invitation").build();
            }

            if (invitation.getStatus() == ProjectInvitation.DENIED) {
                System.out.println("[InvitationsService] Invitation was already denied...");
                return Response.status(ALREADY_DENIED).entity("Invitee already denied invitation").build();
            }

            boolean success = ProjectInvitations.declineInvitation(invitationId);
            if (success) {
                System.out.println("[InvitationsService] Declined invitation successfully");
                return Response.accepted().build();
            } else {
                System.out.println("[InvitationsService] Error while declining invitation");
                return Response.status(UNSUCCESSFUL_ACCEPTANCE).entity("Internal server error").build();
            }
        } catch (SQLException e) {
            System.out.println("[InvitationsService] Error retrieving information from the database");
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/by-user/{userId}")
    public Response getInvitations(@PathParam("userId") int userId,
                                   @QueryParam("token") @NotNull String token) {
        try {
            System.out.println("[InvitationsService] Getting invitations for userId = " + userId);

            Integer userTokenId = UserTokens.getUserId(token);
            if (userTokenId == null) {
                System.out.println("[InvitationsService] No user matching the token found");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            if (userTokenId != userId) {
                System.out.println("[UsersService] Provided user ID not equal to found userTokenId");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            List<ProjectInvitation> invitations = ProjectInvitations.getInvitations(userId);
            System.out.println("[UsersService] Retrieved invitations successfully, invitations = " + invitations.toString());
            return Response.ok(new Gson().toJson(invitations), MediaType.APPLICATION_JSON).build();
        } catch (SQLException e) {
            System.out.println("[UsersService] Error retrieving the information from the database");
            e.printStackTrace();
            return Response.serverError().entity("Internal server error").build();
        }
    }
}
