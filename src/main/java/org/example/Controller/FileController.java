package org.example.Controller;

import org.example.Model.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.example.DB.DatabaseConnector.connectToDatabase;

public class FileController
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

    public static void uploadFile(User user_upload, File upload_file) throws IOException, SQLException
    {
        String query = "INSERT INTO files (id_file, id_user_upload, filename, filepath, filetype, upload_date, filesize) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps;
        try
        {
            ps = connection.prepareStatement(query);
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, user_upload.getId());
            ps.setString(3, upload_file.getName());
            ps.setString(4, upload_file.getAbsolutePath());
            ps.setString(5, "unknown type");
            ps.setString(6, LocalDateTime.now().toString());
            ps.setString(7, String.valueOf(Files.size(Path.of(String.valueOf(upload_file)))));
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        int new_row_file = ps.executeUpdate();
        if (new_row_file > 0)
        {
            System.out.println(user_upload.getUsername() + " UPLOAD '" + upload_file.getName() + "' SUCCESS!");
        } else
        {
            System.out.println("UPLOAD FAILED!");
        }
    }


}
