package com.fv.sdp.ring;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.ConcurrentObservableQueue;
import com.fv.sdp.util.PrettyPrinter;
import com.fv.sdp.util.RandomIdGenerator;
import com.google.gson.Gson;

import javax.validation.constraints.NotNull;
import java.util.Random;

/**
 * Created by filip on 25/06/2017.
 */
public class GameManager
{
    private ApplicationContext appContext;
    private Object moduleLock = new Object(); //used with ACK handler
    public GameEngine gameEngine;

    public GameManager(ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //save app context
        this.appContext = appContext;
        this.appContext.GAME_MANAGER = this;
    }

    public void initGameEngine()
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] INIT GameEngine", this.getClass().getSimpleName()));

        gameEngine = new GameEngine(appContext.PLAYER_MATCH);
    }

    //MESSAGE HANDLER
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

    public synchronized void handleCheckPosition(RingMessage message) //TODO: test
    {
        /*
        2 cases
        1) receive a check-position request  -> send check-position reply
        2) receive a check-position reply -> route to occupied queue
         */

        String messageData = message.getContent();
        //check message type
        try
        {
            //POSITION CHECK RESPONSE (CHECK-POSITION#player_position_json)

            //parse node position
            String positionJson = messageData.split("#")[1];
            GridPosition position = new Gson().fromJson(positionJson, GridPosition.class);

            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] Handling position check response %s", this.getClass().getSimpleName(), message.getId()));

            //add position to occupied positions queue
            occupiedPositions.push(position);

            return;

        }catch (IndexOutOfBoundsException ex)
        {
            //POSITION CHECK REQUEST (CHECK-POSITION#null)

            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] Handling position check request %s", this.getClass().getSimpleName(), message.getId()));

            //get player position
            GridPosition playerPosition = gameEngine.gameGrid.getPlayerPosition();

            //check for auto-message
            if (playerPosition == null)
            {
                playerPosition = new GridPosition(-1, -1); //position out of grid
            }

            //build message
            String playerPositionJson = new Gson().toJson(playerPosition, GridPosition.class);
            String playerPositionMessage = String.format("CHECK-POSITION#%s", playerPositionJson);
            RingMessage messageResponse = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), playerPositionMessage);
            messageResponse.setSourceAddress(message.getSourceAddress());

            //send message
            if (appContext.TOKEN_MANAGER.isHasToken() == false)
            {
                appContext.TOKEN_MANAGER.storeToken(); //token hack for not-ACK response
                appContext.SOCKET_CONNECTOR.sendMessage(messageResponse, SocketConnector.DestinationGroup.SOURCE);
                appContext.TOKEN_MANAGER.releaseTokenSilent(); //token hack
            }
            else
            {
                appContext.SOCKET_CONNECTOR.sendMessage(messageResponse, SocketConnector.DestinationGroup.SOURCE);
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

    //ACTION NOTIFICATION
    public void notifyJoin(Player newPlayer)
    {
        //init game engine
        if (appContext.PLAYER_MATCH == null)
        {
            gameEngine = new GameEngine(appContext.PLAYER_MATCH);
        }
        else
        {
            initGameEngine();
        }

        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Notify new player %s", this.getClass().getSimpleName(), newPlayer.getId()));

        //build message
        String playerJson = new Gson().toJson(newPlayer, Player.class);
        String messageContent = String.format("ENTER-PLAYER#%s", playerJson);
        RingMessage message = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageContent);

        //build ack queue
        int ringNodesCount = appContext.RING_NETWORK.getList().size();
        appContext.ACK_HANDLER.addPendingAck(message.getId(), ringNodesCount, moduleLock);

        //send join message
        if (appContext.TOKEN_MANAGER.isHasToken() == false)
        {
            appContext.TOKEN_MANAGER.storeToken(); //Token hack
            appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.ALL);
            appContext.TOKEN_MANAGER.releaseTokenSilent(); //Token hack
        }
        else
        {
            appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.ALL);
        }


        //set player position
        synchronized (moduleLock)
        {
            try
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Waiting new player ACK", this.getClass().getSimpleName()));

                moduleLock.wait(); //wait ACK //TODO: set timeout?
                //TODO: check if this node/player must add himself to his local RING_NETWORK view

                //set player starting position
                setPlayerStartingPosition();

            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
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

                //token dispose
                appContext.TOKEN_MANAGER.releaseToken();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }



    private ConcurrentObservableQueue<GridPosition> occupiedPositions;
    private void setPlayerStartingPosition() //TODO: test
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Setting player starting position", this.getClass().getSimpleName()));

        //wait ring token
        Object hasTokenSignal = appContext.TOKEN_MANAGER.getHasTokenSignal();
        if (appContext.TOKEN_MANAGER.isHasToken() == false)
        {
            try
            {
                synchronized (hasTokenSignal)
                {
                    //log
                    PrettyPrinter.printTimestampLog(String.format("[%s] Waiting token", this.getClass().getSimpleName()));
                    hasTokenSignal.wait();
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        //build message
        String messageContent = String.format("CHECK-POSITION");
        RingMessage message = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageContent);

        //init response queue
        occupiedPositions = new ConcurrentObservableQueue<>();
        Object queueSignal = occupiedPositions.getQueueSignal(); //activated on queue.push()

        //send message
        appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.ALL); //check token

        //loop on response
        while (occupiedPositions.size() != appContext.RING_NETWORK.getList().size()) //check condition
        {
            try
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Waiting all CHECK-POSITION responses", this.getClass().getSimpleName()));

                synchronized (queueSignal)
                {
                    //wait response
                    queueSignal.wait();
                }

            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        //compute position
        GridPosition startingPosition = gameEngine.computeFreePosition(occupiedPositions);

        //set position
        gameEngine.gameGrid.setPlayerPosition(startingPosition);

        //release ring token
        appContext.TOKEN_MANAGER.releaseToken();
    }

    //public void handleMove(RingMessage message){}
    //public void notifyMove(GameAction movement){}

    //public void handleBomb(RingMessage message){}
    //public void notifyBomb(GameAction bomb){}
}



