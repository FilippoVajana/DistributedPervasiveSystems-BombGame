package com.fv.sdp.ring;

import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.PrettyPrinter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by filip on 6/11/2017.
 */
public class AckHandler implements IMessageHandler
{
    private static AckHandler instance = null;
    public static AckHandler getInstance()
    {
        if (instance == null)
            instance = new AckHandler();
        return instance;
    }

    private Map<String, Integer> queueMap; //retain a map of pending ack to be received (id:count)
    private Map<String, Object> observerMap; //retain a map between <ack queue> - <observer lock>

    private AckHandler()
    {
        //log
        PrettyPrinter.printClassInit(this);

        //init map
        queueMap = new HashMap<>();
        //init synlock map
        observerMap = new HashMap<>();
    }

    public synchronized void addPendingAck(String idAck, int requiredAck, Object observerLock)
    {
        //init queue
        queueMap.put(idAck, requiredAck);
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Setting queue ACK %s (size: %d)", this.getClass().getSimpleName(), idAck, requiredAck));

        //set token
        observerMap.put(idAck, observerLock);
    }

    //TODO test
    @Override
    public synchronized void handle(RingMessage receivedMessage)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Handling ACK %s", this.getClass().getSimpleName(), receivedMessage.getId()));

        //find message ack queue
        Integer ackCount = queueMap.get(receivedMessage.getId());
        //decrement ack count
        ackCount--;

        if (ackCount == 0)
        {
            //get action module lock
            Object moduleLock =  observerMap.get(receivedMessage.getId());
            synchronized (moduleLock)
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Clearing ACK %s", this.getClass().getSimpleName(),  receivedMessage.getId()));

                //notify action module (GameHandler/TokenHandler)
                moduleLock.notify();
            }

            //delete empty queue
            queueMap.remove(receivedMessage.getId());
        }
    }
}
