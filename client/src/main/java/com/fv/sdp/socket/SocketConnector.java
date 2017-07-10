package com.fv.sdp.socket;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.model.Player;
import com.fv.sdp.util.PrettyPrinter;
import com.google.gson.Gson;

import javax.validation.constraints.NotNull;
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
    //app context
    private ApplicationContext appContext;

    private ServerSocket listeningServer;
    private List<ISocketObserver> observersList;

    public SocketConnector(@NotNull ApplicationContext appContext, List<ISocketObserver> observers, int listenerPort)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //save context
        this.appContext = appContext;

        //check observers list
        if (observers == null)
            observers = new ArrayList<>();
        observersList = observers;

        try
        {
            //create listener
            listeningServer = new ServerSocket(listenerPort);
            //update session config
            this.appContext.LISTENER_ADDR = listeningServer.getInetAddress().getHostAddress();
            this.appContext.LISTENER_PORT = listeningServer.getLocalPort();

        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    //Input Side
    public boolean startListener() //syncronous op.
    {
        //wait on client
        PrettyPrinter.printTimestampLog(String.format("[%s] Listening at %s:%d", this.getClass().getSimpleName(), getListenerAddress().getHostAddress(), getListenerPort()));

        while (true) //start stream reader foreach accepted connection
        {
            try {
                Socket client = listeningServer.accept();
                //set up listener
                SocketListenerRunner runner = new SocketListenerRunner(client, observersList, appContext);
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

    /**
     * Convert message to JSON format and then send it to destination
     * @param message
     * @param destination
     */
    private void send(RingMessage message, Player destination)
    {
        //set message origin
        String ip = appContext.LISTENER_ADDR;
        int port = appContext.LISTENER_PORT;
        String messageSource = String.format("%s:%d", ip, port);
        message.setSourceAddress(messageSource);
        try
        {
            //connect socket
            Socket connection = new Socket(InetAddress.getByName(destination.getAddress()), destination.getPort());
            //Thread.sleep(100);

            //log
            //PrettyPrinter.printTimestampLog(String.format("[%s] Created socket %s:%d", this.getClass().getSimpleName(), connection.getInetAddress().getHostAddress(), connection.getLocalPort()));
            //Thread.sleep(100);

            //get writer
            PrintWriter writer = new PrintWriter(connection.getOutputStream());

            //build json message
            String jsonMessage = new Gson().toJson(message);


            if (message.getNeedToken() == true) //TODO: check
            {
                //check token
                while (appContext.TOKEN_MANAGER.isHasToken() == false) //no token
                {
                    //token signal
                    Object tokenSignal = appContext.TOKEN_MANAGER.getTokenStoreSignal();
                    synchronized (tokenSignal)
                    {
                        //log
                        PrettyPrinter.printTimestampLog(String.format("[%s] Socket wait token for message %s - %s", appContext.getPlayerInfo().getId(), message.getType(), message.getId()));
                        //wait for token store
                        tokenSignal.wait(1000);
                    }
                }
            }

            //send message
            writer.println(jsonMessage);
            writer.flush();
            //log
            PrettyPrinter.printSentRingMessage(message, destination.getAddress(), destination.getPort(), appContext.getPlayerInfo());

            //close writer
            writer.close();
            //close socket
            connection.close();
        }catch (Exception ex)
        {
            //log
            PrettyPrinter.printTimestampLog(String.format("ERROR SENDING [%s - %s] TO %s:%d", message.getId(), message.getType(), destination.getAddress(), destination.getPort()));
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            ex.printStackTrace();
        }
    }

    /**
     * Send a RingMessage to a list of destinations.
     * Every send operation is performed on a dedicated thread.
     * Method return only after every thread finished its job.
     * @param message
     * @param destinations
     * @return true: if all send operations have succeeded
     */
    public boolean sendMessage(RingMessage message, List<Player> destinations)
    {
        //send threads list
        ArrayList<Thread> sendThreadList = new ArrayList<>();

        //launch send threads
        for (Player p : destinations)
        {
            //task
            Runnable sendTask = () -> send(message, p);
            //thread
            Thread sendThread = new Thread(sendTask);

            //add thread to list
            sendThreadList.add(sendThread);

            //start thread
            sendThread.start();
        }

        //wait send threads
        for (Thread t : sendThreadList)
        {
            try
            {
                t.join();
            }catch (InterruptedException ex)
            {
                ex.printStackTrace();
                return false;
            }
        }
        return true;
    }

    //TODO: caller must check return value
    /**
     * Initialize message destinations based on DestinationGroup param value.
     * @param message
     * @param dstGroup
     * @return true: if the message was sent to all destinations
     * @implNote the method use sendMessage(RingMessage, List) to actually send the message
     */
    public boolean sendMessage(RingMessage message, DestinationGroup dstGroup)
    {
       try
       {
           switch (dstGroup)
           {
               case ALL:
                   //call send
                   return sendMessage(message, appContext.RING_NETWORK.getList());

               case NEXT:
                   //destination
                   ArrayList<Player> destination = new ArrayList<>();
                   //find next node
                   destination.add(findNextNode());
                    //call send
                   return sendMessage(message, destination);

               case SOURCE:
                   //set destinations list
                   ArrayList<Player> dst = new ArrayList<>();

                   //get source address
                   String ip = message.getSourceAddress().split(":")[0];
                   int port = Integer.parseInt(message.getSourceAddress().split(":")[1]);

                   //build dummy player
                   Player sourcePlayer = new Player("DummyPlayer", ip, port);
                   dst.add(sourcePlayer);

                   //call send
                   return sendMessage(message, dst);
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

    private Player findNextNode()
    {
        Player thisNode = appContext.getPlayerInfo();
        Player nextNode;
        ArrayList<Player> ringNodes = appContext.RING_NETWORK.getList();

        try
        {
            int thisPlayerIndex = 0;
            for (Player p : ringNodes)
            {
                if (p.getCompleteAddress().equals(thisNode.getCompleteAddress()))
                {
                    break;
                }
                thisPlayerIndex++;
            }
            nextNode = ringNodes.get((thisPlayerIndex + 1));
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
    private ApplicationContext appContext;

    public SocketListenerRunner(Socket client, List<ISocketObserver> observers, ApplicationContext appContext) //set up listening thread
    {
        //log
        //PrettyPrinter.printClassInit(this);
        this.client = client;
        observersList = observers;

        //save context
        this.appContext = appContext;
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
        //PrettyPrinter.printTimestampLog(String.format("[%s] Handling client %s:%d", this.getClass().getSimpleName(), client.getInetAddress().getHostAddress(), client.getPort()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

        while (!client.isClosed())
        {
            //read input message
            String input = reader.readLine();

           if (input != null)
           {
               //json parsing
               RingMessage message = new Gson().fromJson(input, RingMessage.class);

               //print log
               PrettyPrinter.printReceivedRingMessage(message, appContext.getPlayerInfo());

               //dispatch message to observers
               for (ISocketObserver observer : observersList)
                   observer.pushMessage(message); //dispatch to NodeManager
           }
        }

        //log
        PrettyPrinter.printTimestampLog(String.format("Disconnected client %s:%d", client.getInetAddress().getHostAddress(), client.getPort()));

        //dispose reader
        reader.close();
        //dispose socket
        client.close();
    }
}