class GameEngine
{
    public Grid gameGrid;
    private int  playerScore;

    public GameEngine(Match match)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //init grid
        gameGrid = new Grid(match.getEdgeLength());

        //init player score
        playerScore = 0;
    }

    public GridPosition computeFreePosition(ConcurrentObservableQueue<GridPosition> occupiedPositions)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Computing player starting position", this.getClass().getSimpleName()));

        //random gen
        Random rndGen = new Random();

        //compute random position
        boolean positionClear = true;
        while (1 == 1)
        {
            //TODO: check bounds
            int x = rndGen.nextInt(gameGrid.getGridEdge() + 1);
            int y = rndGen.nextInt(gameGrid.getGridEdge() + 1);
            GridPosition candidatePosition = new GridPosition(x, y);

            //check position
            for (GridPosition pos : occupiedPositions.getQueue())
            {
                if (candidatePosition.equals(pos))
                {
                    positionClear = false;
                }
            }

            if (positionClear)
            {
                return new GridPosition(x, y);
            }
        }
    }
}

enum GridSector {Green, Red, Yellow, Blue};

class Grid
{
    //grid params
    private final int gridEdge; //TODO: check value

    private GridPosition playerPosition;
    private GridSector playerSector;

    public Grid(int edge)
    {
        gridEdge = edge;
    }

    //player position get/set
    public void setPlayerPosition(GridPosition position)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Player located at (%d,%d)", this.getClass().getSimpleName(), position.x, position.y));

        playerPosition = position;
        playerSector = setPlayerSector(position);
    }
    public GridPosition getPlayerPosition()
    {
        return playerPosition;
    }

    //grid edge get
    public int getGridEdge() {
        return gridEdge;
    }

    private GridSector setPlayerSector(@NotNull GridPosition position)
    {
        int x = position.x;
        int y = position.y;

        if (x < gridEdge/2 && y < gridEdge/2)
        {
            return GridSector.Blue;
        }
        if (x < gridEdge/2 && y >= gridEdge/2)
        {
            return GridSector.Green;
        }
        if (x >= gridEdge/2 && y < gridEdge/2)
        {
            return GridSector.Yellow;
        }
        if (x >= gridEdge/2 && y >= gridEdge/2)
        {
            return GridSector.Red;
        }

        //return default
        return GridSector.Blue;
    }
    public GridSector getPlayerSector()
    {
        return playerSector;
    }
}

class GridPosition
{
    public int x, y;

    public GridPosition(int playerX, int playerY)
    {
        x = playerX;
        y = playerY;
    }

    public static boolean equals(GridPosition pos1, GridPosition pos2)
    {
        if (pos1.x == pos2.x && pos1.y == pos2.y)
            return true;
        return false;
    }
}
