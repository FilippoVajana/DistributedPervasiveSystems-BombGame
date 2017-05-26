package main.java;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalDateTime;

/**
 * Created by filip on 24/05/2017.
 */
public class ClientCore
{
    public static void main(String[] args)
    {
        String sAddress = "localhost";
        int sPort = 9090;
        System.out.println("Default server = " + sAddress + ":" + sPort);

        Socket socket = null;
        try
        {
            socket = new Socket(sAddress, sPort);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

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

        String input = "";
        String output = "";
        while (true)
        {
            try
            {
                output = LocalDateTime.now().toString();
                writer.println(output);
                writer.flush();
                System.out.println("Output = " + output);

                input = reader.readLine();
                System.out.println("Input = " + input);

                Thread.sleep(500);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
