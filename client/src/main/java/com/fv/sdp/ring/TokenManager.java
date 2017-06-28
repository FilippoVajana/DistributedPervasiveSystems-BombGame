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

        //save app context
        this.appContext = appContext;
    }

    private boolean hasToken = false;
    private Object tokenLock = null;

    public boolean isHasToken()
    {
        return hasToken;
    }
    public Object getTokenLock() { return tokenLock; }

    public void storeToken()
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Storing token", this.getClass().getSimpleName()));
        hasToken = true;
    }

    public synchronized void releaseToken()
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Releasing token", this.getClass().getSimpleName()));
        System.out.println("\n\nThere is only one Lord of the Ring,\nonly one who can bend it to his will.\nAnd he does not share power.\n\n");

        //create new token message
        RingMessage tokenMessage = new RingMessage(MessageType.TOKEN, new RandomIdGenerator().getRndId());

        //build ack queue
        appContext.ACK_HANDLER.addPendingAck(tokenMessage.getId(), 1, tokenLock); //ack from token receiver

        //send message via socket
        appContext.SOCKET_CONNECTOR.sendMessage(tokenMessage, SocketConnector.DestinationGroup.NEXT);

        //wait ack
        synchronized (tokenLock)
        {
            try
            {
                System.out.println("WAIT TOKEN ACK");
                tokenLock.wait();
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
