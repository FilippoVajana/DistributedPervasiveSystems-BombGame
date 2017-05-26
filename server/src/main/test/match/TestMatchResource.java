package match;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.resource.MatchResource;
import com.fv.sdp.util.ConcurrentList;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import javax.swing.plaf.metal.MetalBorders;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
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
        target("match").request().post(Entity.entity(m1, MediaType.APPLICATION_JSON));
        target("match").request().post(Entity.entity(m2, MediaType.APPLICATION_JSON));
        target("match").request().post(Entity.entity(m3, MediaType.APPLICATION_JSON));

        //match with players
        // NON INVIA LA LISTA GIOCATORI !!
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
    public void getMatchDetails()
    {
        //add new match
        Match m1 = new Match("game1", 32,90);
        target("match").request().post(Entity.entity(m1, MediaType.APPLICATION_JSON));
        //get match details
        Response response = target("match/game1").request().get();
        assertEquals(m1.getId(), (response.readEntity(Match.class)).getId());
    }

    @Test
    public void getMatchDetailsNull()
    {
        Response response = target("match/game1").request().get();
        assertNull(response.readEntity(Match.class));
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
        setModel();
        Response response = target("match/game1/pl1").request().delete(Response.class);
        assertEquals(200, response.getStatus());

        Response r = target("match/game1").request().get();
        assertEquals(404, r.getStatus());
    }

    @Test
    public void removeAllPlayers()
    {
        setModel();
        target("match/game4/pl1").request().delete(Response.class);
        target("match/game4/pl2").request().delete(Response.class);
        target("match/game4/pl3").request().delete(Response.class);

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
        Response r = target("match/game1/enter").request().post(Entity.entity(pl1, MediaType.APPLICATION_JSON));
        assertEquals(200, r.getStatus());
        assertEquals(1, r.readEntity(Match.class).getPlayers().get_list().size());

    }

}
