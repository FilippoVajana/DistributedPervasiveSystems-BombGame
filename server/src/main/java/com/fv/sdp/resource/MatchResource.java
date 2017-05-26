package com.fv.sdp.resource;

import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import  com.fv.sdp.model.Match;
import com.fv.sdp.model.Player;
import com.fv.sdp.util.ConcurrentList;

import java.util.ArrayList;
import java.util.List;

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
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{matchId}")
    public Response getMatchDetails(@PathParam("matchId") String matchId)
    {
        MatchModel model = MatchModel.getInstance();
        Match opResult = model.getMatch(matchId);
        if (opResult != null)
            return Response.ok(opResult).build();
        return Response.status(Response.Status.NOT_FOUND).build();

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllMatches()
    {
        MatchModel model = MatchModel.getInstance();
        //ArrayList<Match> matches = model.getMatchesList();
        GenericEntity<ArrayList<Match>> matches = new GenericEntity<ArrayList<Match>>(model.getMatchesList()){};
        return Response.ok(matches).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMatch(Match match)
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
    @Path("/{matchId}/enter")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response enterMatch(@PathParam("matchId") String matchId, Player player)
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

    @DELETE
    @Path("/{matchId}/{playerId}")
    public Response deletePlayer(@PathParam("matchId") String matchId, @PathParam("playerId") String playerId)
    {
        MatchModel model = MatchModel.getInstance();
        boolean playerRemoveResult = model.removePlayerFromMatch(matchId, playerId);
        //check for match cancellation
        Match match = model.getMatch(matchId);
        if (match.getPlayers().get_list().size() == 0)
            model.deleteMatch(matchId);
        if (playerRemoveResult)
            return Response.status(Response.Status.OK).build();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}


//////////////////////////////////////////////////////////////////////////////////
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

    //add new match
    public boolean addMatch(Match match)
    {
        //local copy
        ArrayList<Match> list = matchesList.get_list();
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
        return matchesList.get_list();
    }

    //get match details
    public Match getMatch(String matchId)
    {
        ArrayList<Match> list = matchesList.get_list();
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
