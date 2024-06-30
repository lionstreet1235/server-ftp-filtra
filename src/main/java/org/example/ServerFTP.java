package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerFTP
{
    private static final int CONTROL_PORT = 2100;

    public static void main(String[] args)
    {
        try (ServerSocket controlServerSocket = new ServerSocket(CONTROL_PORT))
        {
            System.out.println("SERVER IS OPENING NOW ...");
            while (true)
            {
                Socket clientSocket = controlServerSocket.accept();
                Thread clientThread = new ControlThread(clientSocket);
                System.out.println("CLIENT " + clientThread.getId() + " CONNECTED!");
                clientThread.start();
            }
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

    }
}