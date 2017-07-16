package com.fv.sdp.ring;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.PrettyPrinter;
import com.fv.sdp.util.RandomIdGenerator;

import javax.validation.constraints.NotNull;

/**
 * Created by filip on 6/16/2017.
 */
public class TokenManager
{
    //app context
    private ApplicationContext appContext;

    public TokenManager(@NotNull ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //init module lock
        moduleLock = new Object();

        //init token signal
        tokenStoreSignal = new Object();

        //save app context
        this.appContext = appContext;
    }

    private boolean hasToken = false;
    private Object tokenStoreSignal;

    private Object moduleLock; //TODO: test module lock system

    public boolean isHasToken()
    {
        return hasToken;
    }
    public Object getTokenStoreSignal() { return tokenStoreSignal; }
    public Object getModuleLock()
    {
        return moduleLock;
    }

    public void storeToken()
    {
        synchronized (moduleLock)
        {
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] Storing token", appContext.getPlayerInfo().getId()));
            hasToken = true;

            //signal token store
            synchronized (tokenStoreSignal)
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Signaling token stored", appContext.getPlayerInfo().getId()));
                tokenStoreSignal.notify(); //TODO: check
            }
        }
    }

    public synchronized void releaseToken()
    {
        synchronized (moduleLock)
        {
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] Releasing token", appContext.getPlayerInfo().getId()));
            System.out.println("\n\nThere is only one Lord of the Ring,\nonly one who can bend it to his will.\nAnd he does not share power.\n\n");

            //create new token message
            RingMessage tokenMessage = new RingMessage(MessageType.TOKEN, new RandomIdGenerator().getRndId());

            //build ack queue
            Object ackWaitLock = new Object();
            appContext.ACK_HANDLER.addPendingAck(tokenMessage.getId(), 1, ackWaitLock); //ack from token receiver

            //send message via socket
            appContext.SOCKET_CONNECTOR.sendMessage(tokenMessage, SocketConnector.DestinationGroup.NEXT);

            synchronized (ackWaitLock)
            {
                try
                {
                    //log
                    PrettyPrinter.printTimestampLog(String.format("[%s] Waiting token release ACK", appContext.getPlayerInfo().getId()));

                    ackWaitLock.wait(); //wait message ACK
                    //release token
                    hasToken = false;
                    //log
                    PrettyPrinter.printTimestampLog(String.format("[%s] Token released", appContext.getPlayerInfo().getId()));
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
