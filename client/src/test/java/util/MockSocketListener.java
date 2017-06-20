package util;

import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.PrettyPrinter;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by filip on 19/06/2017.
 */
public class MockSocketListener
{
    public ServerSocket listenSocket;

    public  MockSocketListener()
    {
        try
        {
            listenSocket = new ServerSocket(0);
            //log
            PrettyPrinter.printTimestampLog(String.format("[MockSocketListener] Initialized listener at %s:%d", listenSocket.getInetAddress().getHostAddress(), listenSocket.getLocalPort()));
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public  void startListener()
    {
        while (true)
        {
            try
            {
                Socket client = listenSocket.accept();
                //log
                PrettyPrinter.printTimestampLog(String.format("[MockSocketListener] Accepted client %s:%d", client.getInetAddress().getHostAddress(), client.getPort()));

                Thread clientHandle_t = new Thread(new ClientHandler(client));
                clientHandle_t.start();

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}

class ClientHandler implements Runnable
{
    private final Socket client;
    public ClientHandler(Socket client)
    {
        this.client = client;
    }

    @Override
    public void run()
    {
        try
        {
            //log
            PrettyPrinter.printTimestampLog(String.format("[MockSocketListener] Running listener for %s:%d", client.getInetAddress().getHostAddress(), client.getPort()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            while (!client.isClosed())
            {
                //read input message
                String input = reader.readLine();

                if (input != null)
                {
                    //json parsing
                    RingMessage message = new Gson().fromJson(input, RingMessage.class);

                    //set message source address
                    String ip = client.getInetAddress().getHostAddress();
                    int port = client.getPort();
                    String messageSource = String.format("%s:%d", ip, port);
                    message.setSourceAddress(messageSource);

                    //log
                    PrettyPrinter.printReceivedRingMessage(message);
                }
            }

            //dispose reader
            reader.close();
        }catch (Exception ex)
        {
            PrettyPrinter.printTimestampLog("ERROR");
            ex.printStackTrace();
        }
    }
}
