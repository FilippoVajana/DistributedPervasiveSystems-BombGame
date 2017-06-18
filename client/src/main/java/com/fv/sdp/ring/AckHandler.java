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
    private Map<String, Object> observerSynlockMap; //retain a map between observer - ack queue

    private AckHandler()
    {
        //init map
        queueMap = new HashMap<>();
        //init synlock map
        observerSynlockMap = new HashMap<>();
    }

    public synchronized void addPendingAck(String id, int num, Object observerSynlock)
    {
        //init queue
        queueMap.put(id, num);

        //set token
        observerSynlockMap.put(id, observerSynlock);
    }

    //TODO test
    @Override
    public synchronized void handle(RingMessage receivedMessage)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("Handling ACK-%s", receivedMessage.getId()));

        //find message ack queue
        Integer ackCount = queueMap.get(receivedMessage.getId());
        //decrement ack count
        ackCount--;

        if (ackCount == 0)
        {
            //notify action module (GameHandler/TokenHandler)
            observerSynlockMap.get(receivedMessage.getId()).notify();

            //delete empty queue
            queueMap.remove(receivedMessage.getId());
        }
    }
}
