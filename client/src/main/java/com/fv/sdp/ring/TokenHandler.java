package com.fv.sdp.ring;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.PrettyPrinter;

import javax.validation.constraints.NotNull;

/**
 * Created by filip on 6/18/2017.
 */
public class TokenHandler implements IMessageHandler
{
    //app context
    private ApplicationContext appContext;
    //tokek logic module
    private TokenManager tokenManager;

    public TokenHandler(@NotNull ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //save app context
        this.appContext = appContext;

        //init token logic
        tokenManager = new TokenManager();
    }

    //TODO: test
    /**
     * Handle received TokenMessage setting hasToken property and sending back an ACK message
     * @param receivedMessage: message to be handled
     */
    @Override
    public void handle(RingMessage receivedMessage)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Handling [TOKEN %s]", this.getClass().getSimpleName(), receivedMessage.getId()));

        //set hasToken true
        tokenManager.storeToken();

        //build ack message
        RingMessage ackMessage = new RingMessage(MessageType.ACK, receivedMessage.getId(), receivedMessage.getContent());
        ackMessage.setSourceAddress(receivedMessage.getSourceAddress()); //TODO: MAGIC HACK (send.SOURCE use message source address)

        //send ack
        new SocketConnector().sendMessage(ackMessage, SocketConnector.DestinationGroup.SOURCE);
    }
}
