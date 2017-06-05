package main.java.socket;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
        observersList = observers;
        try
        {
            listeningServer = new ServerSocket(0);
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public boolean startServer() //BLOCCANTE - generare thread dedicato
    {
        //wait on client
        System.out.println(String.format("Listening on %s:%d", getServerAddress(), getServerPort()));
        while (true)
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

    public int getServerPort()
    {
        return listeningServer.getLocalPort();
    }
    public InetAddress getServerAddress()
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

        while (!client.isClosed())
        {
            //read input message
            String input = reader.readLine();

            //dispatch message to observers
            for (ISocketObserver obs : observersList)
                obs.deliverMessage(input);
        }

        //dispose reader
        reader.close();
    }
}
