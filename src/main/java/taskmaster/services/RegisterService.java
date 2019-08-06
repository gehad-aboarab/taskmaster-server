package taskmaster.services;

import com.google.gson.Gson;
import taskmaster.database.entities.User;
import taskmaster.database.tables.Users;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.sql.SQLException;

@Path("register")
public class RegisterService {

    @GET
    @Path("/{name}/{email}/{password}")
    public String register(@PathParam("name") String name, @PathParam("email") String email, @PathParam("password") String password) {
        try {
            User user = Users.register(name, email, password);
            if (user == null)
                return "User already exists";
            return new Gson().toJson(user);
        } catch (SQLException e) {
            System.out.println("[RegisterService] Error retrieving information from the database");
            e.printStackTrace();
            return "Error connecting to the database";
        }
    }

}
