package ring;

import com.fv.sdp.ring.NodeManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

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
    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 seconds max per method tested

    @Before
    public void warmup()
    {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

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
}
