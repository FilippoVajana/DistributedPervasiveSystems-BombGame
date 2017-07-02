package ring;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fv.sdp.ring.NodeManager;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.RandomIdGenerator;
import org.junit.Assert;
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
     * Test store token procedure
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
        node0.appContext.TOKEN_MANAGER.storeToken();
        node0.appContext.SOCKET_CONNECTOR.sendMessage(tokenMessage, SocketConnector.DestinationGroup.NEXT);
        node0.appContext.TOKEN_MANAGER.releaseTokenSilent();
        Thread.sleep(1000);

        Assert.assertEquals(false, node0.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(true, ring.get(1).appContext.TOKEN_MANAGER.isHasToken());

    }

    @Test
    /**
     * Test release token procedure
     */
    public void tokenReleaseTest() throws InterruptedException
    {
        //build test ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //node0 token source
        NodeManager node0 = ring.get(0);

        //release token direct
        node0.appContext.TOKEN_MANAGER.storeToken();
        node0.appContext.TOKEN_MANAGER.releaseToken();
        Thread.sleep(1000);

        Assert.assertEquals(false, node0.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(true, ring.get(1).appContext.TOKEN_MANAGER.isHasToken());
    }

    @Test
    /**
     * Test passing token through the network ring
     */
    public void tokenRingRoundTest() throws InterruptedException
    {
        //build test ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //nodes
        NodeManager node0 = ring.get(0);
        NodeManager node1 = ring.get(1);
        NodeManager node2 = ring.get(2);

        //node0 release token
        node0.appContext.TOKEN_MANAGER.storeToken();
        node0.appContext.TOKEN_MANAGER.releaseToken();
        Thread.sleep(1000);

        Assert.assertEquals(false, node0.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(true, node1.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(false, node2.appContext.TOKEN_MANAGER.isHasToken());


        //node1 release token
        node1.appContext.TOKEN_MANAGER.releaseToken();
        Thread.sleep(1000);

        Assert.assertEquals(false, node0.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(false, node1.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(true, node2.appContext.TOKEN_MANAGER.isHasToken());


        //node2 release token
        node2.appContext.TOKEN_MANAGER.releaseToken();
        Thread.sleep(1000);

        Assert.assertEquals(true, node0.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(false, node1.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(false, node2.appContext.TOKEN_MANAGER.isHasToken());


    }
}
