package com.fv.sdp.ring;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.PrettyPrinter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by filip on 6/11/2017.
 */
public class AckHandler implements IMessageHandler
{
    private ApplicationContext appContext;
    private Map<String, Integer> queueMap; //map of pending ack to be received (id:count)
    private Map<String, Object> observerMap; //map between <ack queue> - <observer lock>

    public AckHandler(ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //init map
        queueMap = new HashMap<>();
        //init synlock map
        observerMap = new HashMap<>();

        //save handler into app context
        appContext.ACK_HANDLER = this;
        this.appContext = appContext;
    }

    public synchronized void addPendingAck(String idAck, int requiredAck, Object observerLock)
    {
        //init queue
        queueMap.put(idAck, requiredAck);
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Setting queue ACK %s (size: %d)", appContext.getPlayerInfo().getId(), idAck, requiredAck));

        //set token
        observerMap.put(idAck, observerLock);
    }

    public synchronized boolean isQueueEmpty(String idAck)
    {
        if (queueMap.get(idAck) != null)
        {
            return false;
        }
        return true;
    }

    @Override
    public synchronized void handle(RingMessage receivedMessage)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Handling ACK %s", appContext.getPlayerInfo().getId(), receivedMessage.getId()));

        //decrement ack count
        try
        {
            queueMap.put(receivedMessage.getId(), queueMap.get(receivedMessage.getId()) - 1);
        }catch (NullPointerException ex)
        {
            //log
            PrettyPrinter.printTimestampError(String.format("[%s] ERROR: Queue %s not found", appContext.getPlayerInfo().getId(), receivedMessage.getId()));

            return;
        }

        if (queueMap.get(receivedMessage.getId()) == 0)
        {
            //get action module lock
            Object moduleLock =  observerMap.get(receivedMessage.getId());
            synchronized (moduleLock)
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Clearing ACK queue %s", appContext.getPlayerInfo().getId(),  receivedMessage.getId()));

                //notify action module (GameHandler/TokenHandler)
                moduleLock.notify();
            }

            //delete empty ack queue
            queueMap.remove(receivedMessage.getId());

            //delete ack queue observer map
            observerMap.remove(receivedMessage.getId());
        }
    }
}
