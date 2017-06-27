import com.fv.sdp.ApplicationContext;
import com.fv.sdp.model.Player;
import com.fv.sdp.ring.NodeManager;
import com.fv.sdp.socket.ISocketObserver;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.ConcurrentList;
import com.fv.sdp.util.PrettyPrinter;
import com.fv.sdp.util.RandomIdGenerator;
import org.junit.Assert;
import org.junit.Test;
import util.MockSocketClient;
import util.MockSocketListener;
import util.MockSocketObserver;
import util.RingBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 6/1/2017.
 */
public class SocketConnectorTest
{
    private String getCurrentMethodName()
    {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }
    private List<ISocketObserver> getObserversList()
    {
        List<ISocketObserver> mockObserverList = new ArrayList<>();
        mockObserverList.add(new MockSocketObserver(1));
        mockObserverList.add(new MockSocketObserver(2));
        mockObserverList.add(new MockSocketObserver(3));
        mockObserverList.add(new MockSocketObserver(4));

        return  mockObserverList;
    }
    @Test
    public void startServerTest()
    {
        List<ISocketObserver> mockObserverList = getObserversList();

        SocketConnector connector = new SocketConnector(new ApplicationContext(), mockObserverList, 0);
        System.out.println(connector.getListenerAddress());
        System.out.println(connector.getListenerPort());

        Assert.assertNotNull(connector);
    }

    @Test
    public void messageDeliveryTest() throws InterruptedException
    {
        List<ISocketObserver> mockObserverList = getObserversList();
        //server creation
        SocketConnector connector = new SocketConnector(new ApplicationContext(), mockObserverList, 0);
        Runnable task = () -> connector.startListener();
        Thread thread = new Thread(task);
        thread.start();

        Thread.sleep(1000);

        MockSocketClient client = new MockSocketClient(connector.getListenerAddress(), connector.getListenerPort());
        for (int i = 0; i < 10; i++)
        {
            RingMessage message = new RingMessage(MessageType.GAME, "asd23", String.format("[%s]", LocalDateTime.now()));
            client.sendMessage(message, false);
            Thread.sleep(100);
        }
    }

    @Test
    public void multiClientDeliveryTest() throws InterruptedException {
        List<ISocketObserver> mockObserverList = getObserversList();
        //server creation
        SocketConnector connector = new SocketConnector(new ApplicationContext(), mockObserverList, 0);
        Runnable task = () -> connector.startListener();
        Thread thread = new Thread(task);
        thread.start();

        Thread.sleep(1000);

        ArrayList<MockSocketClient> clientList = new ArrayList<>();
        clientList.add(new MockSocketClient(connector.getListenerAddress(), connector.getListenerPort()));
        clientList.add(new MockSocketClient(connector.getListenerAddress(), connector.getListenerPort()));

        for (int i = 0; i < 1; i++)
        {
            for (MockSocketClient c : clientList)
                c.sendMessage(new RingMessage(MessageType.GAME,"asd23", String.format("[%s]", LocalDateTime.now())), false);

            Thread.sleep(100);
        }
    }

    @Test
    public void sendSourceTest() throws Exception
    {
        //build test ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //test message
        RingMessage message = new RingMessage(MessageType.TOKEN, RandomIdGenerator.getRndId(), getCurrentMethodName());

        //set message source
        NodeManager node0 = ring.get(0);
        message.setSourceAddress(String.format("%s:%d", node0.appContext.LISTENER_ADDR, node0.appContext.LISTENER_PORT));

        //node1 -> TOKEN -> node0
        NodeManager node1 = ring.get(1);
        node1.appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.SOURCE);

        Thread.sleep(1000);

        Assert.assertEquals(true, node0.appContext.TOKEN_MANAGER.isHasToken());
    }

    @Test
    public  void sendNextTest() throws Exception
    {
        //build test ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //test message
        RingMessage message = new RingMessage(MessageType.TOKEN, RandomIdGenerator.getRndId(), getCurrentMethodName());

        //set message source
        NodeManager node0 = ring.get(0);

        //node0 -> TOKEN -> node1
        NodeManager node1 = ring.get(1);
        node0.appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.NEXT);

        Thread.sleep(1000);

        Assert.assertEquals(false, node0.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(true, node1.appContext.TOKEN_MANAGER.isHasToken());
    }

    @Test
    public  void sendAllTest() throws Exception
    {
        //build test ring
        ArrayList<NodeManager> ring = new RingBuilder().buildTestRing();

        //test message
        RingMessage message = new RingMessage(MessageType.TOKEN, RandomIdGenerator.getRndId(), getCurrentMethodName());

        //set message source
        NodeManager node0 = ring.get(0);

        //node0 -> TOKEN -> node1
        NodeManager node1 = ring.get(1);
        node0.appContext.SOCKET_CONNECTOR.sendMessage(message, SocketConnector.DestinationGroup.ALL);

        Thread.sleep(1000);

        Assert.assertEquals(true, node0.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(true, node1.appContext.TOKEN_MANAGER.isHasToken());
        Assert.assertEquals(true, ring.get(2).appContext.TOKEN_MANAGER.isHasToken());
    }
}






