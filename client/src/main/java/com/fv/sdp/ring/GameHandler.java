package com.fv.sdp.ring;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.PrettyPrinter;

import javax.validation.constraints.NotNull;

/**
 * Created by filip on 26/06/2017.
 */
public class GameHandler implements IMessageHandler
{
    //app context
    private ApplicationContext appContext;
    //game logic module
    private GameManager gameManager;

    public GameHandler(@NotNull ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //save context
        this.appContext = appContext;

        //init game manager module
        gameManager = new GameManager(this.appContext);
    }

    @Override
    public void handle(RingMessage receivedMessage)
    {
        //log
        //PrettyPrinter.printTimestampLog(String.format("[%s] Handling GAME %s", appContext.getPlayerInfo().getId(), receivedMessage.getId()));

        //get message type
        String messageType = receivedMessage.getContent().split("#")[0];

        //choose proper method
        switch (messageType)
        {
            case "ENTER-PLAYER":
                gameManager.handleJoin(receivedMessage);
                break;
            case "EXIT-PLAYER":
                gameManager.handleLeave(receivedMessage);
                break;
            case "CHECK-POSITION":
                gameManager.handleCheckPosition(receivedMessage);
                break;
            case "MOVE":
                gameManager.handleMovement(receivedMessage);
                break;
            case "KILLED":
                gameManager.handleKilledPlayer(receivedMessage);
                break;
            case "BOMB-RELEASE":
                gameManager.handleBombRelease(receivedMessage);
                break;
            case "BOMB-EXPLOSION":
                gameManager.handleBombExplosion(receivedMessage);
                break;
            case "BOMB-KILL":
                gameManager.handleBombKill(receivedMessage);
                break;
            case "GAME-END":
                gameManager.handleGameEnd(receivedMessage);
            default:
                return;
        }

    }
}
