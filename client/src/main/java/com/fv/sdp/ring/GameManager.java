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
        //log
        PrettyPrinter.printClassInit(this);

        //save app context
        this.appContext = appContext;
        this.appContext.GAME_MANAGER = this;
    }

    /**
     * Add new player to the network ring. Player information are stored into message content field as Json String
     * @param message
     */
    public synchronized void handleJoin(RingMessage message)
    {
        //get player info
        String playerJson = message.getContent().split("#")[1]; //TODO: set message format
        Player newPlayer = new Gson().fromJson(playerJson, Player.class);

        //update ring topology
        if (!appContext.RING_NETWORK.contain(newPlayer)) //check duplicated player
        {
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] Added player %s", this.getClass().getSimpleName(), newPlayer.getId()));
            appContext.RING_NETWORK.add(newPlayer);
        }
        else
            PrettyPrinter.printTimestampLog(String.format("[%s] Player %s already added", this.getClass().getSimpleName(), newPlayer.getId()));

        //send back ACK
        RingMessage response = new RingMessage(MessageType.ACK, message.getId());
        response.setSourceAddress(message.getSourceAddress()); //MAGIC HACK
        appContext.SOCKET_CONNECTOR.sendMessage(response, SocketConnector.DestinationGroup.SOURCE);
    }
    public void notifyJoin(Player newPlayer)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Notify new player %s", this.getClass().getSimpleName(), newPlayer.getId()));

        //build message
        String playerJson = new Gson().toJson(newPlayer, Player.class);
        String messageContent = String.format("ENTER-PLAYER#%s", playerJson);
        RingMessage message = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageContent);

        //build ack queue
        int ringNodesCount = appContext.RING_NETWORK.getList().size();
        appContext.ACK_HANDLER.addPendingAck(message.getId(), ringNodesCount, moduleLock);

        //send message
       appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.ALL);//TODO: check result

        //wait ack
        synchronized (moduleLock)
        {
            try
            {
                moduleLock.wait(5000); //wait with 5s timeout
                //TODO: check if this node/player must add himself to his local RING_NETWORK view
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public synchronized void handleLeave(RingMessage message)
    {
        //get player json data
        String playerJson = message.getContent().split("#")[1];

        //get player
        Player player = new Gson().fromJson(playerJson, Player.class);

        //remove from ring
        boolean deleteResult = appContext.RING_NETWORK.remove(player);

        if (!deleteResult)
        {
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] Player %s already out of ring", this.getClass().getSimpleName(), player.getId()));
        }
        else
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] Player %s deleted", this.getClass().getSimpleName(), player.getId()));

        //send back ACK
        RingMessage response = new RingMessage(MessageType.ACK, message.getId());
        response.setSourceAddress(message.getSourceAddress()); //MAGIC HACK
        appContext.SOCKET_CONNECTOR.sendMessage(response, SocketConnector.DestinationGroup.SOURCE);
    }
    public void notifyLeave(Player player)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Notify player %s exit", this.getClass().getSimpleName(), player.getId()));

        //build message
        String playerJson = new Gson().toJson(player, Player.class);
        String messageContent = String.format("EXIT-PLAYER#%s", playerJson);
        RingMessage message = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageContent);

        //build ack queue
        int ringNodesCount = appContext.RING_NETWORK.getList().size();
        appContext.ACK_HANDLER.addPendingAck(message.getId(), ringNodesCount, moduleLock);

        //send message
        appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.ALL);//TODO: check result

        //wait ack
        synchronized (moduleLock)
        {
            try
            {
                moduleLock.wait(5000); //wait with 5s timeout
                //TODO: check if this node/player must add himself to his local RING_NETWORK view
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    //public void handleMove(RingMessage message){}
    //public void notifyMove(GameAction movement){}

    //public void handleBomb(RingMessage message){}
    //public void notifyBomb(GameAction bomb){}
}

class GameLogic
{

}
