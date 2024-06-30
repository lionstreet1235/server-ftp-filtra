package org.example;

import java.io.*;
import java.net.Socket;

public class DataThread extends Thread
{
    Socket dataSocket;
    File file;
    String command;


    public DataThread(Socket dataSocket, File file, String command) throws IOException
    {
        this.dataSocket = dataSocket;
        this.file = file;
        this.command = command.toUpperCase();
    }

    @Override
    public void run()
    {
        if (command.equals("GET"))
        {
            downloadFile();
        } else if (command.equals("UP"))
        {
            uploadFile();
        }
    }

    private void downloadFile()
    {
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
             BufferedOutputStream out = new BufferedOutputStream(dataSocket.getOutputStream()))
        {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        } finally
        {
            try
            {
                dataSocket.close();
            } catch (IOException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    private void uploadFile()
    {
        try (BufferedInputStream in = new BufferedInputStream(dataSocket.getInputStream());
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file)))
        {
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        } finally
        {
            try
            {
                dataSocket.close();
            } catch (IOException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }


}
