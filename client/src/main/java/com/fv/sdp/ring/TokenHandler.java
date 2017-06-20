package com.fv.sdp.ring;

import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.PrettyPrinter;

/**
 * Created by filip on 6/18/2017.
 */
public class TokenHandler implements IMessageHandler
{
    private static TokenHandler instance;
    public static TokenHandler getInstance()
    {
        if (instance == null)
            instance = new TokenHandler();
        return instance;
    }
    private TokenHandler()
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("Initialize %s", this.getClass().getSimpleName()));
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
        PrettyPrinter.printTimestampLog(String.format("HANDLING [TOKEN - %s] FROM %s", receivedMessage.getId(), receivedMessage.getSourceAddress()));

        //set hasToken true
        TokenManager.getInstance().storeToken();

        //send back ack
        RingMessage ackMessage = new RingMessage(MessageType.ACK, receivedMessage.getId());
        ackMessage.setSourceAddress(receivedMessage.getSourceAddress()); //TODO: resolve this MAGIC HACK (send.SOURCE use message source address)
        new SocketConnector().sendMessage(ackMessage, SocketConnector.DestinationGroup.SOURCE);
    }
}
