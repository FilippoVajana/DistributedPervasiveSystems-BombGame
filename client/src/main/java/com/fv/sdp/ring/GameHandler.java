package com.fv.sdp.ring;

import com.fv.sdp.ApplicationContext;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.PrettyPrinter;

/**
 * Created by filip on 26/06/2017.
 */
public class GameHandler implements IMessageHandler
{
    ApplicationContext appContext;
    GameManager gameManager;

    public GameHandler(ApplicationContext appContext)
    {
        //log
        PrettyPrinter.printClassInit(this);

        //save context
        this.appContext = appContext;

        //init game manager module
        gameManager = new GameManager(this.appContext);
    }

    /*
    ENTER-PLAYER#
    EXIT-PLAYER#
    MOVE#
    BOMB#
     */
    @Override
    public void handle(RingMessage receivedMessage)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[%s] Handling GAME %s", this.getClass().getSimpleName(), receivedMessage.getId()));

        //get message type
        String messageType = receivedMessage.getContent().split("#")[0];

        //choose proper method
        switch (messageType)
        {
            case "ENTER-PLAYER":
                gameManager.handleNewPlayerRingEntrance(receivedMessage);
                break;
            case "EXIT-PLAYER":
                break;
            default:
                return;
        }

    }
}
