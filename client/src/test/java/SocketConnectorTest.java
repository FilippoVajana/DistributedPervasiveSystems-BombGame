import main.java.socket.ISocketObserver;
import main.java.socket.SocketConnector;
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
        System.out.println(connector.getServerAddress());
        System.out.println(connector.getServerPort());

        Assert.assertNotNull(connector);

    }

    @Test
    public void messageDeliveryTest() throws InterruptedException
    {
        List<ISocketObserver> mockObserverList = getObserversList();
        //server creation
        SocketConnector connector = new SocketConnector(mockObserverList);
        Runnable task = () -> connector.startServer();
        Thread thread = new Thread(task);
        thread.start();

        Thread.sleep(1000);

        SocketClient client = new SocketClient(connector.getServerAddress(), connector.getServerPort());
        for (int i = 0; i < 100; i++)
        {
            client.sendMessage(String.format("[%s]", LocalDateTime.now()));
            Thread.sleep(100);
        }
    }

    @Test
    public void multiClientDeliveryTest() throws InterruptedException {
        List<ISocketObserver> mockObserverList = getObserversList();
        //server creation
        SocketConnector connector = new SocketConnector(mockObserverList);
        Runnable task = () -> connector.startServer();
        Thread thread = new Thread(task);
        thread.start();

        Thread.sleep(1000);

        ArrayList<SocketClient> clientList = new ArrayList<>();
        clientList.add(new SocketClient(connector.getServerAddress(), connector.getServerPort()));
        clientList.add(new SocketClient(connector.getServerAddress(), connector.getServerPort()));

        for (int i = 0; i < 1; i++)
        {
            for (SocketClient c : clientList)
                c.sendMessage(String.format("[%s]", LocalDateTime.now()));

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
    public void deliverMessage(String message)
    {
        System.out.println(String.format("[Observer#%d] Received: %s", observerId, message));
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

    public void sendMessage(String message)
    {
        try
        {
            PrintWriter writer = new PrintWriter(client.getOutputStream());

            writer.println(message);
            writer.flush();

            System.out.println("Sent: " + message + " to: " + client.getRemoteSocketAddress());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
