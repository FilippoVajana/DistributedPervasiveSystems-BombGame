package ring;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.ring.NodeManager;
import com.fv.sdp.util.ConcurrentList;
import org.junit.Assert;
import org.junit.Test;
import util.RingBuilder;

import java.util.ArrayList;

public class GameTest
{
    /*
    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested
*/
    @Test
    public void joinRingTest() throws InterruptedException
    {
        //set mock match
        Match mockMatch = new Match("MockMatch", 10, 10);

        //setup ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();
        for (NodeManager node : ring)
        {
            node.appContext.PLAYER_MATCH = mockMatch;
            node.appContext.GAME_MANAGER.initGameEngine();
        }

        //setup new node
        NodeManager node = new NodeManager();
        node.appContext.RING_NETWORK = new ConcurrentList<>(ring.get(0).appContext.RING_NETWORK.getList());
        node.appContext.PLAYER_MATCH = mockMatch;
        node.startupNode();
        Thread.sleep(1000);

        //new node mock player
        Player player = new Player("PL_new", node.appContext.LISTENER_ADDR, node.appContext.LISTENER_PORT);
        node.appContext.RING_NETWORK.add(player);

        //add player to the network ring
        Thread notifyJoinThread = new Thread(() -> node.appContext.GAME_MANAGER.notifyJoin(player));
        notifyJoinThread.start();
        Thread.sleep(3000);
        System.out.println("\n\n");

        //simulate token arrival
        NodeManager node2 = ring.get(2);
        node2.appContext.TOKEN_MANAGER.storeToken();
        node2.appContext.TOKEN_MANAGER.releaseToken();


        //wait notify end
        //Thread.sleep(10000);
        notifyJoinThread.join(5000);

        Assert.assertEquals(4, node.appContext.RING_NETWORK.getList().size()); //fake node
        Assert.assertEquals(4, ring.get(0).appContext.RING_NETWORK.getList().size()); //old node
        Assert.assertEquals(false, node.appContext.TOKEN_MANAGER.isHasToken());

    }

    @Test
    public void joinEmptyRingTest() throws Exception
    {
        //set mock match
        Match mockMatch = new Match("MockMatch", 10, 10);

        //setup new node
        NodeManager node = new NodeManager();
        node.appContext.RING_NETWORK = new ConcurrentList<>();
        node.appContext.PLAYER_MATCH = mockMatch;
        node.startupNode();
        Thread.sleep(1000);

        //new node mock player
        Player player = new Player("PL_NEW", node.appContext.LISTENER_ADDR, node.appContext.LISTENER_PORT);
        node.appContext.RING_NETWORK.add(player); //done by RESTConnector

        //add player to the network ring
        Thread notifyJoinThread = new Thread(() -> node.appContext.GAME_MANAGER.joinMatchGrid(player));
        notifyJoinThread.start();
        System.out.println("\n\n");

        notifyJoinThread.join(10000);
        Assert.assertEquals(1, node.appContext.RING_NETWORK.getList().size()); //fake node
        Assert.assertEquals(true, node.appContext.TOKEN_MANAGER.isHasToken());
    }

    @Test
    public void leaveTest() throws InterruptedException
    {
        //setup ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //setting node
        NodeManager node0 = ring.get(0);
        node0.appContext.TOKEN_MANAGER.storeToken();

        //leave ring
        node0.appContext.GAME_MANAGER.notifyLeave(node0.appContext.getPlayerInfo());
        Thread.sleep(500);

        Assert.assertEquals(2, ring.get(1).appContext.RING_NETWORK.getList().size());
    }

    @Test
    public void leaveWaitTokenTest() throws InterruptedException
    {
        //setup ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //setting node
        NodeManager node0 = ring.get(0);

        //leave ring
        Thread notifyLeaveThread = new Thread(() -> node0.appContext.GAME_MANAGER.notifyLeave(node0.appContext.getPlayerInfo()));
        notifyLeaveThread.start();

        //node2 release token
        Thread.sleep(2000);
        ring.get(2).appContext.TOKEN_MANAGER.storeToken();
        ring.get(2).appContext.TOKEN_MANAGER.releaseToken();

        notifyLeaveThread.join();
        Assert.assertEquals(2, ring.get(1).appContext.RING_NETWORK.getList().size());
    }

    @Test
    public void moveTest() throws InterruptedException
    {
        //setup ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //setting node
        NodeManager node0 = ring.get(0);
        node0.appContext.TOKEN_MANAGER.storeToken();

        //move player
        node0.appContext.GAME_MANAGER.movePlayer("w");
    }
}
