package com.fv.sdp.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import  com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.util.ConcurrentList;
import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by filip on 12/05/2017.
 */
@Path("/match")
public class MatchResource
{
    public void resetResourceModel()
    {
        MatchModel.resetModel();
    }
    @GET
    @Path("/setTest")
    public void setResourceModel()
    {
        MatchModel.setTestModel();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMatches()
    {
        MatchModel model = MatchModel.getInstance();
        ArrayList<Match> matches = model.getMatchesList();
        Gson jsonizer = new Gson();
        return Response.ok().entity(jsonizer.toJson(matches)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createMatch(Match match)
    {
        //check invalid data
        if (match.getId().equals("") || match.getEdgeLength() == 0 || match.getVictoryPoints() == 0)
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        MatchModel model = MatchModel.getInstance();
        boolean opResult = model.addMatch(match);
        if (opResult)
            return Response.status(Response.Status.CREATED).build();
        return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    @POST
    @Path("/{matchId}/join")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response joinMatch(@PathParam("matchId") String matchId, Player player)
    {
        MatchModel model = MatchModel.getInstance();
        boolean opResult = model.addPlayerToMatch(matchId, player);
        if (opResult)
        {
            //DEBUG PROBE
            ConcurrentList<Player> pList = model.getMatch(matchId).getPlayers();
            Match m = model.getMatch(matchId);
            return Response.ok(model.getMatch(matchId)).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST
    @Path("/{matchId}/leave")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response leaveMatch(@PathParam("matchId") String matchId, String playerId)
    {
        MatchModel model = MatchModel.getInstance();
        boolean playerRemoveResult = model.removePlayerFromMatch(matchId, playerId);
        //check for match cancellation
        Match match = model.getMatch(matchId);
        if (match.getPlayers().getList().size() == 0)
            model.deleteMatch(matchId);
        if (playerRemoveResult)
            return Response.status(Response.Status.OK).build();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}


//////////////////////////////////////////////////////////////////////////////////
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class MatchModel
{
    //singleton
    private static MatchModel instance = null;
    public static MatchModel getInstance()
    {
        if (instance == null)
        {
            instance = new MatchModel();
        }
        return instance;
    }
    private MatchModel()
    {
        instance = this;
        matchesList = new ConcurrentList<>();
    }

    //data model
    private static ConcurrentList<Match> matchesList = null;
    //reset list
    public static void resetModel()
    {
        new MatchModel();
    }

    public static void setTestModel()
    {
        Match m1 = new Match("game1", 10,10);
        ArrayList<Player>pL2 = new ArrayList<>(); pL2.add(new Player("pl1", "local", 4563)); pL2.add(new Player("pl2", "local", 4563));
        Match m2 = new Match("game2", 32,45,new ConcurrentList<>(pL2));

        ArrayList<Match>mL = new ArrayList<>(); mL.add(m1); mL.add(m2);
       MatchModel mm = MatchModel.getInstance();
       matchesList.setList(mL);
    }




    //add new match
    public boolean addMatch(Match match)
    {
        //local copy
        ArrayList<Match> list = matchesList.getList();
        for(Match m : list)
        {
            if (m.getId().equals(match.getId()))
                return false;
        }
        //else
        matchesList.add(match);
        return true;
    }

    //remove a match
    public boolean deleteMatch(String matchId)
    {
        Match m = new Match(matchId,0,0 );
        matchesList.remove(m);
        return true;
    }

    //get matches list copy
    public ArrayList<Match> getMatchesList()
    {
        return matchesList.getList();
    }

    //get match details
    public Match getMatch(String matchId)
    {
        ArrayList<Match> list = matchesList.getList();
        for (Match m : list)
        {
            if (m.getId().equals(matchId))
                return m;
        }
        return null;
    }

    //add player to a match
    public boolean addPlayerToMatch(String matchId, Player player)
    {
        //check match
        if (!matchesList.contain(new Match(matchId,0,0)))
            return false;
        Match m = matchesList.getElement(new Match(matchId,0,0));
        //check player
        if (m.getPlayers().contain(player))
            return false;
        m.getPlayers().add(player);
        return true;

    }

    //remove player from a match
    public boolean removePlayerFromMatch(String matchId, String playerId)
    {
        Match match = getMatch(matchId);
        if (match != null)
        {
            match.getPlayers().remove(new Player(playerId, "localhost", 0));
            return true;
        }
        return false;
    }

}
