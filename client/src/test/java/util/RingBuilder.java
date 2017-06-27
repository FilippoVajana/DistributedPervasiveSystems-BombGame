package util;

import com.fv.sdp.model.Player;
import com.fv.sdp.ring.NodeManager;
import com.fv.sdp.util.ConcurrentList;

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
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            //build node player
            Player pl = new Player(String.format("PL%d", i), node.getSocketConnector().getListenerAddress().getHostAddress(), node.getSocketConnector().getListenerPort());

            //update nodes list
            nodesList.add(node);
            //update players list
            playersList.add(pl);
        }

        //set nodes app context
        for (NodeManager node : nodesList)
        {
            node.appContext.RING_NETWORK = new ConcurrentList<>(playersList);
        }

        return nodesList;
    }
}
