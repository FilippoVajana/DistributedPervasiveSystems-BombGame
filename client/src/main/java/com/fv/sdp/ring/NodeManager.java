package com.fv.sdp.ring;

import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.ConcurrentObservableQueue;
import com.fv.sdp.socket.ISocketObserver;
import com.fv.sdp.socket.MessageType;

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
    //TODO aggiungere gestione token
    public NodeManager()
    {    }

    public boolean startupNode()
    {
        try
        {
            //init socket message observer
            ArrayList<ISocketObserver> observersList = new ArrayList<>();
            observersList.add(this);
            //init socket connector
            SocketConnector connector = new SocketConnector(observersList);

            //start socket listener
            connector.startListener();

            //init token manager //TODO add Token Manager
            //init ack message handler
            //init game message handler

            //init queue manager
            queueManager = new MessageQueueManager();

            //startup GUI manager
                //init player profile
                //create match
                //enter match

            return true;
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

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
        System.out.println("Initializing " + this.getClass().getCanonicalName());

        //init hashmap
        queuePool = new HashMap<>();
        queuePool.put(MessageType.ACK, new ConcurrentObservableQueue<>());
        queuePool.put(MessageType.GAME, new ConcurrentObservableQueue<>());

        //TODO run multiple observeQueue task
    }

    public void routeMessage(RingMessage message)
    {
        //TODO run method on dedicated thread
        ConcurrentObservableQueue queue = queuePool.get(message.getType());
        //push message into respective queue
        queue.push(message);
    }

    //call foreach queue to be observed (game, ack)
    private void observeQueue(MessageType queueType, ConcurrentObservableQueue<RingMessage> observedQueue, IMessageHandler messageHandler)
    {
        //TODO run method on dedicated thread
        System.out.println("Monitoring queue: " + queueType.name());
        Object token = observedQueue.getQueueToken(); //get sync token

        while (true)
        {
            RingMessage message = observedQueue.pop();
            if (message == null)
            {
                try
                {
                    token.wait(); //wait new message
                }catch (Exception ex)
                {
                    ex.printStackTrace();
                    return;
                }
            }
            else //at least one message available
            {
                //dispatch message to proper observer
                messageHandler.handle(message);
            }
        }
    }

}

