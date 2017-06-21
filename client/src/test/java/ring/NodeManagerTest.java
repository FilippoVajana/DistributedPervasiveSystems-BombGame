package ring;

import com.fv.sdp.ring.NodeManager;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.RandomIdGenerator;
import org.junit.*;
import util.MockSocketClient;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;

/**
 * Created by filip on 21/06/2017.
 */
public class NodeManagerTest
{
    /*
    private static ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @BeforeClass
    public static void setupEnv()
    {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }
    @AfterClass
    public static void resetEnv()
    {
        System.setOut(System.out);
        System.setErr(System.err);
    }
    */
    @Test
    public void initNodeTest() throws InterruptedException
    {
        NodeManager node = new NodeManager();

        Assert.assertNotNull(node);

        Thread.sleep(1000);
    }

    @Test
    public void nodeStartupTest() throws InterruptedException
    {
        //init node
        NodeManager node = new NodeManager();
        Assert.assertNotNull(node);

        //startup node
        node.startupNode();

        Thread.sleep(1000);
    }

    @Test
    public void queueManagerTest() throws Exception
    {
        //init node
        NodeManager node = new NodeManager();
        Assert.assertNotNull(node);

        //startup node
        node.startupNode();

        Thread.sleep(1000);


        MockSocketClient mockClient = new MockSocketClient(InetAddress.getLocalHost(), 9090);
        RingMessage outMessage = new RingMessage(MessageType.TOKEN, RandomIdGenerator.getRndId());
        //send fake message
        mockClient.sendMessage(outMessage, false);

        Thread.sleep(1000);
    }
}
