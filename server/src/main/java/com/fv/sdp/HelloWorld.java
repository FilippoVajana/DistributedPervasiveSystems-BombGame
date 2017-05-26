package com.fv.sdp;

import com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.model.TestModel;
import com.fv.sdp.util.ConcurrentList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Path("/hello")
public class HelloWorld {

    @Path("/test")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTestModel()
    {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(2);
        list.add(45);
        list.add(567);
        TestModel tm = new TestModel("asd", list);

        return Response.ok(tm).build();
    }



    @Path("/player")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlayer()
    {
        return Response.ok(new Player("pl1", "localhost", 4567)).build();
    }

    @Path("/players")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlayersList()
    {
        MatchModel mm = new MatchModel();
        ArrayList<Player> pList = new ArrayList<>(); pList.add(new Player("pl1","local",345)); pList.add(new Player("pl2","remote",45));

        mm.setName("game1");
        mm.setPlayers(pList);
        return Response.ok(mm).build();
    }
}


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class MatchModel
{
    private String name;
    private ArrayList<Player> players; //MODIFICARE CON EREDITARIETA'

    public MatchModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }
}