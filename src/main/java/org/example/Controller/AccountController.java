package org.example.Controller;

import org.example.DB.DatabaseConnector;
import org.example.Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.example.DB.DatabaseConnector.connectToDatabase;

public class AccountController
{
    static Connection connection;

    static
    {
        try
        {
            connection = connectToDatabase();
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }


    public static boolean createUser(User new_user) throws SQLException
    {
        String register_query = "INSERT INTO users (username, password, email, fullname, date_created, anonymous, activated, blocked, id_role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps;
        try
        {
            ps = connection.prepareStatement(register_query);
            ps.setString(1, new_user.getUsername());
            ps.setString(2, new_user.getPassword());
            ps.setString(3, new_user.getEmail());
            ps.setString(4, new_user.getFullname());
            ps.setString(5, new_user.getDate_created());
            ps.setBoolean(6, new_user.isAnonymous());
            ps.setBoolean(7, new_user.isActivated());
            ps.setBoolean(8, new_user.isBlocked());
            ps.setInt(9, new_user.getId_role());

        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        int new_row_user = ps.executeUpdate();
        if (new_row_user > 0)
        {
            System.out.println("REGISTER SUCCESS!");
            return true;
        }
        System.out.println("REGISTER FAILED!");
        return false;
    }


    //Validate username và email
    public static boolean isUserExist(String username, String email) throws SQLException
    {
        String query = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (Connection con = DatabaseConnector.connectToDatabase();
             PreparedStatement ps = con.prepareStatement(query))
        {

            ps.setString(1, username);
            ps.setString(2, email);
            ResultSet rs = ps.executeQuery();

            return rs.next();
        } catch (SQLException e)
        {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    public static User loginUser(String username, String passwd) throws SQLException
    {
        User user_login = null;
        String login_query = "SELECT * FROM users WHERE username=? AND password=?";
        PreparedStatement ps = null;
        try
        {
            ps = connection.prepareStatement(login_query);
            ps.setString(1, username);
            ps.setString(2, passwd);
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        ResultSet rs = ps.executeQuery();
        if (rs.next())
        {
            System.out.println("LOGIN SUCCESS! USER: " + rs.getString("username"));
            user_login = new User(
                    rs.getString("fullname"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("date_created"),
                    rs.getBoolean("anonymous"),
                    rs.getBoolean("activated"),
                    rs.getBoolean("blocked"),
                    rs.getInt("id_role")
            );
            return user_login;
        }

        System.out.println("LOGIN FAILED!");
        return null;

    }
}
