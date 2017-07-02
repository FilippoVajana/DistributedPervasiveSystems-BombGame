import com.fv.sdp.ApplicationContext;
import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.resource.MatchResource;
import com.fv.sdp.rest.RESTConnector;
import com.fv.sdp.ring.NodeManager;
import com.fv.sdp.util.ConcurrentList;
import com.fv.sdp.util.PrettyPrinter;
import com.google.gson.Gson;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 * Created by filip on 14/06/2017.
 */
public class RESTConnectorTest extends JerseyTest
{
    private RESTConnector connector;

    @Override
    protected Application configure()
    {
        return new ResourceConfig(MatchResource.class);
    }

    @Before
    public void setup()
    {
        //reset server data model
        new MatchResource().resetResourceModel();

        //init session parameters
        ApplicationContext appContext = new ApplicationContext();
        //set JdkHttpServerTestContainer
        appContext.REST_BASE_URL = "http://localhost:9998/";

        //init rest connector
        connector = new RESTConnector(appContext);
    }
    private void setServerTestModel()
    {
        Match m1 = new Match("game1", 5,789421);
        Match m2 = new Match("game2", 678654,90);
        Match m3 = new Match("game3@°çé@[?^", 64867,45);

        Gson jsonizer = new Gson();
        target("match").request().post(Entity.entity(jsonizer.toJson(m1), MediaType.APPLICATION_JSON));
        target("match").request().post(Entity.entity(jsonizer.toJson(m2), MediaType.APPLICATION_JSON));
        target("match").request().post(Entity.entity(jsonizer.toJson(m3), MediaType.APPLICATION_JSON));

        //match with players
        ConcurrentList<Player> pList4 = new ConcurrentList<>();
        pList4.add(new Player("pl1", "localhost", 45624));
        pList4.add(new Player("pl2", "127.0.0.1", 56387));
        pList4.add(new Player("pl3", "192.168.1.1", 45624));
        Match m4 = new Match("game4", 34,67, pList4);
        target("match").request().post(Entity.entity(m4, MediaType.APPLICATION_JSON));
    }

    @Test
    public void getMatchList()
    {
        //set test data model
        setServerTestModel();
        //list request
        ArrayList<Match> matchList = connector.getServerMatchList();
        //check return
        Assert.assertEquals(4, matchList.size());
        for (Match m : matchList)
            PrettyPrinter.printMatchDetails(m);
    }

    @Test
    public void createNewMatchTest()
    {
        //init new match
        Match m = new Match("Glory", 99,234);
        //create match request
        boolean creationResult = connector.createServerMatch(m);
        //check return
        Assert.assertTrue(creationResult);
        //get match list
        ArrayList<Match> matchList = connector.getServerMatchList();
        //check response
        for (Match e : matchList)
            PrettyPrinter.printMatchDetails(e);
    }

    @Test
    public void joinMatchTest() throws InterruptedException
    {
        //init node
        NodeManager node =  new NodeManager();
        node.startupNode();
        Thread.sleep(1000);

        //app context
        ApplicationContext appContext =node.appContext;
        //set JdkHttpServerTestContainer
        appContext.REST_BASE_URL = "http://localhost:9998/";

        System.out.println("\n\nSTARTING RESTConnector TEST");
        //init REST connector
        connector = new RESTConnector(appContext);

        //create match
        Match match = new Match("Glory", 56, 67);
        connector.createServerMatch(match);

        //create player
        appContext.setPlayerInfo("PL1", appContext.LISTENER_ADDR, appContext.LISTENER_PORT);
        Player player = appContext.getPlayerInfo();

        //join
        boolean joinResult = connector.joinServerMatch(new Match("Glory",0,0), player);

        //check result
        Assert.assertTrue(joinResult);
        //check server match status
        ArrayList<Match> matchList = connector.getServerMatchList();
        Assert.assertEquals(1, matchList.size());

        Assert.assertEquals(match, appContext.PLAYER_MATCH);
        System.out.println("Current match: ");
        PrettyPrinter.printMatchDetails(appContext.PLAYER_MATCH);

        //chech session config node
        Assert.assertEquals(1, appContext.RING_NETWORK.getList().size());
        System.out.println("Ring topology: ");
        for (Player p : appContext.RING_NETWORK.getList())
            PrettyPrinter.printPlayerDetails(p);
    }

    @Test
    public void leaveMatchTest() throws Exception
    {
        //init node
        NodeManager node =  new NodeManager();
        node.startupNode();
        Thread.sleep(1000);

        //app context
        ApplicationContext appContext =node.appContext;
        //set JdkHttpServerTestContainer
        appContext.REST_BASE_URL = "http://localhost:9998/";

        System.out.println("\n\nSTARTING RESTConnector TEST");
        //init REST connector
        connector = new RESTConnector(appContext);

        //create match
        Match match = new Match("Glory", 56, 67);
        connector.createServerMatch(match);

        //create player
        appContext.setPlayerInfo("PL1", appContext.LISTENER_ADDR, appContext.LISTENER_PORT);
        Player player = appContext.getPlayerInfo();

        //join
        boolean joinResult = connector.joinServerMatch(new Match("Glory",0,0), player);
        Thread.sleep(1000);

        //leave match
        boolean leaveResult = connector.leaveServerMatch(match, player);

        Assert.assertTrue(leaveResult);

        ArrayList<Match> matchList = connector.getServerMatchList();
        Assert.assertEquals(0, matchList.size());
    }
}
