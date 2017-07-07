package ring;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.ring.GridBomb;
import com.fv.sdp.ring.GridPosition;
import com.fv.sdp.ring.GridSector;
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
    public void joinMatchTest() throws InterruptedException
    {
        //set mock match
        Match mockMatch = new Match("MockMatch", 10, 10);

        //setup ring
        final int playerCount = 3;
        ArrayList<NodeManager> ring = new RingBuilder().buildTestMatch(mockMatch, playerCount);

        //setup new node
        NodeManager nodeNew = new NodeManager();
        nodeNew.startupNode();
        Thread.sleep(250);
        //new node mock player
        Player player = new Player("PL_NEW", nodeNew.appContext.LISTENER_ADDR, nodeNew.appContext.LISTENER_PORT);
        nodeNew.appContext.setPlayerInfo(player);

        //set match players
        ConcurrentList<Player> matchPlayers = new ConcurrentList<>(ring.get(0).appContext.RING_NETWORK.getList()); //old ring
        matchPlayers.add(player); //old ring + new player
        mockMatch.setPlayers(matchPlayers);

        //add player to the network ring
        Thread notifyJoinThread = new Thread(() -> nodeNew.appContext.GAME_MANAGER.joinMatchGrid(player, mockMatch));
        notifyJoinThread.start();

        //simulate token arrival
        Thread.sleep(5000);
        System.out.println("\n\n FAKE STORING TOKEN");
        NodeManager nodeToken = ring.get(playerCount - 1);
        nodeToken.appContext.TOKEN_MANAGER.storeToken();
        nodeToken.appContext.TOKEN_MANAGER.releaseToken();


        //wait join notify
        notifyJoinThread.join();

        Assert.assertEquals(playerCount + 1, nodeNew.appContext.RING_NETWORK.getList().size()); //fake node
        Assert.assertEquals(playerCount + 1, ring.get(0).appContext.RING_NETWORK.getList().size()); //old node
        Assert.assertFalse(nodeNew.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertTrue(ring.get(0).appContext.TOKEN_MANAGER.isHasToken());

    }

    @Test
    public void joinEmptyMatchTest() throws Exception
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

        //set match players
        ArrayList<Player> matchPlayers = new ArrayList<>();
        matchPlayers.add(player);
        mockMatch.setPlayers(new ConcurrentList<>(matchPlayers));

        //add player to the network ring
        Thread notifyJoinThread = new Thread(() -> node.appContext.GAME_MANAGER.joinMatchGrid(player, mockMatch));
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
        node0.appContext.GAME_MANAGER.leaveMatchGrid();
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
        Thread notifyLeaveThread = new Thread(() -> node0.appContext.GAME_MANAGER.leaveMatchGrid());
        notifyLeaveThread.start();

        //node2 release token
        Thread.sleep(5000);
        ring.get(2).appContext.TOKEN_MANAGER.storeToken();
        ring.get(2).appContext.TOKEN_MANAGER.releaseToken();

        notifyLeaveThread.join(10000);
        Assert.assertEquals(2, ring.get(1).appContext.RING_NETWORK.getList().size());
    }

    @Test
    public void moveTest() throws InterruptedException
    {
        //setup ring
        Match testMatch = new Match("TEST_MATCH", 10, 10);
        ArrayList<NodeManager> ring = new RingBuilder().buildTestMatch(testMatch, 4);

        //setting node
        NodeManager node0 = ring.get(0);
        node0.appContext.TOKEN_MANAGER.storeToken();

        //move player
        System.out.println("\n\nMoving Node0");
        //UP
        node0.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(0, 0)); //starting position
        node0.appContext.GAME_MANAGER.movePlayer("w");
        Assert.assertTrue(GridPosition.equals(new GridPosition(0, 1), node0.appContext.GAME_MANAGER.getPlayerPosition()));

        //DOWN
        node0.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(0, 0)); //starting position
        node0.appContext.GAME_MANAGER.movePlayer("s");
        Assert.assertTrue(GridPosition.equals(new GridPosition(0, 9), node0.appContext.GAME_MANAGER.getPlayerPosition()));

        //LEFT
        node0.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(0, 0)); //starting position
        node0.appContext.GAME_MANAGER.movePlayer("a");
        Assert.assertTrue(GridPosition.equals(new GridPosition(9, 0), node0.appContext.GAME_MANAGER.getPlayerPosition()));

        //RIGHT
        node0.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(0, 0)); //starting position
        node0.appContext.GAME_MANAGER.movePlayer("d");
        Assert.assertTrue(GridPosition.equals(new GridPosition(1, 0), node0.appContext.GAME_MANAGER.getPlayerPosition()));

        //check token
        Assert.assertFalse(node0.appContext.TOKEN_MANAGER.isHasToken());
    }

    @Test
    public void killTest() throws Exception
    {
        /*
        Node1 kill Node0
         */

        //setup ring
        Match testMatch = new Match("TEST_MATCH", 10, 10);
        ArrayList<NodeManager> ring = new RingBuilder().buildTestMatch(testMatch, 2);

        //setting node0
        NodeManager node0 = ring.get(0);
        node0.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(0,0));

        //setting node1
        NodeManager node1 = ring.get(1);
        node1.appContext.TOKEN_MANAGER.storeToken();
        node1.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(0,1));

        //move node1 on node0
        Thread node1MoveThread = new Thread(() -> node1.appContext.GAME_MANAGER.movePlayer("s"));
        node1MoveThread.start();

        node1MoveThread.join(2000);
        Thread.sleep(1000);
        Assert.assertEquals(1, node1.appContext.GAME_MANAGER.getPlayerScore());
        Assert.assertEquals(1, node1.appContext.RING_NETWORK.size());
    }

    @Test
    public void bombReleaseTest() throws InterruptedException
    {
        //setup ring
        Match testMatch = new Match("TEST_MATCH", 10, 10);
        ArrayList<NodeManager> ring = new RingBuilder().buildTestMatch(testMatch, 2);

        //setting node0
        NodeManager node0 = ring.get(0);
        GridBomb bomb = new GridBomb(0);
        node0.appContext.GAME_MANAGER.getBombQueue().push(bomb);
        Assert.assertTrue(bomb.getBombSOE() == GridSector.Green);

        //set node0 token
        node0.appContext.TOKEN_MANAGER.releaseToken();
        Assert.assertFalse(node0.appContext.TOKEN_MANAGER.isHasToken());

        //node0 release bomb
        System.out.println("\n\nNODE0 RELEASING BOMB");
        Thread releaseBombThread = new Thread(() -> node0.appContext.GAME_MANAGER.releaseBomb());
        releaseBombThread.start();

        //node1 release token to node0
        //Thread.sleep(2000);
        System.out.println("\nNODE1 RELEASING TOKEN");

        NodeManager node1 = ring.get(1);
        node1.appContext.TOKEN_MANAGER.storeToken();
        node1.appContext.TOKEN_MANAGER.releaseToken();

        //wait release
        releaseBombThread.join();
        System.out.println("\nWAITING BOMB EXPLOSION");
        Thread.sleep(8000); //wait explosion

        Assert.assertFalse(node0.appContext.TOKEN_MANAGER.isHasToken());
    }

    @Test
    public void bombKillTest() throws Exception
    {
        //setup ring
        Match testMatch = new Match("TEST_MATCH", 10, 10);
        ArrayList<NodeManager> ring = new RingBuilder().buildTestMatch(testMatch, 5);

        //setup node0
        NodeManager node0 = ring.get(0);
        node0.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(0,0)); //blue sector
        GridBomb bomb = new GridBomb(0);
        node0.appContext.GAME_MANAGER.getBombQueue().push(bomb); //GREEN BOMB
        Assert.assertTrue(bomb.getBombSOE() == GridSector.Green);

        //setup node1
        NodeManager node1 = ring.get(1);
        node1.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(0, 9)); //green sector
        //setup node2
        NodeManager node2 = ring.get(2);
        node2.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(1, 9)); //green sector
        //setup node3
        NodeManager node3 = ring.get(3);
        node3.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(0, 8)); //green sector
        //setup node4
        NodeManager node4 = ring.get(4);
        node4.appContext.GAME_MANAGER.setPlayerPosition(new GridPosition(1, 8)); //green sector

        //node0 release bomb
        System.out.println("\n\nNODE0 RELEASING BOMB");
        Thread releaseBombThread = new Thread(() -> node0.appContext.GAME_MANAGER.releaseBomb());
        releaseBombThread.start();

        //wait release
        releaseBombThread.join();
        System.out.println("\nWAITING BOMB EXPLOSION");
        Thread.sleep(12000); //wait explosion

        Assert.assertFalse(node0.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(3, node0.appContext.GAME_MANAGER.getPlayerScore());
    }
}
