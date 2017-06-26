import com.fv.sdp.SessionConfig;
import com.fv.sdp.model.Player;
import com.fv.sdp.socket.ISocketObserver;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import com.fv.sdp.util.ConcurrentList;
import com.fv.sdp.util.RandomIdGenerator;
import org.junit.Assert;
import org.junit.Test;
import util.MockSocketClient;
import util.MockSocketListener;
import util.MockSocketObserver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by filip on 6/1/2017.
 */
public class SocketConnectorTest
{
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

        SocketConnector connector = new SocketConnector(mockObserverList, 0);
        System.out.println(connector.getListenerAddress());
        System.out.println(connector.getListenerPort());

        Assert.assertNotNull(connector);
    }

    @Test
    public void messageDeliveryTest() throws InterruptedException
    {
        List<ISocketObserver> mockObserverList = getObserversList();
        //server creation
        SocketConnector connector = new SocketConnector(mockObserverList, 0);
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
        SocketConnector connector = new SocketConnector(mockObserverList, 0);
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
        //start mock listener
        MockSocketListener mockListener = new MockSocketListener();
        Runnable listenerTask = () -> mockListener.startListener();
        Thread listenerThread = new Thread(listenerTask);
        listenerThread.start();
        Thread.sleep(500);

        //setting session config
        SessionConfig.getInstance().RING_NETWORK = new ConcurrentList<>();

        //init sender
        SocketConnector sender = new SocketConnector();

        //build message
        String sourceAddress = String.format("%s:%d", mockListener.listenSocket.getInetAddress().getHostAddress(), mockListener.listenSocket.getLocalPort());
        String messageId = RandomIdGenerator.getRndId();
        String messageContent = "TEST MESSAGE";
        RingMessage outMessage = new RingMessage(MessageType.ACK, sourceAddress, messageId, messageContent);

        //send message
        sender.sendMessage(outMessage, SocketConnector.DestinationGroup.SOURCE);

        Thread.sleep(1000);

        //check
        Assert.assertTrue(MockSocketListener.lastMessageReceived.contains("ACK"));
        Assert.assertTrue(MockSocketListener.lastMessageReceived.contains(messageId));
        Assert.assertTrue(MockSocketListener.lastMessageReceived.contains(messageContent));
    }

    @Test
    public  void sendNextTest() throws Exception {
        //start mock listener
        MockSocketListener mockListener = new MockSocketListener();
        Runnable listenerTask = () -> mockListener.startListener();
        Thread listenerThread = new Thread(listenerTask);
        listenerThread.start();
        Thread.sleep(500);

        //setting session config
        ArrayList<Player> ring = new ArrayList<>();
        Player p1 = new Player("NextPlayer", mockListener.listenSocket.getInetAddress().getHostAddress(), mockListener.listenSocket.getLocalPort());
        ring.add(p1);
        SessionConfig.getInstance().RING_NETWORK = new ConcurrentList<>(ring);

        //init sender
        SocketConnector sender = new SocketConnector();

        //build message
        String sourceAddress = String.format("%s:%d", mockListener.listenSocket.getInetAddress().getHostAddress(), mockListener.listenSocket.getLocalPort());
        String messageId = RandomIdGenerator.getRndId();
        String messageContent = "TEST MESSAGE";
        RingMessage outMessage = new RingMessage(MessageType.ACK, sourceAddress, messageId, messageContent);

        //send message
        sender.sendMessage(outMessage, SocketConnector.DestinationGroup.NEXT);

        Thread.sleep(1000);

        //check
        Assert.assertTrue(MockSocketListener.lastMessageReceived.contains("ACK"));
        Assert.assertTrue(MockSocketListener.lastMessageReceived.contains(messageId));
        Assert.assertTrue(MockSocketListener.lastMessageReceived.contains(messageContent));
    }

    @Test
    public  void sendAllTest() throws Exception {
        //start mock listener
        ArrayList<Player> ring = new ArrayList<>();
        for (int i = 0; i < 4; i++)
        {
            MockSocketListener mockListener = new MockSocketListener();
            Runnable listenerTask = () -> mockListener.startListener();
            Thread listenerThread = new Thread(listenerTask);
            listenerThread.start();

            //add ring node
            Player p = new Player("NextPlayer" + i, mockListener.listenSocket.getInetAddress().getHostAddress(), mockListener.listenSocket.getLocalPort());
            ring.add(p);
        }

        Thread.sleep(1000);

        //setting session config
        SessionConfig.getInstance().RING_NETWORK = new ConcurrentList<>(ring);

        //init sender
        SocketConnector sender = new SocketConnector();

        //build message
        String sourceAddress = "127.0.0.1:8000";
        String messageId = RandomIdGenerator.getRndId();
        String messageContent = "TEST MESSAGE";
        RingMessage outMessage = new RingMessage(MessageType.ACK, sourceAddress, messageId, messageContent);

        //send message
        sender.sendMessage(outMessage, SocketConnector.DestinationGroup.ALL);

        Thread.sleep(1000);
    }
}






