package com.fv.sdp.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import  com.fv.sdp.model.Match;
/**
 * Created by filip on 12/05/2017.
 */
@Path("/match")
public class MatchResource
{
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Match getMatchDetails()
    {
        return new Match("asd", 34,23);
    }
}
