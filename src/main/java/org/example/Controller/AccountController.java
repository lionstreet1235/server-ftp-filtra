package org.example.Controller;

import org.example.DB.DatabaseConnector;
import org.example.Model.User;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;

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
        String register_query = "INSERT INTO users (id, username, password, email, fullname, date_created, anonymous, activated, id_role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps;
        try
        {
            ps = connection.prepareStatement(register_query);
            ps.setString(1, new_user.getId());
            ps.setString(2, new_user.getUsername());
            ps.setString(3, new_user.getPassword());
            ps.setString(4, new_user.getEmail());
            ps.setString(5, new_user.getFullname());
            ps.setString(6, new_user.getDate_created());
            ps.setBoolean(7, new_user.isAnonymous());
            ps.setBoolean(8, new_user.isActivated());
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
                    rs.getString("id"),
                    rs.getString("fullname"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("date_created"),
                    rs.getBoolean("anonymous"),
                    rs.getBoolean("activated"),
                    rs.getInt("id_role")
            );
            return user_login;
        }

        System.out.println("LOGIN FAILED!");
        return null;

    }
    public static void sentEmail(String email_user, String otp) throws SQLException {
        String HOST_NAME = "smtp.gmail.com";
        String SSL_PORT = "587"; //  "587" for TSL
        String APP_EMAIL = "tuanduy1411@gmail.com";
        String APP_PASSWORD = "yxzvrylniyxlojpe";

        //(send email)
        //cài đặt properties để connect
        String recipient =email_user     ; // Địa chỉ email người nhận
        String sender = APP_EMAIL; // Địa chỉ email của bạn


        // Thiết lập properties cho SMTP server
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", HOST_NAME);
        properties.put("mail.smtp.port", SSL_PORT);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
//        properties.put("mail.smtp.socketFactory.port", SSL_PORT);
        properties.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

        properties.put("mail.smtp.ssl.trust", HOST_NAME);

        // Xác thực tài khoản email và password

        Session session =Session.getInstance(properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(APP_EMAIL,  APP_PASSWORD);
                    }
                });

        //viết tin nhắn
        try{
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject("Your OTP Code");
            message.setText("Your OTP code is: "+otp);
            //Gửi email
            Transport.send(message);
            System.out.println("Filtran-ftp was sent OTP to your email. Please check your mail");

        }catch (MessagingException mes){
            System.out.println("Failed to send OTP email. Error: " +mes.getMessage());
        }

    }
    //Cập nhật lại trạng thái activated
    public static boolean activateAccount(String otp_from_client, String otp_generated,String email) throws SQLException {
        if(otp_from_client.equals(otp_generated)) {
            try (Connection conn = DatabaseConnector.connectToDatabase()) {

                String query = "UPDATE users SET activated = ? WHERE email = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setBoolean(1, true);
                    stmt.setString(2, email);
                    stmt.executeUpdate();
                }
            }
            return true;
        }
        return false;
    }
    //Xem người dùng đã kích hoạt tài khoản hay chưa
    public static boolean isEmailActivated(String email) throws SQLException {

        try (Connection conn = DatabaseConnector.connectToDatabase()) {

            String query = "SELECT activated FROM users WHERE email = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, email);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        boolean activated = rs.getBoolean("activated");
                        if (activated) {
                            System.out.println("User is Activated");

                            return true;
                        } else {
                            System.out.println("User is not Activated");
                            return false;
                        }
                    } else {
                        System.out.println("Email not found");
                        return false;
                    }
                }

            }

        }
    }


}
