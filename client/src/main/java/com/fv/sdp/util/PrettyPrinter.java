package com.fv.sdp.util;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Created by filip on 6/17/2017.
 */
public class PrettyPrinter
{
    private static final boolean ENABLE_LOG = true;
    private static final boolean ENABLE_ERROR = true;
    private static final boolean ENABLE_MESSAGE_IN = false;
    private static final boolean ENABLE_MESSAGE_OUT = false;
    private static final boolean ENABLE_INIT = false;

    public static void printTimestampLog(String log)
    {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
        String timestamp = String.format("[%s]-(%d)", timeFormatter.format(LocalDateTime.now()), Thread.currentThread().getId());

        if (ENABLE_LOG)
            System.out.println(String.format("%s: \t%s", timestamp, log));
    }

    public static void printTimestampError(String error)
    {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
        String timestamp = String.format("[%s]-(%d)", timeFormatter.format(LocalDateTime.now()), Thread.currentThread().getId());

        if (ENABLE_ERROR)
            System.err.println(String.format("%s: \t%s", timestamp, error));
    }

    public static void printMatchDetails(Match match)
    {
        String playerDetails = "";
        for (Player p : match.getPlayers().getList())
        {
            playerDetails += String.format("\n\tId: %s\t" +
                    "Address: %s:%d", p.getId(), p.getAddress(), p.getPort());
        }
        System.out.println(String.format("Id: %s\n" +
                "Players: %s\n" +
                "Points_V: %d\n" +
                "Points_E: %d\n", match.getId(), playerDetails, match.getVictoryPoints(), match.getEdgeLength()));
    }

    public static void printPlayerDetails(Player player)
    {
        System.out.println(String.format("Id: %s, Addr: %s:%d", player.getId(), player.getAddress(), player.getPort()));
    }

    public static void printReceivedRingMessage(RingMessage message, Player player)
    {
        if (message != null && ENABLE_MESSAGE_IN)
        {
            String messageString = String.format("[%s] MESSAGE-IN [%s %s] FROM %s ### %s ###", player.getId(), message.getType(), message.getId(), message.getSourceAddress(), message.getContent());
            printTimestampLog(messageString);
        }
    }

    public static void printSentRingMessage(RingMessage message, String destination, int port, Player player)
    {
        if (message != null && ENABLE_MESSAGE_OUT)
        {
            String messageString = String.format("[%s] MESSAGE-OUT [%s %s] TO %s:%d ### %s ###", player.getId(), message.getType(), message.getId(), destination, port, message.getContent());
            printTimestampLog(messageString);
        }
    }

    public static void printClassInit(Object obj)
    {
        if (ENABLE_INIT)
            printTimestampLog(String.format("INIT %s", obj.getClass().getSimpleName()));
    }

    public static void printQueuePool(Map<MessageType, ConcurrentObservableQueue<RingMessage>> getQueuePool)
    {
        for (MessageType mType : getQueuePool.keySet())
        {
            String queueString = String.format("QUEUE: %s\n", mType);
            for (RingMessage m : getQueuePool.get(mType).getQueue())
            {
                queueString += String.format("\t\t %s - %s - %s\n", m.getId(), m.getSourceAddress(), m.getContent());
            }

            System.out.println(queueString);
        }

    }
}
