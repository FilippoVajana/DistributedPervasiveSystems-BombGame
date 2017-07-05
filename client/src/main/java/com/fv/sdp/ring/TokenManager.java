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

        //init token lock
        tokenLock = new Object();

        //init token signal
        tokenStoreSignal = new Object();

        //save app context
        this.appContext = appContext;
    }

    private boolean hasToken = false;
    private Object tokenStoreSignal = null;

    private Object moduleLock = new Object(); //TODO: add module lock system used by external caller (GameEngine)

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
            PrettyPrinter.printTimestampLog(String.format("[%s] Storing token", this.getClass().getSimpleName()));
            hasToken = true;

            //signal token awaiter
            synchronized (tokenStoreSignal)
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Signaling token stored", this.getClass().getSimpleName()));

                tokenStoreSignal.notifyAll();
            }
        }
    }

    public synchronized void releaseToken()
    {
        synchronized (moduleLock)
        {
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] Releasing token", this.getClass().getSimpleName()));
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
                    PrettyPrinter.printTimestampLog(String.format("[%s] Waiting token release ACK", this.getClass().getSimpleName()));

                    ackWaitLock.wait(); //wait message ACK
                    //release token
                    hasToken = false;
                    //log
                    PrettyPrinter.printTimestampLog(String.format("[%s] Token released", this.getClass().getSimpleName()));
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
