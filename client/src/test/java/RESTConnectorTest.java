import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.resource.MatchResource;
import com.fv.sdp.util.ConcurrentList;
import com.google.gson.Gson;
import com.fv.sdp.SessionConfig;
import com.fv.sdp.rest.RESTConnector;
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
        SessionConfig config = SessionConfig.getInstance();
        //set JdkHttpServerTestContainer
        config.REST_BASE_URL = "http://localhost:9998/";

        //init rest connector
        connector = new RESTConnector();
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
    private void printMatchDetails(Match match)
    {
        String playerDetails = "";
        for (Player p : match.getPlayers().getList())
        {
            playerDetails += String.format("\tId: %s\t" +
                    "Address: %s:%d\n", p.getId(), p.getAddress(), p.getPort());
        }
        System.out.println(String.format("Id: %s\n" +
                "Players: \n%s\n" +
                "Points_V: %d\n" +
                "Points_E: %d\n\n", match.getId(), playerDetails, match.getVictoryPoints(), match.getEdgeLength()));
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
            printMatchDetails(m);
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
            printMatchDetails(e);
    }

    @Test
    public void joinMatchTest()
    {
        //create match
        connector.createServerMatch(new Match("Glory", 56, 67));
        //create player
        Player player = SessionConfig.getInstance().getPlayerInfo();
        //join
        boolean joinResult = connector.joinServerMatch(new Match("Glory",0,0), player);
        //check result
        Assert.assertTrue(joinResult);
        //check server match status
        ArrayList<Match> matchList = connector.getServerMatchList(); //todo add contain method to ConcurrentArrayList
        Assert.assertEquals(1, matchList.size());
        for (Match m : matchList)
            printMatchDetails(m);
    }

}
