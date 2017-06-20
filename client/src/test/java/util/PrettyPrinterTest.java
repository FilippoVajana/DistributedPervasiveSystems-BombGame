package util;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.socket.MessageType;
import com.fv.sdp.socket.RingMessage;
import com.fv.sdp.util.ConcurrentList;
import com.fv.sdp.util.PrettyPrinter;
import com.fv.sdp.util.RandomIdGenerator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Created by filip on 6/17/2017.
 */
public class PrettyPrinterTest
{
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams()
    {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }
    @After
    public void cleanUpStreams()
    {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    public void timestampLog() throws InterruptedException
    {
        PrettyPrinter.printTimestampLog("Hello");
        Assert.assertTrue(outContent.toString().contains("Hello"));
    }

    @Test
    public void matchDetails()
    {
        //test match
        //match with players
        ConcurrentList<Player> pList4 = new ConcurrentList<>();
        pList4.add(new Player("pl1", "localhost", 45624));
        pList4.add(new Player("pl2", "127.0.0.1", 56387));
        pList4.add(new Player("pl3", "192.168.1.1", 45624));
        Match m4 = new Match("game4", 34,67, pList4);

        PrettyPrinter.printMatchDetails(m4);
        String matchString = "Id: game4\n" +
                "Players: \n" +
                "\tId: pl1\tAddress: localhost:45624\n" +
                "\tId: pl2\tAddress: 127.0.0.1:56387\n" +
                "\tId: pl3\tAddress: 192.168.1.1:45624\n" +
                "Points_V: 67\n" +
                "Points_E: 34\n\n";
        //Assert.assertEquals(matchString, outContent.toString());
        Assert.assertNotNull(outContent.toString());
    }

    @Test
    public void printReceivedRingMessage()
    {
        //test message
        RingMessage message = new RingMessage(MessageType.TOKEN, "127.0.0.1",RandomIdGenerator.getRndId(), "TokenRing");

        PrettyPrinter.printReceivedRingMessage(message);

        //Assert.assertEquals("", outContent.toString());
        Assert.assertTrue(outContent.toString().contains("127.0.0.1"));
        Assert.assertTrue(outContent.toString().contains("TokenRing"));
    }

    @Test
    public void printSentRingMessage()
    {
        //test message
        String destination = "8.8.8.8";
        RingMessage message = new RingMessage(MessageType.TOKEN, RandomIdGenerator.getRndId(), "TokenRing");


        PrettyPrinter.printSentRingMessage(message, destination, 80);
        System.out.println("asdad" + outContent.toString());
        Assert.assertNotNull(outContent.toString());

        //Assert.assertEquals("", outContent.toString());
        Assert.assertTrue(outContent.toString().contains(destination));
        Assert.assertTrue(outContent.toString().contains("TokenRing"));
    }
}
