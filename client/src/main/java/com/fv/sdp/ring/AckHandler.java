package com.fv.sdp.ring;

import com.fv.sdp.socket.RingMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by filip on 6/11/2017.
 */
public class AckHandler implements IMessageHandler
{
    private Map<String, Integer> queueMap; //retain a map of pending ack to be received (id:count)
    private Object tokenLock;

    public AckHandler(Object tokenLock)
    {
        //init map
        queueMap = new HashMap<>();
        this.tokenLock = tokenLock;
    }

    public void addPendingAck(String id, int num)
    {
        //init queue
        queueMap.put(id, num);
    }

    //TODO implement logic
    @Override
    public synchronized void handle(RingMessage message)
    {
        //find message ack queue
        Integer ackCount = queueMap.get(message.getId());
        //decrement ack count
        ackCount--;

        if (ackCount == 0)
        {
            queueMap.remove(message.getId());
            //notify Token Managar
            tokenLock.notify();
        }
    }
}
