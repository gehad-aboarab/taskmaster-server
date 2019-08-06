package taskmaster.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.util.Pair;
import taskmaster.database.entities.User;
import taskmaster.database.tables.UserTokens;
import taskmaster.database.tables.Users;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("access")
public class Login {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("/{email}/{password}")
    @Produces(MediaType.TEXT_PLAIN)
    public String login(@PathParam("email") String email, @PathParam("password") String password) {
        try {
            User user = Users.getByAccount(email, password);
            if (user == null) {
                return "Bad user";
            } else {
                String retVal = new Gson().toJson(user) + "\r\n" + UserTokens.issueToken(user.getUserId());
                return retVal;
            }
        } catch (SQLException e) {
            System.out.println(e);
            return "Error connecting to the database";
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginPost(@FormParam("email") @NotNull String email,
                              @FormParam("password") @NotNull String password) {
        try {
            User user = Users.getByAccount(email, password);
            if (user == null) {
                return Response.status(Response.Status.FORBIDDEN).entity("No such user").build();
            } else {
                Gson gson = new Gson();
                JsonArray jsonArray = new JsonArray();
                jsonArray.add(gson.toJson(user));
                jsonArray.add(UserTokens.issueToken(user.getUserId()));
                JsonObject object = new JsonObject();
                object.add("user", gson.toJsonTree(user));
                object.addProperty("token", UserTokens.getToken(user.getUserId()));
                return Response.ok(gson.toJson(object), MediaType.APPLICATION_JSON).build();
            }
        } catch (SQLException e) {
            System.out.println(e);
            return Response.serverError().entity("No such user").build();
        }
    }

    public static void main(String[] args) {
        String name = "b00062014@aus.edu";
        String password = "2";
        String params = "email=" + name + "&password=" + password;
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://ec2-18-220-107-100.us-east-2.compute.amazonaws.com:8080/myapp/access");

        Form form = new Form();
        form.param("email", name);
        form.param("password", password);

        String s = target.request(MediaType.APPLICATION_JSON).post(Entity.form(form), String.class);
        System.out.println(s);
    }

}
