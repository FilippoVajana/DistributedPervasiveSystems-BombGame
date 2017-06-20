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
    Socket client;
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
            writer.println(new Gson().toJson(message, RingMessage.class));
            writer.flush();
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
                    //log
                    PrettyPrinter.printReceivedRingMessage(responseMessage);
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
            PrettyPrinter.printTimestampLog("ERROR");
        }
    }
}