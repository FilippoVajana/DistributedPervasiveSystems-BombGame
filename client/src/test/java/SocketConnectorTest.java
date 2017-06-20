import com.fv.sdp.socket.ISocketObserver;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import org.junit.Assert;
import org.junit.Test;
import util.MockSocketClient;
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

        SocketConnector connector = new SocketConnector(mockObserverList);
        System.out.println(connector.getListenerAddress());
        System.out.println(connector.getListenerPort());

        Assert.assertNotNull(connector);
    }

    @Test
    public void messageDeliveryTest() throws InterruptedException
    {
        List<ISocketObserver> mockObserverList = getObserversList();
        //server creation
        SocketConnector connector = new SocketConnector(mockObserverList);
        Runnable task = () -> connector.startListener();
        Thread thread = new Thread(task);
        thread.start();

        Thread.sleep(1000);

        MockSocketClient client = new MockSocketClient(connector.getListenerAddress(), connector.getListenerPort());
        for (int i = 0; i < 10; i++)
        {
            RingMessage message = new RingMessage(MessageType.GAME, "asd23", String.format("[%s]", LocalDateTime.now()));
            client.sendMessage(message);
            Thread.sleep(100);
        }
    }

    @Test
    public void multiClientDeliveryTest() throws InterruptedException {
        List<ISocketObserver> mockObserverList = getObserversList();
        //server creation
        SocketConnector connector = new SocketConnector(mockObserverList);
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
                c.sendMessage(new RingMessage(MessageType.GAME,"asd23", String.format("[%s]", LocalDateTime.now())));

            Thread.sleep(100);
        }
    }
}






