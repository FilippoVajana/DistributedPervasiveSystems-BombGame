package com.fv.sdp.ring;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.gui.GUIManager;
import com.fv.sdp.socket.ISocketObserver;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.ConcurrentObservableQueue;
import com.fv.sdp.util.PrettyPrinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NodeManager implements ISocketObserver
{
    //app context
    public ApplicationContext appContext;

    //network modules
    private MessageQueueManager queueManager;

    //functional modules
    private AckHandler ackHandler;
    private TokenHandler tokenHandler;
    private GameHandler gameHandler;
    private GUIManager guiManager;

    public NodeManager()
    {
        //log
        PrettyPrinter.printClassInit(this);

        //init app context
        appContext = new ApplicationContext();

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
            appContext.SOCKET_CONNECTOR = new SocketConnector(appContext, observersList, 0);

            //init ack handler
            ackHandler = new AckHandler(appContext);
            new Thread(() -> queueManager.observeQueue(MessageType.ACK, ackHandler)).start();

            //game handler
            gameHandler = new GameHandler(appContext);
            new Thread(() -> queueManager.observeQueue(MessageType.GAME, gameHandler)).start();

            //token handler
            tokenHandler = new TokenHandler(appContext);
            new Thread(() -> queueManager.observeQueue(MessageType.TOKEN, tokenHandler)).start();

            //start socket listener
            new Thread(() -> appContext.SOCKET_CONNECTOR.startListener()).start();

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
        new Thread(() -> queueManager.routeMessage(message)).start();
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
    }

    public Map<MessageType, ConcurrentObservableQueue<RingMessage>> getQueuePool()
    {
        return queuePool;
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

                        //debug
                        //PrettyPrinter.printQueuePool(getQueuePool());
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

