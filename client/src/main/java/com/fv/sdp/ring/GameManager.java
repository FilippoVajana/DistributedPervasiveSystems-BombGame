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

        String messageId = message.getId();
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
            RingMessage messageResponse = new RingMessage(MessageType.GAME, messageId, playerPositionMessage);
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

    //ACTION NOTIFIER
    private void notifyJoin(Player newPlayer)
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

    private void notifyMove(GridPosition playerPosition)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Notifing player movement", this.getClass().getSimpleName()));

        //build message
        String positionJson = new Gson().toJson(playerPosition, GridPosition.class);
        String playerJson = new Gson().toJson(appContext.getPlayerInfo(), Player.class);
        String messageData = String.format("MOVE#%s#%s", playerJson, positionJson); //TODO: message format
        RingMessage moveMessage = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageData);

        //build ack queue
        int ringNodesCount = appContext.RING_NETWORK.getList().size();
        appContext.ACK_HANDLER.addPendingAck(moveMessage.getId(), ringNodesCount, moduleLock);

        //send message
        appContext.SOCKET_CONNECTOR.sendMessage(moveMessage, SocketConnector.DestinationGroup.ALL); //TODO: check result

        //wait ack
        synchronized (moduleLock)
        {
            try
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Waiting movement ACK", this.getClass().getSimpleName()));

                moduleLock.wait();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    //ACTION ENDPOINT
    public boolean joinMatchGrid(Player player, Match match) //TODO: add GameEngine init with Match, remove from RESTConnector
    {
        //set player game match
        appContext.PLAYER_MATCH = match;

        //set network ring
        appContext.RING_NETWORK = match.getPlayers();

        //init game engine
        if (appContext.PLAYER_MATCH == null)
        {
            gameEngine = new GameEngine(appContext.PLAYER_MATCH);
        }
        else
        {
            initGameEngine();
        }

        //add player to game grid
        try
        {
            //check if match first player
            if (appContext.RING_NETWORK.size() <= 1)
            {
                //set token
                appContext.TOKEN_MANAGER.storeToken();

                //direct add player to grid
                addPlayerToGrid(player);
            }
            else
            {
                //start new player notification process
                notifyJoin(player);
            }
        }catch (Exception ex)
        {
            //log
            PrettyPrinter.printTimestampError(String.format("[%s] ERROR", this.getClass().getSimpleName()));
            ex.printStackTrace();

            return false;
        }

        return true;
    }



    //HELPER METHOD
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
        appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.ALL); //check sync


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
                    queueSignal.wait(5000); //timeout before another check on while condition
                }

            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        //compute position
        GridPosition startingPosition = gameEngine.setStartingPosition(occupiedPositions);
        //set position
        //gameEngine.gameGrid.setPlayerPosition(startingPosition); //already in setStartingPosition()

        //release ring token
        appContext.TOKEN_MANAGER.releaseToken();
    }
    private void addPlayerToGrid(Player player)
    {
        //set player position
        GridPosition startingPosition = gameEngine.setStartingPosition(new ConcurrentObservableQueue<>());

        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Setting first player position (%d,%d)", this.getClass().getSimpleName(), startingPosition.x, startingPosition.y));
    }
    public GridPosition getPlayerPosition()
    {
        return gameEngine.gameGrid.getPlayerPosition();
    }


    public boolean movePlayer(String movementKey)
    {
        //remove case sensitiveness
        movementKey = movementKey.toUpperCase();

        //parse movement
        switch (movementKey)
        {
            case "W":
                gameEngine.moveUp();
                break;
            case "S":
                gameEngine.moveDown();
                break;
            case "A":
                gameEngine.moveLeft();
                break;
            case "D":
                gameEngine.moveRight();
        }

        //get new player position
        GridPosition playerPosition = getPlayerPosition();
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Player moved to (%d,%d)", this.getClass().getSimpleName(), playerPosition.x, playerPosition.y));

        notifyMove(playerPosition);

        return true;
    }

    public void handleMovement(RingMessage receivedMessage)
    {
        //message content
        String messageData = receivedMessage.getContent();

        //get player
        Player movedPlayer = new Gson().fromJson(messageData.split("#")[1], Player.class);

        //check player id
        if (movedPlayer.equals(appContext.getPlayerInfo()) == false) //sender != receiver
        {
            //get position
            GridPosition movedPosition = new Gson().fromJson(messageData.split("#")[2], GridPosition.class);

            //check for overlay
            if (movedPosition.equals(gameEngine.gameGrid.getPlayerPosition()))
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Player %s killed by %s", this.getClass().getSimpleName(), appContext.getPlayerInfo().getId(), movedPlayer.getId()));
                //call palyer killed procedure

                return;
            }
        }

        //send back ack
        RingMessage ackMessage = new RingMessage(MessageType.ACK, receivedMessage.getId());
        ackMessage.setSourceAddress(receivedMessage.getSourceAddress()); //hack
        appContext.SOCKET_CONNECTOR.sendMessage(ackMessage, SocketConnector.DestinationGroup.SOURCE);
    }


    //public void handleBomb(RingMessage message){}
    //private void notifyBomb(GameAction bomb){}
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

    public GridPosition setStartingPosition(ConcurrentObservableQueue<GridPosition> occupiedPositions)
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
            int x = rndGen.nextInt(gameGrid.getGridEdge());
            int y = rndGen.nextInt(gameGrid.getGridEdge());
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
                //set player position
                gameGrid.setPlayerPosition(candidatePosition);
                return candidatePosition;
            }
        }
    }

    public GridPosition moveUp()
    {
        //get present player position
        GridPosition playerPosition = gameGrid.getPlayerPosition();

        //move player
        playerPosition.x = (playerPosition.x + 1)%gameGrid.getGridEdge();

        //set new position
        gameGrid.setPlayerPosition(playerPosition);

        return playerPosition;
    }

    public GridPosition moveDown()
    {
        //get present player position
        GridPosition playerPosition = gameGrid.getPlayerPosition();

        //move player
        playerPosition.x = (playerPosition.x - 1)%gameGrid.getGridEdge();
        if (playerPosition.x < 0)
            playerPosition.x = gameGrid.getGridEdge() - 1;

        //set new position
        gameGrid.setPlayerPosition(playerPosition);

        return playerPosition;
    }

    public GridPosition moveRight()
    {
        //get present player position
        GridPosition playerPosition = gameGrid.getPlayerPosition();

        //move player
        playerPosition.y = (playerPosition.y + 1)%gameGrid.getGridEdge();

        //set new position
        gameGrid.setPlayerPosition(playerPosition);

        return playerPosition;
    }

    public GridPosition moveLeft()
    {
        //get present player position
        GridPosition playerPosition = gameGrid.getPlayerPosition();

        //move player
        playerPosition.y = (playerPosition.y - 1)%gameGrid.getGridEdge();
        if (playerPosition.y < 0)
            playerPosition.y = gameGrid.getGridEdge() - 1;

        //set new position
        gameGrid.setPlayerPosition(playerPosition);

        return playerPosition;
    }
}

enum GridSector {Green, Red, Yellow, Blue};

