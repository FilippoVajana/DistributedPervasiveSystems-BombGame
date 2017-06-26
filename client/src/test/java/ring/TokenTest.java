package ring;

import com.fv.sdp.SessionConfig;
import com.fv.sdp.model.Player;
import com.fv.sdp.ring.NodeManager;
import com.fv.sdp.ring.TokenHandler;
import com.fv.sdp.ring.TokenManager;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.ConcurrentList;
import com.fv.sdp.util.RandomIdGenerator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import util.MockSocketListener;

import java.util.ArrayList;

public class TokenTest
{
    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested

    private void buildTestRing()
    {
        ArrayList<Player> ring = new ArrayList<>();
        for (int i=0; i<3; i++)
        {
            NodeManager node = new NodeManager();
            node.startupNode();
            Player pl = new Player(String.format("PL%d", i), node.getListenerSocket().getListenerAddress().getHostAddress(), node.getListenerSocket().getListenerPort());

            ring.add(pl);
        }
        //set session ring
        SessionConfig.getInstance().RING_NETWORK = new ConcurrentList<>(ring);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void handleStoreTokenDirect() throws Exception
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
        SessionConfig.getInstance().RING_NETWORK = nodes;

        //build message
        RingMessage inMessage = new RingMessage(MessageType.TOKEN, RandomIdGenerator.getRndId(), "TEST TOKEN MESSAGE");
        inMessage.setSourceAddress(String.format("%s:%d", mockPlayer.getAddress(), mockPlayer.getPort()));

        //send message to handler
        TokenHandler.getInstance().handle(inMessage);

        //check
        Assert.assertTrue(TokenManager.getInstance().isHasToken());

        Thread.sleep(500);
    }

    @Test
    public void handleReleaseToken() throws Exception
    {
        //init node
        NodeManager node = new NodeManager();
        Assert.assertNotNull(node);

        //startup node
        node.startupNode();
        Thread.sleep(1000);

        //init fake ring
        ConcurrentList<Player> ring = new ConcurrentList<>();
        ring.add(SessionConfig.getInstance().getPlayerInfo());
        SessionConfig.getInstance().RING_NETWORK = ring;

        //release token
        TokenManager.getInstance().releaseToken();

        Thread.sleep(1000);
    }

    @Test
    public void handleRingReleaseToken() throws Exception
    {
        //build ring
        buildTestRing();

        TokenManager.getInstance().releaseToken();
        Thread.sleep(2000);
    }

    @Test
    public void handleStoreTokenMessage() throws Exception
    {
        //init target node
        NodeManager node = new NodeManager();
        Assert.assertNotNull(node);

        //startup node
        node.startupNode();
        Thread.sleep(1000);

        //init fake ring
        ConcurrentList<Player> ring = new ConcurrentList<>();
        ring.add(SessionConfig.getInstance().getPlayerInfo());
        SessionConfig.getInstance().RING_NETWORK = ring;

        //send fake token message
        RingMessage fakeToken = new RingMessage(MessageType.TOKEN, RandomIdGenerator.getRndId());
        //fakeToken.setSourceAddress(String.format("%s:%d", SessionConfig.getInstance().LISTENER_ADDR, SessionConfig.getInstance().LISTENER_PORT));
        new SocketConnector().sendMessage(fakeToken, SocketConnector.DestinationGroup.NEXT);

        Thread.sleep(3000);

        /*
        l'errore riscontrato (NullPointer) deriva dal fatto che la sorgente del messaggio Token, non avendo
        utilizzato la procedura prevista, pur aspettandosi un ACK di risposta non ha predisposto una entry
        all'interno della coda in AckHandler.
        Soluzione: utilizzare, dopo validazione, la procedura corretta di rilascio del token
         */
    }
}
