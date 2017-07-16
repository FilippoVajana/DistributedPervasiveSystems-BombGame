package match;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.resource.MatchResource;
import com.fv.sdp.util.ConcurrentList;
import com.google.gson.Gson;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by filip on 5/17/2017.
 */
public class TestMatchResource extends JerseyTest
{
    @Override
    protected Application configure()
    {
        return new ResourceConfig(MatchResource.class);
    }

    @Before
    public void setup()
    {
        new MatchResource().resetResourceModel();
    }

    private void setModel()
    {
        Match m1 = new Match("game1", 546464,789421);
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
    public void addMatch()
    {
        Match m1 = new Match("game1", 32,90);
        Response response1 = target("match").request().post(Entity.entity(m1, MediaType.APPLICATION_JSON));
        assertEquals("Should return status 201", 201, response1.getStatus());
    }
    @Test
    public void addMatchDuplicate()
    {
        Match m1 = new Match("game1", 32,90);
        Response response1 = target("match").request().post(Entity.entity(m1, MediaType.APPLICATION_JSON));
        Response response2 = target("match").request().post(Entity.entity(m1, MediaType.APPLICATION_JSON));
        assertEquals("Should return status 201", 201, response1.getStatus());
        assertEquals("Should return status 304", 304, response2.getStatus());
    }

    @Test
    public void addMatchInvalid()
    {
        Match m1 = new Match("", 32,90);
        Response response1 = target("match").request().post(Entity.entity(m1, MediaType.APPLICATION_JSON));
        assertEquals("Should return status 406", 406, response1.getStatus());

        m1 = new Match("game4", 0,90);
        response1 = target("match").request().post(Entity.entity(m1, MediaType.APPLICATION_JSON));
        assertEquals("Should return status 406", 406, response1.getStatus());

        m1 = new Match("sdfsd", 32,0);
        response1 = target("match").request().post(Entity.entity(m1, MediaType.APPLICATION_JSON));
        assertEquals("Should return status 406", 406, response1.getStatus());
    }

    @Test
    public void getAllMatches()
    {
        setModel();
        Response response = target("match/").request().get();
        //debug
        //for (Match m : response)
        //   System.out.println(m.getId());
        assertEquals(4, response.readEntity(new GenericType<List<Match>>(){}).size());
    }

    @Test
    public void removePlayerFromMatch()
    {
        //set test model
        setModel();

        //init player
        Player player = new Player("pl1", "localhost", 0);
        String playerJson = new Gson().toJson(player, Player.class);

        //send first leaving REST message
        Response response = target("match/game1/leave").request().post(Entity.entity(playerJson, MediaType.APPLICATION_JSON));
        assertEquals(200, response.getStatus());

        //send second leaving REST message
        response = target("match/game1/leave").request().post(Entity.entity(playerJson, MediaType.APPLICATION_JSON));
        assertEquals(404, response.getStatus());
    }

    @Test
    public void removeAllPlayers()
    {
        //set test model
        setModel();

        //init players
        String player1 = new Gson().toJson(new Player("pl1", "localhost", 0), Player.class);
        String player2 = new Gson().toJson(new Player("pl2", "localhost", 0), Player.class);
        String player3 = new Gson().toJson(new Player("pl3", "localhost", 0), Player.class);

        target("match/game4/leave").request().post(Entity.entity(player1, MediaType.APPLICATION_JSON));
        target("match/game4/leave").request().post(Entity.entity(player2, MediaType.APPLICATION_JSON));
        target("match/game4/leave").request().post(Entity.entity(player3, MediaType.APPLICATION_JSON));

        Response r = target("match/game4").request().get(Response.class);
        assertEquals(404, r.getStatus());
    }

    @Test
    public void addPlayer()
    {
        //add new match
        Match m1 = new Match("game1", 32,90);
        Response response1 = target("match").request().post(Entity.entity(m1, MediaType.APPLICATION_JSON));
        assertEquals("Should return status 201", 201, response1.getStatus());

        //add player
        Player pl1 = new Player("player1", "localhost", 6583);
        Response r = target("match/game1/join").request().post(Entity.entity(pl1, MediaType.APPLICATION_JSON));
        assertEquals(200, r.getStatus());
        //Match m = r.readEntity(Match.class);
        assertEquals(1, r.readEntity(Match.class).getPlayers().getList().size());

    }

}
