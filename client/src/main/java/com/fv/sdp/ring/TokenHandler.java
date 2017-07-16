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

    public TokenHandler(@NotNull ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //save app context
        this.appContext = appContext;

        //init token logic
        appContext.TOKEN_MANAGER = new TokenManager(this.appContext);
    }

    /**
     * Handle received TokenMessage setting hasToken property and sending back an ACK message
     * @param receivedMessage: message to be handled
     */
    @Override
    public void handle(RingMessage receivedMessage)
    {
        //log
        //PrettyPrinter.printTimestampLog(String.format("[%s] Handling [TOKEN %s]", this.getClass().getSimpleName(), receivedMessage.getId()));

        //build ack message
        RingMessage ackMessage = new RingMessage(MessageType.ACK, receivedMessage.getId(), receivedMessage.getContent());
        //MAGIC HACK (send.SOURCE use message source address)
        ackMessage.setSourceAddress(receivedMessage.getSourceAddress());

        //send ack
        appContext.SOCKET_CONNECTOR.sendMessage(ackMessage, SocketConnector.DestinationGroup.SOURCE);


        //set hasToken true
        appContext.TOKEN_MANAGER.storeToken();
    }
}
