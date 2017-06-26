package ring;

import com.fv.sdp.SessionConfig;
import com.fv.sdp.model.Player;
import com.fv.sdp.ring.GameManager;
import com.fv.sdp.ring.NodeManager;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.ConcurrentList;
import com.fv.sdp.util.PrettyPrinter;
import com.fv.sdp.util.RandomIdGenerator;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Test;
import util.MockSocketListener;

import java.util.ArrayList;

public class GameTest
{
    //@Rule
    //public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested

    private ArrayList<Player> buildTestRing()
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

        return ring;
    }

    @Test
    public void addPlayerDirect() throws Exception
    {
        //startup node
        NodeManager node = new NodeManager();
        node.startupNode();
        Thread.sleep(1000);

        //startup mock listener
        MockSocketListener listener = new MockSocketListener();
        new Thread(() -> listener.startListener()).start();
        Thread.sleep(1000);

        //build message
        Player player = new Player("TestPlayer", listener.listenSocket.getInetAddress().getHostAddress(), listener.listenSocket.getLocalPort());
        String messageContent = String.format("PLAYER#%s", new Gson().toJson(player, Player.class));
        RingMessage message = new RingMessage(MessageType.GAME, RandomIdGenerator.getRndId(), messageContent);
        message.setSourceAddress(String.format("%s:%d", listener.listenSocket.getInetAddress().getHostAddress(), listener.listenSocket.getLocalPort()));

        //init app context
        SessionConfig appContext = SessionConfig.getInstance();
        appContext.RING_NETWORK = new ConcurrentList<>();

        //push message
        new GameManager(appContext).handleNewPlayerRingEntrance(message);

        Assert.assertEquals(1, appContext.RING_NETWORK.getList().size());
        Assert.assertEquals(player, appContext.RING_NETWORK.getList().get(0));
        PrettyPrinter.printPlayerDetails(appContext.RING_NETWORK.getList().get(0));

        Thread.sleep(1000);
    }

    @Test
    public void notifyNewPlayer() throws Exception
    {
        //build ring
        buildTestRing();
        Thread.sleep(1000);

        //new player
        Player player = new Player("TestPlayer", "localhost", 9000);

        //app context
        SessionConfig context = SessionConfig.getInstance();
        //notify
        new GameManager(context).notifyRingEntrance(player);
        Thread.sleep(1000);

        Assert.assertEquals(6, SessionConfig.getInstance().RING_NETWORK.getList().size());
    }
}
