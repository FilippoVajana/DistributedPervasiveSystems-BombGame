package com.fv.sdp.ring;

import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.ConcurrentObservableQueue;
import com.fv.sdp.socket.ISocketObserver;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.util.PrettyPrinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** Funzionalità
    entry point per il main - funzioni di configuratore per i moduli lato client
    1- avvio listener
    2- avvio gestore code messaggi
    3- avvio GUI manager
 */
public class NodeManager implements ISocketObserver
{
    private MessageQueueManager queueManager;

    public NodeManager()
    {
        //log
        PrettyPrinter.printClassInit(this);

        //init queue manager
        queueManager = new MessageQueueManager();
    }

    public boolean startupNode()
    {
        try
        {
            //set this instance as observer for socket messages
            ArrayList<ISocketObserver> observersList = new ArrayList<>();
            observersList.add(this);
            //init socket connector
            SocketConnector connector = new SocketConnector(observersList);

            //init token manager
            TokenManager tokenManager = TokenManager.getInstance();

            //init ack handler
            AckHandler ackHandler = AckHandler.getInstance();
            queueManager.observeQueue(MessageType.ACK, ackHandler);

            //game handler
            //game observer

            //token handler
            TokenHandler tokenHandler = TokenHandler.getInstance();
            //token observer
            queueManager.observeQueue(MessageType.TOKEN, tokenHandler);

            //start socket listener
            connector.startListener();

            //startup GUI manager
            //exit on return

            return true;
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }


    /**
     * Push received message to a MessageQueueManager
     * @param message: massage to be pushed
     */
    @Override
    public void pushMessage(RingMessage message)
    {
        Runnable routeTask = () -> queueManager.routeMessage(message);
        Thread routeThread = new Thread(routeTask);
        routeThread.start();
    }
}


class MessageQueueManager
{
    private Map<MessageType, ConcurrentObservableQueue<RingMessage>> queuePool;

    public MessageQueueManager()
    {
        //log
        PrettyPrinter.printClassInit(this);

        //init hashmap
        queuePool = new HashMap<>();
        queuePool.put(MessageType.ACK, new ConcurrentObservableQueue<>());
        queuePool.put(MessageType.GAME, new ConcurrentObservableQueue<>());
        queuePool.put(MessageType.TOKEN, new ConcurrentObservableQueue<>());

        //TODO: run multiple observeQueue task
    }

    public void routeMessage(RingMessage message)
    {
        ConcurrentObservableQueue queue = queuePool.get(message.getType());
        //push message into specific queue
        queue.push(message);
    }

    //call foreach queue to be observed (game, ack, token)
    public void observeQueue(MessageType queueType, IMessageHandler messageHandler)
    {
        //log
        PrettyPrinter.printTimestampLog("Monitoring queue: " + queueType.name());

        //set queue
        ConcurrentObservableQueue<RingMessage> observedQueue = queuePool.get(queueType);
        //set queue lock
        Object queueLock = observedQueue.getQueueLock(); //get sync lock

        while (true)
        {
            RingMessage message = observedQueue.pop();
            if (message == null)
            {
                synchronized (queueLock)
                {
                    try
                    {
                        queueLock.wait(); //wait new message
                    }catch (Exception ex)
                    {
                        ex.printStackTrace();
                        return;
                    }
                }
            }
            else //at least one message available
            {
                //dispatch message to proper handler
                messageHandler.handle(message);
            }
        }
    }

}

