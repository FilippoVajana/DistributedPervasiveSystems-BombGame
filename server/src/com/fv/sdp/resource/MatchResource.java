package com.fv.sdp.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import  com.fv.sdp.model.Match;

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

class MatchModel //insert singleton
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
        matchesList = new ArrayList<>();
    }

    //data model
    //change to ConcurrentList
    private static ArrayList<Match> matchesList = null;
    //add new match
    public boolean addMatch(Match match)
    {
        //local copy
        ArrayList<Match> list = new ArrayList<>(matchesList);
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
        return new ArrayList<>(matchesList);
    }
    //get match details
    public Match getMatch(Match match)
    {
        return null;
    }
}
