package main.java.socket;

import java.net.ServerSocket;

/**
 * Created by filip on 01/06/2017.
 */
public class SocketConnector
{
    private ServerSocket listeningServer = null;

    public SocketConnector()
    {
        try
        {
            listeningServer = new ServerSocket();
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public int getServerPort()
    {
        return listeningServer.getLocalPort();
    }

    public String getServerAddress()
    {
        return listeningServer.getInetAddress().getAddress().toString();
    }
}
