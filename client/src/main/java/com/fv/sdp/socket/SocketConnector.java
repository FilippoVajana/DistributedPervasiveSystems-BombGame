package com.fv.sdp.socket;

import com.fv.sdp.model.Player;
import com.fv.sdp.ring.TokenManager;
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
import java.util.stream.Stream;

/**
 * Created by filip on 01/06/2017.
 */
public class SocketConnector
{
    private ServerSocket listeningServer;
    private List<ISocketObserver> observersList; //TODO: make final after singleton implementation

    //TODO: singleton
    public SocketConnector(List<ISocketObserver> observers)
    {
        //log
        PrettyPrinter.printTimestampLog("Initialize " + this.getClass().getSimpleName());
        //check observers list
        if (observers == null)
            observers = new ArrayList<>();
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

    //TODO: remove method
    /**
     * Temporary method.
     * Use only for sendMessage()
     */
    public SocketConnector()
    {
        //log
        //PrettyPrinter.printTimestampLog("Initialize temporary" + this.getClass().getSimpleName());
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

    public enum DestinationGroup {ALL, NEXT, SOURCE};

    //TODO: return operation result
    private void send(RingMessage message, Player destination)
    {
        try
        {
            //connect socket
            Socket connection = new Socket(InetAddress.getByName(destination.getAddress()), destination.getPort());
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] Created socket %s:%d", this.getClass().getSimpleName(), connection.getInetAddress().getHostAddress(), connection.getLocalPort()));

            //get writer
            PrintWriter writer = new PrintWriter(connection.getOutputStream());

            //build json message
            String jsonMessage = new Gson().toJson(message);

            //check token
            if (!TokenManager.getInstance().isHasToken())
            {
                Object tokenLock = TokenManager.getInstance().getTokenLock();
                synchronized (tokenLock)
                {
                    PrettyPrinter.printTimestampLog("WAITING RING TOKEN");
                    tokenLock.wait();
                }
            }


            //log
            PrettyPrinter.printSentRingMessage(message, destination.getAddress(), destination.getPort());

            //send message
            writer.println(jsonMessage);
            writer.flush();

            //close writer
            writer.close();
            //close socket
            connection.close();
        }catch (Exception ex)
        {
            ex.printStackTrace();
            //log
            PrettyPrinter.printTimestampLog(String.format("ERROR SENDING [%s - %s] TO %s:%d", message.getId(), message.getType(), destination.getAddress(), destination.getPort()));
        }
    }

    //TODO: test
    //TODO: check send result
    //Output Side
    public boolean sendMessage(RingMessage message, List<Player> destinations)
    {
        for (Player p : destinations)
        {
            //task
            Runnable sendTask = () -> send(message, p);
            //thread
            Thread sendThread = new Thread(sendTask);
            //start thread
            sendThread.start();
        }
        return true;
    }

    //this method serves as router over DestinationGroup enum
    public boolean sendMessage(RingMessage message, DestinationGroup dstGroup)
    {
       try
       {
           switch (dstGroup)
           {
               case ALL:
                   //call send
                   sendMessage(message, SessionConfig.getInstance().RING_NODE.getList());
                   break;
               case NEXT:
                   //find next node
                   Player nextPlayer = findNextNode();
                    //call send
                   send(message, nextPlayer);
                   break;
               case SOURCE:
                   //destination
                   ArrayList<Player> destination = new ArrayList<>();
                   //build dummy player for source node
                   String ip = message.getSourceAddress().split(":")[0];
                   int port = Integer.parseInt(message.getSourceAddress().split(":")[1]);
                   Player sourcePlayer = new Player("DummyPlayer", ip, port);
                   destination.add(sourcePlayer);
                   //call send
                   sendMessage(message, destination);
                   break;
           }
       }catch (Exception ex)
       {
           ex.printStackTrace();
           return false;
       }
        return true;
    }

    private Player findNodeByAddress(String messageSourceAddress, ArrayList<Player> ringNodes)
    {
        //split message source address
        String ip = messageSourceAddress.split(":")[0];
        int port = Integer.parseInt(messageSourceAddress.split(":")[1]);

        //filter ring nodes
        Stream<Player> nodeStream = ringNodes.stream();
        Stream<Player> playerStream = nodeStream.filter(p -> p.getAddress().equals(ip) && p.getPort() == port);

        return playerStream.findFirst().get();
    }

    private Player findNextNode() //TODO: check
    {
        Player thisNode = SessionConfig.getInstance().getPlayerInfo();
        Player nextNode = null;
        ArrayList<Player> ringNodes = SessionConfig.getInstance().RING_NODE.getList();

        try
        {
            int thisPlayerIndex = 0;
            for (Player p : ringNodes)
            {
                if (p.getAddress().equals(thisNode.getAddress()))
                {
                    thisPlayerIndex++;
                    break;
                }
                thisPlayerIndex++;
            }
            nextNode = ringNodes.get(thisPlayerIndex);
        }catch (IndexOutOfBoundsException ex)
        {
            nextNode = ringNodes.get(0);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
        return nextNode;
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

           if (input != null)
           {
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
        }

        //dispose reader
        reader.close();
    }
}
