package com.fv.sdp.socket;

import com.fv.sdp.model.Player;
import com.fv.sdp.util.PrettyPrinter;
import com.google.gson.Gson;
import com.fv.sdp.SessionConfig;

import javax.ws.rs.core.GenericType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 01/06/2017.
 */
public class SocketConnector
{
    private ServerSocket listeningServer;
    private final List<ISocketObserver> observersList;

    public SocketConnector(List<ISocketObserver> observers)
    {
        //log
        PrettyPrinter.printTimestampLog("Initialize " + this.getClass().getSimpleName());

        observersList = observers;
        try
        {
            //create listener
            listeningServer = new ServerSocket(0);
            //update session config
            SessionConfig.getInstance().LISTENER_ADDR = listeningServer.getInetAddress().getHostAddress();
            SessionConfig.getInstance().LISTENER_PORT = listeningServer.getLocalPort();

        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    //Input Side
    public boolean startListener() //syncronous op.
    {
        //wait on client
        PrettyPrinter.printTimestampLog(String.format("Listening on %s:%d", getListenerAddress().getHostAddress(), getListenerPort()));

        while (true) //fire up stream reader foreach accepted connection
        {
            try {
                Socket client = listeningServer.accept();
                //set up listener
                SocketListenerRunner runner = new SocketListenerRunner(client, observersList);
                //create thread
                Thread listenerThread = new Thread(runner);
                //start thread
                listenerThread.start();

            }catch (Exception ex)
            {
                ex.printStackTrace();
            }

        }
    }

    //Output Side
    public boolean sendMessage(RingMessage message, List<InetAddress> destinations) //TODO implementare invio messaggi socket
                                                                                                                                                                    //todo implements ring topology/ change to destinations Enum(NEXT, ALL, SERVER)
    {
        return false;
    }

    public int getListenerPort()
    {
        return listeningServer.getLocalPort();
    }
    public InetAddress getListenerAddress()
    {
        return listeningServer.getInetAddress();
    }
}


class SocketListenerRunner implements Runnable
{
    private final List<ISocketObserver> observersList; //not concurrent access
    private final Socket client;

    public SocketListenerRunner(Socket client, List<ISocketObserver> observers) //set up listening thread
    {
        //log
        PrettyPrinter.printTimestampLog("Initialize " + this.getClass().getSimpleName());

        this.client = client;
        observersList = observers;
    }

    @Override
    public void run()
    {
        try
        {
            runListener(); //listening loop
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void runListener() throws Exception
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("Running listener on %s:%d", client.getInetAddress().getHostAddress(), client.getPort()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

        while (!client.isClosed())
        {
            //read input message
            String input = reader.readLine();

            //json parsing
            RingMessage message = new Gson().fromJson(input, RingMessage.class);

            //set message source address
            String ip = client.getInetAddress().getHostAddress();
            int port = client.getPort();
            String messageSource = String.format("%s:%d", ip, port);
            message.setSourceAddress(messageSource);

            //print log
            PrettyPrinter.printReceivedRingMessage(message);

            //dispatch message to observers
            for (ISocketObserver obs : observersList)
                obs.pushMessage(message); //dispatch to NodeManager
        }

        //dispose reader
        reader.close();
    }
}
