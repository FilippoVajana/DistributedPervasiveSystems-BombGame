import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.resource.MatchResource;
import com.fv.sdp.util.ConcurrentList;
import com.google.gson.Gson;
import com.fv.sdp.gui.GUIManager;
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
 * Created by filip on 15/06/2017.
 */

public class GUIManagerTest extends JerseyTest
{
    private GUIManager gui;
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

        //init gui manager
        gui = new GUIManager();
    }

    private void setServerTestModel()
    {
        Match m1 = new Match("game1", 6,789421);
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
    public void setNickname()
    {
        //enter nickname
        String nicknameInput = "Filippo";
        System.setIn(new java.io.ByteArrayInputStream(nicknameInput.getBytes())); //mock input utente

        gui.setNickname();

        Assert.assertNotNull(SessionConfig.getInstance().PLAYER_NICKNAME);
        Assert.assertEquals("Filippo", SessionConfig.getInstance().PLAYER_NICKNAME);
    }

    @Test
    public void joinMatch()
    {
        setServerTestModel();

        //set mock player
        SessionConfig.getInstance().PLAYER_NICKNAME = "Rambo";
        SessionConfig.getInstance().LISTENER_ADDR = "192.168.1.1";
        SessionConfig.getInstance().LISTENER_PORT = 4567;
        //enter match id
        Integer matchIndex = 0;
        System.setIn(new java.io.ByteArrayInputStream(matchIndex.toString().getBytes())); //mock input utente
        gui.joinMatch();

        //check
        ArrayList<Match> matchList = new RESTConnector().getServerMatchList();
        Assert.assertEquals(1, matchList.get(matchIndex).getPlayers().getList().size());
    }

    @Test
    public void createMatch()
    {
        String inputData = "GloryX\n" +
                "50\n" +
                "60";
        System.setIn(new java.io.ByteArrayInputStream(inputData.getBytes())); //mock input utente
        gui.createMatch();

        //check
        ArrayList<Match> matchList = new RESTConnector().getServerMatchList();
        Assert.assertEquals(1, matchList.size());
        Assert.assertTrue(matchList.get(0).getId().equals("GloryX"));
        Assert.assertTrue(matchList.get(0).getEdgeLength() == 50);
        Assert.assertTrue(matchList.get(0).getVictoryPoints() == 60);
    }

}
