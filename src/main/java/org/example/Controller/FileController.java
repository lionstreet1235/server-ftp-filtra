package org.example.Controller;

import java.sql.Connection;
import java.sql.SQLException;

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

    static void uploadFile()
    {

    }

}
