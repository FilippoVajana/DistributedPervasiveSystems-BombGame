package util;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.ring.NodeManager;
import com.fv.sdp.util.ConcurrentList;
import com.fv.sdp.util.PrettyPrinter;

import java.util.ArrayList;

/**
 * Created by filip on 27/06/2017.
 */
public class RingBuilder
{

    public ArrayList<NodeManager> buildTestRing()
    {
        ArrayList<Player> playersList = new ArrayList<>();
        ArrayList<NodeManager> nodesList = new ArrayList<>();

        for (int i=0; i<3; i++)
        {
            //build node
            NodeManager node = new NodeManager();
            node.startupNode();
            try
            {
                Thread.sleep(250);
                System.out.println(String.format("Node%d - %s:%d", i, node.appContext.LISTENER_ADDR, node.appContext.LISTENER_PORT));
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            //build node player
            Player pl = new Player(String.format("PL%d", i), node.appContext.LISTENER_ADDR, node.appContext.LISTENER_PORT);
            node.appContext.setPlayerInfo(pl.getId(), pl.getAddress(), pl.getPort());

            //update nodes list
            nodesList.add(node);
            //update players list
            playersList.add(pl);
        }

        //set nodes app context
        for (NodeManager node : nodesList)
        {
            node.appContext.RING_NETWORK = new ConcurrentList<>(playersList);

            //print node ring view
            System.out.println("\nRing view");
            for (Player pl : node.appContext.RING_NETWORK.getList())
                PrettyPrinter.printPlayerDetails(pl);

        }

        //print white lines
        System.out.println("\n\n");

        return nodesList;
    }

    public ArrayList<NodeManager> buildTestMatch()
    {
        try
        {
            //test match
            System.out.println("INITIALIZING  RING NODES");
            Match testMatch = new Match("Mock_Match", 20, 10);
            //init nodes
            final int NODES_COUNT = 3;
            ArrayList<NodeManager> nodeList = new ArrayList<>();

            for (int i = 0; i < NODES_COUNT; i++)
            {
                NodeManager node = new NodeManager();
                node.startupNode();
                Thread.sleep(250);

                //init node context
                node.appContext.PLAYER_MATCH = testMatch;
                node.appContext.GAME_MANAGER.initGameEngine();

                node.appContext.setPlayerInfo(new Player(String.format("PL_%d", i), node.appContext.LISTENER_ADDR, node.appContext.LISTENER_PORT));

                //add node to list
                nodeList.add(node);
            }
            System.out.println("\n\n");

            //update match players
            System.out.println("SETTING MATCH PLAYERS");
            ArrayList<Player> playerList = new ArrayList<>();
            for (NodeManager node : nodeList)
            {
                playerList.add(node.appContext.getPlayerInfo());
            }
            testMatch.setPlayers(new ConcurrentList<>(playerList));
            System.out.println("\n\n");

            //join nodes to match grid
            System.out.println("JOINING NODES TO MATCH");
            nodeList.get(0).appContext.TOKEN_MANAGER.storeToken();
            for (NodeManager node : nodeList)
            {
                Thread.sleep(1000);
                node.appContext.GAME_MANAGER.joinMatchGrid(node.appContext.getPlayerInfo(), testMatch);
                Thread.sleep(250);

                int x = node.appContext.GAME_MANAGER.getPlayerPosition().x;
                int y = node.appContext.GAME_MANAGER.getPlayerPosition().y;
                System.err.println(String .format("Player %s start at (%d,%d)", node.appContext.getPlayerInfo().getId(), x, y));
            }
            System.out.println("\n\n");

            Thread.sleep(1000);
            return nodeList;
        }catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }
}
