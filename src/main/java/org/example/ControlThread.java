package org.example;

import org.example.Controller.AccountController;
import org.example.Model.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;

import java.util.UUID;


public class ControlThread extends Thread
{
    private final BufferedReader in;
    private static PrintWriter out;
    String control_command;
    final int DATA_PORT = 2000;
    User user_login = null;
    final String UPLOAD_DIRECTORY = "upload";

    public ControlThread(Socket clientSocket) throws IOException
    {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                control_command = in.readLine();
                if (control_command == null)
                {
                    break;
                }
                if (control_command.contains("ls"))
                {
                    showFileAndDirectory(control_command);
                    continue;
                }
                switch (control_command)
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
                    case "OTP":
                        activateEmail();
                        break;
                    default:
                        System.out.println("WRONG COMMAND FROM CLIENT!");
                        break;
                }

            } catch (IOException | SQLException e)
            {
                // Xử lý ngoại lệ SocketException khi client ngắt kết nối ngang
                if (e instanceof SocketException)
                {
                    System.out.println("Client disconnected!");
                } else
                {
                    throw new RuntimeException(e);
                }
                break; // Thoát khỏi vòng lặp khi client ngắt kết nối
            }
        }
    }

    //Kiểm tra email đã kích hoạt chưa
    private void activateEmail() throws SQLException, IOException {
        if (user_login == null) {
            out.println("REQUIRED LOGIN FIRST!");
            return;
        } else if (AccountController.isEmailActivated(user_login.getEmail())) {
            out.println("Email already activated!");
            return;
        }
        {
            out.println();
            //Tạo otp
            Random numberOTP = new Random();
            int otp_random = numberOTP.nextInt(99999);
            String sendOTP = String.format("%05d", otp_random); // Đảm bảo OTP có 5 chữ số
            AccountController.sentEmail(user_login.getEmail(), sendOTP);

            //
            String otp_from_client = in.readLine();
            if (AccountController.activateAccount(otp_from_client, sendOTP, user_login.getEmail())) {
                out.println("Verified OTP successfully!");
            } else {
                out.println("OTP NOT VERIFIED!");
            }
        }

    }

    private void logout()
    {
        if (user_login == null)
        {
            out.println("REQUIRED LOGIN FIRST!");
            return;
        }
        System.out.println(user_login.getUsername() + " logout");
        out.println("Goodbye " + user_login.getUsername() + "!");
        user_login = null;
    }

    private void login() throws IOException, SQLException
    {
        String user_name = in.readLine();
        String pass_word = in.readLine();
        user_login = AccountController.loginUser(user_name, pass_word);
        if (user_login != null)
        {
            out.println("LOGIN SUCCESS!");
        } else
        {
            out.println("LOGIN FAILED!");
        }
    }

    private void register() throws SQLException, IOException
    {
        if (user_login != null)
        {
            out.println("LOGOUT FIRST PLEASE");
            return;
        }
        out.println("OK");
        String fullname = in.readLine();
        String username = in.readLine();
        String email = in.readLine();
        String password = in.readLine();


        if (AccountController.isUserExist(username, email))
        {
            out.println("Username or Email already exist!");
        } else
        {
            //Default values:
            //anonymous = true
            //activated = false
            //blocked = false
            //id_role = 2 (normal client) >< 1 (admin)
            User new_user = new User(
                    UUID.randomUUID().toString(),
                    fullname,
                    username,
                    email, password,
                    LocalDateTime.now().toString(),
                    true,
                    false,
                    2);
            if (AccountController.createUser(new_user))
            {
                out.println("REGISTER SUCCESS!");
                User login_new_user = AccountController.loginUser(username, password);
                if (login_new_user != null)
                {
                    createPersonalDirectory(login_new_user);
                }
            } else
            {
                out.println("REGISTER FAILED!");
            }
        }

    }

    private void createPersonalDirectory(User new_user)
    {
        File new_user_directory = new File(UPLOAD_DIRECTORY + File.separator + new_user.getUsername());
        if (!new_user_directory.exists())
        {
            if (new_user_directory.mkdirs())
            {
                System.out.println("CREATE DIRECTORY FOR " + new_user.getUsername());
            } else
            {
                System.out.println(new_user + " CREATE DIRECTORY FAILED!");
            }
        } else
        {
            System.out.println("DIRECTORY ALREADY EXISTS FOR " + new_user.getUsername());
        }

    }

    private void downloadFile() throws IOException
    {
        //Chua login thi ko duoc downfile
        if (user_login == null)
        {
            out.println("REQUIRED LOGIN FIRST!");
            return;
        }
        out.println("--- DOWNLOAD FILE ---");
        String file_name = in.readLine();
        File file_down = new File(UPLOAD_DIRECTORY
                + File.separator
                + user_login.getUsername()
                + File.separator
                + file_name);
        if (!file_down.exists())
        {
            out.println("file name '" + file_name + "'" + " NOT FOUND!");
        } else
        {
            try
            {
                out.println("STARTING DOWNLOAD ...");
                ServerSocket serverDataSocket = new ServerSocket(DATA_PORT);
                Socket clientDataSocket = serverDataSocket.accept();
                Thread dataThread = new DataThread(clientDataSocket, file_down, "GET", user_login);
                dataThread.start();
                serverDataSocket.close();
            } catch (IOException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    private void uploadFile() throws IOException
    {
        if (user_login == null)
        {
            out.println("REQUIRED LOGIN FIRST!");
            return;
        }
        File upload_dir = new File(UPLOAD_DIRECTORY + File.separator + user_login.getUsername());
        if (!upload_dir.exists())
        {
            upload_dir.mkdirs();
        }
        out.println("--- UPLOAD FILE ---");
        String upload_file_name = in.readLine();
        String new_file_name = getUniqueFileName(upload_file_name);
        File upload_file = new File(new_file_name);
        out.println("STARTING UPLOAD ...");
        ServerSocket serverDataSocket = new ServerSocket(DATA_PORT);
        Socket clientDataSocket = serverDataSocket.accept();
        Thread dataThread = new DataThread(clientDataSocket, upload_file, "UP", user_login);
        dataThread.start();

        serverDataSocket.close();
    }

    private String getUniqueFileName(String filename)
    {
        File file = new File(UPLOAD_DIRECTORY
                + File.separator
                + user_login.getUsername()
                + File.separator
                + filename);
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
            file = new File(UPLOAD_DIRECTORY
                    + File.separator
                    + user_login.getUsername()
                    + File.separator
                    + new_file_name);
            count++;
        }
        return file.getAbsolutePath();
    }

    private void showFileAndDirectory(String control_command)
    {
        if (user_login == null)
        {
            out.println("REQUIRED LOGIN FIRST!");
            return;
        }
        String[] LS_parts = control_command.split(" ");
        if (LS_parts.length == 1)
        {
            File currentFolder = new File(UPLOAD_DIRECTORY + File.separator + user_login.getUsername());
            walk(currentFolder, currentFolder.getAbsolutePath().length());
        } else if (LS_parts.length == 2)
        {
            String folderPath = UPLOAD_DIRECTORY + File.separator + user_login.getUsername() + File.separator + LS_parts[1];
            File folder = new File(folderPath);
            if (folder.exists() && folder.isDirectory())
            {
                walk(folder, folder.getAbsolutePath().length());
            } else
            {
                out.println("The specified path is not a valid directory.");
            }
        }
        out.println("EXIT");
    }

    public void walk(File folder, int baseLength)
    {
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null)
        {
            for (File file : listOfFiles)
            {
                String relativePath = file.getAbsolutePath().substring(baseLength + 1);
                if (file.isDirectory())
                {
                    out.println("Directory: \t" + relativePath + File.separator);
                } else
                {
                    out.println("File: \t" + relativePath);
                }
            }
        } else
        {
            out.println("The folder is empty or cannot be read.");
        }
    }

}

