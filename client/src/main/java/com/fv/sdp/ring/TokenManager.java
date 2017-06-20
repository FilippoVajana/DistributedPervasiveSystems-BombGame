package com.fv.sdp.ring;

import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.PrettyPrinter;
import com.fv.sdp.util.RandomIdGenerator;

/**
 * Created by filip on 6/16/2017.
 */
public class TokenManager
{
    private static TokenManager instance = null;
    private TokenManager()
    {
        //init token lock
        tokenLock = new Object();
    }
    public static TokenManager getInstance()
    {
        if (instance == null)
            instance = new TokenManager();
        return instance;
    }

    private boolean hasToken = false;
    private Object tokenLock = null;

    public boolean isHasToken()
    {
        return hasToken;
    }
    public Object getTokenLock() {return tokenLock;}

    public void storeToken()
    {
        synchronized (tokenLock)
        {
            //log
            PrettyPrinter.printTimestampLog("Storing ring token");
            hasToken = true;
            tokenLock.notify();
        }
    }

    public synchronized void releaseToken()
    {
        //log
        PrettyPrinter.printTimestampLog("Releasing ring token");

        //create new token message
        RingMessage tokenMessage = new RingMessage(MessageType.TOKEN, new RandomIdGenerator().getRndId(), new String(""));

        //build ack queue
        AckHandler.getInstance().addPendingAck(tokenMessage.getId(), 1, tokenLock); //ack from token receiver

        //send message via socket(next ring node) //todo ring topology



        //wait on all ack
        try
        {
            tokenLock.wait();
            //releas token
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
