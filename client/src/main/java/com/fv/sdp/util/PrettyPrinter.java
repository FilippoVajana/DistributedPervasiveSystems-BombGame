package com.fv.sdp.util;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.socket.RingMessage;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by filip on 6/17/2017.
 */
public class PrettyPrinter
{
    public static void printTimestampLog(String log)
    {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
        String timestamp = String.format("[%s]-(%d)", timeFormatter.format(LocalDateTime.now()), Thread.currentThread().getId());

        System.out.println(String.format("%s: \t%s", timestamp, log));
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

    public static void printRingMessage(RingMessage message, String sender)
    {
        String messageString = String.format("MESSAGE [%s - %s] FROM %s ### %s ###", message.getType(), message.getId(), sender, message.getContent());
        printTimestampLog(messageString);
    }
}
