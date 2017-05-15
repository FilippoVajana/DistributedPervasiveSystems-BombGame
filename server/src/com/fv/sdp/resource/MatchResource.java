package com.fv.sdp.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import  com.fv.sdp.model.Match;
import com.fv.sdp.util.ConcurrentList;

import java.util.ArrayList;

/**
 * Created by filip on 12/05/2017.
 */
@Path("/match")
public class MatchResource
{
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/{id}")
    public Match getMatchDetails(@PathParam("id") String id)
    {
        MatchModel model = MatchModel.getInstance();
        Match opResult = model.getMatch(id);
        if (opResult != null)
            return opResult;
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    public ArrayList<Match> getMatches()
    {
        MatchModel model = MatchModel.getInstance();
        return model.getMatchesList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    //@Produces(MediaType.APPLICATION_XML)
    public Response addMatch(Match match)
    {
        MatchModel model = MatchModel.getInstance();
        boolean opResult = model.addMatch(match);
        if (opResult)
            return Response.status(Response.Status.CREATED).build();
        return Response.status(Response.Status.NOT_MODIFIED).build();
    }
}

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
    //change to ConcurrentList
    private static ConcurrentList<Match> matchesList = null;

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
    public boolean deleteMatch(Match match)
    {
        return false;
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
}
