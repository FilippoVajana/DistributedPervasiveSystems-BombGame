package util;

import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.PrettyPrinter;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by filip on 19/06/2017.
 */
public class MockSocketClient
{
    private Socket client;
    private String lastMessageSent;
    private String lastMessageReceived;

    public String getLastMessageSent()
    {
        return lastMessageSent;
    }
    public String getLastMessageReceived()
    {
        return lastMessageReceived;
    }

    public MockSocketClient(InetAddress serverAddr, int serverPort)
    {
        try
        {
            client = new Socket(serverAddr, serverPort);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void sendMessage(RingMessage message, boolean waitResponse)
    {
        try
        {
            PrintWriter writer = new PrintWriter(client.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            //send message
            String outMessage = new Gson().toJson(message, RingMessage.class);
            writer.println(outMessage);
            writer.flush();
            lastMessageSent = outMessage;

            //log
            PrettyPrinter.printSentRingMessage(message, client.getInetAddress().getHostAddress(), client.getPort());

            //read response
            if (waitResponse)
            {
                String responseJson = reader.readLine();
                //check response data
                if (responseJson != null)
                {
                    RingMessage responseMessage = new Gson().fromJson(responseJson, RingMessage.class);
                    lastMessageReceived = responseMessage.toString();

                    //log
                    PrettyPrinter.printReceivedRingMessage(responseMessage);
                }
            }

            //reader.close();
            //client.close();
        } catch (IOException e)
        {
            e.printStackTrace();
            PrettyPrinter.printTimestampLog("ERROR");
        }
    }
}
