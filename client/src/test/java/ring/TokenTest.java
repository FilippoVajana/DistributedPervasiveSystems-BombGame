package ring;

import com.fv.sdp.ring.NodeManager;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.RandomIdGenerator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import util.RingBuilder;

import java.util.ArrayList;

public class TokenTest
{
    /*@Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested
    */
    private String getCurrentMethodName()
    {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    @Test
    /**
     * Mock TOKEN from node0 to node1
     */
    public void tokenStoreTest() throws InterruptedException
    {
        //build test ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //mock token message from node0
        NodeManager node0 = ring.get(0);
        RingMessage tokenMessage = new RingMessage(MessageType.TOKEN, RandomIdGenerator.getRndId(), getCurrentMethodName());
        tokenMessage.setSourceAddress(String.format("%s:%d", node0.appContext.LISTENER_ADDR, node0.appContext.LISTENER_PORT));

        //send mock token message to node1
        node0.appContext.SOCKET_CONNECTOR.sendMessage(tokenMessage, SocketConnector.DestinationGroup.NEXT);

        Thread.sleep(2000);

    }

    @Test
    public void tokenReleaseTest()
    {
        //build test ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();
    }
}
