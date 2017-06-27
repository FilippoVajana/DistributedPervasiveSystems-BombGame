package com.fv.sdp.ring;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.model.Player;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.PrettyPrinter;
import com.fv.sdp.util.RandomIdGenerator;
import com.google.gson.Gson;

/**
 * Created by filip on 25/06/2017.
 */
public class GameManager
{
    private ApplicationContext appContext;
    private Object moduleLock = new Object();

    public GameManager(ApplicationContext appContext)
    {
        this.appContext = appContext;
    }

    /**
     * Update players list. New player informations are stored into message content field as Json String
     * @param message
     */
    public void handleNewPlayerRingEntrance(RingMessage message)
    {
        //get player info
        String playerJson = message.getContent().split("#")[1]; //TODO: set message format
        Player newPlayer = new Gson().fromJson(playerJson, Player.class);

        //update ring topology
        appContext.RING_NETWORK.add(newPlayer); //TODO: check duplicated player

        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Added player %s", this.getClass().getSimpleName(), newPlayer.getId()));

        //send back ACK
        RingMessage response = new RingMessage(MessageType.ACK, message.getId());
        response.setSourceAddress(message.getSourceAddress()); //MAGIC HACK
        new SocketConnector().sendMessage(response, SocketConnector.DestinationGroup.SOURCE);
        //TODO: context.getSocketConnector().sendMessage()....
    }

    public void notifyRingEntrance(Player newPlayer)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Notify new player %s", this.getClass().getSimpleName(), newPlayer.getId()));

        //build message
        String playerJson = new Gson().toJson(newPlayer, Player.class);
        String messageContent = String.format("ENTER-PLAYER#%s", playerJson);
        RingMessage message = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageContent);

        //build ack queue
        int ringNodesCount = appContext.RING_NETWORK.getList().size();
        AckHandler.getInstance().addPendingAck(message.getId(), ringNodesCount, moduleLock);

        //send message
        new SocketConnector().sendMessage(message, SocketConnector.DestinationGroup.ALL);

        //wait ack
        synchronized (moduleLock)
        {
            try
            {
                moduleLock.wait();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}

//Maybe a GameMessageBuilder?
