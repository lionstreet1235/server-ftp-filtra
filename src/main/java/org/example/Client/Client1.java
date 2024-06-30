package org.example.Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client1
{
    static Scanner scanner;
    static Socket socket = null;
    static BufferedReader in = null;
    static PrintWriter out = null;
    static final String SERVER_NAME = "localhost";
    static final int DATA_PORT = 2000;
    static final int CONTROL_PORT = 2100;
    static final String DOWNLOAD_DIRECTORY = "download";

    public static void main(String[] args)
    {
        try
        {
            scanner = new Scanner(System.in);
            socket = new Socket(SERVER_NAME, CONTROL_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("----- WELCOME TO FILTRA SERVER -----");
            //Send control command from client to server
            sendCommandToServer();

        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void sendCommandToServer() throws IOException
    {
        while (true)
        {
            System.out.print("COMMAND: ");
            String command = scanner.nextLine();
            String[] command_parts = command.split(" ");
            if (command_parts[0].equalsIgnoreCase("LS"))
            {
                showFileAndDirectory(command);
                continue;
            }

            if (command.equalsIgnoreCase("QUIT"))
            {
                socket.close();
                break;
            } else if (command.equalsIgnoreCase("HELP"))
            {
                showHelp();
            } else
            {
                command = command.toUpperCase();
                out.println(command);
                switch (command)
                {
                    case "LOG":
                        login();
                        break;
                    case "REG":
                        register();
                        break;
                    case "GET":
                        downloadFile();
                        break;
                    case "UP":
                        uploadFile();
                        break;
                    case "OUT":
                        logout();
                        break;
                    default:
                        System.out.println("WRONG COMMAND! (type 'help')");
                        break;
                }
            }
            out.flush();
            System.out.println("---------------------------");
        }
    }

    private static void logout() throws IOException
    {
        String res = in.readLine();
        System.out.println(res);
    }

    private static void login() throws IOException
    {
        System.out.println("--- LOGIN ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String passwd = scanner.nextLine();
        out.println(username);
        out.println(passwd);
        String res_login = in.readLine();
        System.out.println(res_login);
    }

    private static void register() throws IOException
    {
        String res = in.readLine();
        System.out.println(res);
        if (res.contains("LOGOUT"))
        {
            return;
        }
        System.out.println("--- REGISTER ---");
        System.out.print("Fullname: ");
        String fullname = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String passwd = scanner.nextLine();
        System.out.print("Retype your password: ");
        String repasswd = scanner.nextLine();
        if (!repasswd.equals(passwd))
        {
            return;
        }
        //viet ham validate thong tin roi moi gui qua server
        //kiem tra empty/null, email dung cu phap, password > 6 ky tu, repassword == password ...
        out.println(fullname);
        out.println(username);
        out.println(email);
        out.println(passwd);
        String res_register = in.readLine();
        System.out.println(res_register);
    }

    private static void showHelp()
    {
        //viet trang help cho client
        System.out.println("reg - register new account");
        System.out.println("log - login your account");
        System.out.println("ls - show file on server (or use 'ls <folder-name>')");
        System.out.println("get - download file from server");
        System.out.println("up - upload file to server");
        System.out.println("out - logout");
        System.out.println("quit - quit from the server");
        System.out.println("help - see this help");
    }

    private static void downloadFile() throws IOException
    {
        String respond = in.readLine();
        System.out.println(respond);
        if (!respond.contains("DOWNLOAD"))
        {
            return;
        }
        System.out.print("Filename you want to download: ");
        String filename = scanner.nextLine();
        out.println(filename);
        String serverResponse = in.readLine();
        if (serverResponse.contains("STARTING"))
        {
            File download_directory = new File(DOWNLOAD_DIRECTORY);
            if (!download_directory.exists())
            {
                download_directory.mkdirs();
            }

            String new_file_name = getUniqueFileName(filename);
            System.out.println("File download location: " + new_file_name);
            try (Socket dataSocket = new Socket(SERVER_NAME, DATA_PORT);
                 BufferedInputStream in = new BufferedInputStream(dataSocket.getInputStream());
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new_file_name)))
            {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1)
                {
                    out.write(buffer, 0, bytesRead);
                }

                out.flush();
                System.out.println("FILE DOWNLOADED SUCCESSFUL!");
            } catch (IOException e)
            {
                System.out.println(e.getMessage());
            }
        } else
        {
            System.out.println(serverResponse);
        }
    }

    private static void uploadFile() throws IOException
    {
        File download_dir = new File(DOWNLOAD_DIRECTORY);
        if (!download_dir.exists())
        {
            download_dir.mkdirs();
        }
        String respond = in.readLine();
        System.out.println(respond);
        if (respond.contains("REQUIRED"))
        {
            return;
        }
        System.out.print("Filename: ");
        String filename = scanner.nextLine();
        String filePath = DOWNLOAD_DIRECTORY + File.separator + filename;
        File uploadFile = new File(filePath);
        if (!uploadFile.exists())
        {
            System.out.println("'" + filename + "'" + " not found!");
            return;
        }
        out.println(filename);
        System.out.println(in.readLine());
        try (Socket dataSocket = new Socket(SERVER_NAME, DATA_PORT);
             BufferedInputStream in = new BufferedInputStream(new FileInputStream(uploadFile));
             BufferedOutputStream out = new BufferedOutputStream(dataSocket.getOutputStream()))
        {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
            dataSocket.close();
            System.out.println("FILE UPLOADED SUCCESSFUL!");
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static String getUniqueFileName(String filename)
    {
        File file = new File(DOWNLOAD_DIRECTORY + File.separator + filename);
        if (!file.exists())
        {
            return file.getAbsolutePath();
        }

        String name = filename;
        String extension = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1)
        {
            name = filename.substring(0, dotIndex);
            extension = filename.substring(dotIndex);
        }

        int count = 1;
        while (file.exists())
        {
            String new_file_name = name + "(" + count + ")" + extension;
            file = new File(DOWNLOAD_DIRECTORY + File.separator + new_file_name);
            count++;
        }
        return file.getAbsolutePath();
    }

    private static void showFileAndDirectory(String command) throws IOException
    {
        out.println(command);
        String response_LS;
        while ((response_LS = in.readLine()) != null)
        {
            if (response_LS.equals("EXIT"))
            {
                break;
            }
            System.out.println(response_LS);
            if (response_LS.contains("REQUIRED"))
            {
                break;
            }
        }
    }
}
