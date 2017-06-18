import com.fv.sdp.util.PrettyPrinter;
import com.google.gson.Gson;
import com.fv.sdp.socket.ISocketObserver;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.socket.SocketConnector;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
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
        mockObserverList.add(new MockObserver(1));
        mockObserverList.add(new MockObserver(2));
        mockObserverList.add(new MockObserver(3));
        mockObserverList.add(new MockObserver(4));

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

        SocketClient client = new SocketClient(connector.getListenerAddress(), connector.getListenerPort());
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

        ArrayList<SocketClient> clientList = new ArrayList<>();
        clientList.add(new SocketClient(connector.getListenerAddress(), connector.getListenerPort()));
        clientList.add(new SocketClient(connector.getListenerAddress(), connector.getListenerPort()));

        for (int i = 0; i < 1; i++)
        {
            for (SocketClient c : clientList)
                c.sendMessage(new RingMessage(MessageType.GAME,"asd23", String.format("[%s]", LocalDateTime.now())));

            Thread.sleep(100);
        }
    }
}

class MockObserver implements ISocketObserver
{
    private int observerId;

    public MockObserver(int id)
    {
        observerId = id;
    }

    @Override
    public void pushMessage(RingMessage message)
    {
        //log
        PrettyPrinter.printTimestampLog(String.format("[Observer#%d] Received: %s", observerId, message.getContent()));
    }
}

class SocketClient
{
    Socket client;
    public SocketClient(InetAddress serverAddr, int serverPort)
    {
        try
        {
            client = new Socket(serverAddr, serverPort);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void sendMessage(RingMessage message)
    {
        try
        {
            PrintWriter writer = new PrintWriter(client.getOutputStream());

            writer.println(new Gson().toJson(message, RingMessage.class));
            writer.flush();

            //log
            PrettyPrinter.printSentRingMessage(message, client.getInetAddress().getHostAddress(), client.getPort());

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
