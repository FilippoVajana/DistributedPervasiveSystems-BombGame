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

    public synchronized void handleCheckPosition(RingMessage message)
    {
        String messageId = message.getId();
        String messageData = message.getContent();

        //parse message data
        String[] messageDataComponents = messageData.split("#");

        //check message type
        if (messageDataComponents.length > 1)
        {
            //POSITION CHECK RESPONSE (CHECK-POSITION#player_position_json)
            //log
            PrettyPrinter.printTimestampLog(String.format("[%s] Handling position check response %s", this.getClass().getSimpleName(), message.getId()));

            //parse player position
            String positionJson = messageDataComponents[1];
            GridPosition position = new Gson().fromJson(positionJson, GridPosition.class);

            //add position to occupied positions queue
            occupiedPositions.push(position);

            return;
        }
        else
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
            messageResponse.setNeedToken(false);

            //send message
            appContext.SOCKET_CONNECTOR.sendMessage(messageResponse, SocketConnector.DestinationGroup.SOURCE);
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

    public void handleMovement(RingMessage receivedMessage)
    {
        //send back ack
        RingMessage ackMessage = new RingMessage(MessageType.ACK, receivedMessage.getId());
        ackMessage.setSourceAddress(receivedMessage.getSourceAddress()); //hack
        appContext.SOCKET_CONNECTOR.sendMessage(ackMessage, SocketConnector.DestinationGroup.SOURCE);

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

                //call player killed procedure
                playerKilled(movedPlayer);

                return;
            }
        }
    }

    public void handleKilledPlayer(RingMessage receivedMessage)
    {
        //send back ack
        RingMessage ackMessage = new RingMessage(MessageType.ACK, receivedMessage.getId());
        ackMessage.setSourceAddress(receivedMessage.getSourceAddress()); //hack
        appContext.SOCKET_CONNECTOR.sendMessage(ackMessage, SocketConnector.DestinationGroup.SOURCE);

        //get killed player
        String playerData = receivedMessage.getContent().split("#")[1];
        Player killedPlayer = new Gson().fromJson(playerData, Player.class);

        //remove killed node
        appContext.RING_NETWORK.remove(killedPlayer);

        //update score
        gameEngine.incrementPlayerScore();

        //notify gui
        appContext.GUI_MANAGER.notifyPlayerKill(killedPlayer);

        //check victory condition
        if (appContext.RING_NETWORK.size() == 1 && appContext.RING_NETWORK.contain(appContext.getPlayerInfo()))
        {
            //notify gui victory
            appContext.GUI_MANAGER.notifyPlayerWin();
        }
    }


    //ACTION NOTIFIER
    private void notifyJoin(Player newPlayer)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Notify new player %s", this.getClass().getSimpleName(), newPlayer.getId()));

        //build message
        String playerJson = new Gson().toJson(newPlayer, Player.class);
        String messageContent = String.format("ENTER-PLAYER#%s", playerJson);
        RingMessage joinMessage = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageContent);
        joinMessage.setNeedToken(false);

        //setup ack queue
        Object ackWaitLock = new Object();
        appContext.ACK_HANDLER.addPendingAck(joinMessage.getId(), appContext.RING_NETWORK.size(), ackWaitLock);

        //send join message
        appContext.SOCKET_CONNECTOR.sendMessage(joinMessage, SocketConnector.DestinationGroup.ALL);

        //set player position
        synchronized (ackWaitLock)
        {
            try
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Waiting new player notification ACK", this.getClass().getSimpleName()));

                ackWaitLock.wait(); //wait ACK
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

        //setup ack queue
        Object ackWaitLock = new Object();
        appContext.ACK_HANDLER.addPendingAck(message.getId(), appContext.RING_NETWORK.size(), ackWaitLock);

        //send message
        appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.ALL);

        //wait ack
        synchronized (ackWaitLock)
        {
            try
            {
                ackWaitLock.wait();

                //token dispose
                appContext.TOKEN_MANAGER.releaseToken(); //TODO: move to endpoint
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void notifyMove(GridPosition playerPosition)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Notifying player movement", this.getClass().getSimpleName()));

        //build message
        String positionJson = new Gson().toJson(playerPosition, GridPosition.class);
        String playerJson = new Gson().toJson(appContext.getPlayerInfo(), Player.class);
        String messageData = String.format("MOVE#%s#%s", playerJson, positionJson); //TODO: message format
        RingMessage moveMessage = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageData);

        //build ack queue
        Object ackWaitLock = new Object();
        appContext.ACK_HANDLER.addPendingAck(moveMessage.getId(), appContext.RING_NETWORK.size(), ackWaitLock);

        //send message
        appContext.SOCKET_CONNECTOR.sendMessage(moveMessage, SocketConnector.DestinationGroup.ALL); //TODO: check result

        //wait ack
        synchronized (ackWaitLock)
        {
            try
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Waiting movement ACK", this.getClass().getSimpleName()));

                ackWaitLock.wait();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    //ACTION ENDPOINT
    public boolean joinMatchGrid(Player player, Match match)
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
                addPlayerToGrid();
            }
            else
            {
                //start new player notification process
                notifyJoin(player);

                //set player starting position
                setPlayerStartingPosition();

                //release token
                appContext.TOKEN_MANAGER.releaseToken();
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
    public boolean leaveMatchGrid()
    {
        //notify
        notifyLeave(appContext.getPlayerInfo());

        //release token
        appContext.TOKEN_MANAGER.releaseToken();


        //clean app context
        appContext.PLAYER_MATCH = null;
        appContext.RING_NETWORK = null;

        return true;
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

        //notify other players
        notifyMove(playerPosition);

        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Player moved to (%d,%d)", this.getClass().getSimpleName(), playerPosition.x, playerPosition.y));

        //release token
        appContext.TOKEN_MANAGER.releaseToken();

        return true;
    }
    public boolean releaseBomb(GridBomb bomb)
    {
        return false;
    }



    //HELPER METHOD
    private ConcurrentObservableQueue<GridPosition> occupiedPositions;
    private void setPlayerStartingPosition()
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Setting player starting position", appContext.getPlayerInfo().getCompleteAddress()));

        //build message
        String messageContent = String.format("CHECK-POSITION");
        RingMessage message = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageContent);

        //init response queue
        occupiedPositions = new ConcurrentObservableQueue<>();
        Object queueSignal = occupiedPositions.getQueueSignal(); //activated on queue.push()

        //wait ring token
        Object hasTokenSignal = appContext.TOKEN_MANAGER.getTokenStoreSignal();
        while(appContext.TOKEN_MANAGER.isHasToken() == false)
        {
            try
            {
                synchronized (hasTokenSignal)
                {
                    //log
                    PrettyPrinter.printTimestampLog(String.format("[%s] Waiting token", appContext.getPlayerInfo().getCompleteAddress()));

                    hasTokenSignal.wait(1000);
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        //lock down token module during socket send
        synchronized (appContext.TOKEN_MANAGER.getModuleLock())
        {
            //send message
            appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.ALL); //check sync
        }

        //loop on response
        while (occupiedPositions.size() != appContext.RING_NETWORK.getList().size())
        {
            try
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Waiting all CHECK-POSITION responses", appContext.getPlayerInfo().getCompleteAddress()));

                synchronized (queueSignal)
                {
                    //wait response
                    queueSignal.wait(1000); //timeout before another check on while condition
                }

            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        //compute position
        GridPosition playerStartingPosition = gameEngine.setStartingPosition(occupiedPositions);
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Player %s start at position (%d,%d)", appContext.getPlayerInfo().getCompleteAddress(), appContext.getPlayerInfo().getId(), playerStartingPosition.x, playerStartingPosition.y));
    }
    private void addPlayerToGrid()
    {
        //set player position
        GridPosition startingPosition = gameEngine.setStartingPosition(new ConcurrentObservableQueue<>());

        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Setting first player position (%d,%d)", this.getClass().getSimpleName(), startingPosition.x, startingPosition.y));
    }
    private void playerKilled(Player killer)
    {
        //notify gui manager
        appContext.GUI_MANAGER.notifyPlayerKilled(killer);

        //notify killer
        notifyPlayerKilled(killer);
    }
    private void notifyPlayerKilled(Player killer)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Notify player %s killed", this.getClass().getSimpleName(), appContext.getPlayerInfo().getId()));

        //build message
        String playerKilledJson = new Gson().toJson(appContext.getPlayerInfo(), Player.class);
        String messageData = String.format("KILLED#%s", playerKilledJson);

        RingMessage killedMessage = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageData);
        killedMessage.setSourceAddress(killer.getCompleteAddress()); //hack
        killedMessage.setNeedToken(false);

        //prepare ack queue
        Object ackWaitLock = new Object();
        appContext.ACK_HANDLER.addPendingAck(killedMessage.getId(), 1, ackWaitLock);

        //send message
        appContext.SOCKET_CONNECTOR.sendMessage(killedMessage, SocketConnector.DestinationGroup.SOURCE);

        //wait ack
        synchronized (ackWaitLock)
        {
            try
            {
                //log
                PrettyPrinter.printTimestampLog(String.format("[%s] Wait on player killed ACK", this.getClass().getSimpleName()));
                ackWaitLock.wait(); //wait
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }


    //GRID GETTER/SETTER
    public GridPosition getPlayerPosition()
    {
        return gameEngine.gameGrid.getPlayerPosition();
    }
    public void setPlayerPosition(GridPosition position)
    {
        gameEngine.gameGrid.setPlayerPosition(position);
    }
    public int getPlayerScore()
    {
        return gameEngine.getPlayerScore();
    }

}

//GAME ENGINE
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

    public int getPlayerScore()
    {
        return playerScore;
    }
    public void incrementPlayerScore()
    {
        playerScore++;
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
        { //TODO: fix loop on collision
            int x = rndGen.nextInt(gameGrid.getGridEdge());
            int y = rndGen.nextInt(gameGrid.getGridEdge());
            GridPosition candidatePosition = new GridPosition(x, y);

            //check position
            for (GridPosition pos : occupiedPositions.getQueue())
            {
                if (candidatePosition.equals(pos))
                {
                    positionClear = false;
                    System.out.println(String.format("Position occupied (%d, %d)", candidatePosition.x, candidatePosition.y));
                    try
                    {
                        Thread.sleep(500);
                        break;
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                        break;
                    }
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

    public GridPosition moveRight()
    {
        //get present player position
        GridPosition playerPosition = gameGrid.getPlayerPosition();

        //move player
        playerPosition.x = (playerPosition.x + 1)%gameGrid.getGridEdge();

        //set new position
        gameGrid.setPlayerPosition(playerPosition);

        return playerPosition;
    }

    public GridPosition moveLeft()
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

    public GridPosition moveUp()
    {
        //get present player position
        GridPosition playerPosition = gameGrid.getPlayerPosition();

        //move player
        playerPosition.y = (playerPosition.y + 1)%gameGrid.getGridEdge();

        //set new position
        gameGrid.setPlayerPosition(playerPosition);

        return playerPosition;
    }

    public GridPosition moveDown()
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

