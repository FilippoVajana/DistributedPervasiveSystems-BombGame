package com.fv.sdp;

import com.fv.sdp.model.Player;
import com.fv.sdp.resource.MatchResource;
import com.fv.sdp.util.ConcurrentList;
import com.google.gson.Gson;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

@Path("/hello")
public class HelloWorld
{

    @Path("/player")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlayer()
    {
        Player pl = new Player("pl1", "localhost", 4567);
        return Response.ok().entity(pl).build();
    }

    @Path("/players")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlayersList()
    {
        MatchModel<Player> mm = new MatchModel();
        ArrayList<Player> pList = new ArrayList<>();
            pList.add(new Player("pl1","local",345));
            pList.add(new Player("pl2","remote",45));

        mm.setName("game1");
        mm.setPlayers(pList);
       return Response.ok().build();
    }

    @Path("gson")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response testGson()
    {
        Gson jsonizer = new Gson();
        MatchModel<Player> mm = new MatchModel();
        ArrayList<Player> pList = new ArrayList<>();
        pList.add(new Player("pl1","local",345));
        pList.add(new Player("pl2","remote",45));

        mm.setName("game1");
        mm.setPlayers(pList);
        return Response.ok().entity(jsonizer.toJson(mm)).build();

    }
}


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class MatchModel<T>
{
    private String name;
    private ConcurrentList<T> players;

    public MatchModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ConcurrentList<T> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<T> players) {
        this.players = new ConcurrentList<T>( players);
    }
}