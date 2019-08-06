package taskmaster.database.tables;

import taskmaster.database.DatabaseManager;
import taskmaster.database.entities.Project;
import taskmaster.database.entities.ProjectInvitation;
import taskmaster.database.entities.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProjectInvitations {

    private static ProjectInvitation fromResultSet(ResultSet resultSet) throws SQLException {
        int invitationId = resultSet.getInt("invitation_id");
        int inviterId = resultSet.getInt("inviter_id");
        User inviter = Users.getUserById(inviterId);
        inviter.setPassword("");
        int inviteeId = resultSet.getInt("invitee_id");
        User invitee = Users.getUserById(inviteeId);
        invitee.setPassword("");
        int projectId = resultSet.getInt("project_id");
        Project project = Projects.getProjectById(projectId);
        int status = resultSet.getInt("status");
        return new ProjectInvitation(invitationId, inviter, invitee, project, status);
    }

    public static List<ProjectInvitation> getInvitations(int userId) throws SQLException {
        System.out.println("[ProjectInvitations] Getting invitations, userId = " + userId);

        String query = "SELECT * " +
                "FROM project_invitations " +
                "WHERE invitee_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("[ProjectInvitations] executed query successfully...");

            List<ProjectInvitation> invitations = new ArrayList<>();
            while (resultSet.next()) {
                invitations.add(fromResultSet(resultSet));
            }
            System.out.println("[ProjectInvitations] Returning list: " + invitations.toString());
            return invitations;
        }
    }

    public static ProjectInvitation getInvitation(int invitationId) throws SQLException {
        System.out.println("[ProjectInvitations] Getting invitation, invitation = " + invitationId);

        String query = "SELECT * " +
                "FROM project_invitations " +
                "WHERE invitation_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, invitationId);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("[ProjectInvitations] executed query successfully...");

            if (resultSet.next()) {
                return fromResultSet(resultSet);
            } else {
                return null;
            }
        }
    }

    public static ProjectInvitation getInvitation(int userId, int projectId) throws SQLException {
        System.out.printf("[ProjectInvitations] getting invitation, userId = %d, projectId = %d\n",
                userId,
                projectId);

        String query = "SELECT * " +
                "FROM project_invitations " +
                "WHERE invitee_id = ? " +
                "AND project_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, projectId);
            System.out.println("[ProjectInvitations] Prepared statement successfully");
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                System.out.println("[ProjectInvitations] No invitation found");
                return null;
            } else {
                return fromResultSet(resultSet);
            }
        }
    }

    public static boolean invite(int inviterId, int inviteeId, int projectId) throws SQLException {
        System.out.printf("[ProjectInvitations] Inviting user, inviterId = %s, inviteeId = %s, projectId = %s\n",
                inviterId, inviteeId, projectId);

        String query = "INSERT INTO project_invitations " +
                "(inviter_id, invitee_id, project_id) " +
                "VALUES (?, ?, ?)";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, inviterId);
            preparedStatement.setInt(2, inviteeId);
            preparedStatement.setInt(3, projectId);
            int updatedRecords = preparedStatement.executeUpdate();
            System.out.println("[ProjectInvitations] Executed query successfully, updatedRecords = " + updatedRecords);
            return updatedRecords == 1;
        }

    }

    public static boolean acceptInvitation(int invitationId) throws SQLException {
        System.out.println("[ProjectInvitations] Accepting invitation, invitationId = " + invitationId);
        String query = "UPDATE project_invitations " +
                "SET status = ? " +
                "WHERE invitation_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, ProjectInvitation.ACCEPTED);
            preparedStatement.setInt(2, invitationId);

            System.out.println("[ProjectInvitations] Successfully prepared statement");

            int updatedRecords = preparedStatement.executeUpdate();
            if (updatedRecords != 1) {
                System.out.println("[ProjectInvitations] No records were updated?");
                return false;
            }

            ProjectInvitation invitation = getInvitation(invitationId);

            System.out.println("[ProjectInvitations] Adding user to project");
            return Projects.addUserToProject(invitation.getProject().getProjectId(), invitation.getInvitee().getUserId());
        }
    }

    public static boolean declineInvitation(int invitationId) throws SQLException {
        System.out.println("[ProjectInvitations] Declining invitation, invitationId = " + invitationId);
        String query = "UPDATE project_invitations " +
                "SET status = ? " +
                "WHERE invitation_id = ?";
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try (PreparedStatement preparedStatement = databaseManager.prepareStatement(query)) {
            preparedStatement.setInt(1, ProjectInvitation.DENIED);
            preparedStatement.setInt(2, invitationId);

            System.out.println("[ProjectInvitations] Successfully prepared statement");

            int updatedRecords = preparedStatement.executeUpdate();
            if (updatedRecords != 1) {
                System.out.println("[ProjectInvitations] No records were updated?");
                return false;
            } else {
                System.out.println("[ProjectInvitations] Successfully denied invitation");
                return true;
            }
        }
    }

    public static boolean isInvited(int userId, int projectId) throws SQLException {
        System.out.printf("[ProjectInvitations] Checking if user is invited, userId = %d, projectId = %d\n",
                userId,
                projectId);

        ProjectInvitation invitation = getInvitation(userId, projectId);
        if (invitation == null) {
            System.out.println("[ProjectInvitations] User was invited to that project");
            return false;
        } else {
            System.out.println("[ProjectInvitations] User was not invited to that project");
            return true;
        }
    }

}
