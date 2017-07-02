package util;

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
}
