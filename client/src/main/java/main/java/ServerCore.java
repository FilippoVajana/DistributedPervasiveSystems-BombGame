package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by filip on 24/05/2017.
 */
public class ServerCore
{
    public static void main(String[] args) throws IOException {
        System.out.println("Hello Client");
        new ServerCore().startInServer();
        System.in.read();
    }

    public void startInServer()
    {
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        try
        {
            serverSocket = new ServerSocket(9090);
            System.out.println("Server port = " + serverSocket.getLocalPort());

            while (true)
            {
                try
                {
                    System.out.println("Server listening . . .");
                    clientSocket = serverSocket.accept();
                    Thread sThread = new Thread(new InServerService(clientSocket));
                    sThread.start();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

class InServerService implements Runnable
{
    private Socket socket = null;
    public InServerService(Socket socket)
    {
        this.socket = socket;
        System.out.println("New client = " + socket.getInetAddress().getHostAddress());
    }

    public void run()
    {
        BufferedReader reader = null;
        PrintWriter writer = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        String input = null;
        String output = null;
        while (!socket.isClosed())
        {

            try
            {
                input = reader.readLine();
                System.out.println("Input = " + input);

                output = "Reply - " + input;
                writer.println(output);
                writer.flush();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }
}
