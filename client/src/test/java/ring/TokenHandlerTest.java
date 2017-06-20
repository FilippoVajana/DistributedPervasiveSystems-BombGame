package ring;

import com.fv.sdp.SessionConfig;
import com.fv.sdp.model.Player;
import com.fv.sdp.ring.TokenHandler;
import com.fv.sdp.ring.TokenManager;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.ConcurrentList;
import com.fv.sdp.util.RandomIdGenerator;
import org.junit.Assert;
import org.junit.Test;
import util.MockSocketListener;

/**
 * Created by filip on 6/18/2017.
 */
public class TokenHandlerTest //todo implements test
{
    @Test
    public void handleMessage() throws Exception
    {
        //start mock listener
        MockSocketListener mockListener = new MockSocketListener();
        Runnable listenerTask = () -> mockListener.startListener();
        Thread listenerThread = new Thread(listenerTask);
        listenerThread.start();

        Thread.sleep(500);

        //add mock listener as ring node
        ConcurrentList<Player> nodes = new ConcurrentList<>();
        Player mockPlayer = new Player("MockPlayer", mockListener.listenSocket.getInetAddress().getHostAddress(), mockListener.listenSocket.getLocalPort());
        nodes.add(mockPlayer);

        //set ring nodes
        SessionConfig.getInstance().RING_NODE = nodes;

        //build message
        RingMessage inMessage = new RingMessage(MessageType.TOKEN, RandomIdGenerator.getRndId());
        inMessage.setSourceAddress(String.format("%s:%d", mockPlayer.getAddress(), mockPlayer.getPort()));

        //send message to handler
        TokenHandler.getInstance().handle(inMessage);

        //check
        Assert.assertTrue(TokenManager.getInstance().isHasToken());

        Thread.sleep(500);

    }
}
