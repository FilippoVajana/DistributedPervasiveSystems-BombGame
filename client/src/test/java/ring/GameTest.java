package ring;

import com.fv.sdp.ApplicationContext;
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
import util.RingBuilder;

import java.util.ArrayList;

public class GameTest
{
    //@Rule
    //public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested

    @Test
    public void joinTest() throws InterruptedException
    {
        //setup ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //setup new node
        NodeManager node = new NodeManager();
        node.appContext.RING_NETWORK = new ConcurrentList<>(ring.get(0).appContext.RING_NETWORK.getList());
        node.startupNode();
        Thread.sleep(500);

        //new player
        Player player = new Player("PL_new", node.appContext.LISTENER_ADDR, node.appContext.LISTENER_PORT);

        //add player
        node.appContext.GAME_MANAGER.notifyJoin(player);
        Thread.sleep(500);

        Assert.assertEquals(3, node.appContext.RING_NETWORK.getList().size()); //fake node
        Assert.assertEquals(4, ring.get(0).appContext.RING_NETWORK.getList().size()); //old node
    }

    @Test
    public void leaveTest() throws InterruptedException
    {
        //setup ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //leave ring
        NodeManager node0 = ring.get(0);
        node0.appContext.PLAYER_NICKNAME = "PL0";
        node0.appContext.GAME_MANAGER.notifyLeave(node0.appContext.getPlayerInfo());
        Thread.sleep(500);

        Assert.assertEquals(2, ring.get(1).appContext.RING_NETWORK.getList().size());
    }
}
