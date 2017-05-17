package match;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.TestModel;
import com.fv.sdp.resource.MatchResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.*;
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
        Match response = target("match/game1").request().get(Match.class);
        assertEquals(m1.getId(), response.getId());
    }

    @Test
    public void getMatchDetailsNull()
    {
        Match response = target("match/game1").request().get(Match.class);
        assertNull(response);
    }

    @Test
    public void getAllMatches()
    {
        setModel();
        List<Match> response = target("match/").request().get(new GenericType<List<Match>>() {});
        //debug
        //for (Match m : response)
        //   System.out.println(m.getId());
        assertEquals(3, response.size());
    }

}
