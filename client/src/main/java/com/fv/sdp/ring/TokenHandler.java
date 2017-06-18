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
    //TODO test
    @Override
    public void handle(RingMessage receivedMessage)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("Handling TOKEN-%s", receivedMessage.getId()));

        //set hasToken true
        TokenManager.getInstance().storeToken();

        //send back ack
        RingMessage ackMessage = new RingMessage(MessageType.ACK, receivedMessage.getId());
        //new SocketConnector().send() //todo implements send method

    }
}
